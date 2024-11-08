package com.rental.camp.order.repository;

import com.rental.camp.order.dto.CartItem;

import java.math.BigDecimal;
import java.util.List;

public interface CartItemRepositoryCustom {
    boolean existsByUserIdAndRentalItemId(Long userId, Long rentalItemId);

    List<BigDecimal> findRentalItemPricesByCartItemIds(List<Long> cartItemIds);

    List<CartItem> findCartItemsWithRentalInfoByUserId(Long userId);
}