package com.rental.camp.rental.repository;

import com.rental.camp.admin.dto.RentalStatusResponse;
import com.rental.camp.rental.dto.MyItemsResponse;
import com.rental.camp.rental.dto.MyOrdersResponse;
import com.rental.camp.rental.dto.MyRentalItemsResponse;
import com.rental.camp.rental.dto.RentalItemDetailResponse;
import com.rental.camp.rental.model.RentalItem;
import com.rental.camp.rental.model.type.RentalItemCategory;
import com.rental.camp.rental.model.type.RentalItemStatus;
import com.rental.camp.rental.model.type.RentalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalItemRepositoryCustom {
    Page<RentalItem> findAvailableItemsByType(RentalItemCategory category, Pageable pageable);
    RentalItemDetailResponse findItemDetailById(Long id);
    Page<RentalItem> findItemsBySearchKeyword(String keyword, Pageable pageable);
    Page<MyRentalItemsResponse> findRentalItemsByUserId(Long userId, Pageable pageable);
    Page<MyItemsResponse> findItemsByUserId(Long userId, Pageable pageable);
    Page<MyOrdersResponse> findOrdersByUserId(Long userId, Pageable pageable);
    Page<RentalItem> findItemsByStatus(RentalItemStatus status, Pageable pageable);
    Integer countByRentalItemStatus(RentalItemStatus status);
    Integer countByRentalStatus(RentalStatus status);
    Integer countByMonth(int month);
    Page<RentalStatusResponse> findItemsByRentalStatus(RentalStatus status, Pageable pageable);
}
