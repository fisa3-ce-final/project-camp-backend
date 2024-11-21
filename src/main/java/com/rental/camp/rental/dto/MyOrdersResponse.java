package com.rental.camp.rental.dto;

import com.rental.camp.rental.model.type.RentalItemCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MyOrdersResponse {
    private Long orderId;
    private String orderStatus;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
}
