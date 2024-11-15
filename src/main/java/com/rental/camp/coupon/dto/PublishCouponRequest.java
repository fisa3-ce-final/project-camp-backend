package com.rental.camp.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublishCouponRequest {
    private String name;
    private String type;
    private BigDecimal discount;
    private Integer amount;
    private LocalDateTime expiryDate;
}
