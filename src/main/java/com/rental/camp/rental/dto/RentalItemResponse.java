package com.rental.camp.rental.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RentalItemResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private String status;
}
