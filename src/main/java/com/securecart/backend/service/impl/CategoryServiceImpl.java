package com.securecart.backend.service.impl;

import com.securecart.backend.dto.CategoryRequest;
import com.securecart.backend.dto.CategoryResponse;
import com.securecart.backend.dto.MessageResponse;
import com.securecart.backend.entity.Category;
import com.securecart.backend.exception.BadRequestException;
import com.securecart.backend.exception.ResourceNotFoundException;
import com.securecart.backend.repository.CategoryRepository;
import com.securecart.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByEnabledTrueOrderByNameAsc()
                .stream()
                .map(category -> CategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Category already exists.");
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .enabled(true)
                .build();

        Category saved = categoryRepository.save(category);

        return CategoryResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .build();
    }

    @Override
    @Transactional
    public MessageResponse deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (!category.getProducts().isEmpty()) {
            throw new BadRequestException("Cannot delete category with existing products.");
        }

        categoryRepository.delete(category);
        return MessageResponse.builder()
                .message("Category deleted successfully.")
                .build();
    }
}
