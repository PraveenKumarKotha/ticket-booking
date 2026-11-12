package com.app.ticketbooking.catalog;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    @OneToOne(optional = false)
    @JoinColumn(name = "show_seat_id", unique = true)
    private ShowSeat showSeat;

    private LocalDateTime bookedAt;
    private BigDecimal amount;
}