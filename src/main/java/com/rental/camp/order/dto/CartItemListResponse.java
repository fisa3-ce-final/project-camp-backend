package com.rental.camp.order.dto;

import com.rental.camp.coupon.dto.Coupon;
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
    private List<CartItem> cartItems;
    private List<Coupon> coupons;
}