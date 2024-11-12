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
public class CartItemService {
    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRepository;

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

    @Transactional
    public UpdateCartItemResponse updateCartItemQuantity(UpdateCartItemRequest request) {
        // 장바구니 아이템과 대여 아이템 정보 조회
        CartItemResponse cartItem = cartItemRepository.findCartItemWithRentalInfo(
                request.getCartItemId(),
                request.getUserId()
        );

        if (cartItem == null) {
            return UpdateCartItemResponse.error("장바구니 아이템을 찾을 수 없습니다.");
        }

        // 재고 확인
        if (request.getQuantity() > cartItem.getRentalItem().getStock()) {
            return UpdateCartItemResponse.error(
                    "주문 가능한 수량을 초과하였습니다. 현재 재고: " + cartItem.getRentalItem().getStock()
            );
        }

        // 최소 수량 확인
        if (request.getQuantity() < 1) {
            return UpdateCartItemResponse.error("수량은 1보다 작을 수 없습니다.");
        }

        // 수량 업데이트
        long updatedCount = cartItemRepository.updateCartItemQuantity(
                request.getCartItemId(),
                request.getUserId(),
                request.getQuantity()
        );

        if (updatedCount == 0) {
            return UpdateCartItemResponse.error("수량 변경에 실패했습니다.");
        }

        // 업데이트 후 최신 정보 조회
        CartItemResponse updatedCartItem = cartItemRepository.findCartItemWithRentalInfo(
                request.getCartItemId(),
                request.getUserId()
        );

        return new UpdateCartItemResponse(updatedCartItem, "수량이 성공적으로 변경되었습니다.");
    }

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

    // 장바구니 특정 물품 삭제 로직
    public void deleteCartItem(Long cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new RuntimeException("장바구니에 담지 않은 물건입니다");
        }
        cartItemRepository.deleteById(cartItemId);

    }
}
