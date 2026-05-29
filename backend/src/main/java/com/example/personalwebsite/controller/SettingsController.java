package com.example.personalwebsite.controller;

import com.example.personalwebsite.model.dto.SiteSettingsDTO;
import com.example.personalwebsite.model.dto.response.ApiResponse;
import com.example.personalwebsite.model.entity.BlogPost;
import com.example.personalwebsite.service.BlogPostService;
import com.example.personalwebsite.service.SiteSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SettingsController {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_TYPES = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final String UPLOAD_DIR = "static/uploads";

    private final SiteSettingsService siteSettingsService;
    private final BlogPostService blogPostService;

    // ==================== Admin Settings ====================

    @GetMapping("/admin/settings")
    public ApiResponse<SiteSettingsDTO> getAllSettings() {
        SiteSettingsDTO settings = siteSettingsService.getSettingsDTO();
        return ApiResponse.success(settings);
    }

    @PutMapping("/admin/settings")
    public ApiResponse<Void> updateSettings(@RequestBody SiteSettingsDTO settings) {
        siteSettingsService.updateFromDTO(settings);
        return ApiResponse.success("Settings updated successfully", null);
    }

    @PostMapping("/admin/upload")
    public ApiResponse<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ApiResponse.error("Invalid file name");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_TYPES.contains(extension)) {
            return ApiResponse.error("File type not allowed. Allowed types: jpg, png, gif, webp");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ApiResponse.error("File size exceeds 5MB limit");
        }

        try {
            String filename = System.currentTimeMillis() + "." + extension;
            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            String url = "/uploads/" + filename;
            log.info("Uploaded file: {} to {}", filename, filePath);
            return ApiResponse.success("File uploaded successfully", url);
        } catch (IOException e) {
            log.error("Failed to upload file", e);
            return ApiResponse.error("Failed to upload file: " + e.getMessage());
        }
    }

    // ==================== Public Site Data ====================

    // Note: /public/home and /public/about are handled by PublicAPIController

    // ==================== SEO Endpoints ====================

    @GetMapping(value = "/robots.txt", produces = "text/plain")
    public String getRobotsTxt() {
        return "User-agent: *\n" +
               "Allow: /\n" +
               "Sitemap: http://localhost:8080/api/sitemap.xml\n";
    }

    @GetMapping(value = "/sitemap.xml", produces = "application/xml")
    public String getSitemapXml() {
        StringBuilder sitemap = new StringBuilder();
        sitemap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sitemap.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // Add static pages
        sitemap.append("  <url>\n");
        sitemap.append("    <loc>http://localhost:8080/</loc>\n");
        sitemap.append("    <changefreq>weekly</changefreq>\n");
        sitemap.append("    <priority>1.0</priority>\n");
        sitemap.append("  </url>\n");

        sitemap.append("  <url>\n");
        sitemap.append("    <loc>http://localhost:8080/blog</loc>\n");
        sitemap.append("    <changefreq>daily</changefreq>\n");
        sitemap.append("    <priority>0.9</priority>\n");
        sitemap.append("  </url>\n");

        sitemap.append("  <url>\n");
        sitemap.append("    <loc>http://localhost:8080/about</loc>\n");
        sitemap.append("    <changefreq>monthly</changefreq>\n");
        sitemap.append("    <priority>0.7</priority>\n");
        sitemap.append("  </url>\n");

        sitemap.append("  <url>\n");
        sitemap.append("    <loc>http://localhost:8080/tools</loc>\n");
        sitemap.append("    <changefreq>monthly</changefreq>\n");
        sitemap.append("    <priority>0.6</priority>\n");
        sitemap.append("  </url>\n");

        // Add published blog posts
        List<BlogPost> posts = blogPostService.findByStatus("published");
        for (BlogPost post : posts) {
            sitemap.append("  <url>\n");
            sitemap.append("    <loc>http://localhost:8080/blog/").append(post.getSlug()).append("</loc>\n");
            sitemap.append("    <lastmod>").append(post.getUpdatedAt() != null ? post.getUpdatedAt().toLocalDate().toString() : LocalDate.now().toString()).append("</lastmod>\n");
            sitemap.append("    <changefreq>monthly</changefreq>\n");
            sitemap.append("    <priority>0.8</priority>\n");
            sitemap.append("  </url>\n");
        }

        sitemap.append("</urlset>\n");
        return sitemap.toString();
    }
}