package com.securecart.backend.service.impl;

import com.securecart.backend.dto.CartItemResponse;
import com.securecart.backend.dto.CartResponse;
import com.securecart.backend.dto.MessageResponse;
import com.securecart.backend.entity.Cart;
import com.securecart.backend.entity.CartItem;
import com.securecart.backend.entity.Product;
import com.securecart.backend.entity.User;
import com.securecart.backend.enums.ProductStatus;
import com.securecart.backend.exception.BadRequestException;
import com.securecart.backend.exception.ResourceNotFoundException;
import com.securecart.backend.repository.CartItemRepository;
import com.securecart.backend.repository.CartRepository;
import com.securecart.backend.repository.ProductRepository;
import com.securecart.backend.repository.UserRepository;
import com.securecart.backend.service.CartService;
import com.securecart.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public CartResponse addToCart(Long productId, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new BadRequestException("Quantity must be at least 1.");
        }

        User currentUser = getCurrentUser();
        Product product = productRepository.findByIdAndEnabledTrue(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (product.getStatus() == ProductStatus.OUT_OF_STOCK || product.getStockQuantity() == 0) {
            throw new BadRequestException("Product is out of stock.");
        }

        Cart cart = getOrCreateCart(currentUser);

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseGet(() -> CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(0)
                        .unitPrice(product.getPrice())
                        .lineTotal(BigDecimal.ZERO)
                        .build());

        int updatedQuantity = cartItem.getQuantity() + quantity;
        if (updatedQuantity > product.getStockQuantity()) {
            throw new BadRequestException("Requested quantity exceeds available stock.");
        }

        cartItem.setQuantity(updatedQuantity);
        cartItem.setUnitPrice(product.getPrice());
        cartItem.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(updatedQuantity)));
        cartItemRepository.save(cartItem);

        recalculateCartTotal(cart);
        return getMyCart();
    }

    @Override
    @Transactional
    public MessageResponse removeFromCart(Long productId) {
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product does not exist in cart."));

        cartItemRepository.delete(cartItem);
        recalculateCartTotal(cart);

        return MessageResponse.builder()
                .message("Product removed from cart successfully.")
                .build();
    }

    @Override
    @Transactional
    public CartResponse updateCartQuantity(Long productId, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new BadRequestException("Quantity must be at least 1.");
        }

        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product does not exist in cart."));

        Product product = cartItem.getProduct();
        if (quantity > product.getStockQuantity()) {
            throw new BadRequestException("Requested quantity exceeds available stock.");
        }

        cartItem.setQuantity(quantity);
        cartItem.setLineTotal(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
        cartItemRepository.save(cartItem);

        recalculateCartTotal(cart);
        return getMyCart();
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getMyCart() {
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);

        List<CartItemResponse> items = cartItemRepository.findByCartId(cart.getId())
                .stream()
                .map(item -> CartItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .product(item.getProduct().getName())
                        .price(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .lineTotal(item.getLineTotal())
                        .build())
                .toList();

        return CartResponse.builder()
                .items(items)
                .totalAmount(cart.getTotalAmount())
                .build();
    }

    private User getCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).totalAmount(BigDecimal.ZERO).build()));
    }

    private void recalculateCartTotal(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        BigDecimal total = items.stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalAmount(total);
        cartRepository.save(cart);
    }
}
