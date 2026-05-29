-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP
);

-- Admin user (password: admin123 - BCrypt hashed)
INSERT INTO users (username, password, role, created_at, updated_at, login_attempts)
VALUES ('admin', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (username) DO NOTHING;

-- Categories table
CREATE TABLE IF NOT EXISTS category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    sort INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tags table
CREATE TABLE IF NOT EXISTS tag (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Blog posts table
CREATE TABLE IF NOT EXISTS blog_post (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    excerpt TEXT,
    content TEXT NOT NULL,
    cover_image VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    is_top BOOLEAN NOT NULL DEFAULT FALSE,
    reading_time INTEGER DEFAULT 0,
    read_count INTEGER DEFAULT 0,
    author_id BIGINT REFERENCES users(id),
    category_id BIGINT REFERENCES category(id),
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Blog post tags
CREATE TABLE IF NOT EXISTS blog_post_tag (
    post_id BIGINT NOT NULL REFERENCES blog_post(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, tag_id)
);

-- Comments table
CREATE TABLE IF NOT EXISTS comment (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES blog_post(id) ON DELETE CASCADE,
    author_name VARCHAR(100) NOT NULL,
    author_email VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    parent_id BIGINT REFERENCES comment(id),
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Site settings
CREATE TABLE IF NOT EXISTS site_settings (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    value TEXT NOT NULL,
    type VARCHAR(20) DEFAULT 'string',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Page views
CREATE TABLE IF NOT EXISTS page_view (
    id BIGSERIAL PRIMARY KEY,
    page_url VARCHAR(500) NOT NULL,
    pv_count INTEGER DEFAULT 0,
    uv_count INTEGER DEFAULT 0,
    date DATE NOT NULL,
    hour INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(page_url, date, hour)
);

-- Unique visitors
CREATE TABLE IF NOT EXISTS unique_visitor (
    id BIGSERIAL PRIMARY KEY,
    visitor_id VARCHAR(100) NOT NULL UNIQUE,
    first_visit_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_visit_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_pv INTEGER DEFAULT 0
);

-- Daily stats
CREATE TABLE IF NOT EXISTS daily_stats (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL UNIQUE,
    total_pv INTEGER DEFAULT 0,
    total_uv INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Post history table
CREATE TABLE IF NOT EXISTS post_history (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES blog_post(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    excerpt TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id)
);

-- Index for post_history
CREATE INDEX IF NOT EXISTS idx_post_history_post_id ON post_history(post_id);
CREATE INDEX IF NOT EXISTS idx_post_history_created_at ON post_history(created_at DESC);