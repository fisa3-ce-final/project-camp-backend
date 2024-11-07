package com.rental.camp.rental.controller;

import com.rental.camp.rental.dto.RentalItemCreateRequest;
import com.rental.camp.rental.dto.RentalItemDetailResponse;
import com.rental.camp.rental.dto.RentalItemRequest;
import com.rental.camp.rental.dto.RentalItemResponse;
import com.rental.camp.rental.model.type.RentalItemCategory;
import com.rental.camp.rental.service.RentalItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/rental-items")
@RestController
public class RentalItemController {
    private final RentalItemService rentalItemService;

    @GetMapping("/category/{type}")
    public Page<RentalItemResponse> getRentalItems(@PathVariable String type,
                                                   @RequestBody RentalItemRequest requestDto) {
        RentalItemCategory rentalItemCategory;

        try {
            rentalItemCategory = RentalItemCategory.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(type + "은 유효하지 않은 카테고리입니다.");
        }

        return rentalItemService.getRentalItems(rentalItemCategory, requestDto);
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
