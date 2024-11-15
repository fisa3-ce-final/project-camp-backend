package com.rental.camp.admin.controller;

import com.rental.camp.admin.service.AdminCouponService;
import com.rental.camp.coupon.dto.AdminCouponResponse;
import com.rental.camp.coupon.dto.PublishCouponRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/admin")
@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminCouponController {
    private final AdminCouponService adminCouponService;

    @PostMapping("/coupon")
    public AdminCouponResponse publishCoupon(@RequestBody PublishCouponRequest publishCouponRequest) {
        return adminCouponService.createCoupon(publishCouponRequest);
    }


    @GetMapping("/coupon")
    public Page<AdminCouponResponse> findAllCoupon(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminCouponService.findAllCoupon(pageable);
    }

    @DeleteMapping("/coupon/{couponId}")
    public ResponseEntity<?> deleteCoupon(@PathVariable(name = "couponId") Long couponId) {
        try {
            adminCouponService.deleteCoupon(couponId);
            return ResponseEntity.ok("삭제 성공");
        } catch (EntityNotFoundException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
