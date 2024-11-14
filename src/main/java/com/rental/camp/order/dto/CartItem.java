package com.rental.camp.order.dto;

import com.rental.camp.rental.dto.RentalItemResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Long id; // 기존 cartItemId를 id로 변경
    private Long rentalItemId;
    private Integer quantity;
    private RentalItemResponse rentalItem;

    // 필요한 생성자들
    public CartItem(Long id, Integer quantity, RentalItemResponse rentalItem) {
        this.id = id;
        this.quantity = quantity;
        this.rentalItem = rentalItem;
    }


}