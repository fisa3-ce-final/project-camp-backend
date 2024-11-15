package com.rental.camp.coupon.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Table(name = "user_coupon")
public class UserCoupon {

    @Id
    @SequenceGenerator(
            name = "user_coupon_id_seq",
            sequenceName = "user_coupon_id_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_coupon_id_seq"
    )
    @Column(name = "id", updatable = false)
    private Long id;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime acquiredAt;

    @Column(nullable = false)
    private Boolean isUsed;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;
}
