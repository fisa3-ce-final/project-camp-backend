// CouponRepositoryCustom.java
package com.rental.camp.coupon.repository;

import com.rental.camp.coupon.dto.CouponDto;

import java.util.List;

public interface CouponRepositoryCustom {
    List<CouponDto> findCouponsByUserId(Long userId);
}
