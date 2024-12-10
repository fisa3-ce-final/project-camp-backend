package com.rental.camp.rental.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Table(name = "ip_tracking")
public class IpTracking {

    @Id
    @SequenceGenerator(
            name = "ip_tracking_id_seq",
            sequenceName = "ip_tracking_id_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "ip_tracking_id_seq"
    )
    private Long id;

    @Column(nullable = false)
    private String ip;

    @Column(name = "rental_item_id", nullable = false)
    private Long rentalItemId;
}
