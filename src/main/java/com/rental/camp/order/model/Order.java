package com.rental.camp.order.model;

import com.rental.camp.order.model.type.OrderStatus;
import com.rental.camp.rental.model.type.RentalStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Table(name = "orders")
public class Order {

    @Id
    @SequenceGenerator(
            name = "orders_id_seq",
            sequenceName = "orders_id_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "orders_id_seq"
    )
    @Column(name = "id", updatable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 50)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "rental_status", nullable = false, length = 50)
    private RentalStatus rentalStatus = RentalStatus.RENTED;

    @Column(nullable = false)
    private LocalDateTime rentalDate;

    @Column(nullable = false)
    private LocalDateTime returnDate;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 쿠폰 ID가 null일 수 있도록 nullable = true로 설정
    @Column(name = "coupon_id", nullable = true)
    private Long couponId;
}
