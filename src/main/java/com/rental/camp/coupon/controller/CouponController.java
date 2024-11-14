package com.rental.camp.coupon.controller;

import com.rental.camp.coupon.dto.CouponResponse;
import com.rental.camp.coupon.dto.IssueCouponRequest;
import com.rental.camp.coupon.model.UserCoupon;
import com.rental.camp.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @GetMapping("/user-coupons")
    public ResponseEntity<Page<CouponResponse>> getUserCouponList(JwtAuthenticationToken principle, Pageable pageable) {
        String uuid = principle.getName();
        Page<CouponResponse> couponResponse = couponService.getCouponList(uuid, pageable);
        return ResponseEntity.ok(couponResponse);
    }

    @PostMapping("/user-coupons")
    public ResponseEntity<?> issueCoupon(@RequestBody IssueCouponRequest request, JwtAuthenticationToken principle) {
        try {
            String uuid = principle.getName();
            UserCoupon userCoupon = couponService.issueUserCoupon(uuid, request.getCouponId());
            return ResponseEntity.ok(userCoupon);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
