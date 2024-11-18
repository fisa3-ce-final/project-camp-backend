package com.rental.camp.rental.repository;

import com.rental.camp.rental.model.RentalItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalItemImageRepository extends JpaRepository<RentalItemImage, Long> {
    List<RentalItemImage> findByRentalItemId(long rentalId);
}
