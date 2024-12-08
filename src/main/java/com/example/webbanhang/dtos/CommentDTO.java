package com.example.webbanhang.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data//toString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("content")
    private String content;

    @JsonProperty("parent_id")
    private Long parentId;
}
