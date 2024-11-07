package com.rental.camp.order.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.order.dto.OrderConflictDTO;
import com.rental.camp.order.dto.QOrderConflictDTO;
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
    public List<OrderConflictDTO> findConflictingOrdersWithItemNames(Long rentalItemId, LocalDateTime rentalDate, LocalDateTime returnDate) {
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        QRentalItem rentalItem = QRentalItem.rentalItem;

        return queryFactory.select(
                        new QOrderConflictDTO(
                                rentalItem.name,    // RentalItem의 이름
                                order.returnDate    // Order의 반납 날짜
                        ))
                .from(order)
                .join(orderItem).on(order.id.eq(orderItem.orderId)) // Order와 OrderItem을 orderId로 조인
                .join(rentalItem).on(orderItem.rentalItemId.eq(rentalItem.id)) // OrderItem과 RentalItem을 rentalItemId로 조인
                .where(
                        rentalItem.id.eq(rentalItemId)
                                .and(order.returnDate.after(rentalDate))
                                .and(order.rentalDate.before(returnDate))
                )
                .fetch();
    }
    
}

