package com.rental.camp.coupon.model;

import com.rental.camp.coupon.model.type.CouponType;
import jakarta.persistence.*;
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
@Table(name = "coupon")
public class Coupon {

    @Id
    @SequenceGenerator(
            name = "coupon_id_seq",
            sequenceName = "coupon_id_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "coupon_id_seq"
    )
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private CouponType type;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted;
}
