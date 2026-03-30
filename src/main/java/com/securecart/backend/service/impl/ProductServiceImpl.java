package com.securecart.backend.service.impl;

import com.securecart.backend.dto.MessageResponse;
import com.securecart.backend.dto.ProductDisableRequest;
import com.securecart.backend.dto.ProductRequest;
import com.securecart.backend.dto.ProductResponse;
import com.securecart.backend.dto.StockUpdateRequest;
import com.securecart.backend.entity.Category;
import com.securecart.backend.entity.Product;
import com.securecart.backend.enums.ProductStatus;
import com.securecart.backend.exception.BadRequestException;
import com.securecart.backend.exception.ResourceNotFoundException;
import com.securecart.backend.repository.CategoryRepository;
import com.securecart.backend.repository.ProductRepository;
import com.securecart.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findByEnabledTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdAndEnabledTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndEnabledTrue(name)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        return productRepository.findByCategoryIdAndEnabledTrue(categoryId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName().trim())
                .description(request.getDescription().trim())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .status(request.getStockQuantity() <= 0 ? ProductStatus.OUT_OF_STOCK : ProductStatus.ACTIVE)
                .enabled(true)
                .category(category)
                .build();

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);
        product.setStatus(request.getStockQuantity() <= 0 ? ProductStatus.OUT_OF_STOCK : ProductStatus.ACTIVE);

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public MessageResponse deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        productRepository.delete(product);
        return MessageResponse.builder()
                .message("Product deleted successfully.")
                .build();
    }

    @Override
    @Transactional
    public ProductResponse updateStock(Long id, StockUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        int updatedStock = product.getStockQuantity() + request.getQuantityChange();
        if (Boolean.TRUE.equals(request.getMarkOutOfStock())) {
            updatedStock = 0;
        }
        if (updatedStock < 0) {
            throw new BadRequestException("Stock cannot be negative.");
        }

        product.setStockQuantity(updatedStock);
        product.setStatus(updatedStock == 0 ? ProductStatus.OUT_OF_STOCK : ProductStatus.ACTIVE);

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse changeProductEnabled(Long id, ProductDisableRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setEnabled(request.getEnabled());
        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .status(product.getStatus())
                .enabled(product.isEnabled())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .build();
    }
}
