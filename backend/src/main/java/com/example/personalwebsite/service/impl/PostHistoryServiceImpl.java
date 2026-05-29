package com.example.personalwebsite.service.impl;

import com.example.personalwebsite.exception.ResourceNotFoundException;
import com.example.personalwebsite.mapper.BlogPostMapper;
import com.example.personalwebsite.mapper.PostHistoryMapper;
import com.example.personalwebsite.model.entity.BlogPost;
import com.example.personalwebsite.model.entity.PostHistory;
import com.example.personalwebsite.service.PostHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class PostHistoryServiceImpl implements PostHistoryService {

    private static final int MAX_VERSIONS_PER_POST = 10;

    private final PostHistoryMapper postHistoryMapper;
    private final BlogPostMapper blogPostMapper;

    public PostHistoryServiceImpl(PostHistoryMapper postHistoryMapper, BlogPostMapper blogPostMapper) {
        this.postHistoryMapper = postHistoryMapper;
        this.blogPostMapper = blogPostMapper;
    }

    @Override
    @Transactional
    public void saveHistory(Long postId, Long createdBy) {
        BlogPost post = blogPostMapper.findById(postId);
        if (post == null) {
            log.warn("Cannot save history for non-existent post: {}", postId);
            return;
        }

        PostHistory history = new PostHistory();
        history.setPostId(postId);
        history.setTitle(post.getTitle());
        history.setContent(post.getContent());
        history.setExcerpt(post.getExcerpt());
        history.setStatus(post.getStatus());
        history.setCreatedAt(LocalDateTime.now());
        history.setCreatedBy(createdBy);

        postHistoryMapper.insert(history);

        // Cleanup old versions if exceeding max
        deleteOldVersions(postId);

        log.info("Saved history for post {} (history id: {})", postId, history.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostHistory> getHistoryByPostId(Long postId) {
        return postHistoryMapper.findByPostId(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public PostHistory getHistoryById(Long historyId) {
        PostHistory history = postHistoryMapper.findById(historyId);
        if (history == null) {
            throw new ResourceNotFoundException("PostHistory", "id", historyId);
        }
        return history;
    }

    @Override
    @Transactional
    public void restoreVersion(Long postId, Long historyId) {
        PostHistory history = getHistoryById(historyId);
        if (!history.getPostId().equals(postId)) {
            throw new ResourceNotFoundException("PostHistory", "id", historyId);
        }

        BlogPost post = blogPostMapper.findById(postId);
        if (post == null) {
            throw new ResourceNotFoundException("BlogPost", "id", postId);
        }

        // Update post with history content
        post.setTitle(history.getTitle());
        post.setContent(history.getContent());
        post.setExcerpt(history.getExcerpt());
        post.setUpdatedAt(LocalDateTime.now());

        blogPostMapper.update(post);

        log.info("Restored post {} to history version {}", postId, historyId);
    }

    @Override
    @Transactional
    public void deleteOldVersions(Long postId) {
        long count = postHistoryMapper.countByPostId(postId);
        if (count > MAX_VERSIONS_PER_POST) {
            int deleteCount = (int) (count - MAX_VERSIONS_PER_POST);
            postHistoryMapper.deleteOldVersions(postId, deleteCount);
            log.info("Cleaned up {} old versions for post {}", deleteCount, postId);
        }
    }
}