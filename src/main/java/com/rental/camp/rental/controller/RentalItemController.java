package com.rental.camp.rental.controller;

import com.rental.camp.rental.dto.RentalItemCreateRequest;
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

    @PostMapping
    public String createRentalItem(@RequestBody RentalItemCreateRequest rentalItemCreateRequest) {
        try {
            rentalItemService.createRentalItem(rentalItemCreateRequest);
            return "대여글 등록 성공";
        } catch (Exception e) {
            e.printStackTrace();
            return "대여글 등록 실패";
        }
    }
}
