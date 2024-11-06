package com.rental.camp.rental.controller;

import com.rental.camp.rental.dto.RentalItemRequestDto;
import com.rental.camp.rental.dto.RentalItemResponseDto;
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
    public Page<RentalItemResponseDto> getRentalItems(@RequestBody RentalItemRequestDto requestDto) {
        return rentalItemService.getRentalItems(requestDto);
    }
}
