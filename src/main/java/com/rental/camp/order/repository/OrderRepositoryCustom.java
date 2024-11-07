package com.rental.camp.order.repository;

import com.rental.camp.order.dto.OrderConflictDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepositoryCustom {
    
    List<OrderConflictDTO> findConflictingOrdersWithItemNames(Long rentalItemId, LocalDateTime rentalDate, LocalDateTime returnDate);
}