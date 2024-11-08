package com.rental.camp.order.repository;

import com.rental.camp.order.dto.OrderConflict;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepositoryCustom {

    List<OrderConflict> findConflictingOrdersWithItemNames(List<Long> rentalItemIds, LocalDateTime rentalDate, LocalDateTime returnDate);
}