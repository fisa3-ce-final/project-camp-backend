package com.rental.camp.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private List<Long> cartItemIds;
    private LocalDateTime rentalDate;
    private LocalDateTime returnDate;
    private Long userCouponId;  // 쿠폰 ID는 선택적 필드

    
}
