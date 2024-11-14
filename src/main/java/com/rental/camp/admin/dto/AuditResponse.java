package com.rental.camp.admin.dto;

import com.rental.camp.rental.model.type.RentalItemCategory;
import com.rental.camp.rental.model.type.RentalItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AuditResponse {
    private Long rentalItemId;
    private String name;
    private RentalItemCategory category;
    private BigDecimal price;
    private RentalItemStatus status;
    private LocalDateTime createdAt;
}
