package com.rental.camp.order.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.order.dto.OrderConflict;
import com.rental.camp.order.dto.OrderDetails;
import com.rental.camp.order.dto.OrderItemInfo;
import com.rental.camp.order.model.Order;
import com.rental.camp.order.model.QOrder;
import com.rental.camp.order.model.QOrderItem;
import com.rental.camp.order.model.type.OrderStatus;
import com.rental.camp.rental.model.QRentalItem;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public OrderRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }


    // findConflictingOrdersWithItemNames 메서드
    @Override
    public List<OrderConflict> findConflictingOrdersWithItemNames(List<Long> rentalItemIds, LocalDateTime rentalDate, LocalDateTime returnDate) {
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        QRentalItem rentalItem = QRentalItem.rentalItem;

        return queryFactory
                .select(Projections.constructor(OrderConflict.class,
                        rentalItem.name,
                        order.returnDate))
                .from(order)
                .join(orderItem).on(order.id.eq(orderItem.orderId))
                .join(rentalItem).on(orderItem.rentalItemId.eq(rentalItem.id))
                .where(
                        rentalItem.id.in(rentalItemIds)
                                .and(order.rentalDate.lt(returnDate))
                                .and(order.returnDate.gt(rentalDate))
                )
                .fetch();
    }

    public void updateOrderStatus(Long userId, Long cartItemId, OrderStatus status) {
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;

        // OrderItem 테이블과 조인하여 해당 cartItemId를 가진 주문을 찾아 상태 업데이트
        queryFactory
                .update(order)
                .set(order.orderStatus, status)
                .where(
                        order.userId.eq(userId)
                                .and(
                                        JPAExpressions
                                                .selectOne()
                                                .from(orderItem)
                                                .where(
                                                        orderItem.orderId.eq(order.id)
                                                                .and(orderItem.id.eq(cartItemId))
                                                )
                                                .exists()
                                )
                )
                .execute();
    }
    
    @Override
    public OrderDetails findOrderWithDetailsByOrderIdAndUserId(Long orderId, Long userId) {
        QOrder qOrder = QOrder.order;
        QOrderItem qOrderItem = QOrderItem.orderItem;
        QRentalItem qRentalItem = QRentalItem.rentalItem;

        // Order 조회
        Order order = queryFactory
                .selectFrom(qOrder)
                .where(
                        qOrder.id.eq(orderId),
                        qOrder.userId.eq(userId)
                )
                .fetchOne();

        if (order == null) {
            throw new RuntimeException("주문을 찾을 수 없습니다: orderId=" + orderId);
        }

        // OrderItems 조회
        List<OrderItemInfo> orderItems = queryFactory
                .select(Projections.constructor(OrderItemInfo.class,
                        qOrderItem.rentalItemId,
                        qRentalItem.name.as("itemName"),
                        qOrderItem.quantity,
                        qOrderItem.price,
                        qOrderItem.subtotal))
                .from(qOrderItem)
                .join(qRentalItem).on(qOrderItem.rentalItemId.eq(qRentalItem.id))
                .where(qOrderItem.orderId.eq(orderId))
                .fetch();

        return new OrderDetails(order, orderItems);
    }


}

