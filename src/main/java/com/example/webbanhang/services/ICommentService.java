package com.example.webbanhang.services;

import com.example.webbanhang.dtos.CommentDTO;
import com.example.webbanhang.exceptions.DataNotFoundException;
import com.example.webbanhang.models.Comment;
import com.example.webbanhang.responses.CommentResponse;

import java.util.List;

public interface ICommentService {
    Comment insertComment(CommentDTO comment);
    void deleteComment(Long commentId);
    void updateComment(Long id, CommentDTO commentDTO) throws DataNotFoundException;
    List<CommentResponse> getCommentsByUserAndProduct(Long userId, Long productId);
    List<CommentResponse> getCommentsByProduct(Long productId);
    List<CommentResponse> getCommentsWithRepliesByProduct(Long productId);
    Comment replyToComment(Long parentCommentId, CommentDTO replyDTO) throws DataNotFoundException;
}
