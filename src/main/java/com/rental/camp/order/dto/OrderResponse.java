package com.rental.camp.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    private Long userId;
    private String message;                  // 성공 메시지
    private Long orderId;                    // 주문 ID
    private String username;                 // 사용자 이름
    private String address;                  // 사용자 주소
    private String phone;                    // 사용자 전화번호
    private List<OrderItemInfo> orderItems;  // 주문 아이템 정보
    private Long rentalDays;                 // 렌탈 기간 (일수)
    private BigDecimal totalItemPrice;       // 쿠폰 적용 전 총 금액
    private BigDecimal discountAmount;       // 할인 금액 (쿠폰 사용 시)
    private BigDecimal finalPrice;           // 쿠폰 적용 후 최종 금액
    private String createdAt;
    private String upadtedAt;// 주문 생성 날짜 (YYYY-MM-DD)
    private List<OrderConflict> conflicts;   // 대여 기간 중복 정보

    // 성공적인 주문 생성 시 사용하는 생성자
    public OrderResponse(Long userId, String message, Long orderId, String username, String address, String phone,
                         List<OrderItemInfo> orderItems, Long rentalDays, BigDecimal totalItemPrice,
                         BigDecimal discountAmount, BigDecimal finalPrice, String createdAt) {
        this.userId = userId;
        this.message = message;
        this.orderId = orderId;
        this.username = username;
        this.address = address;
        this.phone = phone;
        this.orderItems = orderItems;
        this.rentalDays = rentalDays;
        this.totalItemPrice = totalItemPrice;
        this.discountAmount = discountAmount;
        this.finalPrice = finalPrice;
        this.createdAt = createdAt;
    }

    // 주문 조회시 사용하는 생성자

    public OrderResponse(Long userId, String message, Long orderId, String username, String address, String phone, List<OrderItemInfo> orderItems, Long rentalDays, BigDecimal totalItemPrice, BigDecimal discountAmount, BigDecimal finalPrice, String createdAt, String upadtedAt) {
        this.userId = userId;
        this.message = message;
        this.orderId = orderId;
        this.username = username;
        this.address = address;
        this.phone = phone;
        this.orderItems = orderItems;
        this.rentalDays = rentalDays;
        this.totalItemPrice = totalItemPrice;
        this.discountAmount = discountAmount;
        this.finalPrice = finalPrice;
        this.createdAt = createdAt;
        this.upadtedAt = upadtedAt;
    }

    // 주문 충돌이 발생했을 때 사용하는 생성자
    public OrderResponse(String message, Long userId, List<OrderConflict> conflicts) {
        this.message = message;
        this.userId = userId;
        this.conflicts = conflicts;
    }
}
