// CouponRepositoryImpl.java
package com.rental.camp.coupon.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.coupon.dto.Coupon;
import com.rental.camp.coupon.model.QCoupon;
import com.rental.camp.coupon.model.QUserCoupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Coupon> findCouponsByUserId(Long userId) {
        QCoupon coupon = QCoupon.coupon;
        QUserCoupon userCoupon = QUserCoupon.userCoupon;

        return queryFactory
                .select(Projections.constructor(Coupon.class,
                        coupon.id,
                        coupon.name,
                        coupon.discount,
                        coupon.type,
                        coupon.expiryDate))
                .from(coupon)
                .innerJoin(userCoupon)
                .on(
                        coupon.id.eq(userCoupon.couponId)
                                .and(userCoupon.userId.eq(userId))
                                .and(userCoupon.isUsed.eq(false))
                )
                .where(
                        coupon.isDeleted.eq(false)
                                .and(coupon.expiryDate.after(LocalDateTime.now()))
                )
                .fetch();
    }
}
