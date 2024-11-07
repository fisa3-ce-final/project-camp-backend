package com.rental.camp.order.service;

import com.rental.camp.coupon.dto.CouponDto;
import com.rental.camp.coupon.repository.CouponRepository;
import com.rental.camp.order.dto.CartItemDto;
import com.rental.camp.order.dto.CartItemListResponse;
import com.rental.camp.order.dto.CartItemRequest;
import com.rental.camp.order.dto.CartItemResponse;
import com.rental.camp.order.model.CartItem;
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
        CartItem cartItem = new CartItem();
        cartItem.setUserId(requestDto.getUserId());
        cartItem.setRentalItemId(requestDto.getRentalItemId());
        cartItemRepository.save(cartItem);

        CartItemResponse responseDto = new CartItemResponse();
        responseDto.setUserId(requestDto.getUserId());
        return responseDto;
    }


    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRepository;

    public CartItemListResponse getCartItemsByUserId(Long userId) {
        // 장바구니 아이템 조회
        List<CartItemDto> cartItems = cartItemRepository.findCartItemsWithRentalInfoByUserId(userId);

        // 유저 보유 쿠폰 조회
        List<CouponDto> coupons = couponRepository.findCouponsByUserId(userId);

        CartItemListResponse responseDto = new CartItemListResponse();
        responseDto.setUserId(userId);
        responseDto.setCartItems(cartItems);
        responseDto.setCoupons(coupons);
        return responseDto;
    }
}
