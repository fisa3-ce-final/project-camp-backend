package com.rental.camp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalItemRequest {
    private int page; // 페이지 번호
    private int size; // 한 페이지에 표시할 항목
}
