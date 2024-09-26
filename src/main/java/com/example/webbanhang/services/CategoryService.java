package com.example.webbanhang.services;


import com.example.webbanhang.dtos.CategoryDTO;
import com.example.webbanhang.models.Category;
import com.example.webbanhang.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService{
    private final CategoryRepository categoryRepository;
    @Override
    public Category createCategory(CategoryDTO categoryDTO) {
        Category newCategory = Category.builder()
                .name(categoryDTO.getName()).build();
        return categoryRepository.save(newCategory);
    }

    @Override
    public Category getCategoryId(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category updateCategory(long categoryId, CategoryDTO categoryDTO) {
        Category existingCategory = getCategoryId(categoryId);
        existingCategory.setName(categoryDTO.getName());
        categoryRepository.save(existingCategory);
        return existingCategory;
    }

    @Override
    public void deleteCategory(long id) {
        //xoá xong
        categoryRepository.deleteById(id);
    }
}
