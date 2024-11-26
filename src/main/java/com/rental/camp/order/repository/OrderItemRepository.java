package com.rental.camp.order.repository;

import com.rental.camp.order.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    Integer countByRentalItemId(Long rentalItemId);
}
