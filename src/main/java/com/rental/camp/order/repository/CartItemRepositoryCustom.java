package com.rental.camp.order.repository;

import com.rental.camp.order.dto.CartItemDto;

import java.math.BigDecimal;
import java.util.List;

public interface CartItemRepositoryCustom {
    boolean existsByUserIdAndRentalItemId(Long userId, Long rentalItemId);

    List<BigDecimal> findRentalItemPricesByCartItemIds(List<Long> cartItemIds);

    List<CartItemDto> findCartItemsWithRentalInfoByUserId(Long userId);
}