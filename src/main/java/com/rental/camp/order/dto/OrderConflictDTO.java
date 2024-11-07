package com.rental.camp.order.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderConflictDTO {
    private String rentalItemName;
    private LocalDateTime conflictReturnDate;

    @QueryProjection
    public OrderConflictDTO(String rentalItemName, LocalDateTime conflictReturnDate) {
        this.rentalItemName = rentalItemName;
        this.conflictReturnDate = conflictReturnDate;
    }
}