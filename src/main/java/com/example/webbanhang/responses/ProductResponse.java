package com.example.webbanhang.responses;

import com.example.webbanhang.models.Product;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse extends BaseResponse{
    private String name;

    private float price;

    private String thumbnail;

    private String description;

    @JsonProperty("category_id")
    private long categoryId;
    public static ProductResponse fromProduct(Product product){
        ProductResponse productResponse = ProductResponse.builder()
                .name(product.getName())
                .price(product.getPrice())
                .thumbnail(product.getThumbnail())
                .description(product.getDescription())
                .categoryId(product.getCategory().getId())
                .build();
        productResponse.setCreatedAt(product.getCreateAt());
        productResponse.setUpdatedAt(product.getUpdatedAt());
        return productResponse;
    }
}