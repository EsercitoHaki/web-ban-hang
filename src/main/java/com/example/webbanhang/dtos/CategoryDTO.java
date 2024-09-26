package com.example.webbanhang.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {
    @NotEmpty(message = "Category's name cannot be empty")
    private String name;
}