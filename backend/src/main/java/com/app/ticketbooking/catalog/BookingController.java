package com.app.ticketbooking.catalog;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/seats")
public class BookingController {

    private final ShowSeatRepository seatRepo;
    private final BookingRepository bookingRepo;
    private final StringRedisTemplate redis;

    public BookingController(ShowSeatRepository seatRepo,
                             BookingRepository bookingRepo,
                             StringRedisTemplate redis) {
        this.seatRepo = seatRepo;
        this.bookingRepo = bookingRepo;
        this.redis = redis;
    }

    public record BookRequest(String customerName) {}

    @PostMapping("/{seatId}/book")
    @Transactional
    public Booking book(@PathVariable Long seatId, @RequestBody BookRequest req) {
        String key = "hold:seat:" + seatId;
        String holder = redis.opsForValue().get(key);
        if (holder == null) {
            throw new SeatUnavailableException("Hold the seat first (no active hold)");
        }
        if (!holder.equals(req.customerName())) {
            throw new SeatUnavailableException("Seat is held by someone else");
        }

        ShowSeat seat = seatRepo.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));
        if (!"AVAILABLE".equals(seat.getStatus())) {
            throw new SeatUnavailableException("Seat already booked");
        }
        seat.setStatus("BOOKED");
        seatRepo.saveAndFlush(seat);

        Booking booking = new Booking();
        booking.setCustomerName(req.customerName());
        booking.setShowSeat(seat);
        booking.setBookedAt(LocalDateTime.now());
        booking.setAmount(seat.getShow().getPrice());
        Booking saved = bookingRepo.save(booking);

        redis.delete(key);
        return saved;
    }
}