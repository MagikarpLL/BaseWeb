package com.example.personalwebsite.service;

import com.example.personalwebsite.model.entity.Comment;
import java.util.List;

public interface CommentService {
    Comment findById(Long id);
    List<Comment> findByPostId(Long postId);
    List<Comment> findByStatus(String status);
    List<Comment> findPending();
    List<Comment> findAll();
    Comment create(Comment comment);
    Comment update(Comment comment);
    void updateStatus(Long id, String status);
    void deleteById(Long id);
    long countAll();

    // Threaded comments (nested)
    List<Comment> findThreadedByPostId(Long postId);
}