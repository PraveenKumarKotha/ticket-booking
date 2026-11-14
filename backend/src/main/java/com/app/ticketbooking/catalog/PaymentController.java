package com.app.ticketbooking.catalog;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentRepository paymentRepo;
    private final BookingRepository bookingRepo;

    public PaymentController(PaymentRepository paymentRepo, BookingRepository bookingRepo) {
        this.paymentRepo = paymentRepo;
        this.bookingRepo = bookingRepo;
    }

    public record PayRequest(Long bookingId) {}

    @PostMapping
    public Payment pay(@RequestHeader("Idempotency-Key") String key,
                       @RequestBody PayRequest req) {
        // 1. Seen this key before? Return the saved result, do not charge again.
        var existing = paymentRepo.findByIdempotencyKey(key);
        if (existing.isPresent()) {
            return existing.get();
        }

        // 2. New key: "charge" (fake) and save.
        Booking booking = bookingRepo.findById(req.bookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Payment p = new Payment();
        p.setIdempotencyKey(key);
        p.setBookingId(booking.getId());
        p.setAmount(booking.getAmount());
        p.setStatus("SUCCESS");
        p.setPaidAt(LocalDateTime.now());
        return paymentRepo.save(p);
    }
}