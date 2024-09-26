package com.example.webbanhang.controller;

import com.example.webbanhang.dtos.CategoryDTO;
import com.example.webbanhang.models.Category;
import com.example.webbanhang.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/categories")
//Dependency Injection
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("")
    //Nếu tham số truyn vào là 1 object thì sao ? => Data Transfer Object = Request Object
    public ResponseEntity<?> createCategory(
            @Valid @RequestBody CategoryDTO categoryDTO,
            BindingResult result){
        if (result.hasErrors()){
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(errorMessages);
        }
        categoryService.createCategory(categoryDTO);
        return ResponseEntity.ok("Thêm mới danh mục thành công");
    }

    //Hiển thị tất cả các category
    @GetMapping("") //http://localhost:8088/api/v1/categories?page=1&limit=10
    public ResponseEntity<List<Category>> getAllCategories(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ){
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO
    ){
        categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok("Cập nhật category thành công");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Xoá category có id: " + id + "thành công");
    }
}