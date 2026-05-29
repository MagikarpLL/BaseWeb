# 个人网站项目

一个集个人展示、博客发布、小工具集于一体的综合性个人网站。

## 技术栈

### 后端
- Java 21 + Spring Boot 3.x
- MyBatis 3.x
- PostgreSQL 15+ (Docker)
- Spring Security + JWT

### 前端
- Vue 3.5+ (Composition API)
- Vite 6.x
- Vue Router 4.x
- Pinia 2.x
- Element Plus 2.x
- TypeScript

## 项目结构

```
personal-website/
├── docs/                    # 需求文档 & 项目规范
├── backend/                 # Spring Boot 后端
└── frontend/                # Vue 3 前端
```

## 快速开始

### 环境要求

- JDK 21 (本地安装于 `D:\CodeProgram\jdk-21.0.1`，未配置全局环境变量)
- Node.js 18+
- Docker & Docker Compose
- Maven 3.8+

> ⚠️ 注意：JDK 未配置全局环境变量，项目已配置 `.mvn/jvm.config` 指定本地 JDK 路径。

### 1. 启动数据库

```bash
cd backend
docker-compose up -d  # 启动 PostgreSQL
```

### 2. 配置后端

```bash
# 编辑 backend/src/main/resources/application-dev.yml
# 配置数据库连接和 JWT 密钥

# 初始化数据库
# 访问 http://localhost:8080/api/init (首次启动时)
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 5. 访问

- 前端: http://localhost:5173
- 后端 API: http://localhost:8080/api
- 管理后台: http://localhost:5173/admin (账号 admin/admin123)

## 开发规范

详见 [docs/10-项目规范](./docs/10-项目规范/) 目录：

- [01-后端项目结构](./docs/10-项目规范/01-后端项目结构.md)
- [02-前端项目结构](./docs/10-项目规范/02-前端项目结构.md)
- [03-代码规范](./docs/10-项目规范/03-代码规范.md)
- [04-JDK和Maven配置](./docs/10-项目规范/04-JDK和Maven配置.md)

## 需求文档

详见 [docs/](./docs/) 目录：

| 模块 | 文档 |
|------|------|
| 项目概述 | [01-项目概述.md](./docs/01-项目概述.md) |
| 个人网站展示 | [02-个人网站展示模块/](./docs/02-个人网站展示模块/) |
| 博客管理 | [03-博客管理模块/](./docs/03-博客管理模块/) |
| 用户认证 | [04-用户与认证模块/](./docs/04-用户与认证模块/) |
| SEO 优化 | [05-SEO优化模块/](./docs/05-SEO优化模块/) |
| 技术实现 | [06-技术实现文档/](./docs/06-技术实现文档/) |
| 网站配置 | [07-网站配置模块/](./docs/07-网站配置模块/) |
| 评论功能 | [08-评论功能模块/](./docs/08-评论功能模块/) |
| 访问统计 | [09-访问统计模块/](./docs/09-访问统计模块/) |

## 功能模块

| 模块 | 说明 |
|------|------|
| 首页 | 个人介绍 + 最新文章 + 精选工具 |
| 关于页 | 技能展示 + 经历时间线 + 项目展示 |
| 工具集 | 8 个在线工具 (JSON 格式化、URL 编码等) |
| 博客 | 文章列表 + 详情 + Markdown 渲染 |
| 管理后台 | 文章/分类/标签/评论/配置/统计管理 |

## 接口文档

启动后端后访问：
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

## 部署

详见项目完成后生成的部署文档。

## License

MIT
