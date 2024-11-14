package com.rental.camp.admin.dto;

import com.rental.camp.rental.model.type.RentalItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AuditRequest {
    private RentalItemStatus status;
    private int page;
    private int size;
}
