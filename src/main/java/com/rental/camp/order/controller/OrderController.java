package com.rental.camp.order.controller;

import com.rental.camp.order.dto.OrderRequest;
import com.rental.camp.order.dto.OrderResponse;
import com.rental.camp.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/reserve")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest requestDTO) {
        try {
            // OrderService를 사용하여 주문 생성
            OrderResponse responseDTO = orderService.createOrder(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (RuntimeException e) {
            // 예외 발생 시, 오류 메시지를 포함하여 409 Conflict 상태 코드 반환
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/complete")
    public ResponseEntity<OrderResponse> completeOrder(@RequestBody OrderRequest request) {
        OrderResponse response = orderService.completeOrder(request);
        return ResponseEntity.ok(response);
    }
}