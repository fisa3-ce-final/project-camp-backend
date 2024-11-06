package com.rental.camp.rental.controller;

import com.rental.camp.rental.dto.RentalItemDetailResponse;
import com.rental.camp.rental.dto.RentalItemRequest;
import com.rental.camp.rental.dto.RentalItemResponse;
import com.rental.camp.rental.service.RentalItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/rental-items")
@RestController
public class RentalItemController {
    private final RentalItemService rentalItemService;

    @GetMapping
    public Page<RentalItemResponse> getRentalItems(@RequestBody RentalItemRequest requestDto) {
        return rentalItemService.getRentalItems(requestDto);
    }

    @GetMapping("/{id}")
    public RentalItemDetailResponse getRentalItem(@PathVariable Long id) {
        return rentalItemService.getRentalItem(id);
    }
}
