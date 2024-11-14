package com.rental.camp.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rental.camp.coupon.dto.CouponResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemListResponse {
    private Long userId;
    private List<CartItemResponse> cartItems;
    private List<CouponResponse> coupons;
}