package com.app.ticketbooking.catalog;

import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shows")
public class ShowController {

    private final ShowRepository showRepo;
    private final MovieRepository movieRepo;

    public ShowController(ShowRepository showRepo, MovieRepository movieRepo) {
        this.showRepo = showRepo;
        this.movieRepo = movieRepo;
    }

    public record CreateShowRequest(Long movieId, String hall,
                                    LocalDateTime startTime, BigDecimal price) {}

    @GetMapping
    public List<Show> getAll() {
        return showRepo.findAll();
    }

    @PostMapping
    public Show create(@RequestBody CreateShowRequest req) {
        Movie movie = movieRepo.findById(req.movieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        Show show = new Show();
        show.setMovie(movie);
        show.setHall(req.hall());
        show.setStartTime(req.startTime());
        show.setPrice(req.price());
        return showRepo.save(show);
    }
}