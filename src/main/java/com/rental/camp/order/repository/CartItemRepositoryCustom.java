package com.rental.camp.order.repository;

import com.rental.camp.order.dto.CartItemResponse;

import java.util.List;

public interface CartItemRepositoryCustom {
    boolean existsByUserIdAndRentalItemId(Long userId, Long rentalItemId);

    List<CartItemResponse> findCartItemsWithRentalInfoByUserId(Long userId);

    public boolean updateCartItemQuantity(Long cartItemId, Long userId, Integer quantity);

    public CartItemResponse findCartItemWithRentalInfo(Long cartItemId, Long userId);

}