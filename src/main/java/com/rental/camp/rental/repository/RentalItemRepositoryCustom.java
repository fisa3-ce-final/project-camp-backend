package com.rental.camp.rental.repository;

import com.rental.camp.rental.dto.RentalItemDetailResponse;
import com.rental.camp.rental.model.RentalItem;
import com.rental.camp.rental.model.type.RentalItemCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalItemRepositoryCustom {
    Page<RentalItem> findAvailableItemsByType(RentalItemCategory category, Pageable pageable);
    RentalItemDetailResponse findItemDetailById(Long id);
}
