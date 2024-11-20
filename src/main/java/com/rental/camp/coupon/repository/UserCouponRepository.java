package com.rental.camp.coupon.repository;

import com.rental.camp.coupon.model.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long>, UserCouponRepositoryCustom {
    Optional<UserCoupon> findByCouponIdAndUserId(Long couponId, Long userId);


    List<UserCoupon> findByUserId(Long userId);


}
