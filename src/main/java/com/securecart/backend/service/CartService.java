package com.securecart.backend.service;

import com.securecart.backend.dto.CartResponse;
import com.securecart.backend.dto.MessageResponse;

public interface CartService {
    CartResponse addToCart(Long productId, Integer quantity);

    MessageResponse removeFromCart(Long productId);

    CartResponse updateCartQuantity(Long productId, Integer quantity);

    CartResponse getMyCart();
}
