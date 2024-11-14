package com.rental.camp.coupon.repository;

import com.rental.camp.coupon.model.UserCoupon;

public interface UserCouponRepositoryCustom {
    // 유저 쿠폰 획득
    UserCoupon createUserCoupon(Long userId, Long couponId);
}
