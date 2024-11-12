package com.rental.camp.order.dto;

import com.rental.camp.rental.dto.RentalItemResponse;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

@Builder
public class CartItem {
    private Long id; // 기존 cartItemId를 id로 변경
    private Long userId;

    public CartItem(Long id) {
        this.id = id;
    }

    private Long rentalItemId;
    private Integer quantity;
    private RentalItemResponse rentalItem;

    // 필요한 생성자들
    public CartItem(Long id, Integer quantity, RentalItemResponse rentalItem) {
        this.id = id;
        this.quantity = quantity;
        this.rentalItem = rentalItem;
    }

    public CartItem(Long id, Long userId, Long rentalItemId, Integer quantity) {
        this.id = id;
        this.userId = userId;
        this.rentalItemId = rentalItemId;
        this.quantity = quantity;
    }

    public CartItem(Long id, Long userId, Long rentalItemId, Integer quantity, RentalItemResponse rentalItem) {
        this.id = id;
        this.userId = userId;
        this.rentalItemId = rentalItemId;
        this.quantity = quantity;
        this.rentalItem = rentalItem;
    }

}