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

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // pending 상태 주문 생성
    @PostMapping("/reserve")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest requestDTO, JwtAuthenticationToken principal) {
        String uuid = principal.getName();

        // OrderService를 사용하여 주문 생성
        OrderResponse responseDTO = orderService.createOrder(uuid, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    // 주문 완료
    @PutMapping("/complete")
    public ResponseEntity<?> completeOrder(@RequestBody OrderRequest request, JwtAuthenticationToken principal) {

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
        OrderResponse response = orderService.getOrderDetails(uuid, orderId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}/pending")
    public ResponseEntity<?> deletePendingOrder(@PathVariable(name = "orderId") Long orderId, JwtAuthenticationToken principal) {
        String uuid = principal.getName();

        orderService.deletePending(uuid, orderId);
        return ResponseEntity.ok("삭제 성공");
    }


    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable(name = "orderId") Long orderId, JwtAuthenticationToken principal) {

        String uuid = principal.getName();
        OrderResponse response = orderService.cancelOrder(uuid, orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> findPendingOrder(JwtAuthenticationToken principal) {

        String uuid = principal.getName();
        List<PendingOrderResponse> response = orderService.findPendingOrder(uuid);
        return ResponseEntity.ok(response);
    }

    // 예외처리 핸들러
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleEntityNotFoundException(RuntimeException e) {

        return ResponseEntity.badRequest().body(e.getMessage());
    }


}