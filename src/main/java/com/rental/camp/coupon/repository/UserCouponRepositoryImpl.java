package com.rental.camp.coupon.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.coupon.model.UserCoupon;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class UserCouponRepositoryImpl implements UserCouponRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    @PersistenceContext
    private final EntityManager entityManager;

    // 유저 쿠폰 획득
    @Override
    public UserCoupon createUserCoupon(Long userId, Long couponId) {
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setAcquiredAt(LocalDateTime.now());
        userCoupon.setIsUsed(false);

        entityManager.persist(userCoupon);

        return userCoupon;
    }
}
