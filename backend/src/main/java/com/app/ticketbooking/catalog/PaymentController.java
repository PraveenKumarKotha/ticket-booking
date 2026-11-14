package com.app.ticketbooking.catalog;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentRepository paymentRepo;
    private final BookingRepository bookingRepo;
    private final ShowSeatRepository seatRepo;

    public PaymentController(PaymentRepository paymentRepo,
                             BookingRepository bookingRepo,
                             ShowSeatRepository seatRepo) {
        this.paymentRepo = paymentRepo;
        this.bookingRepo = bookingRepo;
        this.seatRepo = seatRepo;
    }

    public record PayRequest(Long bookingId, boolean simulateFailure) {}

    @PostMapping
    @Transactional
    public Payment pay(@RequestHeader("Idempotency-Key") String key,
                       @RequestBody PayRequest req) {
        var existing = paymentRepo.findByIdempotencyKey(key);
        if (existing.isPresent()) {
            return existing.get();
        }

        Booking booking = bookingRepo.findById(req.bookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Payment p = new Payment();
        p.setIdempotencyKey(key);
        p.setBookingId(booking.getId());
        p.setAmount(booking.getAmount());
        p.setPaidAt(LocalDateTime.now());

        if (req.simulateFailure()) {
            p.setStatus("FAILED");
            Payment saved = paymentRepo.save(p);

            ShowSeat seat = booking.getShowSeat();
            seat.setStatus("AVAILABLE");
            seatRepo.save(seat);
            bookingRepo.delete(booking);
            return saved;
        }

        p.setStatus("SUCCESS");
        return paymentRepo.save(p);
    }
}