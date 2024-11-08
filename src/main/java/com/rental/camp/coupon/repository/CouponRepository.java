package com.rental.camp.coupon.repository;

import com.rental.camp.coupon.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponRepositoryCustom {
}
