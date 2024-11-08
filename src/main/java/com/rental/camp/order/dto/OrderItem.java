package com.rental.camp.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Long rentalItemId;  // 대여 아이템 ID
    private Integer quantity;   // 주문한 아이템 수량
    private BigDecimal price;   // 단가
    private BigDecimal subtotal;  // 소계 (quantity * price)
}

