package com.rental.camp.coupon.repository;

import com.rental.camp.coupon.model.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponRepositoryCustom {

    Page<Coupon> findByIsDeletedFalseAndExpiryDateAfter(LocalDateTime now, Pageable pageable);
}
