package com.rental.camp.order.repository;

import com.rental.camp.order.dto.OrderConflict;
import com.rental.camp.order.dto.OrderDetails;
import com.rental.camp.order.model.type.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepositoryCustom {

    public void updateOrderStatus(Long userId, Long cartItemId, OrderStatus status);

    List<OrderConflict> findConflictingOrdersWithItemNames(List<Long> rentalItemIds, LocalDateTime rentalDate, LocalDateTime returnDate);

    OrderDetails findOrderWithDetailsByOrderIdAndUserId(Long orderId, Long userId);
}