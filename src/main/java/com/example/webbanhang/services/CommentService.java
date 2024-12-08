package com.example.webbanhang.services;

import com.example.webbanhang.dtos.CommentDTO;
import com.example.webbanhang.exceptions.DataNotFoundException;
import com.example.webbanhang.models.Comment;
import com.example.webbanhang.models.Product;
import com.example.webbanhang.models.User;
import com.example.webbanhang.repositories.CommentRepository;
import com.example.webbanhang.repositories.ProductRepository;
import com.example.webbanhang.repositories.UserRepository;
import com.example.webbanhang.responses.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentService implements ICommentService{
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Comment insertComment(CommentDTO commentDTO) {
        User user = userRepository.findById(commentDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Product product = productRepository.findById(commentDTO.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Comment parentComment = null;
        if (commentDTO.getParentId() != null) {
            parentComment = commentRepository.findById(commentDTO.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
        }

        Comment newComment = Comment.builder()
                .user(user)
                .product(product)
                .content(commentDTO.getContent())
                .parent(parentComment)
                .build();

        return commentRepository.save(newComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public void updateComment(Long id, CommentDTO commentDTO) throws DataNotFoundException {
        Comment existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Comment not found"));
        existingComment.setContent(commentDTO.getContent());
        commentRepository.save(existingComment);
    }

    @Override
    public List<CommentResponse> getCommentsByUserAndProduct(Long userId, Long productId) {
        List<Comment> comments = commentRepository.findByUserIdAndProductId(userId, productId);
        return comments.stream()
                .map(CommentResponse::fromComment)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentResponse> getCommentsByProduct(Long productId) {
        List<Comment> comments = commentRepository.findByProductIdAndParentIsNull(productId);
        return comments.stream()
                .map(CommentResponse::fromComment)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentResponse> getCommentsWithRepliesByProduct(Long productId) {
        List<Comment> comments = commentRepository.findByProductIdAndParentIsNull(productId);
        return comments.stream()
                .map(this::mapCommentWithReplies)
                .collect(Collectors.toList());
    }

    @Override
    public Comment replyToComment(Long parentCommentId, CommentDTO replyDTO) throws DataNotFoundException {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new DataNotFoundException("Parent comment not found"));

        User user = userRepository.findById(replyDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Product product = productRepository.findById(replyDTO.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Comment reply = Comment.builder()
                .content(replyDTO.getContent())
                .parent(parentComment)
                .user(user)
                .product(product)
                .build();

        return commentRepository.save(reply);
    }

    private CommentResponse mapCommentWithReplies(Comment comment) {
        List<CommentResponse> replies = comment.getReplies().stream()
                .map(this::mapCommentWithReplies)
                .collect(Collectors.toList());

        // Tạo đối tượng CommentResponse từ comment hiện tại
        CommentResponse commentResponse = CommentResponse.fromComment(comment);

        // Thêm các reply vào CommentResponse
        commentResponse.setReplies(replies);
        return commentResponse;
    }
}
