package com.rental.camp.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTotalAmount {
    private BigDecimal totalAmount;  // 총 결제 금액
    private Long couponId;           // 적용된 쿠폰 ID (선택)
}
