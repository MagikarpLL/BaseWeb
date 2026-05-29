<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useHead } from '@unhead/vue'
import { ElSkeleton, ElTag, ElButton } from 'element-plus'
import { marked } from 'marked'
import hljs from 'highlight.js'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
import CommentList from '@/components/blog/CommentList.vue'
import PostCard from '@/components/blog/PostCard.vue'
import { blogApi, type Post, type Comment, type CommentFormData } from '@/api'
import { formatDate } from '@/utils/formatters'
import { useSiteStore } from '@/stores/site'
import { useBlogPostSchema, useBreadcrumbSchema } from '@/composables/useSeo'

// Configure marked to use highlight.js
marked.setOptions({
  breaks: true,
  gfm: true
})

const route = useRoute()
const router = useRouter()
const siteStore = useSiteStore()

const post = ref<Post | null>(null)
const comments = ref<Comment[]>([])
const relatedPosts = ref<Post[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const prevNext = ref<{ prev: Post | null; next: Post | null }>({ prev: null, next: null })

// TOC state
interface TocItem {
  id: string
  text: string
  level: number
  element: HTMLElement
}
const tocList = ref<TocItem[]>([])
const activeTocId = ref<string>('')
const contentRef = ref<HTMLElement | null>(null)

// Reading progress state
const readingProgress = ref(0)

// Scroll and resize handlers
let scrollHandler: () => void
let resizeHandler: () => void
let observer: IntersectionObserver | null = null

const slug = computed(() => route.params.slug as string)

// Configure marked with custom renderer for syntax highlighting
const renderer = new marked.Renderer()
renderer.code = function({ text, lang }: { text: string; lang?: string }) {
  const language = lang && hljs.getLanguage(lang) ? lang : 'plaintext'
  const highlighted = hljs.highlight(text, { language }).value
  return `<pre><code class="hljs language-${language}">${highlighted}</code></pre>`
}

marked.use({ renderer })

const renderedContent = computed(() => {
  if (!post.value?.content) return ''
  return marked(post.value.content)
})

const readingTime = computed(() => {
  if (!post.value?.content) return 0
  const words = post.value.content.split(/\s+/).length
  return Math.ceil(words / 200)
})

const currentUrl = computed(() => {
  if (typeof window === 'undefined') return ''
  return window.location.href
})

// SEO Schema generators
const blogPostSchema = useBlogPostSchema(() => post.value)
const breadcrumbSchema = useBreadcrumbSchema(() => post.value ? [
  { name: 'Home', url: '/' },
  { name: 'Blog', url: '/blog' },
  { name: post.value.categoryName || 'Blog', url: `/blog?category=${post.value.categoryId}` },
  { name: post.value.title, url: currentUrl.value }
] : [])

useHead(() => ({
  title: () => post.value ? `${post.value.title} - ${siteStore.siteName}` : 'Loading...',
  meta: () => post.value ? [
    { name: 'description', content: post.value.excerpt || '' },
    { property: 'og:title', content: post.value.title },
    { property: 'og:description', content: post.value.excerpt || '' },
    { property: 'og:image', content: post.value.coverImage || '' },
    { property: 'og:type', content: 'article' },
    { property: 'og:published_time', content: post.value.publishedAt || post.value.createdAt },
    { property: 'og:modified_time', content: post.value.updatedAt },
    { property: 'article:author', content: siteStore.settings.profile.name || 'Anonymous' },
    { name: 'twitter:card', content: post.value.coverImage ? 'summary_large_image' : 'summary' },
    { name: 'twitter:title', content: post.value.title },
    { name: 'twitter:description', content: post.value.excerpt || '' },
    { name: 'twitter:image', content: post.value.coverImage || '' }
  ] : [],
  script: [
    ...(blogPostSchema.value ? [{
      type: 'application/ld+json',
      innerHTML: JSON.stringify(blogPostSchema.value)
    }] : []),
    ...(breadcrumbSchema.value ? [{
      type: 'application/ld+json',
      innerHTML: JSON.stringify(breadcrumbSchema.value)
    }] : [])
  ]
}))

async function fetchPost() {
  loading.value = true
  error.value = null
  try {
    const res = await blogApi.getPost(slug.value)
    post.value = res.data.data
  } catch {
    error.value = 'Post not found'
  } finally {
    loading.value = false
  }
}

async function fetchComments() {
  if (!post.value?.slug) return
  try {
    const res = await blogApi.getComments(post.value.slug)
    comments.value = res.data.data
  } catch {
    comments.value = []
  }
}

async function fetchRelatedPosts() {
  if (!post.value?.categoryId) return
  try {
    const res = await blogApi.getPosts({
      category: String(post.value.categoryId),
      status: 'published',
      sort: 'publishedAt,desc'
    })
    // Backend returns { posts: [], pagination: {...}, filters: {...} }
    const data = res.data.data
    const allPosts = data.posts || []
    // Filter out current post and limit to 3
    relatedPosts.value = allPosts.filter((p: Post) => p.id !== post.value?.id).slice(0, 3)

    // Find prev/next based on publishedAt
    const currentIndex = allPosts.findIndex((p: Post) => p.id === post.value?.id)
    prevNext.value = {
      prev: currentIndex > 0 ? allPosts[currentIndex - 1] : null,
      next: currentIndex < allPosts.length - 1 ? allPosts[currentIndex + 1] : null
    }
  } catch {
    relatedPosts.value = []
    prevNext.value = { prev: null, next: null }
  }
}

async function incrementViewCount() {
  if (!slug.value) return
  try {
    await blogApi.incrementView(slug.value)
  } catch {
    // Silently fail - view count is not critical
  }
}

async function handleCommentSubmit(data: CommentFormData) {
  if (!post.value?.slug) return
  try {
    await blogApi.createComment(post.value.slug, data)
    await fetchComments()
  } catch {
    // Error handled by API interceptor
  }
}

function getShareUrl(): string {
  const encodedTitle = encodeURIComponent(post.value?.title || '')
  const encodedUrl = encodeURIComponent(currentUrl.value)
  return `https://twitter.com/intent/tweet?text=${encodedTitle}&url=${encodedUrl}`
}

function goBack() {
  router.back()
}

function parseHeadings() {
  if (!contentRef.value) return
  const headings = contentRef.value.querySelectorAll('h2, h3')
  tocList.value = []
  
  headings.forEach((heading) => {
    const el = heading as HTMLElement
    // Generate id if not present
    if (!el.id) {
      el.id = el.textContent?.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '') || `heading-${tocList.value.length}`
    }
    tocList.value.push({
      id: el.id,
      text: el.textContent || '',
      level: parseInt(el.tagName.charAt(1)),
      element: el
    })
  })
  
  // Set up IntersectionObserver for active TOC tracking
  setupTocObserver()
}

function setupTocObserver() {
  if (observer) {
    observer.disconnect()
  }
  
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          activeTocId.value = entry.target.id
        }
      })
    },
    {
      rootMargin: '-80px 0px -70% 0px',
      threshold: 0
    }
  )
  
  tocList.value.forEach((item) => {
    observer?.observe(item.element)
  })
}

function scrollToHeading(id: string) {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

function updateReadingProgress() {
  const scrollTop = window.scrollY
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  readingProgress.value = docHeight > 0 ? Math.min(100, Math.round((scrollTop / docHeight) * 100)) : 0
}

scrollHandler = () => {
  updateReadingProgress()
}

resizeHandler = () => {
  updateReadingProgress()
}

onMounted(async () => {
  await fetchPost()
  if (post.value) {
    incrementViewCount()
    fetchComments()
    fetchRelatedPosts()
  }
  
  window.addEventListener('scroll', scrollHandler, { passive: true })
  window.addEventListener('resize', resizeHandler, { passive: true })
  updateReadingProgress()
})

onUnmounted(() => {
  window.removeEventListener('scroll', scrollHandler)
  window.removeEventListener('resize', resizeHandler)
  if (observer) {
    observer.disconnect()
  }
})

// Watch for content render to parse headings
watch(renderedContent, async () => {
  if (renderedContent.value) {
    await nextTick()
    parseHeadings()
  }
})
</script>

<template>
  <DefaultLayout>
    <!-- Reading Progress Bar -->
    <div class="reading-progress-bar">
      <div class="reading-progress-fill" :style="{ width: `${readingProgress}%` }" />
    </div>
    
    <Header :title="siteStore.siteName" />
    <main class="blog-detail">
      <!-- Breadcrumb Navigation -->
      <nav v-if="post" class="breadcrumb-nav" aria-label="Breadcrumb">
        <ol class="breadcrumb-list">
          <li class="breadcrumb-item">
            <RouterLink to="/">Home</RouterLink>
          </li>
          <li class="breadcrumb-item">
            <RouterLink to="/blog">Blog</RouterLink>
          </li>
          <li v-if="post.categoryName" class="breadcrumb-item">
            <RouterLink :to="`/blog?category=${post.categoryId}`">{{ post.categoryName }}</RouterLink>
          </li>
          <li class="breadcrumb-item active" aria-current="page">{{ post.title }}</li>
        </ol>
      </nav>
      
      <ElSkeleton v-if="loading" :rows="10" animated />
      <template v-else-if="error">
        <div class="error-state">
          <h2>{{ error }}</h2>
          <ElButton @click="goBack">Go Back</ElButton>
        </div>
      </template>
      <template v-else-if="post">
        <div class="article-layout">
          <article class="article">
            <!-- Cover Image -->
            <div v-if="post.coverImage" class="cover-image">
              <img :src="post.coverImage" :alt="post.title" />
            </div>

            <!-- Article Header -->
            <header class="article-header">
              <div class="article-meta">
                <span v-if="post.categoryName" class="category">
                  <ElTag type="primary" size="small">{{ post.categoryName }}</ElTag>
                </span>
                <span class="date">{{ formatDate(post.publishedAt || post.createdAt) }}</span>
                <span class="reading-time">{{ readingTime }} min read</span>
              </div>
              <h1 class="article-title">{{ post.title }}</h1>
              <div v-if="post.tags?.length" class="tags">
                <ElTag v-for="tag in post.tags" :key="tag.id" size="small">
                  {{ tag.name }}
                </ElTag>
              </div>
            </header>

            <!-- Article Content -->
            <div ref="contentRef" class="article-content markdown-body" v-html="renderedContent" />

            <!-- Article Footer -->
            <footer class="article-footer">
              <div class="share-section">
                <span class="share-label">Share:</span>
                <a
                  :href="getShareUrl()"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="share-link"
                >
                  Twitter
                </a>
              </div>
            </footer>

            <!-- Comments Section -->
            <CommentList
              v-if="siteStore.settings.allowComments"
              :comments="comments"
              :post-id="post.id"
              @submit="handleCommentSubmit"
            />
          </article>

          <!-- Table of Contents Sidebar -->
          <aside v-if="tocList.length > 0" class="toc-sidebar">
            <div class="toc-container">
              <h4 class="toc-title">Table of Contents</h4>
              <nav class="toc-nav">
                <ul class="toc-list">
                  <li
                    v-for="item in tocList"
                    :key="item.id"
                    :class="['toc-item', `toc-level-${item.level}`, { active: activeTocId === item.id }]"
                  >
                    <a :href="`#${item.id}`" @click.prevent="scrollToHeading(item.id)">
                      {{ item.text }}
                    </a>
                  </li>
                </ul>
              </nav>
            </div>
          </aside>
        </div>

        <!-- Related Posts Section -->
        <section v-if="relatedPosts.length > 0" class="related-posts">
          <h3 class="related-title">Related Posts</h3>
          <div class="related-grid">
            <PostCard v-for="relatedPost in relatedPosts" :key="relatedPost.id" :post="relatedPost" />
          </div>
        </section>

        <!-- Navigation -->
        <nav class="article-nav">
          <div class="prev-next-nav">
            <RouterLink v-if="prevNext.prev" :to="`/blog/${prevNext.prev.slug}`" class="prev-link">
              <span class="nav-label">← Previous</span>
              <span class="nav-title">{{ prevNext.prev.title }}</span>
            </RouterLink>
            <span v-else />
            <RouterLink v-if="prevNext.next" :to="`/blog/${prevNext.next.slug}`" class="next-link">
              <span class="nav-label">Next →</span>
              <span class="nav-title">{{ prevNext.next.title }}</span>
            </RouterLink>
          </div>
        </nav>
      </template>
    </main>
    <Footer />
  </DefaultLayout>
</template>

<style scoped>
/* Reading Progress Bar */
.reading-progress-bar {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: #f0f0f0;
  z-index: 9999;
}

.reading-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff 0%, #79bbff 100%);
  transition: width 0.1s ease-out;
}

.blog-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.error-state {
  text-align: center;
  padding: 60px 20px;
}

.article-layout {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 32px;
  align-items: start;
}

.article {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.cover-image {
  width: 100%;
  max-height: 400px;
  overflow: hidden;
}

.cover-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-header {
  padding: 32px 32px 0;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #666;
}

.category {
  display: flex;
  align-items: center;
}

.date,
.reading-time {
  color: #999;
}

.article-title {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin: 0 0 16px;
  line-height: 1.3;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-bottom: 24px;
}

.article-content {
  padding: 0 32px 32px;
  font-size: 16px;
  line-height: 1.8;
  color: #333;
}

.article-content:deep(h1),
.article-content:deep(h2),
.article-content:deep(h3),
.article-content:deep(h4) {
  margin-top: 32px;
  margin-bottom: 16px;
  font-weight: 600;
  color: #333;
}

.article-content:deep(p) {
  margin-bottom: 16px;
}

.article-content:deep(code) {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Fira Code', monospace;
  font-size: 0.9em;
}

.article-content:deep(pre) {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin-bottom: 16px;
}

.article-content:deep(pre code) {
  background: none;
  padding: 0;
}

.article-content:deep(blockquote) {
  border-left: 4px solid #409eff;
  margin: 16px 0;
  padding: 8px 16px;
  background: #f9f9f9;
  color: #666;
}

.article-content:deep(img) {
  max-width: 100%;
  border-radius: 8px;
}

.article-content:deep(a) {
  color: #409eff;
  text-decoration: none;
}

.article-content:deep(a:hover) {
  text-decoration: underline;
}

.article-content:deep(ul),
.article-content:deep(ol) {
  margin-bottom: 16px;
  padding-left: 24px;
}

.article-content:deep(li) {
  margin-bottom: 8px;
}

.article-footer {
  padding: 24px 32px;
  border-top: 1px solid #f0f0f0;
}

.share-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.share-label {
  color: #666;
  font-size: 14px;
}

.share-link {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.share-link:hover {
  text-decoration: underline;
}

/* Table of Contents Sidebar */
.toc-sidebar {
  position: sticky;
  top: 80px;
}

.toc-container {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.toc-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.toc-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.toc-item {
  margin-bottom: 8px;
}

.toc-item a {
  display: block;
  font-size: 13px;
  color: #666;
  text-decoration: none;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
  line-height: 1.4;
}

.toc-item a:hover {
  color: #409eff;
  background: rgba(64, 158, 255, 0.08);
}

.toc-item.active > a {
  color: #409eff;
  font-weight: 600;
  background: rgba(64, 158, 255, 0.1);
}

.toc-item.toc-level-3 {
  padding-left: 16px;
}

.toc-item.toc-level-3 a {
  font-size: 12px;
}

/* Related Posts */
.related-posts {
  margin-top: 48px;
  padding-top: 32px;
  border-top: 1px solid #f0f0f0;
}

.related-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 24px;
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.article-nav {
  margin-top: 24px;
}

.prev-next-nav {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 0;
  border-top: 1px solid #f0f0f0;
}

.prev-link,
.next-link {
  display: flex;
  flex-direction: column;
  text-decoration: none;
  padding: 12px 16px;
  border-radius: 8px;
  background: #f9f9f9;
  transition: all 0.2s;
  max-width: 48%;
}

.prev-link:hover,
.next-link:hover {
  background: #f0f0f0;
}

.next-link {
  text-align: right;
}

.nav-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.nav-title {
  font-size: 14px;
  color: #409eff;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Breadcrumb Navigation */
.breadcrumb-nav {
  margin-bottom: 16px;
}

.breadcrumb-list {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  list-style: none;
  margin: 0;
  padding: 0;
  font-size: 14px;
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

.breadcrumb-item:not(.active)::after {
  content: '/';
  color: #ccc;
}

.breadcrumb-item a {
  color: #409eff;
  text-decoration: none;
}

.breadcrumb-item a:hover {
  text-decoration: underline;
}

.breadcrumb-item.active {
  color: #999;
}

/* Responsive */
@media (max-width: 1024px) {
  .article-layout {
    grid-template-columns: 1fr;
  }
  
  .toc-sidebar {
    display: none;
  }
}

@media (max-width: 768px) {
  .article-header {
    padding: 24px 20px 0;
  }

  .article-content {
    padding: 0 20px 24px;
  }

  .article-footer {
    padding: 20px;
  }

  .article-title {
    font-size: 24px;
  }
  
  .related-grid {
    grid-template-columns: 1fr;
  }
}
</style>