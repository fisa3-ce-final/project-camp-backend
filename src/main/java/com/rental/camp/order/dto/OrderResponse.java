package com.rental.camp.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long userId;
    private String message;  // 성공 메시지
    private List<OrderConflict> conflicts; // 중복된 아이템 정보를 담을 필드 추가

    public OrderResponse(Long userId, String message) {
        this.userId = userId;
        this.message = message;
    }
}
