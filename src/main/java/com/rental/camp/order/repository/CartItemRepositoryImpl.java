package com.rental.camp.order.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.order.dto.CartItemResponse;
import com.rental.camp.rental.dto.RentalItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.rental.camp.order.model.QCartItem.cartItem;
import static com.rental.camp.rental.model.QRentalItem.rentalItem;

@Repository
@RequiredArgsConstructor
public class CartItemRepositoryImpl implements CartItemRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsByUserIdAndRentalItemId(Long userId, Long rentalItemId) {
        Integer result = queryFactory
                .selectOne()
                .from(cartItem)
                .where(cartItem.userId.eq(userId)
                        .and(cartItem.rentalItemId.eq(rentalItemId)))
                .fetchFirst();
        return result != null;
    }

    @Override
    public CartItemResponse findCartItemWithRentalInfo(Long cartItemId, Long userId) {
        return queryFactory
                .select(Projections.constructor(CartItemResponse.class,
                        cartItem.id,
                        cartItem.quantity,
                        Projections.constructor(RentalItemResponse.class,
                                rentalItem.id,
                                rentalItem.name,
                                rentalItem.price,
                                rentalItem.stock,
                                rentalItem.category,
                                rentalItem.status
                        )
                ))
                .from(cartItem)
                .join(rentalItem).on(cartItem.rentalItemId.eq(rentalItem.id))
                .where(
                        cartItem.id.eq(cartItemId),
                        cartItem.userId.eq(userId)
                )
                .fetchOne();
    }

    @Override
    public boolean updateCartItemQuantity(Long cartItemId, Long userId, Integer quantity) {
        long updatedCount = queryFactory
                .update(cartItem)
                .set(cartItem.quantity, quantity)
                .where(
                        cartItem.id.eq(cartItemId),
                        cartItem.userId.eq(userId)
                )
                .execute();
        return updatedCount > 0;
    }

    @Override
    public List<CartItemResponse> findCartItemsWithRentalInfoByUserId(Long userId) {
        return queryFactory
                .select(Projections.constructor(CartItemResponse.class,
                        cartItem.id,
                        cartItem.quantity,
                        Projections.constructor(RentalItemResponse.class,
                                rentalItem.id,
                                rentalItem.name,
                                rentalItem.price,
                                rentalItem.stock,
                                rentalItem.category,
                                rentalItem.status
                        )
                ))
                .from(cartItem)
                .join(rentalItem).on(cartItem.rentalItemId.eq(rentalItem.id))
                .where(cartItem.userId.eq(userId))
                .fetch();
    }

}
