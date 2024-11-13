package com.rental.camp.order.repository;

import com.rental.camp.order.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CartItemRepository extends JpaRepository<CartItem, Long>, CartItemRepositoryCustom {
    boolean existsByUserIdAndRentalItemId(Long userId, Long rentalItemId);


    void deleteAllByUserIdAndRentalItemIdIn(Long userId, List<Long> rentalItemIds);

}