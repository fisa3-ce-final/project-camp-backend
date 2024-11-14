package com.rental.camp.rental.repository;

import com.rental.camp.rental.model.RentalItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalItemRepository extends JpaRepository<RentalItem, Long>, RentalItemRepositoryCustom {

}
