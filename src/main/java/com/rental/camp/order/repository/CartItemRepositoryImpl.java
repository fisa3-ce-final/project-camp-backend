package com.rental.camp.order.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.order.dto.CartItemDto;
import com.rental.camp.order.model.QCartItem;
import com.rental.camp.rental.dto.RentalItemResponse;
import com.rental.camp.rental.model.QRentalItem;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
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

    @Override
    public List<BigDecimal> findRentalItemPricesByCartItemIds(List<Long> cartItemIds) {
        QCartItem cartItem = QCartItem.cartItem;
        QRentalItem rentalItem = QRentalItem.rentalItem;

        return queryFactory.select(rentalItem.price)
                .from(cartItem)
                .join(rentalItem).on(cartItem.rentalItemId.eq(rentalItem.id)) // rentalItemId를 기준으로 조인
                .where(cartItem.id.in(cartItemIds))
                .fetch();
    }
    
    @Override
    public List<CartItemDto> findCartItemsWithRentalInfoByUserId(Long userId) {
        return queryFactory
                .select(Projections.fields(CartItemDto.class,
                        QCartItem.cartItem.id.as("cartItemId"),
                        QCartItem.cartItem.quantity,
                        Projections.fields(RentalItemResponse.class,
                                QRentalItem.rentalItem.id.as("id"),
                                QRentalItem.rentalItem.name.as("name"),
                                QRentalItem.rentalItem.price.as("price"),
                                QRentalItem.rentalItem.stock.as("stock"),
                                QRentalItem.rentalItem.category.as("category"),
                                QRentalItem.rentalItem.status.as("status")
                        ).as("rentalItem")
                ))
                .from(QCartItem.cartItem)
                .join(QRentalItem.rentalItem).on(QCartItem.cartItem.rentalItemId.eq(QRentalItem.rentalItem.id))
                .where(QCartItem.cartItem.userId.eq(userId))
                .fetch();
    }
}
