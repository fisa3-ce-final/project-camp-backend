package com.rental.camp.order.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class OrderConflict {
    private String rentalItemName;
    private LocalDateTime conflictReturnDate;

    @QueryProjection
    public OrderConflict(String rentalItemName, LocalDateTime conflictReturnDate) {
        this.rentalItemName = rentalItemName;
        this.conflictReturnDate = conflictReturnDate;
    }
}