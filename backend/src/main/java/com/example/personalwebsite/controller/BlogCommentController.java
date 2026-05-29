package com.example.personalwebsite.controller;

import com.example.personalwebsite.exception.BadRequestException;
import com.example.personalwebsite.model.dto.response.ApiResponse;
import com.example.personalwebsite.model.dto.response.CommentResponse;
import com.example.personalwebsite.model.entity.BlogPost;
import com.example.personalwebsite.model.entity.Comment;
import com.example.personalwebsite.service.BlogPostService;
import com.example.personalwebsite.service.CaptchaService;
import com.example.personalwebsite.service.CommentService;
import com.example.personalwebsite.util.GravatarUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BlogCommentController {

    private final CommentService commentService;
    private final BlogPostService blogPostService;
    private final CaptchaService captchaService;

    // ==================== Public Endpoints ====================

    @GetMapping("/blog/posts/{slug}/comments")
    public ApiResponse<List<CommentResponse>> getCommentsBySlug(@PathVariable String slug) {
        BlogPost post = blogPostService.findBySlug(slug);
        if (post == null) {
            return ApiResponse.error("Post not found");
        }

        List<Comment> comments = commentService.findThreadedByPostId(post.getId());
        List<CommentResponse> responses = comments.stream()
                .map(this::toCommentResponseWithChildren)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    @PostMapping("/blog/posts/{slug}/comments")
    public ApiResponse<CommentResponse> submitComment(
            @PathVariable String slug,
            @Valid @RequestBody CommentSubmissionRequest request,
            HttpServletRequest httpRequest,
            HttpSession session) {

        BlogPost post = blogPostService.findBySlug(slug);
        if (post == null) {
            return ApiResponse.error("Post not found");
        }

        // Validate captcha
        String sessionId = session.getId();
        if (!captchaService.validateCaptcha(sessionId, request.getCaptcha())) {
            return ApiResponse.error("Invalid or expired captcha code");
        }

        Comment comment = new Comment();
        comment.setPostId(post.getId());
        comment.setAuthorName(request.getAuthorName());
        comment.setAuthorEmail(request.getAuthorEmail());
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        comment.setStatus("pending");
        comment.setIpAddress(getClientIp(httpRequest));
        comment.setUserAgent(httpRequest.getHeader("User-Agent"));
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        Comment created = commentService.create(comment);
        log.info("New comment submitted for post '{}' by {} (pending)", slug, request.getAuthorName());

        return ApiResponse.success("Comment submitted successfully, awaiting approval", toCommentResponse(created));
    }

    // ==================== Admin Endpoints ====================

    @GetMapping("/admin/comments")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CommentResponse>> getAllComments(
            @RequestParam(required = false) String status) {
        List<Comment> comments;
        if (status != null && !status.isEmpty()) {
            comments = commentService.findByStatus(status);
        } else {
            comments = commentService.findAll();
        }

        List<CommentResponse> responses = comments.stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    @PutMapping("/admin/comments/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CommentResponse> updateCommentStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request) {
        Comment comment = commentService.findById(id);
        if (comment == null) {
            return ApiResponse.error("Comment not found");
        }

        commentService.updateStatus(id, request.getStatus());
        comment.setStatus(request.getStatus());
        comment.setUpdatedAt(LocalDateTime.now());

        log.info("Comment {} status updated to {}", id, request.getStatus());
        return ApiResponse.success("Comment status updated successfully", toCommentResponse(comment));
    }

    @PostMapping("/admin/comments/{id}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CommentResponse> replyToComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentSubmissionRequest request,
            HttpServletRequest httpRequest) {
        Comment parentComment = commentService.findById(id);
        if (parentComment == null) {
            return ApiResponse.error("Comment not found");
        }

        Comment reply = new Comment();
        reply.setPostId(parentComment.getPostId());
        reply.setAuthorName(request.getAuthorName());
        reply.setAuthorEmail(request.getAuthorEmail());
        reply.setContent(request.getContent());
        reply.setParentId(id);
        reply.setStatus("approved");
        reply.setIpAddress(getClientIp(httpRequest));
        reply.setUserAgent(httpRequest.getHeader("User-Agent"));
        reply.setCreatedAt(LocalDateTime.now());
        reply.setUpdatedAt(LocalDateTime.now());

        Comment created = commentService.create(reply);
        log.info("Reply to comment {} submitted by {}", id, request.getAuthorName());

        return ApiResponse.success("Reply submitted successfully", toCommentResponse(created));
    }

    @DeleteMapping("/admin/comments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteComment(@PathVariable Long id) {
        Comment comment = commentService.findById(id);
        if (comment == null) {
            return ApiResponse.error("Comment not found");
        }

        commentService.deleteById(id);
        log.info("Comment {} deleted", id);
        return ApiResponse.success("Comment deleted successfully", null);
    }

    // ==================== Helper Methods ====================

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private CommentResponse toCommentResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setPostId(comment.getPostId());
        response.setAuthorName(comment.getAuthorName());
        response.setAuthorEmail(comment.getAuthorEmail());
        response.setContent(comment.getContent());
        response.setParentId(comment.getParentId());
        response.setStatus(comment.getStatus());
        response.setCreatedAt(comment.getCreatedAt());
        // Generate Gravatar URL
        if (comment.getAuthorEmail() != null && !comment.getAuthorEmail().isEmpty()) {
            response.setAvatar(GravatarUtil.getGravatarUrl(comment.getAuthorEmail(), 48));
        }
        return response;
    }

    private CommentResponse toCommentResponseWithChildren(Comment comment) {
        CommentResponse response = toCommentResponse(comment);
        if (comment.getChildren() != null && !comment.getChildren().isEmpty()) {
            List<CommentResponse> childResponses = comment.getChildren().stream()
                    .map(this::toCommentResponseWithChildren)
                    .collect(Collectors.toList());
            response.setChildren(childResponses);
        }
        return response;
    }

    // ==================== Inner Classes for Validation ====================

    @lombok.Data
    public static class CommentSubmissionRequest {
        @NotBlank(message = "Author name is required")
        @Size(min = 2, max = 20, message = "Author name must be 2-20 characters")
        private String authorName;

        @NotBlank(message = "Author email is required")
        @Email(message = "Invalid email format")
        private String authorEmail;

        @NotBlank(message = "Content is required")
        @Size(min = 5, max = 2000, message = "Content must be 5-2000 characters")
        private String content;

        @NotBlank(message = "Captcha code is required")
        @Size(min = 4, max = 4, message = "Captcha must be 4 characters")
        private String captcha;

        private Long parentId;
    }

    @lombok.Data
    public static class StatusUpdateRequest {
        @NotBlank(message = "Status is required")
        @Pattern(regexp = "^(pending|approved|rejected)$", message = "Status must be pending, approved, or rejected")
        private String status;
    }
}