package com.srivenkateswara.agencies.controller;

import com.srivenkateswara.agencies.dto.ApiResponse;
import com.srivenkateswara.agencies.dto.CartDto;
import com.srivenkateswara.agencies.dto.CartItemRequest;
import com.srivenkateswara.agencies.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Cart management endpoints")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get logged-in user cart with calculated subtotal & charges")
    public ResponseEntity<ApiResponse<CartDto>> getCart() {
        CartDto cart = cartService.getCartDtoForCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartDto>> addItemToCart(@Valid @RequestBody CartItemRequest request) {
        CartDto cart = cartService.addItemToCart(request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully", cart));
    }

    @PutMapping("/items/{id}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartDto>> updateCartItemQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        CartDto cart = cartService.updateCartItemQuantity(id, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cart item quantity updated", cart));
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartDto>> removeCartItem(@PathVariable Long id) {
        CartDto cart = cartService.removeCartItem(id);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @DeleteMapping
    @Operation(summary = "Clear all items in cart")
    public ResponseEntity<ApiResponse<CartDto>> clearCart() {
        CartDto cart = cartService.clearCart();
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", cart));
    }
}
