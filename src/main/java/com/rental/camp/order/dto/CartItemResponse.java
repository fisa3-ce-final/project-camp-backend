package com.rental.camp.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rental.camp.rental.dto.RentalItemForCartResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

public class CartItemResponse {
    private Long id;
    private String message = "장바구니 담기 완료";
    private Integer quantity;
    private RentalItemForCartResponse rentalItem;

    public CartItemResponse(Long id, Integer quantity, RentalItemForCartResponse rentalItem) {
        this.id = id;
        this.quantity = quantity;
        this.rentalItem = rentalItem;
    }


}