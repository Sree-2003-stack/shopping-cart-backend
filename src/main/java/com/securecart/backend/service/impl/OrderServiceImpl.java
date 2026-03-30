package com.securecart.backend.service.impl;

import com.securecart.backend.dto.CheckoutRequest;
import com.securecart.backend.dto.OrderItemResponse;
import com.securecart.backend.dto.OrderResponse;
import com.securecart.backend.entity.Cart;
import com.securecart.backend.entity.CartItem;
import com.securecart.backend.entity.Order;
import com.securecart.backend.entity.OrderItem;
import com.securecart.backend.entity.Product;
import com.securecart.backend.entity.User;
import com.securecart.backend.enums.OrderStatus;
import com.securecart.backend.enums.PaymentMethod;
import com.securecart.backend.enums.ProductStatus;
import com.securecart.backend.exception.BadRequestException;
import com.securecart.backend.exception.ResourceNotFoundException;
import com.securecart.backend.repository.CartItemRepository;
import com.securecart.backend.repository.CartRepository;
import com.securecart.backend.repository.OrderRepository;
import com.securecart.backend.repository.ProductRepository;
import com.securecart.backend.repository.UserRepository;
import com.securecart.backend.service.OrderService;
import com.securecart.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        if (request.getPaymentMethod() != PaymentMethod.CASH_ON_DELIVERY) {
            throw new BadRequestException("Only CASH_ON_DELIVERY is supported.");
        }

        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Cart is empty."));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty.");
        }

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (!product.isEnabled()) {
                throw new BadRequestException("Product is disabled: " + product.getName());
            }
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }
        }

        Order order = Order.builder()
                .user(user)
                .totalAmount(cart.getTotalAmount())
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .status(OrderStatus.PLACED)
                .items(new ArrayList<>())
                .build();

        order = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int remainingStock = product.getStockQuantity() - cartItem.getQuantity();
            product.setStockQuantity(remainingStock);
            product.setStatus(remainingStock == 0 ? ProductStatus.OUT_OF_STOCK : ProductStatus.ACTIVE);
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .lineTotal(cartItem.getLineTotal())
                    .build();
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order = orderRepository.save(order);

        cartItemRepository.deleteAll(cartItems);
        cart.setTotalAmount(BigDecimal.ZERO);
        cartRepository.save(cart);

        return toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getCurrentUserOrders() {
        User user = getCurrentUser();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .product(item.getProduct().getName())
                        .price(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .lineTotal(item.getLineTotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .orderId(order.getId())
                .items(itemResponses)
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private User getCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }
}
