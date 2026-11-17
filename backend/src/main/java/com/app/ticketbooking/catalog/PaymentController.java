package com.app.ticketbooking.catalog;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentRepository paymentRepo;
    private final BookingRepository bookingRepo;
    private final ShowSeatRepository seatRepo;
    private final StringRedisTemplate redis;

    public PaymentController(PaymentRepository paymentRepo,
                             BookingRepository bookingRepo,
                             ShowSeatRepository seatRepo,
                             StringRedisTemplate redis) {
        this.paymentRepo = paymentRepo;
        this.bookingRepo = bookingRepo;
        this.seatRepo = seatRepo;
        this.redis = redis;
    }

    public record PayRequest(Long seatId, String customerName, boolean simulateFailure) {}

    @PostMapping
    @Transactional
    public Payment pay(@RequestHeader("Idempotency-Key") String key,
                       @RequestBody PayRequest req) {
        // 1. Same key seen before? Return the saved result.
        var existing = paymentRepo.findByIdempotencyKey(key);
        if (existing.isPresent()) {
            return existing.get();
        }

        // 2. Only the holder may pay.
        String holdKey = "hold:seat:" + req.seatId();
        String holder = redis.opsForValue().get(holdKey);
        if (holder == null) {
            throw new SeatUnavailableException("Hold the seat first (no active hold)");
        }
        if (!holder.equals(req.customerName())) {
            throw new SeatUnavailableException("Seat is held by someone else");
        }

        ShowSeat seat = seatRepo.findById(req.seatId())
                .orElseThrow(() -> new RuntimeException("Seat not found"));
        if (!"AVAILABLE".equals(seat.getStatus())) {
            throw new SeatUnavailableException("Seat already booked");
        }

        Payment p = new Payment();
        p.setIdempotencyKey(key);
        p.setAmount(seat.getShow().getPrice());
        p.setPaidAt(LocalDateTime.now());

        // 3. Payment failed: save the record, change nothing else. The hold expires by itself.
        if (req.simulateFailure()) {
            p.setStatus("FAILED");
            return paymentRepo.save(p);
        }

        // 4. Payment succeeded: now the seat becomes BOOKED.
        seat.setStatus("BOOKED");
        seatRepo.saveAndFlush(seat);

        Booking booking = new Booking();
        booking.setCustomerName(req.customerName());
        booking.setShowSeat(seat);
        booking.setBookedAt(LocalDateTime.now());
        booking.setAmount(seat.getShow().getPrice());
        Booking savedBooking = bookingRepo.save(booking);

        p.setBookingId(savedBooking.getId());
        p.setStatus("SUCCESS");
        Payment saved = paymentRepo.save(p);

        redis.delete(holdKey);
        return saved;
    }
}