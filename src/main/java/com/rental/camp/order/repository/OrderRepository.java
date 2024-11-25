package com.rental.camp.order.repository;

import com.rental.camp.order.model.Order;
import com.rental.camp.order.model.type.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {


    Optional<Order> findByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus);
}
