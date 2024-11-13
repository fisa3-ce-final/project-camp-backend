package com.rental.camp.rental.dto;

import com.rental.camp.rental.model.type.RentalItemCategory;
import com.rental.camp.rental.model.type.RentalItemStatus;
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
    private RentalItemCategory category;
    private RentalItemStatus status;
}
