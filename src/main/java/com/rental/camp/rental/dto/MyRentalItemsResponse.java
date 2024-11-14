package com.rental.camp.rental.dto;

import com.rental.camp.order.model.type.OrderStatus;
import com.rental.camp.rental.model.type.RentalItemCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MyRentalItemsResponse {
    private String rentalItem;
    private RentalItemCategory category;
    private Integer quantity;
    private OrderStatus orderStatus;
    private LocalDateTime rentalDate;
    private LocalDateTime returnDate;
}
