package com.example.personalwebsite.service;

import com.example.personalwebsite.model.entity.PostHistory;
import java.util.List;

public interface PostHistoryService {
    void saveHistory(Long postId, Long createdBy);
    List<PostHistory> getHistoryByPostId(Long postId);
    PostHistory getHistoryById(Long historyId);
    void restoreVersion(Long postId, Long historyId);
    void deleteOldVersions(Long postId);
}