package com.example.webbanhang.services;

import com.example.webbanhang.dtos.CategoryDTO;
import com.example.webbanhang.models.Category;

import java.util.List;

public interface ICategoryService {
    Category createCategory(CategoryDTO category);
    Category getCategoryById(long id);
    List<Category> getAllCategories();
    Category updateCategory(long categoryId, CategoryDTO category);
    void deleteCategory(long id);
}
