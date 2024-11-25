package com.rental.camp.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveAmountRequest {
    private Long orderId;
    private BigDecimal amount;
}
