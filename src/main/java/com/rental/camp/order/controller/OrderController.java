package com.rental.camp.order.controller;

import com.rental.camp.order.dto.OrderRequest;
import com.rental.camp.order.dto.OrderResponse;
import com.rental.camp.order.dto.PaymentInfo;
import com.rental.camp.order.dto.SaveAmountRequest;
import com.rental.camp.order.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/reserve")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest requestDTO, JwtAuthenticationToken principle) {
        String uuid = principle.getName();
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
    public ResponseEntity<OrderResponse> completeOrder(@RequestBody OrderRequest request, JwtAuthenticationToken principle) {
        String uuid = principle.getName();
        OrderResponse response = orderService.completeOrder(uuid, request);
        return ResponseEntity.ok(response);
    }

    // 특정 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetails(
            @PathVariable(name = "orderId") Long orderId,
            JwtAuthenticationToken principle) {
        String uuid = principle.getName();
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
            JwtAuthenticationToken principle
    ) {
        String uuid = principle.getName();
        try {
            OrderResponse response = orderService.cancelOrder(uuid, orderId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/amount")
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
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

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
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