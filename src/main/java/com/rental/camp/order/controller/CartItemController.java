package com.rental.camp.order.controller;

import com.rental.camp.order.dto.*;
import com.rental.camp.order.service.CartItemService;
import jakarta.validation.Valid;
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

    @PutMapping("/quantity")
    public ResponseEntity<UpdateCartItemResponse> updateCartItemQuantity(
            @Valid @RequestBody UpdateCartItemRequest request) {
        UpdateCartItemResponse response = cartItemService.updateCartItemQuantity(request);

        if (response.getCartItem() == null) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    // 예외 처리 메서드 추가
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteCartItem(@RequestBody CartItem request) {
        try {
            cartItemService.deleteCartItem(request.getId());
            return ResponseEntity.ok("삭제 완료");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
