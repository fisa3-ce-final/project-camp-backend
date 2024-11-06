// CartItemController.java
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
}
