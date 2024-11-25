package com.rental.camp.order.controller;

import com.rental.camp.order.dto.*;
import com.rental.camp.order.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    // 결제 모듈을 통한 주문 완료
    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody ConfirmRequestData request, JwtAuthenticationToken principal) {

        String uuid = principal.getName();
        OrderResponse response = orderService.confirmOrder(uuid, request);
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

    @GetMapping("/amount")
    public ResponseEntity<?> getTotalAmount(JwtAuthenticationToken principal) {
        try {
            String uuid = principal.getName();
            BigDecimal amount = orderService.getPendingOrderTotalAmount(uuid);

            if (amount == null) {
                return ResponseEntity.notFound().build();
            }

            amount = amount.setScale(0, RoundingMode.DOWN);

            return ResponseEntity.ok(amount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("주문 금액 조회 실패: " + e.getMessage());
        }
    }

    @GetMapping("/paymentInfo")
    public ResponseEntity<?> getPaymentInfo(JwtAuthenticationToken principal) {
        try {
            String uuid = principal.getName();
            PaymentInfo paymentInfo = orderService.getPaymentInfo(uuid);

            if (paymentInfo == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(paymentInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("주문 조회 실패: " + e.getMessage());
        }

    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @PostMapping("/saveAmount")
    public ResponseEntity<?> saveTemp(HttpSession session, @RequestBody SaveAmountRequest saveAmountRequest) {
        session.setAttribute(String.valueOf(saveAmountRequest.getOrderId()), saveAmountRequest.getAmount());
        return ResponseEntity.ok("결제 정보 저장 완료");
    }

}