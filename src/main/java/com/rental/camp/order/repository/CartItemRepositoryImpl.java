package com.rental.camp.order.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.order.dto.CartItemResponse;
import com.rental.camp.order.model.QCartItem;
import com.rental.camp.rental.dto.RentalItemForCartResponse;
import com.rental.camp.rental.dto.RentalItemResponse;
import com.rental.camp.rental.model.QRentalItem;
import com.rental.camp.rental.model.QRentalItemImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

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
        QCartItem cartItem = QCartItem.cartItem;
        QRentalItem rentalItem = QRentalItem.rentalItem;
        QRentalItemImage rentalItemImage = QRentalItemImage.rentalItemImage;

        // 장바구니 아이템 기본 정보 조회
        List<CartItemResponse> cartItems = queryFactory
                .select(Projections.constructor(CartItemResponse.class,
                        cartItem.id,
                        cartItem.quantity,
                        Projections.constructor(RentalItemForCartResponse.class,
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

        // 각 상품의 이미지 정보를 조회하여 매핑
        for (CartItemResponse cartItemResponse : cartItems) {
            List<RentalItemForCartResponse.ImageDto> itemImages = queryFactory
                    .selectFrom(rentalItemImage)
                    .where(rentalItemImage.rentalItemId.eq(cartItemResponse.getRentalItem().getId()))
                    .orderBy(rentalItemImage.imageOrder.asc())
                    .fetch()
                    .stream()
                    .map(image -> {
                        RentalItemForCartResponse.ImageDto imageDto = new RentalItemForCartResponse.ImageDto();
                        imageDto.setImageUrl(image.getImageUrl());
                        imageDto.setImageOrder(image.getImageOrder());
                        return imageDto;
                    })
                    .collect(Collectors.toList());

            cartItemResponse.getRentalItem().setImage(itemImages);
        }

        return cartItems;
    }

}
