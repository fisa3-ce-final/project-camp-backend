package com.rental.camp.rental.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Table(name = "rental_item")
public class RentalItem {

    @Id
    @SequenceGenerator(
            name = "rental_item_id_seq",
            sequenceName = "rental_item_id_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "rental_item_id_seq"
    )
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false, length = 255)
    private String category;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(nullable = false)
    private Integer viewCount;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal ratingAvg;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}
