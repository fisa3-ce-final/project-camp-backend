package com.rental.camp.order.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
import java.util.Optional;

@Repository
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public OrderRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Optional<Order> findCancellableOrder(Long orderId, Long userId) {
        QOrder qOrder = QOrder.order;

        LocalDateTime cancellationDeadline = LocalDateTime.now().minusMinutes(30);

        Order order = queryFactory
                .selectFrom(qOrder)
                .where(qOrder.id.eq(orderId)
                        .and(qOrder.userId.eq(userId))
                        .and(qOrder.orderStatus.eq(OrderStatus.COMPLETED))
                        .and(qOrder.updatedAt.after(cancellationDeadline)))
                .fetchOne();

        return Optional.ofNullable(order);
    }


    @Override
    public boolean updateOrderStatus(Long orderId, OrderStatus status) {
        QOrder qOrder = QOrder.order;

        long updatedCount = queryFactory
                .update(qOrder)
                .set(qOrder.orderStatus, status)
                .set(qOrder.updatedAt, LocalDateTime.now())
                .where(qOrder.id.eq(orderId))
                .execute();

        return updatedCount > 0;
    }

    @Override
    public List<OrderItemInfo> findOrderItems(Long orderId) {
        QOrderItem qOrderItem = QOrderItem.orderItem;
        QRentalItem qRentalItem = QRentalItem.rentalItem;

        return queryFactory
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
    }

}



