package com.rental.camp.order.controller;

import com.rental.camp.order.dto.OrderRequest;
import com.rental.camp.order.dto.OrderResponse;
import com.rental.camp.order.dto.PendingOrderResponse;
import com.rental.camp.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/reserve")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest requestDTO, JwtAuthenticationToken principal) {
        String uuid = principal.getName();
        try {
            // OrderService를 사용하여 주문 생성
            OrderResponse responseDTO = orderService.createOrder(uuid, requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (RuntimeException e) {
            // 예외 발생 시, 오류 메시지를 포함하여 409 Conflict 상태 코드 반환
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/complete")
    public ResponseEntity<OrderResponse> completeOrder(@RequestBody OrderRequest request, JwtAuthenticationToken principal) {
        String uuid = principal.getName();
        OrderResponse response = orderService.completeOrder(uuid, request);
        return ResponseEntity.ok(response);
    }

    // 특정 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetails(
            @PathVariable(name = "orderId") Long orderId,
            JwtAuthenticationToken principal) {
        String uuid = principal.getName();
        try {
            OrderResponse response = orderService.getOrderDetails(uuid, orderId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable(name = "orderId") Long orderId,
            JwtAuthenticationToken principal
    ) {
        String uuid = principal.getName();
        try {
            OrderResponse response = orderService.cancelOrder(uuid, orderId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<?> findPendingOrder(JwtAuthenticationToken principal) {
        String uuid = principal.getName();
        try {
            PendingOrderResponse response = orderService.findPendingOrder(uuid);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}