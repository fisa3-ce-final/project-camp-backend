package com.rental.camp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MyOrdersResponse {
    private String rentalItemName;
    private String category;
    private Integer stock;
    private String orderStatus;
    private LocalDateTime orderDate;
}
