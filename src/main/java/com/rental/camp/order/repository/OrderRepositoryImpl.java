package com.rental.camp.order.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.order.dto.OrderConflict;
import com.rental.camp.order.model.QOrder;
import com.rental.camp.order.model.QOrderItem;
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

}

