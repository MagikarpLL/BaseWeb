package com.example.personalwebsite.service.impl;

import com.example.personalwebsite.exception.BadRequestException;
import com.example.personalwebsite.exception.ResourceNotFoundException;
import com.example.personalwebsite.mapper.CommentMapper;
import com.example.personalwebsite.model.entity.Comment;
import com.example.personalwebsite.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;

    public CommentServiceImpl(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Comment findById(Long id) {
        Comment comment = commentMapper.findById(id);
        if (comment == null) {
            throw new ResourceNotFoundException("Comment", "id", id);
        }
        return comment;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findByPostId(Long postId) {
        return commentMapper.findByPostId(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findByStatus(String status) {
        return commentMapper.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findPending() {
        return commentMapper.findPending();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findAll() {
        return commentMapper.findAll();
    }

    @Override
    @Transactional
    public Comment create(Comment comment) {
        validateComment(comment);

        if (comment.getStatus() == null) {
            comment.setStatus("pending");
        }

        LocalDateTime now = LocalDateTime.now();
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);

        commentMapper.insert(comment);
        log.info("Created comment for post {} by {}", comment.getPostId(), comment.getAuthorName());
        return comment;
    }

    @Override
    @Transactional
    public Comment update(Comment comment) {
        Comment existing = findById(comment.getId());
        validateComment(comment);

        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.update(comment);
        log.info("Updated comment: {} (id: {})", comment.getId());
        return comment;
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        Comment comment = findById(id);
        commentMapper.updateStatus(id, status, LocalDateTime.now());
        log.info("Updated comment {} status to {}", id, status);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Comment comment = findById(id);
        commentMapper.deleteById(id);
        log.info("Deleted comment: {} (id: {})", comment.getId(), id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return commentMapper.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findThreadedByPostId(Long postId) {
        List<Comment> allComments = commentMapper.findByPostId(postId);
        return buildCommentTree(allComments);
    }

    private List<Comment> buildCommentTree(List<Comment> comments) {
        // Separate root comments and replies
        List<Comment> roots = new java.util.ArrayList<>();
        List<Comment> replies = new java.util.ArrayList<>();

        for (Comment comment : comments) {
            if (comment.getParentId() == null) {
                roots.add(comment);
            } else {
                replies.add(comment);
            }
        }

        // Build tree recursively
        for (Comment root : roots) {
            buildChildren(root, replies);
        }

        return roots;
    }

    private void buildChildren(Comment parent, List<Comment> allReplies) {
        List<Comment> children = new java.util.ArrayList<>();
        List<Comment> remaining = new java.util.ArrayList<>();

        for (Comment reply : allReplies) {
            if (reply.getParentId().equals(parent.getId())) {
                children.add(reply);
            } else {
                remaining.add(reply);
            }
        }

        // Recursively build children for each child
        for (Comment child : children) {
            buildChildren(child, remaining);
        }

        if (!children.isEmpty()) {
            parent.setChildren(children);
        }
    }

    private void validateComment(Comment comment) {
        if (comment.getPostId() == null) {
            throw new BadRequestException("Post ID cannot be null");
        }
        if (comment.getAuthorName() == null || comment.getAuthorName().trim().isEmpty()) {
            throw new BadRequestException("Author name cannot be empty");
        }
        if (comment.getAuthorEmail() == null || comment.getAuthorEmail().trim().isEmpty()) {
            throw new BadRequestException("Author email cannot be empty");
        }
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new BadRequestException("Comment content cannot be empty");
        }
    }
}