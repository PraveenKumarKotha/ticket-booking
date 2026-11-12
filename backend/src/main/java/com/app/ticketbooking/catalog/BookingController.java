package com.app.ticketbooking.catalog;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/seats")
public class BookingController {

    private final ShowSeatRepository seatRepo;
    private final BookingRepository bookingRepo;

    public BookingController(ShowSeatRepository seatRepo, BookingRepository bookingRepo) {
        this.seatRepo = seatRepo;
        this.bookingRepo = bookingRepo;
    }

    public record BookRequest(String customerName) {}

    @PostMapping("/{seatId}/book")
    @Transactional
    public Booking book(@PathVariable Long seatId, @RequestBody BookRequest req) {
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
        return bookingRepo.save(booking);
    }
}