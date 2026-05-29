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
('siteName', 'Personal Website', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('siteDescription', 'A personal website with blog and tools', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('profile.name', 'Admin', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('profile.title', 'Full Stack Developer', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('profile.bio', 'Passionate developer interested in Java, Vue.js, and building useful tools.', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('profile.avatar', '', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('social.github', 'https://github.com', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('social.twitter', '', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('social.email', 'admin@example.com', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('allowComments', 'true', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('requireApproval', 'false', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- About page skills
('about.skills.0.name', 'Java', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.skills.0.level', '90', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.skills.1.name', 'Spring Boot', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.skills.1.level', '85', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.skills.2.name', 'Vue.js', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.skills.2.level', '80', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.skills.3.name', 'TypeScript', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.skills.3.level', '75', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- About page timeline
('about.timeline.0.date', '2020 - Present', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.timeline.0.title', 'Senior Developer', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.timeline.0.description', 'Working on full-stack web applications.', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.timeline.0.type', 'work', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.timeline.1.date', '2016 - 2020', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.timeline.1.title', 'Software Engineer', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.timeline.1.description', 'Developed and maintained enterprise applications.', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.timeline.1.type', 'work', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.timeline.2.date', '2012 - 2016', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.timeline.2.title', 'Computer Science Degree', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.timeline.2.description', 'Bachelor degree in Computer Science.', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.timeline.2.type', 'education', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- About page projects
('about.projects.0.name', 'Personal Website', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.projects.0.description', 'This website built with Vue.js and Spring Boot.', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.projects.0.url', 'https://github.com', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.projects.0.techs', 'Vue.js,Spring Boot,PostgreSQL', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.projects.1.name', 'Toolkit App', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.projects.1.description', 'Collection of useful developer tools.', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.projects.1.url', '', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('about.projects.1.techs', 'Vue.js,Vite,TypeScript', 'string', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (setting_key) DO NOTHING;
