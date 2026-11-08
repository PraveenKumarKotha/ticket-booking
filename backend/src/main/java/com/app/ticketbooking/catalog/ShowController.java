package com.app.ticketbooking.catalog;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/shows")
public class ShowController {

    private final ShowRepository showRepo;
    private final MovieRepository movieRepo;
    private final ShowSeatRepository seatRepo;

    public ShowController(ShowRepository showRepo, MovieRepository movieRepo,
                          ShowSeatRepository seatRepo) {
        this.showRepo = showRepo;
        this.movieRepo = movieRepo;
        this.seatRepo = seatRepo;
    }

    public record CreateShowRequest(Long movieId, String hall,
                                    LocalDateTime startTime, BigDecimal price) {}

    @GetMapping
    public List<Show> getAll() {
        return showRepo.findAll();
    }

    @PostMapping
    @Transactional
    public Show create(@RequestBody CreateShowRequest req) {
        Movie movie = movieRepo.findById(req.movieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        Show show = new Show();
        show.setMovie(movie);
        show.setHall(req.hall());
        show.setStartTime(req.startTime());
        show.setPrice(req.price());
        Show saved = showRepo.save(show);

        List<ShowSeat> seats = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            ShowSeat seat = new ShowSeat();
            seat.setShow(saved);
            seat.setSeatNumber("A" + i);
            seats.add(seat);
        }
        seatRepo.saveAll(seats);

        return saved;

    }
    @GetMapping("/{id}/seats")
    public List<ShowSeat> getSeats(@PathVariable Long id) {
        return seatRepo.findByShowId(id);
    }
}