package com.rental.camp.rental.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rental.camp.rental.model.QRentalItem;
import com.rental.camp.rental.model.RentalItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class RentalItemRepository implements RentalItemRepositoryCustom {
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
}
