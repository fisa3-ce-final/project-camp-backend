package com.rental.camp.order.dto;

import com.rental.camp.order.model.type.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingOrderResponse {
    Long userId;
    Long id;
    OrderStatus orderStatus;
    BigDecimal totalAmount;
    LocalDateTime CreatedAt;


}
