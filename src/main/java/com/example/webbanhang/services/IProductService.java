package com.example.webbanhang.services;

import com.example.webbanhang.dtos.ProductDTO;
import com.example.webbanhang.dtos.ProductImageDTO;
import com.example.webbanhang.exceptions.DataNotFoundException;
import com.example.webbanhang.models.Product;
import com.example.webbanhang.models.ProductImage;
import com.example.webbanhang.responses.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface IProductService {
    Product createProduct(ProductDTO productDTO) throws Exception;
    Product getProductById(long id) throws Exception;
    public Page<ProductResponse> getAllProducts(String keyword,
                                                Long categoryId, PageRequest pageRequest);
    Product updateProduct(long id, ProductDTO productDTO) throws Exception;
    void deleteProduct(long id);
    boolean existsByName(String name);
    ProductImage createProductImage(
            Long productId,
            ProductImageDTO productImageDTO) throws Exception;

    String storeFile(MultipartFile file) throws IOException;
    List<Product> findProductsByIds(List<Long> productIds);
    List<Product> findSuggestionsByProductId(Long productId) throws DataNotFoundException;
}
