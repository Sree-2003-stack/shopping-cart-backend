package com.securecart.backend.service;

import com.securecart.backend.dto.CategoryRequest;
import com.securecart.backend.dto.CategoryResponse;
import com.securecart.backend.dto.MessageResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();

    CategoryResponse createCategory(CategoryRequest request);

    MessageResponse deleteCategory(Long id);
}
