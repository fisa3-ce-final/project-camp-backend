package com.rental.camp.coupon.dto;

import lombok.Data;

@Data
// 사용자 쿠폰 발급을 위한 dto
public class IssueCouponRequest {
    private Long couponId;
}