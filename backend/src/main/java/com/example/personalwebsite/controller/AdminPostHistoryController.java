package com.example.personalwebsite.controller;

import com.example.personalwebsite.model.entity.PostHistory;
import com.example.personalwebsite.service.PostHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/posts/{postId}/history")
public class AdminPostHistoryController {

    private final PostHistoryService postHistoryService;

    public AdminPostHistoryController(PostHistoryService postHistoryService) {
        this.postHistoryService = postHistoryService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHistory(@PathVariable Long postId) {
        List<PostHistory> histories = postHistoryService.getHistoryByPostId(postId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", histories);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{historyId}")
    public ResponseEntity<Map<String, Object>> getHistoryDetail(
            @PathVariable Long postId,
            @PathVariable Long historyId) {
        PostHistory history = postHistoryService.getHistoryById(historyId);
        if (!history.getPostId().equals(postId)) {
            throw new IllegalArgumentException("History does not belong to the specified post");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("data", history);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareVersions(
            @PathVariable Long postId,
            @RequestParam Long from,
            @RequestParam Long to) {
        PostHistory fromHistory = postHistoryService.getHistoryById(from);
        PostHistory toHistory = postHistoryService.getHistoryById(to);

        if (!fromHistory.getPostId().equals(postId) || !toHistory.getPostId().equals(postId)) {
            throw new IllegalArgumentException("History does not belong to the specified post");
        }

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("from", fromHistory);
        comparison.put("to", toHistory);
        response.put("data", comparison);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{historyId}/restore")
    public ResponseEntity<Map<String, Object>> restoreVersion(
            @PathVariable Long postId,
            @PathVariable Long historyId,
            @AuthenticationPrincipal Long userId) {
        postHistoryService.restoreVersion(postId, historyId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Version restored successfully");
        return ResponseEntity.ok(response);
    }
}