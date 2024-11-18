// CouponDto.java
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
public class CouponResponse {
    private Long couponId;
    private String name;
    private BigDecimal discount;
    private CouponType type;
    private LocalDateTime expiryDate;
    private boolean isUsed;
    private LocalDateTime createdAt;

    // QueryDSL을 위한 생성자
    public CouponResponse(Long couponId, String name, BigDecimal discount,
                          CouponType type, LocalDateTime expiryDate) {
        this.couponId = couponId;
        this.name = name;
        this.discount = discount;
        this.type = type;
        this.expiryDate = expiryDate;
        this.isUsed = false; // 기본값 설정
    }

    public CouponResponse(Long couponId, String name, BigDecimal discount,
                          CouponType type, LocalDateTime expiryDate,
                          Boolean isUsed) {
        this.couponId = couponId;
        this.name = name;
        this.discount = discount;
        this.type = type;
        this.expiryDate = expiryDate;
        this.isUsed = isUsed != null ? isUsed : false;
    }

    public CouponResponse(Long couponId, String name, BigDecimal discount,
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
}