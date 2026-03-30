package com.securecart.backend.controller;

import com.securecart.backend.dto.CartQuantityRequest;
import com.securecart.backend.dto.CartResponse;
import com.securecart.backend.dto.MessageResponse;
import com.securecart.backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add/{productId}")
    public ResponseEntity<CartResponse> addToCart(@PathVariable Long productId,
                                                  @Valid @RequestBody CartQuantityRequest request) {
        return ResponseEntity.ok(cartService.addToCart(productId, request.getQuantity()));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<MessageResponse> removeFromCart(@PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeFromCart(productId));
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<CartResponse> updateQuantity(@PathVariable Long productId,
                                                       @Valid @RequestBody CartQuantityRequest request) {
        return ResponseEntity.ok(cartService.updateCartQuantity(productId, request.getQuantity()));
    }

    @GetMapping
    public ResponseEntity<CartResponse> getMyCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }
}
