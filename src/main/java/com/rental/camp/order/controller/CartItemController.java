package com.rental.camp.order.controller;

import com.rental.camp.order.dto.*;
import com.rental.camp.order.service.CartItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart-items")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;

    @PostMapping
    public ResponseEntity<CartItemResponse> addCartItem(@RequestBody CartItemRequest requestDto, JwtAuthenticationToken principal) {
        String uuid = principal.getName();
        CartItemResponse responseDto = cartItemService.addCartItem(uuid, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<CartItemListResponse> getCartItems(JwtAuthenticationToken principal) {
        String uuid = principal.getName();
        CartItemListResponse responseDto = cartItemService.getCartItemsByUserId(uuid);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/quantity")
    public ResponseEntity<UpdateCartItemResponse> updateCartItemQuantity(
            @Valid @RequestBody UpdateCartItemRequest request, JwtAuthenticationToken principal) {
        String uuid = principal.getName();
        UpdateCartItemResponse response = cartItemService.updateCartItemQuantity(uuid, request);
        if (response.getCartItem() == null) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteCartItem(@PathVariable(name = "id") Long id) {
        try {
            cartItemService.deleteCartItem(id);
            return ResponseEntity.ok("삭제 완료");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // 예외 처리 메서드
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @GetMapping("/quantity")
    public Long getCartQuantity(JwtAuthenticationToken principal) {
        String uuid = principal.getName();
        return cartItemService.getCartQuantity(uuid);
    }
}
