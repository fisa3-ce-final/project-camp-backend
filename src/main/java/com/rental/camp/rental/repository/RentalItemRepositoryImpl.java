package com.rental.camp.rental.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.order.model.QOrder;
import com.rental.camp.order.model.QOrderItem;
import com.rental.camp.rental.dto.MyItemsResponse;
import com.rental.camp.rental.dto.MyOrdersResponse;
import com.rental.camp.rental.dto.MyRentalItemsResponse;
import com.rental.camp.rental.dto.RentalItemDetailResponse;
import com.rental.camp.rental.model.QRentalItem;
import com.rental.camp.rental.model.QRentalItemImage;
import com.rental.camp.rental.model.RentalItem;
import com.rental.camp.rental.model.type.RentalItemCategory;
import com.rental.camp.rental.model.type.RentalItemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class RentalItemRepositoryImpl implements RentalItemRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<RentalItem> findAvailableItemsByType(RentalItemCategory category, Pageable pageable) {
        QRentalItem rentalItem = QRentalItem.rentalItem;

        List<RentalItem> items = jpaQueryFactory.selectFrom(rentalItem)
                .where(rentalItem.status.eq(String.valueOf(RentalItemStatus.AVAILABLE))
                        .and(rentalItem.category.eq(category.toString()))
                        .and(rentalItem.isDeleted.isFalse()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 데이터 개수
        long total = Optional.ofNullable(jpaQueryFactory.select(rentalItem.count())
                .from(rentalItem)
                .where(rentalItem.status.eq(String.valueOf(RentalItemStatus.AVAILABLE))
                        .and(rentalItem.category.eq(category.toString()))
                        .and(rentalItem.isDeleted.isFalse()))
                .fetchOne()).orElse(0L); // null일 경우 0으로 처리

        // PageImpl로 페이징된 결과 반환
        return new PageImpl<>(items, pageable, total);
    }

    @Override
    public RentalItemDetailResponse findItemDetailById(Long id) {
        QRentalItem rentalItem = QRentalItem.rentalItem;
        QRentalItemImage rentalItemImage = QRentalItemImage.rentalItemImage;

        // 상품 기본 정보 조회
        RentalItemDetailResponse itemDetail = jpaQueryFactory
                .select(Projections.fields(RentalItemDetailResponse.class,
                        rentalItem.id,
                        rentalItem.name,
                        rentalItem.description,
                        rentalItem.price,
                        rentalItem.stock,
                        rentalItem.category,
                        rentalItem.status,
                        rentalItem.viewCount,
                        rentalItem.ratingAvg,
                        rentalItem.createdAt
                ))
                .from(rentalItem)
                .where(rentalItem.id.eq(id)
                        .and(rentalItem.isDeleted.isFalse()))
                .fetchOne();

        // 상품 찾을 수 없으면 null 반환
        if (itemDetail == null) {
            return null;
        }

        // 상품 이미지 매핑
        List<RentalItemDetailResponse.ImageDto> itemDetailImages = jpaQueryFactory.selectFrom(rentalItemImage)
                .where(rentalItemImage.rentalItemId.eq(id))
                .orderBy(rentalItemImage.imageOrder.asc())
                .fetch()
                .stream()
                .map(image -> {
                    RentalItemDetailResponse.ImageDto imageDto = new RentalItemDetailResponse.ImageDto();
                    imageDto.setImageUrl(image.getImageUrl());
                    imageDto.setImageOrder(image.getImageOrder());
                    return imageDto;
                })
                .collect(Collectors.toList());

        itemDetail.setImage(itemDetailImages);

        return itemDetail;
    }

    @Override
    public Page<MyRentalItemsResponse> findRentalItemsByUserId(Long userId, Pageable pageable) {
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        QRentalItem rentalItem = QRentalItem.rentalItem;

        List<MyRentalItemsResponse> myRentalItems = jpaQueryFactory.select(Projections.constructor(
                        MyRentalItemsResponse.class,
                        rentalItem.name,
                        rentalItem.category,
                        orderItem.quantity,
                        order.orderStatus.stringValue(),
                        order.rentalDate,
                        order.returnDate
                ))
                .from(order)
                .join(orderItem).on(order.id.eq(orderItem.orderId))
                .join(rentalItem).on(orderItem.rentalItemId.eq(rentalItem.id))
                .where(order.userId.eq(userId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(jpaQueryFactory.select(order.count())
                .from(order)
                .join(orderItem).on(order.id.eq(orderItem.orderId))
                .join(rentalItem).on(orderItem.rentalItemId.eq(rentalItem.id))
                .where(order.userId.eq(userId))
                .fetchOne()).orElse(0L);

        return new PageImpl<>(myRentalItems, pageable, total);
    }

    @Override
    public Page<MyItemsResponse> findItemsByUserId(Long userId, Pageable pageable) {
        QRentalItem rentalItem = QRentalItem.rentalItem;

        List<MyItemsResponse> myItems = jpaQueryFactory.select(Projections.constructor(
                    MyItemsResponse.class,
                    rentalItem.name,
                    rentalItem.category,
                    rentalItem.stock,
                    rentalItem.status,
                    rentalItem.createdAt
                ))
                .from(rentalItem)
                .where(rentalItem.userId.eq(userId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(jpaQueryFactory.select(rentalItem.count())
                .from(rentalItem)
                .where(rentalItem.userId.eq(userId))
                .fetchOne()).orElse(0L);

        return new PageImpl<>(myItems, pageable, total);
    }

    @Override
    public Page<MyOrdersResponse> findOrdersByUserId(Long userId, Pageable pageable) {
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        QRentalItem rentalItem = QRentalItem.rentalItem;

        List<MyOrdersResponse> myOrders = jpaQueryFactory.select(Projections.constructor(
                    MyOrdersResponse.class,
                    rentalItem.name,
                    rentalItem.category,
                    orderItem.quantity,
                    order.orderStatus.stringValue(),
                    order.createdAt
                ))
                .from(order)
                .join(orderItem).on(order.id.eq(orderItem.orderId))
                .join(rentalItem).on(orderItem.rentalItemId.eq(rentalItem.id))
                .where(order.userId.eq(userId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(jpaQueryFactory.select(order.count())
                .from(order)
                .where(order.userId.eq(userId))
                .fetchOne()).orElse(0L);

        return new PageImpl<>(myOrders, pageable, total);
    }
}
