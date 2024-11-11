// CouponRepositoryCustom.java
package com.rental.camp.coupon.repository;

import com.rental.camp.coupon.dto.Coupon;

import java.util.List;

public interface CouponRepositoryCustom {
    List<Coupon> findCouponsByUserId(Long userId);
}
