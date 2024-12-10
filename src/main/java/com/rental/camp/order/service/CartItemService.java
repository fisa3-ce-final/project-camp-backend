package com.rental.camp.order.service;

import com.rental.camp.coupon.dto.CouponResponse;
import com.rental.camp.coupon.repository.CouponRepository;
import com.rental.camp.order.dto.*;
import com.rental.camp.order.repository.CartItemRepository;
import com.rental.camp.rental.model.RentalItem;
import com.rental.camp.rental.repository.RentalItemRepository;
import com.rental.camp.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartItemService {
    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final RentalItemRepository rentalItemRepository;

    @Transactional
    public CartItemResponse addCartItem(String uuid, CartItemRequest requestDto) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();

        if (requestDto.getRentalItemId() == null || requestDto.getRentalItemId() <= 0) {
            throw new IllegalArgumentException("유효하지 않은 대여 상품 ID입니다.");
        }

        RentalItem rentalItem = rentalItemRepository.findById(requestDto.getRentalItemId())
                .orElseThrow(() -> new EntityNotFoundException("해당 대여 상품을 찾을 수 없습니다."));
        if(rentalItem.getStock() <=0){
            throw new RuntimeException("재고가 없는 상품입니다.");
        }

        if (cartItemRepository.existsByUserIdAndRentalItemId(userId, requestDto.getRentalItemId())) {
            throw new IllegalArgumentException("이미 장바구니에 담긴 아이템입니다.");
        }

        com.rental.camp.order.model.CartItem cartItem = new com.rental.camp.order.model.CartItem();
        cartItem.setUserId(userId);
        cartItem.setRentalItemId(requestDto.getRentalItemId());

        cartItemRepository.save(cartItem);

        CartItemResponse responseDto = new CartItemResponse();
        responseDto.setId(userId);
        return responseDto;
    }

    @Transactional
    public UpdateCartItemResponse updateCartItemQuantity(String uuid, UpdateCartItemRequest request) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        CartItemResponse cartItem = cartItemRepository.findCartItemWithRentalInfo(
                request.getCartItemId(),
                userId
        );

        if (cartItem == null) {
            return UpdateCartItemResponse.error("장바구니 아이템을 찾을 수 없습니다.");
        }

        if (request.getQuantity() > cartItem.getRentalItemResponse().getStock()) {
            return UpdateCartItemResponse.error(
                    "주문 가능한 수량을 초과하였습니다. 현재 재고: " + cartItem.getRentalItemResponse().getStock()
            );
        }

        if (request.getQuantity() < 1) {
            return UpdateCartItemResponse.error("수량은 1보다 작을 수 없습니다.");
        }

        boolean updated = cartItemRepository.updateCartItemQuantity(
                request.getCartItemId(),
                userId,
                request.getQuantity()
        );

        if (!updated) {
            return UpdateCartItemResponse.error("수량 변경에 실패했습니다.");
        }

        CartItemResponse updatedCartItem = cartItemRepository.findCartItemWithRentalInfo(
                request.getCartItemId(),
                userId
        );

        return new UpdateCartItemResponse(updatedCartItem, "수량이 성공적으로 변경되었습니다.");
    }

    public CartItemListResponse getCartItemsByUserId(String uuid) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        List<CartItemResponse> cartItems = cartItemRepository.findCartItemsWithRentalInfoByUserId(userId);
        List<CouponResponse> coupons = couponRepository.findCouponsByUserId(userId);

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

    public Long getCartQuantity(String uuid) {
        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        return cartItemRepository.countByUserId(userId);

    }
}


