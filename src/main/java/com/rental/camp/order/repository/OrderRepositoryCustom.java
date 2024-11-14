package com.rental.camp.order.repository;

import com.rental.camp.order.dto.OrderItemInfo;
import com.rental.camp.order.dto.OrderRequest;
import com.rental.camp.order.model.Order;
import com.rental.camp.order.model.type.OrderStatus;
import com.rental.camp.rental.model.RentalItem;
import com.rental.camp.user.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderRepositoryCustom {

    // public void updateOrderStatus(Long userId, Long cartItemId, OrderStatus status);

    //List<OrderConflict> findConflictingOrdersWithItemNames(List<Long> rentalItemIds, LocalDateTime rentalDate, LocalDateTime returnDate);

    // OrderDetails findOrderWithDetailsByOrderIdAndUserId(Long orderId, Long userId);

    List<OrderItemInfo> findOrderItemsWithDetails(Long orderId);

    Optional<Order> findOrderByIdAndUserId(Long orderId, Long userId);

    Optional<Order> findPendingOrderByUserAndItem(String uuid, OrderRequest request);
    

    Map<Long, RentalItem> findRentalItemsByIds(List<Long> rentalItemIds);

    Optional<User> findUserById(Long userId);

    Optional<Order> findCancellableOrder(Long orderId, Long userId);

    boolean updateOrderStatus(Long orderId, OrderStatus status);

    List<OrderItemInfo> findOrderItems(Long orderId);
}