package com.rental.camp.order.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.order.dto.CartItem;
import com.rental.camp.order.dto.CartItemResponse;
import com.rental.camp.order.model.QCartItem;
import com.rental.camp.rental.dto.RentalItemResponse;
import com.rental.camp.rental.model.QRentalItem;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.rental.camp.order.model.QCartItem.cartItem;

@RequiredArgsConstructor
public class CartItemRepositoryImpl implements CartItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsByUserIdAndRentalItemId(Long userId, Long rentalItemId) {
        return queryFactory.selectOne()
                .from(cartItem)
                .where(cartItem.userId.eq(userId)
                        .and(cartItem.rentalItemId.eq(rentalItemId)))
                .fetchFirst() != null;
    }

//    @Override
//    public List<BigDecimal> findRentalItemPricesByCartItemIds(List<Long> cartItemIds) {
//        QCartItem cartItem = QCartItem.cartItem;
//        QRentalItem rentalItem = QRentalItem.rentalItem;
//
//        return queryFactory.select(rentalItem.price)
//                .from(cartItem)
//                .join(rentalItem).on(cartItem.rentalItemId.eq(rentalItem.id)) // rentalItemId를 기준으로 조인
//                .where(cartItem.id.in(cartItemIds))
//                .fetch();
//    }

    @Override
    public List<CartItemResponse> findCartItemsWithRentalInfoByUserId(Long userId) {
        return queryFactory
                .select(Projections.constructor(CartItemResponse.class,
                        QCartItem.cartItem.id,
                        QCartItem.cartItem.quantity,
                        Projections.constructor(RentalItemResponse.class,
                                QRentalItem.rentalItem.id,
                                QRentalItem.rentalItem.name,
                                QRentalItem.rentalItem.price,
                                QRentalItem.rentalItem.stock,
                                QRentalItem.rentalItem.category,
                                QRentalItem.rentalItem.status
                        )
                ))
                .from(QCartItem.cartItem)
                .join(QRentalItem.rentalItem).on(QCartItem.cartItem.rentalItemId.eq(QRentalItem.rentalItem.id))
                .where(QCartItem.cartItem.userId.eq(userId))
                .fetch();
    }

    @Override
    public List<CartItem> findAllByIdAndUserId(List<Long> cartItemIds, Long userId) {
        QCartItem qCartItem = QCartItem.cartItem;

        return queryFactory
                .select(Projections.constructor(CartItem.class,
                        qCartItem.id,
                        qCartItem.userId,
                        qCartItem.rentalItemId,
                        qCartItem.quantity
                        // 필요한 다른 필드들 추가
                ))
                .from(qCartItem)
                .where(
                        qCartItem.id.in(cartItemIds)
                                .and(qCartItem.userId.eq(userId))
                )
                .fetch();
    }

}
