package com.app.ticketbooking.catalog;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/seats")
public class HoldController {

    private final StringRedisTemplate redis;
    private final ShowSeatRepository seatRepo;

    public HoldController(StringRedisTemplate redis, ShowSeatRepository seatRepo) {
        this.redis = redis;
        this.seatRepo = seatRepo;
    }

    public record HoldRequest(String customerName) {}

    @PostMapping("/{seatId}/hold")
    public Map<String, Object> hold(@PathVariable Long seatId,
                                    @RequestBody HoldRequest req) {
        ShowSeat seat = seatRepo.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));
        if (!"AVAILABLE".equals(seat.getStatus())) {
            throw new SeatUnavailableException("Seat already booked");
        }
        String key = "hold:seat:" + seatId;
        Boolean ok = redis.opsForValue()
                .setIfAbsent(key, req.customerName(), Duration.ofMinutes(5));
        if (!Boolean.TRUE.equals(ok)) {
            throw new SeatUnavailableException("Seat is held by someone else");
        }
        return Map.of("seatId", seatId, "heldBy", req.customerName(), "expiresInSeconds", 300);
    }
}