// CouponRepositoryImpl.java
package com.rental.camp.coupon.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.coupon.dto.CouponResponse;
import com.rental.camp.coupon.model.Coupon;
import com.rental.camp.coupon.model.QCoupon;
import com.rental.camp.coupon.model.QUserCoupon;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    // 유저가 가진 모든  쿠폰 조회
    @Override
    public Page<CouponResponse> findCouponsByUserId(Long userId, Pageable pageable) {
        QCoupon coupon = QCoupon.coupon;
        QUserCoupon userCoupon = QUserCoupon.userCoupon;

        List<CouponResponse> content = queryFactory
                .select(Projections.constructor(CouponResponse.class,
                        coupon.id,
                        coupon.name,
                        coupon.discount,
                        coupon.type,
                        coupon.expiryDate,
                        userCoupon.isUsed,
                        coupon.createdAt))
                .from(coupon)
                .innerJoin(userCoupon)
                .on(
                        coupon.id.eq(userCoupon.couponId)
                                .and(userCoupon.userId.eq(userId))
                ).
                orderBy(coupon.createdAt.desc())
                .offset(pageable.getOffset())     // Pageable의 오프셋 설정
                .limit(pageable.getPageSize())    // Pageable의 페이지 크기 설정
                .fetch();

        long total = queryFactory
                .selectFrom(coupon)
                .innerJoin(userCoupon)
                .on(
                        coupon.id.eq(userCoupon.couponId)
                                .and(userCoupon.userId.eq(userId))
                )
                .fetchCount();

        return new PageImpl<>(content, pageable, total); // Page 객체 반환
    }

    // 주문 시 적용 가능한 쿠폰 조회
    @Override
    public List<CouponResponse> findCouponsByUserId(Long userId) {
        QCoupon coupon = QCoupon.coupon;
        QUserCoupon userCoupon = QUserCoupon.userCoupon;

        return queryFactory
                .select(Projections.constructor(CouponResponse.class,
                        coupon.id,
                        coupon.name,
                        coupon.discount,
                        coupon.type,
                        coupon.expiryDate,
                        coupon.createdAt
                ))
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

    @Override
    public Page<Coupon> findAllCoupon(Pageable pageable) {
        QCoupon coupon = QCoupon.coupon;

        List<Coupon> coupons = queryFactory
                .selectFrom(coupon)
                .where(coupon.isDeleted.eq(false))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(coupon.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(coupon.count())
                .from(coupon)
                .where(coupon.isDeleted.eq(false))
                .fetchOne();

        return new PageImpl<>(coupons, pageable, total != null ? total : 0L);
    }
}
