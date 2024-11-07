package com.rental.camp.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderErrorResponseDTO {
    private String message;  // 실패 메시지
    private List<OrderConflictDTO> conflicts;  // 충돌 항목 목록
}