package com.example.webbanhang.responses;

import com.example.webbanhang.models.Comment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("content")
    private String content;

    // User's information
    @JsonProperty("user")
    private UserResponse userResponse;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("replies")
    private List<CommentResponse> replies; // Danh sách các reply

    // Chuyển từ Comment entity sang CommentResponse
    public static CommentResponse fromComment(Comment comment) {
        List<CommentResponse> replyResponses = comment.getReplies().stream()
                .map(CommentResponse::fromComment) // Đệ quy để chuyển các reply
                .collect(Collectors.toList());

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userResponse(UserResponse.fromUser(comment.getUser()))
                .updatedAt(comment.getUpdatedAt())
                .replies(replyResponses) // Bao gồm danh sách reply
                .build();
    }
}
