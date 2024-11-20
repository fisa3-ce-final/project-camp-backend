// CouponDto.java
package com.rental.camp.coupon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class ActiveCouponResponse {
    private Long couponId;
    private String name;
    private Integer amount;
    private BigDecimal discount;
    private CouponType type;
    private LocalDateTime expiryDate;
    private boolean isUsed;
    private boolean isReceived;
    private LocalDateTime createdAt;

    public ActiveCouponResponse(Long couponId, String name, BigDecimal discount,
                                CouponType type, LocalDateTime expiryDate,
                                Boolean isUsed, LocalDateTime createdAt) {
        this.couponId = couponId;
        this.name = name;
        this.discount = discount;
        this.type = type;
        this.expiryDate = expiryDate;
        this.isUsed = isUsed != null ? isUsed : false;
        this.createdAt = createdAt;
    }

    public ActiveCouponResponse(Long couponId, String name, BigDecimal discount,
                                CouponType type, LocalDateTime expiryDate,
                                LocalDateTime createdAt) {
        this.couponId = couponId;
        this.name = name;
        this.discount = discount;
        this.type = type;
        this.expiryDate = expiryDate;
        this.createdAt = createdAt;
    }

    public ActiveCouponResponse(Long couponId, String name, Integer amount, BigDecimal discount, CouponType type, LocalDateTime expiryDate, boolean isUsed, LocalDateTime createdAt) {
        this.couponId = couponId;
        this.name = name;
        this.amount = amount;
        this.discount = discount;
        this.type = type;
        this.expiryDate = expiryDate;
        this.isUsed = isUsed;
        this.createdAt = createdAt;
    }
}