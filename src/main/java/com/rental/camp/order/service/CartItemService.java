package com.rental.camp.order.service;

import com.rental.camp.coupon.dto.Coupon;
import com.rental.camp.coupon.repository.CouponRepository;
import com.rental.camp.order.dto.CartItemListResponse;
import com.rental.camp.order.dto.CartItemRequest;
import com.rental.camp.order.dto.CartItemResponse;
import com.rental.camp.order.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartItemService {

    public CartItemResponse addCartItem(CartItemRequest requestDto) {
        boolean exists = cartItemRepository.existsByUserIdAndRentalItemId(requestDto.getUserId(), requestDto.getRentalItemId());
        if (exists) {
            throw new IllegalArgumentException("이미 장바구니에 담긴 아이템입니다.");
        }
        com.rental.camp.order.model.CartItem cartItem = new com.rental.camp.order.model.CartItem();
        cartItem.setUserId(requestDto.getUserId());
        cartItem.setRentalItemId(requestDto.getRentalItemId());
        cartItemRepository.save(cartItem);

        CartItemResponse responseDto = new CartItemResponse();
        responseDto.setId(requestDto.getUserId());
        return responseDto;
    }


    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRepository;

    public CartItemListResponse getCartItemsByUserId(Long userId) {
        // 장바구니 아이템 조회
        List<CartItemResponse> cartItems = cartItemRepository.findCartItemsWithRentalInfoByUserId(userId);
        // 유저 보유 쿠폰 조회
        List<Coupon> coupons = couponRepository.findCouponsByUserId(userId);

        return CartItemListResponse.builder()
                .userId(userId)
                .cartItems(cartItems)
                .coupons(coupons)
                .build();
    }
}
