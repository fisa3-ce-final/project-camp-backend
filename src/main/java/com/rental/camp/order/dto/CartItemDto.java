package com.rental.camp.order.dto;

import com.rental.camp.rental.dto.RentalItemResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CartItemDto {
    private Long cartItemId;
    private Integer quantity;
    private RentalItemResponse rentalItem; // RentalItemResponseDto로 변경
}
