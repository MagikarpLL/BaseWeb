# 05-SEO 实现

## 5.1 元数据管理实现

### 安装依赖

```bash
npm install @unhead/vue @unhead/schema-org
```

### 基础配置

```typescript
// src/main.ts
import { createApp } from 'vue'
import { createHead } from '@unhead/vue'
import App from './App.vue'

const app = createApp(App)
app.use(createHead())
app.mount('#app')
```

### 页面使用方式

```vue
<script setup lang="ts">
import { useSeoMeta, useHead } from '@unhead/vue'

// 方式1: useSeoMeta（推荐，语义化）
useSeoMeta({
  title: '文章标题 - 网站名',
  description: '文章描述内容',
  ogTitle: '文章标题',
  ogDescription: '文章描述',
  ogImage: 'https://yoursite.com/og-image.jpg',
  ogType: 'article',
  twitterCard: 'summary_large_image',
})

// 方式2: useHead（更灵活）
useHead({
  title: '页面标题',
  meta: [
    { name: 'keywords', content: '关键词1, 关键词2' },
    { property: 'article:published_time', content: '2026-04-20' },
  ],
  link: [
    { rel: 'canonical', href: 'https://yoursite.com/current-page' },
  ],
})
</script>
```

### 各页面 Meta 标签实现

```typescript
// 首页 `/`
useSeoMeta({
  title: '张三 - 全栈工程师的个人网站',
  description: '张三，全栈工程师，专注于 Web 开发的技术博客，分享 Vue.js、Java、Spring Boot 等技术经验。',
  ogTitle: '张三 - 全栈工程师的个人网站',
  ogDescription: '全栈工程师的技术博客和在线工具',
  ogType: 'website',
})

// 关于页 `/about`
useSeoMeta({
  title: '关于 - 张三',
  description: '了解张三的技能、工作经历和项目经验',
  ogTitle: '关于 - 张三',
  ogType: 'profile',
})

// 博客列表页 `/blog`
useSeoMeta({
  title: '博客 - 张三的技术分享',
  description: '张三的技术博客，涵盖前端、后端、架构等多个领域的技术分享和实战经验',
  ogTitle: '博客 - 张三的技术分享',
  ogType: 'website',
})

// 博客详情页 `/blog/:slug`
useSeoMeta({
  title: `${post.title} - 张三的博客`,
  description: post.excerpt,
  ogTitle: post.title,
  ogDescription: post.excerpt,
  ogImage: post.coverImage,
  ogType: 'article',
  articlePublishedTime: post.publishedAt,
  articleModifiedTime: post.updatedAt,
  articleAuthor: '张三',
  articleSection: post.category.name,
  twitterCard: 'summary_large_image',
})

// 工具集页 `/tools`
useSeoMeta({
  title: '在线工具 - 张三的工具箱',
  description: '免费的在线工具集，包括 JSON 格式化、URL 编码、Base64 编解码等实用工具',
  ogTitle: '在线工具 - 张三的工具箱',
  ogType: 'website',
})
```

## 5.2 JSON-LD 结构化数据实现

### 博客详情页 JSON-LD

```typescript
import { defineArticle, defineWebPage, useSchemaOrg } from '@unhead/schema-org/vue'

useSchemaOrg([
  defineWebPage({
    '@type': 'WebPage',
    name: post.title,
  }),
  defineArticle({
    headline: post.title,
    description: post.excerpt,
    datePublished: post.publishedAt,
    dateModified: post.updatedAt,
    author: { '@type': 'Person', name: '张三' },
    image: post.coverImage,
    publisher: { '@type': 'Person', name: '张三' },
  }),
])
```

### 网站整体 JSON-LD

```typescript
// App.vue
useHead({
  script: [{
    type: 'application/ld+json',
    children: JSON.stringify({
      '@context': 'https://schema.org',
      '@type': 'WebSite',
      name: '张三的技术站',
      url: 'https://yoursite.com',
      description: '全栈工程师的技术博客和在线工具',
      author: { '@type': 'Person', name: '张三', url: 'https://yoursite.com/about' },
      potentialAction: {
        '@type': 'SearchAction',
        target: 'https://yoursite.com/blog?search={search_term_string}',
        'query-input': 'required name=search_term_string',
      },
    }),
  }],
})
```

### 面包屑 JSON-LD

```typescript
// 博客详情页
useHead({
  script: [{
    type: 'application/ld+json',
    children: JSON.stringify({
      '@context': 'https://schema.org',
      '@type': 'BreadcrumbList',
      itemListElement: [
        { '@type': 'ListItem', position: 1, name: '首页', item: 'https://yoursite.com' },
        { '@type': 'ListItem', position: 2, name: '博客', item: 'https://yoursite.com/blog' },
        { '@type': 'ListItem', position: 3, name: post.title, item: `https://yoursite.com/blog/${post.slug}` },
      ],
    }),
  }]
})
```

## 5.3 站点地图实现

### 安装插件

```bash
npm install -D vite-plugin-sitemap
```

### Vite 配置

```typescript
// vite.config.ts
import Sitemap from 'vite-plugin-sitemap'

export default defineConfig({
  plugins: [
    vue(),
    Sitemap({
      hostname: 'https://yoursite.com',
      generateRobotsTxt: true,
      robots: [
        { userAgent: '*', allow: '/', disallow: ['/admin/', '/api/'] },
        { userAgent: '*', sitemap: 'https://yoursite.com/sitemap.xml' },
      ],
    }),
  ],
})
```

### 后端动态生成 Sitemap

```java
@RestController
@RequestMapping("/sitemap.xml")
public class SitemapController {

    @GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
    public String generateSitemap() {
        List<BlogPost> posts = blogService.getAllPublishedPosts();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

        addUrl(sb, "https://yoursite.com/", "weekly", "1.0");
        addUrl(sb, "https://yoursite.com/blog", "daily", "0.9");
        addUrl(sb, "https://yoursite.com/about", "monthly", "0.7");
        addUrl(sb, "https://yoursite.com/tools", "weekly", "0.8");

        for (BlogPost post : posts) {
            addUrl(sb, "https://yoursite.com/blog/" + post.getSlug(),
                "monthly", "0.6", post.getUpdatedAt().toString());
        }

        sb.append("</urlset>");
        return sb.toString();
    }
}
```

### Sitemap 优先级

| 页面 | Priority | Changefreq |
|------|----------|------------|
| 首页 `/` | 1.0 | weekly |
| 博客列表 `/blog` | 0.9 | daily |
| 关于 `/about` | 0.7 | monthly |
| 工具集 `/tools` | 0.8 | weekly |
| 博客详情 `/blog/:slug` | 0.6-0.8 | monthly |
| 工具详情 `/tools/:slug` | 0.5-0.7 | monthly |

## 5.4 Robots.txt

```
User-agent: *
Allow: /
Disallow: /admin/
Disallow: /api/
Disallow: /login
Disallow: /_nuxt/
Disallow: /*.json

Sitemap: https://yoursite.com/sitemap.xml
```

### 路径说明

| 路径 | 规则 | 原因 |
|------|------|------|
| `/` | Allow | 允许抓取 |
| `/admin/*` | Disallow | 管理后台不需要索引 |
| `/api/*` | Disallow | API 不是面向用户的内容 |
| `/login` | Disallow | 登录页不需要索引 |
| `/_nuxt/*` | Disallow | Vue 内部资源 |
| `/*.json` | Disallow | 不需要索引 JSON 文件 |

## 5.5 搜索引擎提交

| 搜索引擎 | 提交 URL |
|----------|----------|
| Google | https://search.google.com/search-console |
| Bing | https://www.bing.com/webmasters |
| 百度 | https://ziyuan.baidu.com |
| 搜狗 | https://zhanzhang.sogou.com |
