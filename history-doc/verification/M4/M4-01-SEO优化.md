# M4-01 - SEO 优化

> 对应任务: [01-迭代计划.md](../../plan/01-迭代计划.md#m4-01-seo-优化meta标签-sitemap-robots-json-ld)
> 涉及 Spec: [05-SEO实现.md](../../spec/05-SEO实现.md)

## 验证目标

所有公开页面 meta 标签正确，sitemap.xml 和 robots.txt 可访问。

## 测试用例

### TC-01: 首页 meta 标签

**前置条件：**
- 无

**测试步骤：**
1. 访问 `/`
2. 检查 `<head>` 中的 meta 标签

**预期结果：**
- `<title>` 正确
- `<meta name="description">` 正确
- og:title、og:description 正确

### TC-02: 博客详情页 meta 标签

**前置条件：**
- 存在已发布的文章

**测试步骤：**
1. 访问 `/blog/{slug}`
2. 检查 meta 标签

**预期结果：**
- `<title>` 包含文章标题
- `<meta name="description">` 包含文章摘要
- og:type = "article"

### TC-03: sitemap.xml 可访问

**前置条件：**
- 无

**测试步骤：**
1. 访问 `/sitemap.xml`
2. 验证返回 XML 内容

**预期结果：**
- 返回 200
- 包含所有页面 URL

### TC-04: robots.txt 可访问

**前置条件：**
- 无

**测试步骤：**
1. 访问 `/robots.txt`
2. 验证返回内容

**预期结果：**
- 返回 200
- 包含 sitemap 引用

### TC-05: JSON-LD 结构化数据

**前置条件：**
- 存在文章

**测试步骤：**
1. 访问 `/blog/{slug}`
2. 检查 `<script type="application/ld+json">`

**预期结果：**
- JSON-LD 格式正确
- 包含 Article 结构化数据

## 验收标准

- [ ] TC-01: 首页 meta 标签通过
- [ ] TC-02: 博客详情页 meta 标签通过
- [ ] TC-03: sitemap.xml 可访问通过
- [ ] TC-04: robots.txt 可访问通过
- [ ] TC-05: JSON-LD 结构化数据通过
- [ ] 所有测试用例通过