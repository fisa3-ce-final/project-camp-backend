package com.rental.camp.rental.repository;

import com.rental.camp.rental.model.RentalItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalItemRepositoryCustom {
    Page<RentalItem> findAvailableItems(Pageable pageable);
}
