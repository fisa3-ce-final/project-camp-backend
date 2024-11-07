package com.rental.camp.order.controller;

import com.rental.camp.order.dto.OrderRequestDTO;
import com.rental.camp.order.dto.OrderResponseDTO;
import com.rental.camp.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reserve")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequestDTO requestDTO) {
        try {
            // OrderService를 사용하여 주문 생성
            OrderResponseDTO responseDTO = orderService.createOrder(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (RuntimeException e) {
            // 예외 발생 시, 오류 메시지를 포함하여 409 Conflict 상태 코드 반환
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}