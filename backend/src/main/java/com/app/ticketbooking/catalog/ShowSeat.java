package com.app.ticketbooking.catalog;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "show_seats")
@Getter @Setter @NoArgsConstructor
public class ShowSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "show_id")
    private Show show;

    private String seatNumber;

    private String status = "AVAILABLE";
}