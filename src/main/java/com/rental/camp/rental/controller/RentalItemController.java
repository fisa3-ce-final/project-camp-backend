package com.rental.camp.rental.controller;

import com.rental.camp.rental.dto.MyItemsResponse;
import com.rental.camp.rental.dto.MyOrdersResponse;
import com.rental.camp.rental.dto.MyPageRequest;
import com.rental.camp.rental.dto.MyRentalItemsResponse;
import com.rental.camp.rental.dto.RentalItemCreateRequest;
import com.rental.camp.rental.dto.RentalItemDetailResponse;
import com.rental.camp.rental.dto.RentalItemRequest;
import com.rental.camp.rental.dto.RentalItemResponse;
import com.rental.camp.rental.model.type.RentalItemCategory;
import com.rental.camp.rental.service.RentalItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequiredArgsConstructor
@RequestMapping("/rental-items")
@RestController
public class RentalItemController {
    private final RentalItemService rentalItemService;

    @GetMapping("/category/{type}")
    public Page<RentalItemResponse> getRentalItems(@PathVariable(name = "type") String type,
                                                   @ModelAttribute RentalItemRequest requestDto) {
        RentalItemCategory rentalItemCategory;

        try {
            rentalItemCategory = RentalItemCategory.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(type + "은 유효하지 않은 카테고리입니다.");
        }

        return rentalItemService.getRentalItems(rentalItemCategory, requestDto);
    }

    @GetMapping("/{id}")
    public RentalItemDetailResponse getRentalItem(@PathVariable(name = "id") Long id) {
        return rentalItemService.getRentalItem(id);
    }

    @PostMapping
    public ResponseEntity<String> createRentalItem(@ModelAttribute RentalItemCreateRequest rentalItemCreateRequest,
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

    @GetMapping("/search/{keyword}")
    public Page<RentalItemResponse> searchRentalItems(@PathVariable(name = "keyword") String keyword,
                                                      @ModelAttribute RentalItemRequest requestDto) {
        return rentalItemService.searchRentalItems(keyword, requestDto);
    }

    @PostMapping("/{id}/views")
    public ResponseEntity<String> addViewNum(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok("조회수 올리기 성공");
    }

    @GetMapping("/my-rental-items")
    public Page<MyRentalItemsResponse> getMyRentalItems(@ModelAttribute MyPageRequest pageRequest, JwtAuthenticationToken principal) {
        String uuid = principal.getName();
        return rentalItemService.getMyRentalItems(uuid, pageRequest);
    }

    @GetMapping("/my-items")
    public Page<MyItemsResponse> getMyItems(@ModelAttribute MyPageRequest pageRequest, JwtAuthenticationToken principal) {
        String uuid = principal.getName();
        return rentalItemService.getMyItems(uuid, pageRequest);
    }

    @GetMapping("/my-orders")
    public Page<MyOrdersResponse> getMyOrders(@ModelAttribute MyPageRequest pageRequest, JwtAuthenticationToken principal) {
        String uuid = principal.getName();
        return rentalItemService.getMyOrders(uuid, pageRequest);
    }

    @PutMapping("/review/{id}")
    public String reviewUtilization(@PathVariable(name = "id") Long id,
                                    @RequestParam(name = "ratingAvg") BigDecimal ratingAvg,
                                    JwtAuthenticationToken principal) {
        String uuid = principal.getName();
        BigDecimal newRatingAvg = rentalItemService.reviewUtilization(id, uuid, ratingAvg);
        return "후기 별점 업데이트 : " + newRatingAvg.toString();
    }
}
