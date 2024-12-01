package com.rental.camp.admin.dto;

import com.rental.camp.order.model.type.OrderStatus;
import com.rental.camp.rental.model.type.RentalItemCategory;
import com.rental.camp.rental.model.type.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RentalStatusResponse {
    private Long orderId;
    private Long userId;
    private String nickname;
    private OrderStatus orderStatus;
    private LocalDateTime rentalDate;
    private LocalDateTime returnDate;
    private RentalStatus status;
    private BigDecimal price;
}
