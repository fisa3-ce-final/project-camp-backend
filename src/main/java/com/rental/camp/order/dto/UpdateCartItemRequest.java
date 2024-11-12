package com.rental.camp.order.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemRequest {
    private Long userId;
    private Long cartItemId;
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private Integer quantity;
}