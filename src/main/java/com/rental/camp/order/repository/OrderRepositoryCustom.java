package com.rental.camp.order.repository;

import com.rental.camp.order.dto.OrderItemInfo;
import com.rental.camp.order.dto.OrderRequest;
import com.rental.camp.order.model.CartItem;
import com.rental.camp.order.model.Order;
import com.rental.camp.order.model.type.OrderStatus;
import com.rental.camp.rental.model.RentalItem;
import com.rental.camp.user.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderRepositoryCustom {


    List<OrderItemInfo> findOrderItemsWithDetails(Long orderId);

    Optional<Order> findOrderByIdAndUserId(Long orderId, Long userId);

    Optional<Order> findPendingOrderByUserAndItem(String uuid, OrderRequest request);


    List<CartItem> checkRentalItemStock(List<Long> cartItemIds, Long userId);

    Order findOrderByCartItems(List<Long> cartItemIds, Long userId);

    Optional<Order> findPendingOrderByOrderId(Long orderId);

    List<Order> findPendingOrderByUser(Long userId);

    Map<Long, RentalItem> findRentalItemsByIds(List<Long> rentalItemIds);

    Optional<User> findUserById(Long userId);

    Optional<Order> findCancellableOrder(Long orderId, Long userId);

    boolean updateOrderStatus(Long orderId, OrderStatus status);

    List<OrderItemInfo> findOrderItems(Long orderId);

    boolean existsByUserIdAndStatusAndCartItemIds(Long userId, OrderStatus status, List<Long> cartItemIds);

    boolean existsByUserIdAndStatusAndCouponId(Long userId, OrderStatus status, Long couponId);

    void deleteOrderItemsByOrderId(Long orderId);
}