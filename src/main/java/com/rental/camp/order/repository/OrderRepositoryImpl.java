package com.rental.camp.order.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.order.dto.OrderItemInfo;
import com.rental.camp.order.dto.OrderRequest;
import com.rental.camp.order.model.*;
import com.rental.camp.order.model.type.OrderStatus;
import com.rental.camp.rental.model.QRentalItem;
import com.rental.camp.rental.model.RentalItem;
import com.rental.camp.user.model.QUser;
import com.rental.camp.user.model.User;
import com.rental.camp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;


    private BooleanExpression existsOrderItemForCartItem(QOrderItem qOrderItem, OrderRequest request) {
        CartItem cartItem = cartItemRepository.findById(request.getCartItemIds().get(0))
                .orElseThrow(() -> new RuntimeException("CartItem not found: " + request.getCartItemIds().get(0)));

        return JPAExpressions
                .selectOne()
                .from(qOrderItem)
                .where(
                        qOrderItem.orderId.eq(QOrder.order.id)
                                .and(qOrderItem.rentalItemId.eq(cartItem.getRentalItemId()))
                )
                .exists();
    }

    @Override
    public List<OrderItemInfo> findOrderItemsWithDetails(Long orderId) {
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

    @Override
    public Optional<Order> findOrderByIdAndUserId(Long orderId, Long userId) {
        QOrder qOrder = QOrder.order;

        return Optional.ofNullable(
                queryFactory
                        .selectFrom(qOrder)
                        .where(
                                qOrder.id.eq(orderId)
                                        .and(qOrder.userId.eq(userId))
                        )
                        .fetchOne()
        );
    }

    @Override
    public Optional<Order> findPendingOrderByUserAndItem(String uuid, OrderRequest request) {
        QOrder qOrder = QOrder.order;
        QOrderItem qOrderItem = QOrderItem.orderItem;

        Long userId = userRepository.findByUuid(UUID.fromString(uuid)).getId();
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(qOrder)
                        .where(
                                qOrder.userId.eq(userId)
                                        .and(qOrder.orderStatus.eq(OrderStatus.PENDING))
                                        .and(existsOrderItemForCartItem(qOrderItem, request))
                        )
                        .fetchOne()
        );
    }

    @Override
    public Optional<Order> findPendingOrderByOrderId(Long orderId) {
        QOrder qOrder = QOrder.order;
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(qOrder)
                        .where(
                                qOrder.id.eq(orderId)
                                        .and(qOrder.orderStatus.eq(OrderStatus.PENDING))
                        )
                        .fetchOne()
        );
    }

    @Override
    public Optional<Order> findPendingOrderByUser(Long userId) {
        QOrder qOrder = QOrder.order;
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(qOrder)
                        .where(
                                qOrder.userId.eq(userId)
                                        .and(qOrder.orderStatus.eq(OrderStatus.PENDING))
                        )
                        .fetchOne()
        );
    }

    @Override
    public Map<Long, RentalItem> findRentalItemsByIds(List<Long> rentalItemIds) {
        QRentalItem qRentalItem = QRentalItem.rentalItem;

        return queryFactory
                .selectFrom(qRentalItem)
                .where(qRentalItem.id.in(rentalItemIds))
                .fetch()
                .stream()
                .collect(Collectors.toMap(RentalItem::getId, Function.identity()));
    }

    @Override
    public Optional<User> findUserById(Long userId) {
        QUser qUser = QUser.user;

        return Optional.ofNullable(
                queryFactory
                        .selectFrom(qUser)
                        .where(qUser.id.eq(userId))
                        .fetchOne()
        );
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

    @Override
    public boolean existsByUserIdAndStatusAndCartItemIds(Long userId, OrderStatus status, List<Long> cartItemIds) {
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        QCartItem cartItem = QCartItem.cartItem;

        Integer count = queryFactory.selectOne().from(order)
                .join(orderItem).on(order.id.eq(orderItem.orderId))
                .join(cartItem).on(orderItem.rentalItemId.eq(cartItem.rentalItemId))
                .where(
                        order.userId.eq(userId),
                        order.orderStatus.eq(status),
                        cartItem.id.in(cartItemIds)
                )
                .fetchFirst();
        return count != null;
    }

    @Override
    public boolean existsByUserIdAndStatusAndCouponId(Long userId, OrderStatus status, Long couponId) {
        QOrder order = QOrder.order;

        Integer count = queryFactory
                .selectOne()
                .from(order)
                .where(
                        order.userId.eq(userId),
                        order.orderStatus.eq(status),
                        order.couponId.eq(couponId) // 쿠폰 ID 조건
                )
                .fetchFirst();

        return count != null;
    }


}



