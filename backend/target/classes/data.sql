-- Admin user (password: admin123 - BCrypt hashed)
-- BCrypt hash for 'admin123' with cost factor 10
INSERT INTO users (username, password, role, created_at, updated_at, login_attempts)
VALUES ('admin', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (username) DO NOTHING;

-- Initial categories
INSERT INTO category (name, slug, description, sort, created_at, updated_at) VALUES
('Programming', 'programming', 'Posts about programming languages and development', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Technology', 'technology', 'Technology news and reviews', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Life', 'life', 'Personal life and experiences', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Projects', 'projects', 'Project showcases and updates', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (slug) DO NOTHING;

-- Initial tags
INSERT INTO tag (name, slug, created_at, updated_at) VALUES
('Java', 'java', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Spring Boot', 'spring-boot', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('JavaScript', 'javascript', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Vue.js', 'vue-js', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Python', 'python', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Database', 'database', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('DevOps', 'devops', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Machine Learning', 'machine-learning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (slug) DO NOTHING;

-- Initial site settings
INSERT INTO site_settings (setting_key, value, type, created_at, updated_at) VALUES
('site.name', 'Personal Website', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('site.title', 'My Personal Website', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('site.description', 'A personal website with blog and tools', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('site.keywords', 'blog,personal,programming,technology', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('site.author', 'Admin', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('site.email', 'admin@example.com', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('site.footer_text', '© 2024 Personal Website. All rights reserved.', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('analytics.enabled', 'true', 'boolean', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('comments.moderation', 'true', 'boolean', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('comments.allowed_html', 'false', 'boolean', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('posts.per_page', '10', 'number', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('posts.featured_count', '3', 'number', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('seo.google_verification', '', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('seo.bing_verification', '', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('social.github', '', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('social.twitter', '', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('social.linkedin', '', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (setting_key) DO NOTHING;
