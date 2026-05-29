# 02-API 契约

## 2.1 API 设计原则

### 基础规范

| 规范 | 说明 |
|------|------|
| 协议 | HTTPS |
| 数据格式 | JSON |
| 字符编码 | UTF-8 |
| 认证方式 | JWT Bearer Token |
| 分页 | Page/Size 模式 |

### 统一响应格式

```java
public record ApiResponse<T>(
    int code,
    String message,
    T data,
    long timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Success", data, System.currentTimeMillis());
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, System.currentTimeMillis());
    }
}
```

### HTTP 状态码

| 状态码 | 含义 | 使用场景 |
|--------|------|----------|
| 200 | OK | 成功读取/更新 |
| 201 | Created | 成功创建 |
| 400 | Bad Request | 参数校验失败 |
| 401 | Unauthorized | 未认证 |
| 403 | Forbidden | 无权限 |
| 404 | Not Found | 资源不存在 |
| 500 | Internal Server Error | 服务器错误 |

---

## 2.2 公开 API

### 公开内容接口

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `GET /api/public/home` | GET | 首页数据 | 否 |
| `GET /api/public/about` | GET | 关于页数据 | 否 |
| `GET /api/public/tools` | GET | 工具列表 | 否 |
| `GET /api/public/tools/:slug` | GET | 指定工具详情 | 否 |

### 博客公开接口

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `GET /api/blog/posts` | GET | 文章列表（分页） | 否 |
| `GET /api/blog/posts/:slug` | GET | 文章详情 | 否 |
| `GET /api/blog/categories` | GET | 分类列表 | 否 |
| `GET /api/blog/tags` | GET | 标签列表 | 否 |
| `GET /api/blog/posts/related/:slug` | GET | 相关文章 | 否 |

### 评论公开接口

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `GET /api/blog/posts/:slug/comments` | GET | 获取文章评论列表 | 否 |
| `POST /api/blog/posts/:slug/comments` | POST | 提交评论 | 否 |

### 认证接口

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `POST /api/auth/login` | POST | 登录 | 否 |
| `POST /api/auth/refresh` | POST | 刷新 Token | Cookie |
| `POST /api/auth/logout` | POST | 登出 | 否 |

### 统计埋点接口

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `POST /api/analytics/track` | POST | 记录访问数据 | 否 |

---

## 2.3 管理后台 API

### 管理员博客管理

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `GET /api/admin/posts` | GET | 文章列表（管理） | ADMIN |
| `GET /api/admin/posts/:id` | GET | 文章详情 | ADMIN |
| `POST /api/admin/posts` | POST | 创建文章 | ADMIN |
| `PUT /api/admin/posts/:id` | PUT | 更新文章 | ADMIN |
| `DELETE /api/admin/posts/:id` | DELETE | 删除文章 | ADMIN |
| `POST /api/admin/posts/:id/publish` | POST | 发布文章 | ADMIN |
| `POST /api/admin/posts/:id/draft` | POST | 转为草稿 | ADMIN |

### 管理员分类管理

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `GET /api/admin/categories` | GET | 分类列表 | ADMIN |
| `POST /api/admin/categories` | POST | 创建分类 | ADMIN |
| `PUT /api/admin/categories/:id` | PUT | 更新分类 | ADMIN |
| `DELETE /api/admin/categories/:id` | DELETE | 删除分类 | ADMIN |
| `PUT /api/admin/categories/reorder` | PUT | 批量更新排序 | ADMIN |

### 管理员标签管理

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `GET /api/admin/tags` | GET | 标签列表 | ADMIN |
| `POST /api/admin/tags` | POST | 创建标签 | ADMIN |
| `PUT /api/admin/tags/:id` | PUT | 更新标签 | ADMIN |
| `DELETE /api/admin/tags/:id` | DELETE | 删除标签 | ADMIN |

### 管理员网站配置

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `GET /api/admin/settings` | GET | 获取所有配置 | ADMIN |
| `PUT /api/admin/settings` | PUT | 更新配置 | ADMIN |
| `POST /api/admin/settings/upload` | POST | 上传图片 | ADMIN |

### 管理员评论管理

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `GET /api/admin/comments` | GET | 评论列表（管理） | ADMIN |
| `PUT /api/admin/comments/:id/status` | PUT | 更新评论状态 | ADMIN |
| `POST /api/admin/comments/:id/reply` | POST | 回复评论 | ADMIN |
| `DELETE /api/admin/comments/:id` | DELETE | 删除评论 | ADMIN |

### 管理员统计分析

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `GET /api/admin/analytics/overview` | GET | 统计数据概览 | ADMIN |
| `GET /api/admin/analytics/trend` | GET | 趋势数据 | ADMIN |
| `GET /api/admin/analytics/pages` | GET | 页面统计 | ADMIN |
| `GET /api/admin/analytics/sources` | GET | 来源统计 | ADMIN |

### 管理员用户管理

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `GET /api/admin/users/me` | GET | 当前用户信息 | ADMIN |

---

## 2.4 请求/响应示例

### 首页数据

`GET /api/public/home`

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "profile": {
      "name": "张三",
      "title": "全栈工程师",
      "bio": "热爱技术，专注于 Web 开发",
      "avatar": "https://yoursite.com/images/avatar.jpg",
      "socialLinks": [
        { "platform": "github", "url": "https://github.com/zhangsan" },
        { "platform": "email", "url": "mailto:zhangsan@example.com" }
      ]
    },
    "latestPosts": [
      {
        "id": 1,
        "title": "Vue 3 Composition API 最佳实践",
        "slug": "vue3-composition-api-best-practices",
        "excerpt": "深入解析 Vue 3 Composition API 的使用方法...",
        "publishedAt": "2026-04-20",
        "readCount": 1250
      }
    ],
    "featuredTools": [
      {
        "id": 1,
        "name": "JSON 格式化",
        "icon": "fa-solid fa-code",
        "description": "在线格式化、压缩、校验 JSON",
        "path": "/tools/json-formatter"
      }
    ]
  }
}
```

### 关于页数据

`GET /api/public/about`

```json
{
  "code": 200,
  "data": {
    "profile": {
      "name": "张三",
      "title": "全栈工程师",
      "location": "上海",
      "avatar": "https://yoursite.com/images/avatar.jpg",
      "bio": "详细的个人介绍...",
      "socialLinks": []
    },
    "skills": [
      { "name": "Vue.js", "level": 90, "category": "frontend" }
    ],
    "experiences": {
      "education": [
        { "period": "2015-2019", "school": "XX大学", "major": "计算机科学", "degree": "本科" }
      ],
      "work": [
        { "period": "2022-至今", "company": "XX公司", "position": "高级工程师", "description": "负责..." }
      ]
    },
    "projects": [
      { "name": "项目名称", "description": "项目描述...", "techStack": ["Vue.js", "Spring Boot"], "url": "https://github.com/...", "image": "https://yoursite.com/images/project1.jpg" }
    ]
  }
}
```

### 工具列表数据

`GET /api/public/tools`

```json
{
  "code": 200,
  "data": {
    "categories": ["all", "formatter", "encoder", "converter", "generator", "other"],
    "tools": [
      { "id": 1, "name": "JSON 格式化", "slug": "json-formatter", "icon": "fa-solid fa-code", "description": "JSON 字符串格式化、压缩、校验", "category": "formatter" }
    ]
  }
}
```

### 文章列表

`GET /api/blog/posts?page=1&size=10&category=frontend&tag=vuejs&sort=latest`

```json
{
  "code": 200,
  "data": {
    "posts": [
      {
        "id": 1,
        "title": "Vue 3 Composition API 最佳实践",
        "slug": "vue3-composition-api-best-practices",
        "excerpt": "深入解析...",
        "publishedAt": "2026-04-20",
        "readingTime": 5,
        "category": { "id": 1, "name": "前端", "slug": "frontend" },
        "tags": [{ "id": 1, "name": "Vue.js", "slug": "vuejs" }],
        "readCount": 1250
      }
    ],
    "pagination": { "page": 1, "size": 10, "total": 45, "totalPages": 5 },
    "filters": {
      "categories": [{ "id": 1, "name": "前端", "slug": "frontend", "count": 20 }],
      "tags": [{ "id": 1, "name": "Vue.js", "slug": "vuejs", "count": 10 }]
    }
  }
}
```

### 文章详情

`GET /api/blog/posts/:slug`

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "title": "Vue 3 Composition API 最佳实践",
    "slug": "vue3-composition-api-best-practices",
    "content": "## 引言\n\n这是一篇关于...",
    "excerpt": "深入解析 Vue 3 Composition API 的使用方法...",
    "publishedAt": "2026-04-20",
    "updatedAt": "2026-04-21",
    "readingTime": 5,
    "category": { "id": 1, "name": "前端", "slug": "frontend" },
    "tags": [{ "id": 1, "name": "Vue.js", "slug": "vuejs" }],
    "author": { "id": 1, "name": "张三", "avatar": "https://yoursite.com/images/avatar.jpg" },
    "readCount": 1250,
    "relatedPosts": [
      { "id": 2, "title": "Vue 3 响应式原理深入理解", "slug": "vue3-reactivity-in-depth", "excerpt": "深入理解 Vue 3 的响应式系统...", "publishedAt": "2026-04-15" }
    ]
  }
}
```

### 登录

`POST /api/auth/login`

请求：
```json
{ "username": "admin", "password": "password123" }
```

成功响应：
```json
{
  "code": 200,
  "message": "Login successful",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "token_type": "Bearer",
    "expires_in": 900,
    "user": { "id": 1, "username": "admin", "role": "ADMIN" }
  }
}
```

失败响应：
```json
{ "code": 401, "message": "Invalid username or password", "data": null }
```

### 创建文章

`POST /api/admin/posts`

请求：
```json
{
  "title": "Vue 3 Composition API 最佳实践",
  "slug": "vue3-composition-api-best-practices",
  "categoryId": 1,
  "tagIds": [1, 2, 3],
  "excerpt": "深入解析 Vue 3 Composition API 的使用方法...",
  "content": "## 引言\n\n这是一篇关于...",
  "status": "published",
  "coverImage": "/uploads/covers/vue3.jpg"
}
```

响应：
```json
{
  "code": 200,
  "data": {
    "id": 1, "title": "Vue 3 Composition API 最佳实践", "slug": "vue3-composition-api-best-practices", "status": "published", "publishedAt": "2026-04-20", "createdAt": "2026-04-19", "updatedAt": "2026-04-20"
  }
}
```

### 评论列表

`GET /api/blog/posts/:slug/comments`

```json
{
  "code": 200,
  "data": {
    "comments": [
      {
        "id": 1, "authorName": "张三", "authorEmail": "zhangsan@example.com",
        "authorAvatar": "https://www.gravatar.com/avatar/...",
        "content": "这篇文章讲得很清楚！", "createdAt": "2026-04-20T14:30:00Z",
        "replies": [
          { "id": 2, "authorName": "博主", "authorEmail": "admin@example.com", "content": "感谢支持！", "createdAt": "2026-04-20T15:00:00Z", "replies": [] }
        ]
      }
    ],
    "total": 2
  }
}
```

### 提交评论

`POST /api/blog/posts/:slug/comments`

```json
{ "authorName": "访客", "authorEmail": "visitor@example.com", "content": "写得很好！", "parentId": null, "captcha": "abc123" }
```

### 统计埋点

`POST /api/analytics/track`

```json
{ "type": "pageview", "url": "/blog/vue3-composition-api-best-practices", "referrer": "https://google.com", "visitorId": "uuid-xxx", "userAgent": "Mozilla/5.0...", "screenWidth": 1920, "screenHeight": 1080 }
```

### 分类列表

`GET /api/admin/categories`

```json
{ "code": 200, "data": [{ "id": 1, "name": "前端", "slug": "frontend", "description": "前端技术相关文章", "sort": 1, "postCount": 20 }] }
```

### 标签列表

`GET /api/admin/tags`

```json
{ "code": 200, "data": [{ "id": 1, "name": "Vue.js", "slug": "vuejs", "postCount": 10 }] }
```

### 网站配置

`PUT /api/admin/settings`

```json
{
  "siteName": "张三的技术站", "siteUrl": "https://yoursite.com", "siteDescription": "全栈工程师的技术博客",
  "name": "张三", "title": "全栈工程师", "avatar": "/uploads/avatar.jpg", "bio": "热爱技术，专注于 Web 开发", "location": "上海",
  "socialLinks": [{ "platform": "github", "url": "https://github.com/zhangsan", "isShow": true }],
  "aboutContent": "## 个人介绍\n\n这是我的详细介绍...",
  "skills": [], "experiences": {}, "projects": []
}
```

### 图片上传

`POST /api/admin/settings/upload` (multipart/form-data)

响应：
```json
{ "code": 200, "data": { "url": "/uploads/avatar-1745404800.jpg", "filename": "avatar-1745404800.jpg" } }
```

### 统计数据概览

`GET /api/admin/analytics/overview`

```json
{
  "code": 200,
  "data": {
    "today": { "pv": 1234, "uv": 456 },
    "yesterday": { "pv": 1089, "uv": 398 },
    "total": { "pv": 456789, "uv": 12345 },
    "onlineCount": 23
  }
}
```

### 趋势数据

`GET /api/admin/analytics/trend`

```json
{ "code": 200, "data": [{ "date": "2026-04-17", "pv": 1100, "uv": 400 }] }
```

### 页面统计

`GET /api/admin/analytics/pages`

```json
{ "code": 200, "data": [{ "page": "/blog/vue3-composition-api", "pv": 234, "avgDuration": 180 }] }
```

### 错误响应

```json
{ "code": 400, "message": "Validation failed: title is required", "data": null, "timestamp": 1745404800000 }
```

---

## 2.5 参数校验

### 文章创建校验规则

| 字段 | 规则 |
|------|------|
| title | 必填，1-200 字符 |
| slug | 必填，字母、数字、连字符，3-100 字符，唯一 |
| categoryId | 必填，存在 |
| tagIds | 可选，最多 5 个，都存在 |
| excerpt | 可选，最多 500 字符 |
| content | 必填，Markdown 格式 |
| status | 必填，draft 或 published |

### 评论提交校验规则

| 字段 | 规则 |
|------|------|
| authorName | 必填，2-20 字符 |
| authorEmail | 必填，有效邮箱格式 |
| content | 必填，5-2000 字符 |
| captcha | 必填，4 位验证码 |
