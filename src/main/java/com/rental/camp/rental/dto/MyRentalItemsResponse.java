package com.rental.camp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MyRentalItemsResponse {
    private String rentalItem;
    private String category;
    private Integer stock;
    private String rentalStatus;
    private LocalDateTime rentalDate;
    private LocalDateTime returnDate;
}
