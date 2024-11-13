package com.rental.camp.rental.dto;

import com.rental.camp.rental.model.type.RentalItemCategory;
import com.rental.camp.rental.model.type.RentalItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MyItemsResponse {
    private String rentalItem;
    private RentalItemCategory category;
    private Integer stock;
    private RentalItemStatus status;
    private LocalDateTime rentalDate;
}
