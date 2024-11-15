package com.rental.camp.coupon.dto;

import com.rental.camp.coupon.model.type.CouponType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCouponResponse {
    private Long couponId;
    private String name;
    private BigDecimal discount;
    private CouponType type;
    private LocalDateTime expiryDate;
    private Integer amount;
    private LocalDateTime createdAt;
}
