package com.rental.camp.order.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.order.dto.OrderConflict;
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

}

