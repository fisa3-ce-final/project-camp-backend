package com.rental.camp.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SaveAmountRequest {
    private Long orderId;
    private Long amount;
}
