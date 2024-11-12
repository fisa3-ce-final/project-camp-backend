package com.rental.camp.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemInfo {
    private Long rentalItemId;
    private String itemName;    // 상품 이름
    private int quantity;       // 수량
    private BigDecimal price;   // 상품 단가
    private BigDecimal subtotal; // 수량 x 단가

    public OrderItemInfo(Long rentalItemId, String itemName, int quantity, BigDecimal price, BigDecimal subtotal) {
        this.rentalItemId = rentalItemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
    }
}