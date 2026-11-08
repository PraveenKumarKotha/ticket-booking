package com.app.ticketbooking.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {
    List<ShowSeat> findByShowId(Long showId);
}