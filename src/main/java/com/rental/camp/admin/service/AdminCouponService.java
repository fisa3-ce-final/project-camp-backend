package com.rental.camp.admin.service;

import com.rental.camp.coupon.dto.AdminCouponResponse;
import com.rental.camp.coupon.dto.PublishCouponRequest;
import com.rental.camp.coupon.model.Coupon;
import com.rental.camp.coupon.model.type.CouponType;
import com.rental.camp.coupon.repository.CouponRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminCouponService {
    private final CouponRepository couponRepository;
    @PersistenceContext
    private final EntityManager entityManager;

    @Transactional
    public AdminCouponResponse createCoupon(PublishCouponRequest request) {
        Coupon coupon = new Coupon();
        coupon.setName(request.getName());
        coupon.setAmount(request.getAmount());
        coupon.setDiscount(request.getDiscount());
        coupon.setType(CouponType.valueOf(request.getType())); // Enum 변환
        coupon.setExpiryDate(request.getExpiryDate());
        coupon.setAmount(request.getAmount());
        coupon.setIsDeleted(false);
        Coupon savedCoupon = couponRepository.save(coupon);


        entityManager.flush();
        entityManager.refresh(savedCoupon);
        return AdminCouponResponse.builder()
                .couponId(savedCoupon.getId())
                .name(savedCoupon.getName())
                .discount(savedCoupon.getDiscount())
                .type(savedCoupon.getType())
                .expiryDate(savedCoupon.getExpiryDate())
                .amount(savedCoupon.getAmount())
                .createdAt(savedCoupon.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<AdminCouponResponse> findAllCoupon(Pageable pageable) {
        return couponRepository.findAllCoupon(pageable)
                .map(coupon -> AdminCouponResponse.builder()
                        .couponId(coupon.getId())
                        .name(coupon.getName())
                        .discount(coupon.getDiscount())
                        .type(coupon.getType())
                        .expiryDate(coupon.getExpiryDate())
                        .amount(coupon.getAmount())
                        .createdAt(coupon.getCreatedAt())
                        .build()
                );
    }

    @Transactional
    public void deleteCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new EntityNotFoundException("쿠폰을 찾을 수 없습니다 id: " + couponId));

        // 이미 삭제된 쿠폰인지 확인
        if (coupon.getIsDeleted()) {
            throw new IllegalStateException("이미 삭제된 쿠폰입니다: " + couponId);
        }

        coupon.setIsDeleted(true);

        couponRepository.save(coupon);
        entityManager.flush();
    }

}
