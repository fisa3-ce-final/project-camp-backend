package com.rental.camp.rental.dto;

import com.rental.camp.rental.model.type.RentalItemCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MyOrdersResponse {
    private String rentalItemName;
    private RentalItemCategory category;
    private Integer stock;
    private String orderStatus;
    private LocalDateTime orderDate;
}
