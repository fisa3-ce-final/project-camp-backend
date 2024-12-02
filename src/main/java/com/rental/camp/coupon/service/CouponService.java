package com.rental.camp.coupon.service;

import com.rental.camp.coupon.dto.ActiveCouponResponse;
import com.rental.camp.coupon.dto.CouponResponse;
import com.rental.camp.coupon.model.Coupon;
import com.rental.camp.coupon.model.UserCoupon;
import com.rental.camp.coupon.repository.CouponRepository;
import com.rental.camp.coupon.repository.UserCouponRepository;
import com.rental.camp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final RedisTemplate<String, Long> redisTemplate;


    public Page<ActiveCouponResponse> getAllActiveCoupons(String uuid, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        Page<Coupon> coupons = couponRepository.findByIsDeletedFalseAndExpiryDateAfterOrderByCreatedAtDesc(now, pageable);

        List<UserCoupon> receivedCoupons = userCouponRepository.findByUserId(userId);

        List<Long> receivedCouponIds = receivedCoupons.stream()
                .map(UserCoupon::getCouponId)
                .toList();

        // Coupon 엔티티를 CouponResponse로 변환
        return coupons.map(coupon -> ActiveCouponResponse.builder()
                .couponId(coupon.getId())
                .name(coupon.getName())
                .amount(coupon.getAmount())
                .discount(coupon.getDiscount())
                .type(coupon.getType())
                .expiryDate(coupon.getExpiryDate())
                .isUsed(false)
                .isReceived(receivedCouponIds.contains(coupon.getId()))
                .createdAt(coupon.getCreatedAt())

                .build());
    }

    public Page<CouponResponse> getCouponList(String uuid, Pageable pageable) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        return couponRepository.findCouponsByUserId(userId, pageable);
    }

//    @Transactional
//    public UserCoupon issueUserCoupon(String uuid, Long couponId) {
//        // 쿠폰 유효성 확인 (존재 여부, 삭제 여부, 만료일 등)
//        Coupon coupon = couponRepository.findById(couponId)
//                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 ID입니다."));
//
//        if (coupon.getIsDeleted() || coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
//            throw new IllegalStateException("이미 삭제되었거나 만료된 쿠폰입니다.");
//        }
//        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
//        // amount 감소 검증
//        if (coupon.getAmount() <= 0) {
//            throw new IllegalStateException("쿠폰의 수량이 부족합니다.");
//        }
//        if (userCouponRepository.findByCouponIdAndUserId(couponId, userId).isPresent()) {
//            throw new IllegalStateException("이미 발급받은 쿠폰입니다.");
//        }
//
//        // amount 감소
//        coupon.setAmount(coupon.getAmount() - 1);
//        couponRepository.save(coupon); // 쿠폰 정보 저장
//
//        // UserCoupon 생성 및 저장
//        return userCouponRepository.createUserCoupon(userId, couponId);
//    }

    public void initializeCouponInRedis(Coupon coupon) {
        String redisKey = "coupon:" + coupon.getId().toString();
        redisTemplate.opsForValue().set(redisKey, coupon.getAmount().longValue());
    }

    @Transactional
    public UserCoupon issueUserCoupon(String uuid, Long couponId) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        String redisKey = "coupon:" + couponId;

        // Lua 스크립트 실행
        String script = "local couponKey = KEYS[1] " +
                "local amount = tonumber(redis.call('get', couponKey)) " +
                "if not amount or amount <= 0 then " +
                "    return -1 " +
                "end " +
                "redis.call('decr', couponKey) " +
                "return redis.call('get', couponKey)";

        List<String> keys = Collections.singletonList(redisKey);
        Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), keys);

        if (result == null || result == -1) {
            throw new IllegalStateException("쿠폰의 수량이 부족합니다.");
        }

        // UserCoupon 생성
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 ID입니다."));

        if (coupon.getIsDeleted() || coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("이미 삭제되었거나 만료된 쿠폰입니다.");
        }

        if (userCouponRepository.findByCouponIdAndUserId(couponId, userId).isPresent()) {
            throw new IllegalStateException("이미 발급받은 쿠폰입니다.");
        }

        // Redis의 감소된 최종 값을 데이터베이스에 동기화
        coupon.setAmount(result.intValue());
        couponRepository.save(coupon);

        return userCouponRepository.createUserCoupon(userId, couponId);
    }
}

