package com.app.ticketbooking.catalog;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seats")
public class BookingController {

    private final ShowSeatRepository seatRepo;

    public BookingController(ShowSeatRepository seatRepo) {
        this.seatRepo = seatRepo;
    }

    @PostMapping("/{seatId}/book")
    public ShowSeat book(@PathVariable Long seatId) {
        ShowSeat seat = seatRepo.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));
        if (!"AVAILABLE".equals(seat.getStatus())) {
            throw new SeatUnavailableException("Seat already booked");
        }
        seat.setStatus("BOOKED");
        return seatRepo.save(seat);
    }
}