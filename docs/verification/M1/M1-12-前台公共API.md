# M1-12 - 前台公共 API

> 对应任务: [01-迭代计划.md](../../plan/01-迭代计划.md#m1-12-前台公共-api)
> 涉及 Spec: [02-API契约.md](../../spec/02-API契约.md)

## 验证目标

前台公开 API（首页/文章列表/文章详情）正常返回数据。

## 测试用例

### TC-01: 首页 API

**前置条件：**
- 后端服务已启动

**测试步骤：**
1. GET `/api/public/home`
2. 验证返回首页数据

**预期结果：**
- 返回包含 site_info、latest_posts 等字段

### TC-02: 文章列表 API

**前置条件：**
- 存在已发布的文章

**测试步骤：**
1. GET `/api/blog/posts`
2. 验证返回文章列表

**预期结果：**
- 只返回已发布文章
- 包含 title、excerpt、slug、created_at 等字段

### TC-03: 文章列表分页

**前置条件：**
- TC-02 已通过

**测试步骤：**
1. GET `/api/blog/posts?page=1&page_size=10`
2. 验证分页参数生效

**预期结果：**
- 返回分页数据
- 包含 total、page、page_size

### TC-04: 文章详情 API

**前置条件：**
- 存在已发布的文章

**测试步骤：**
1. GET `/api/blog/posts/{slug}`
2. 验证返回文章详情

**预期结果：**
- 返回完整文章数据
- 包含 content（Markdown）
- 包含 author、category、tags 信息

### TC-05: 获取不存在的文章

**前置条件：**
- 无

**测试步骤：**
1. GET `/api/blog/posts/not-exist-slug`
2. 验证返回 404

**预期结果：**
- 返回 404 错误码
- message 提示文章不存在

## 验收标准

- [ ] TC-01: 首页 API 通过
- [ ] TC-02: 文章列表 API 通过
- [ ] TC-03: 文章列表分页通过
- [ ] TC-04: 文章详情 API 通过
- [ ] TC-05: 获取不存在的文章通过
- [ ] 所有测试用例通过