package com.example.webbanhang.repositories;

import com.example.webbanhang.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
