package com.rental.camp.order.controller;

import com.rental.camp.order.dto.CartItemListResponse;
import com.rental.camp.order.dto.CartItemRequest;
import com.rental.camp.order.dto.CartItemResponse;
import com.rental.camp.order.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart-items")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;

    @PostMapping
    public ResponseEntity<CartItemResponse> addCartItem(@RequestBody CartItemRequest requestDto) {
        CartItemResponse responseDto = cartItemService.addCartItem(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<CartItemListResponse> getCartItems(@RequestParam(name = "userId") Long userId) {
        CartItemListResponse responseDto = cartItemService.getCartItemsByUserId(userId);
        return ResponseEntity.ok(responseDto);
    }

    // 예외 처리 메서드 추가
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
