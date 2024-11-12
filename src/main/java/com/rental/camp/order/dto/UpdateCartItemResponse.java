package com.rental.camp.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemResponse {
    private CartItemResponse cartItem;
    private String message;

    public static UpdateCartItemResponse error(String message) {
        return new UpdateCartItemResponse(null, message);
    }
}