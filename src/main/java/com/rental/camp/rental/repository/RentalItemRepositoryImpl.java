package com.rental.camp.rental.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.rental.dto.RentalItemDetailResponse;
import com.rental.camp.rental.model.QRentalItem;
import com.rental.camp.rental.model.QRentalItemImage;
import com.rental.camp.rental.model.RentalItem;
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
    public Page<RentalItem> findAvailableItems(Pageable pageable) {
        QRentalItem rentalItem = QRentalItem.rentalItem;

        List<RentalItem> items = jpaQueryFactory.selectFrom(rentalItem)
                .where(rentalItem.status.eq("available")
                        .and(rentalItem.isDeleted.isFalse()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 데이터 개수
        long total = Optional.ofNullable(jpaQueryFactory.select(rentalItem.count())
                .from(rentalItem)
                .where(rentalItem.status.eq("available")
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

        // TODO: 상품 리뷰 매핑

        return itemDetail;
    }
}
