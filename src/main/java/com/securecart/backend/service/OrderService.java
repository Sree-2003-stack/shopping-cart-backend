package com.securecart.backend.service;

import com.securecart.backend.dto.CheckoutRequest;
import com.securecart.backend.dto.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse checkout(CheckoutRequest request);

    List<OrderResponse> getCurrentUserOrders();

    List<OrderResponse> getAllOrders();
}
