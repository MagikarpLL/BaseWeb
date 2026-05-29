# 术语表 (GLOSSARY)

> 本文档统一项目中所有文档使用的术语定义，避免歧义。
> 新增术语时请按模块分类添加，保持字母/拼音排序。

---

## 项目与角色

| 术语 | 英文 | 定义 |
|------|------|------|
| 访客 | Visitor | 未登录的网站浏览者，可访问所有公开页面 |
| 管理员 | Admin | 已登录的系统管理者，拥有后台管理权限 |
| 前台 | Frontend (Public) | 面向访客的公开页面（首页、博客、关于等） |
| 管理后台 | Admin Panel | 面向管理员的内容管理系统，路径 `/admin/*` |
| MVP | Minimum Viable Product | 最小可行产品，本项目定义为 M1 迭代完成后的状态 |

---

## 内容与数据

| 术语 | 英文 | 定义 |
|------|------|------|
| 文章 | Blog Post | 博客内容单元，有草稿(draft)和已发布(published)两种状态 |
| 草稿 | Draft | 文章状态之一，仅管理员可见，不对外展示 |
| 已发布 | Published | 文章状态之一，对所有访客可见 |
| 软删除 | Soft Delete | 通过 `deleted_at` 字段标记删除，数据仍保留在数据库中，可恢复 |
| Slug | Slug | URL 友好的唯一标识符，由小写字母、数字、连字符组成（如 `vue3-composition-api`） |
| 摘要 | Excerpt | 文章的简短描述，最多 500 字符，用于列表展示和 SEO description |
| 分类 | Category | 文章的一级归类，每篇文章必须且只能属于一个分类 |
| 标签 | Tag | 文章的二级归类，每篇文章可有 0-5 个标签 |
| 精选 | Featured | 标记为推荐展示的内容（如首页精选工具、关于页精选项目） |

---

## 认证与安全

| 术语 | 英文 | 定义 |
|------|------|------|
| Access Token | Access Token | JWT 令牌，有效期 15 分钟，用于 API 认证 |
| Refresh Token | Refresh Token | 刷新令牌，有效期 7 天，存储在 HTTP-only Cookie 中 |
| 账户锁定 | Account Lock | 连续 5 次密码错误后锁定账户 15 分钟 |
| 路由守卫 | Route Guard | Vue Router 的 `beforeEach` 钩子，用于检查认证状态 |

---

## 评论

| 术语 | 英文 | 定义 |
|------|------|------|
| 待审核 | Pending | 评论初始状态，仅管理员可见 |
| 已通过 | Approved | 管理员审核通过的评论，对访客可见 |
| 已拒绝 | Rejected | 管理员拒绝的评论，不对访客显示 |
| 嵌套回复 | Nested Reply | 评论的回复，本项目最多支持 2 层嵌套 |
| 验证码 | CAPTCHA | 评论提交时需输入的 4 位随机字符，防止机器人 |

---

## 统计

| 术语 | 英文 | 定义 |
|------|------|------|
| PV | Page View | 页面浏览量，每次加载页面计数 +1 |
| UV | Unique Visitor | 独立访客数，基于 visitor_id 去重计数 |
| 埋点 | Tracking | 前端自动上报页面访问数据的行为 |
| 趋势 | Trend | 按时间维度展示的 PV/UV 变化数据 |
| 来源 | Source | 访客进入网站的渠道（直接访问/搜索引擎/外部链接） |

---

## 技术实现

| 术语 | 英文 | 定义 |
|------|------|------|
| SPA | Single Page Application | 单页应用，本项目前端架构（Vue 3） |
| 响应式 | Responsive | 页面根据屏幕宽度自动调整布局 |
| 懒加载 | Lazy Loading | 路由级按需加载组件，减少首屏体积 |
| 骨架屏 | Skeleton | 数据加载完成前显示的占位 UI |
| Toast | Toast | 页面顶部或底部的临时提示消息，自动消失 |
| 错误边界 | Error Boundary | 捕获子组件渲染错误的组件，防止整个页面崩溃 |
| 自动保存 | Auto Save | 编辑器每 30 秒自动保存内容到本地存储 |
| 预渲染 | Pre-rendering | 构建时生成静态 HTML，用于 SEO（不引入 SSR） |

---

## 文档体系

| 术语 | 英文 | 定义 |
|------|------|------|
| Requirement | Requirement | 需求文档，回答"做什么" |
| Spec | Specification | 技术规格，回答"怎么做" |
| Plan | Plan | 迭代计划，回答"何时做" |
| 追溯矩阵 | Traceability Matrix | Requirement ↔ Spec ↔ 任务 的双向对应关系表 |
| 越界 | Scope Creep | 文档内容超出其所属层次（如 Requirement 中出现 JSON 结构定义） |
