package com.securecart.backend.service;

import com.securecart.backend.dto.MessageResponse;
import com.securecart.backend.dto.ProductDisableRequest;
import com.securecart.backend.dto.ProductRequest;
import com.securecart.backend.dto.ProductResponse;
import com.securecart.backend.dto.StockUpdateRequest;

import java.util.List;

public interface ProductService {
    List<ProductResponse> getAllProducts();

    ProductResponse getProductById(Long id);

    List<ProductResponse> searchProductsByName(String name);

    List<ProductResponse> getProductsByCategory(Long categoryId);

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);

    MessageResponse deleteProduct(Long id);

    ProductResponse updateStock(Long id, StockUpdateRequest request);

    ProductResponse changeProductEnabled(Long id, ProductDisableRequest request);
}
