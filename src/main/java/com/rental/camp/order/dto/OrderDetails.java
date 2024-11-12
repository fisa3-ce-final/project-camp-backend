package com.rental.camp.order.dto;

import com.rental.camp.order.model.Order;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderDetails {
    private Order order;
    private List<OrderItemInfo> orderItems;

    public OrderDetails(Order order, List<OrderItemInfo> orderItems) {
        this.order = order;
        this.orderItems = orderItems;
    }

    public Order getOrder() {
        return order;
    }

    public List<OrderItemInfo> getOrderItems() {
        return orderItems;
    }
}