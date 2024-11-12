package com.rental.camp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MyItemsResponse {
    private String rentalItem;
    private String category;
    private Integer stock;
    private String status;
    private LocalDateTime rentalDate;
}
