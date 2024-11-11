package com.rental.camp.order.repository;

import com.rental.camp.order.dto.CartItem;
import com.rental.camp.order.dto.CartItemResponse;

import java.util.List;

public interface CartItemRepositoryCustom {
    boolean existsByUserIdAndRentalItemId(Long userId, Long rentalItemId);


    //List<BigDecimal> findRentalItemPricesByCartItemIds(List<Long> cartItemIds);

    List<CartItemResponse> findCartItemsWithRentalInfoByUserId(Long userId);

    List<CartItem> findAllByIdAndUserId(List<Long> cartItemIds, Long userId);

}