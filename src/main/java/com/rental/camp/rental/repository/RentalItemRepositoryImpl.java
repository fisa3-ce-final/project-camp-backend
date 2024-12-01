package com.rental.camp.rental.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.admin.dto.RentalStatusResponse;
import com.rental.camp.community.model.QCommunityPost;
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
import com.rental.camp.rental.model.type.RentalStatus;
import com.rental.camp.user.model.QUser;
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

        BooleanBuilder whereClause = new BooleanBuilder();
        whereClause.and(rentalItem.status.eq(RentalItemStatus.AVAILABLE));

        // category가 ALL이 아니면 추가 필터링
        if (category != RentalItemCategory.ALL) {
            whereClause.and(rentalItem.category.eq(category));
        }

        List<RentalItem> items = jpaQueryFactory.select(rentalItem)
                .from(rentalItem)
                .where(whereClause)
                .orderBy(rentalItem.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 데이터 개수
        long total = Optional.ofNullable(jpaQueryFactory.select(rentalItem.count())
                .from(rentalItem)
                .where(whereClause)
                .fetchOne()).orElse(0L); // null일 경우 0으로 처리

        // PageImpl로 페이징된 결과 반환
        return new PageImpl<>(items, pageable, total);
    }

    @Override
    public RentalItemDetailResponse findItemDetailById(Long id) {
        QRentalItem rentalItem = QRentalItem.rentalItem;
        QRentalItemImage rentalItemImage = QRentalItemImage.rentalItemImage;
        QUser user = QUser.user;
        QCommunityPost communityPost = QCommunityPost.communityPost;

        // 상품 기본 정보 조회
        RentalItemDetailResponse itemDetail = jpaQueryFactory
                .select(Projections.fields(RentalItemDetailResponse.class,
                        user.uuid,
                        user.nickname,
                        user.imageUrl,
                        rentalItem.id,
                        rentalItem.name,
                        rentalItem.description,
                        rentalItem.price,
                        rentalItem.stock,
                        rentalItem.category,
                        rentalItem.status,
                        rentalItem.viewCount,
                        rentalItem.ratingAvg,
                        rentalItem.createdAt,

                        // 후기글 개수 추가
                        Expressions.asNumber(
                                JPAExpressions.select(communityPost.count())
                                        .from(communityPost)
                                        .where(communityPost.rentalItemId.eq(rentalItem.id))
                        ).intValue().as("reviewNum")
                ))
                .from(rentalItem)
                .join(user).on(user.id.eq(rentalItem.userId))
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
    public Page<RentalItem> findItemsBySearchKeyword(String keyword, Pageable pageable) {
        QRentalItem rentalItem = QRentalItem.rentalItem;

        List<RentalItem> items = jpaQueryFactory.select(rentalItem)
                .from(rentalItem)
                .where(rentalItem.description.contains(keyword)
                        .and(rentalItem.status.eq(RentalItemStatus.AVAILABLE)))
                .orderBy(rentalItem.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(jpaQueryFactory.select(rentalItem.count())
                .from(rentalItem)
                .where(rentalItem.description.contains(keyword)
                        .and(rentalItem.status.eq(RentalItemStatus.AVAILABLE)))
                .fetchOne()).orElse(0L);

        return new PageImpl<>(items, pageable, total);
    }

    @Override
    public Page<MyRentalItemsResponse> findRentalItemsByUserId(Long userId, Pageable pageable) {
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        QRentalItem rentalItem = QRentalItem.rentalItem;

        List<MyRentalItemsResponse> myRentalItems = jpaQueryFactory.select(Projections.constructor(
                        MyRentalItemsResponse.class,
                        rentalItem.id,
                        rentalItem.name,
                        rentalItem.category,
                        orderItem.quantity,
                        order.orderStatus,
                        order.rentalDate,
                        order.returnDate
                ))
                .from(order)
                .join(orderItem).on(order.id.eq(orderItem.orderId))
                .join(rentalItem).on(orderItem.rentalItemId.eq(rentalItem.id))
                .where(order.userId.eq(userId))
                .orderBy(order.createdAt.desc())
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
                .orderBy(rentalItem.createdAt.desc())
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

        List<MyOrdersResponse> myOrders = jpaQueryFactory.select(Projections.constructor(
                    MyOrdersResponse.class,
                    order.id,
                    order.orderStatus.stringValue(),
                    order.totalAmount,
                    order.createdAt
                ))
                .from(order)
                .where(order.userId.eq(userId))
                .orderBy(order.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(jpaQueryFactory.select(order.count())
                .from(order)
                .where(order.userId.eq(userId))
                .fetchOne()).orElse(0L);

        return new PageImpl<>(myOrders, pageable, total);
    }

    @Override
    public Page<RentalItem> findItemsByStatus(RentalItemStatus status, Pageable pageable) {
        QRentalItem rentalItem = QRentalItem.rentalItem;

        BooleanBuilder whereClause = new BooleanBuilder();

        // category가 ALL이 아니면 추가 필터링
        if (status != RentalItemStatus.ALL) {
            whereClause.and(rentalItem.status.eq(status));
        }

        List<RentalItem> rentalItems = jpaQueryFactory.selectFrom(rentalItem)
                .where(whereClause)
                .orderBy(rentalItem.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(jpaQueryFactory.select(rentalItem.count())
                .from(rentalItem)
                .where(whereClause)
                .fetchOne()).orElse(0L);

        return new PageImpl<>(rentalItems, pageable, total);
    }

    @Override
    public Integer countByRentalItemStatus(RentalItemStatus status) {
        QRentalItem rentalItem = QRentalItem.rentalItem;

        Long auditNum = jpaQueryFactory.select(rentalItem.count())
                .from(rentalItem)
                .where(rentalItem.status.eq(status))
                .fetchOne();

        return auditNum.intValue();
    }

    @Override
    public Integer countByRentalStatus(RentalStatus status) {
        QOrder order = QOrder.order;

        Long rentalNum = jpaQueryFactory.select(order.count())
                .from(order)
                .where(order.rentalStatus.eq(status))
                .fetchOne();

        return rentalNum.intValue();
    }

    @Override
    public Integer countByMonth(int month) {
        QUser user = QUser.user;

        Long userNum = jpaQueryFactory.select(user.count())
                .from(user)
                .where(user.createdAt.month().eq(month))
                .fetchOne();

        return userNum.intValue();
    }

    @Override
    public Page<RentalStatusResponse> findAllOrders(Pageable pageable) {
        QOrder order = QOrder.order;
        QUser user = QUser.user;

        List<RentalStatusResponse> rentalItemList = jpaQueryFactory.select(Projections.constructor(
                    RentalStatusResponse.class,
                    order.id,
                    order.userId,
                    user.nickname,
                    order.orderStatus,
                    order.rentalDate,
                    order.returnDate,
                    order.rentalStatus,
                    order.totalAmount
                ))
                .from(order)
                .join(user).on(order.userId.eq(user.id))
                .orderBy(order.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(jpaQueryFactory.select(order.count())
                .from(order)
                .fetchOne()).orElse(0L);

        return new PageImpl<>(rentalItemList, pageable, total);
    }
}
