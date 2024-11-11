package com.rental.camp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalItemResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private String status;
}
