package com.rental.camp.order.dto;

import com.rental.camp.coupon.dto.CouponDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemListResponse {
    private Long userId;
    private List<CartItemDto> cartItems;
    private List<CouponDto> coupons;
}