package com.rental.camp.order.service;

import com.rental.camp.coupon.dto.Coupon;
import com.rental.camp.coupon.repository.CouponRepository;
import com.rental.camp.order.dto.*;
import com.rental.camp.order.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartItemService {
    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRepository;

    @Transactional
    public CartItemResponse addCartItem(CartItemRequest requestDto) {
        if (cartItemRepository.existsByUserIdAndRentalItemId(requestDto.getUserId(), requestDto.getRentalItemId())) {
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

    @Transactional
    public UpdateCartItemResponse updateCartItemQuantity(UpdateCartItemRequest request) {
        CartItemResponse cartItem = cartItemRepository.findCartItemWithRentalInfo(
                request.getCartItemId(),
                request.getUserId()
        );

        if (cartItem == null) {
            return UpdateCartItemResponse.error("장바구니 아이템을 찾을 수 없습니다.");
        }

        if (request.getQuantity() > cartItem.getRentalItem().getStock()) {
            return UpdateCartItemResponse.error(
                    "주문 가능한 수량을 초과하였습니다. 현재 재고: " + cartItem.getRentalItem().getStock()
            );
        }

        if (request.getQuantity() < 1) {
            return UpdateCartItemResponse.error("수량은 1보다 작을 수 없습니다.");
        }

        boolean updated = cartItemRepository.updateCartItemQuantity(
                request.getCartItemId(),
                request.getUserId(),
                request.getQuantity()
        );

        if (!updated) {
            return UpdateCartItemResponse.error("수량 변경에 실패했습니다.");
        }

        CartItemResponse updatedCartItem = cartItemRepository.findCartItemWithRentalInfo(
                request.getCartItemId(),
                request.getUserId()
        );

        return new UpdateCartItemResponse(updatedCartItem, "수량이 성공적으로 변경되었습니다.");
    }

    public CartItemListResponse getCartItemsByUserId(Long userId) {
        List<CartItemResponse> cartItems = cartItemRepository.findCartItemsWithRentalInfoByUserId(userId);
        List<Coupon> coupons = couponRepository.findCouponsByUserId(userId);

        return CartItemListResponse.builder()
                .userId(userId)
                .cartItems(cartItems)
                .coupons(coupons)
                .build();
    }

    @Transactional
    public void deleteCartItem(Long cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new IllegalArgumentException("장바구니에 담지 않은 물건입니다");
        }
        cartItemRepository.deleteById(cartItemId);
    }
}

