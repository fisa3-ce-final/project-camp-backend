// CouponRepositoryCustom.java
package com.rental.camp.coupon.repository;

import com.rental.camp.coupon.dto.CouponResponse;
import com.rental.camp.coupon.model.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CouponRepositoryCustom {
    List<CouponResponse> findCouponsByUserId(Long userId);


    public Page<CouponResponse> findCouponsByUserId(Long userId, Pageable pageable);

    Page<Coupon> findAllCoupon(Pageable pageable);
}
