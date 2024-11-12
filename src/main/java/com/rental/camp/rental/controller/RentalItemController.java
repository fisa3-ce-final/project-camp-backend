package com.rental.camp.rental.controller;

import com.rental.camp.rental.dto.*;
import com.rental.camp.rental.model.type.RentalItemCategory;
import com.rental.camp.rental.service.RentalItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
    public ResponseEntity<String> createRentalItem(@RequestBody RentalItemCreateRequest rentalItemCreateRequest,
                                                   JwtAuthenticationToken principal) {
        String uuid = principal.getName();

        try {
            rentalItemService.createRentalItem(uuid, rentalItemCreateRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body("대여글 등록 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("대여글 등록 실패");
        }
    }

    @GetMapping("/my-rental-items")
    public Page<MyRentalItemsResponse> getMyRentalItems(@RequestBody MyPageRequest pageRequest, JwtAuthenticationToken principal) {
        String uuid = principal.getName();
        return rentalItemService.getMyRentalItems(uuid, pageRequest);
    }

    @GetMapping("/my-items")
    public Page<MyItemsResponse> getMyItems(@RequestBody MyPageRequest pageRequest, JwtAuthenticationToken principal) {
        String uuid = principal.getName();
        return rentalItemService.getMyItems(uuid, pageRequest);
    }

    @GetMapping("/my-orders")
    public Page<MyOrdersResponse> getMyOrders(@RequestBody MyPageRequest pageRequest, JwtAuthenticationToken principal) {
        String uuid = principal.getName();
        return rentalItemService.getMyOrders(uuid, pageRequest);
    }
}
