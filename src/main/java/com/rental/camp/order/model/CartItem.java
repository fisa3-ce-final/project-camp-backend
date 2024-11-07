package com.rental.camp.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "cart_item")
public class CartItem {

    @Id
    @SequenceGenerator(
            name = "cart_item_id_seq",
            sequenceName = "cart_item_id_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "cart_item_id_seq"
    )
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(nullable = false)
    private Integer quantity = 1;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "rental_item_id", nullable = false)
    private Long rentalItemId;
}
