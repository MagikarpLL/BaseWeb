<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

interface Post {
  id: number
  title: string
  slug: string
  excerpt?: string
  coverImage?: string
  publishedAt?: string
  categoryName?: string
  tags?: Array<{ id: number; name: string; slug: string }>
  viewCount?: number
}

const props = defineProps<{
  post: Post
  highlightKeyword?: string
}>()

function highlightText(text: string, keyword: string): string {
  if (!keyword) return text
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

const formattedDate = computed(() => {
  if (!props.post.publishedAt) return ''
  return new Date(props.post.publishedAt).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
})

const highlightedTitle = computed(() => 
  props.highlightKeyword ? highlightText(props.post.title, props.highlightKeyword) : props.post.title
)

const highlightedExcerpt = computed(() => 
  props.highlightKeyword && props.post.excerpt ? highlightText(props.post.excerpt, props.highlightKeyword) : props.post.excerpt
)
</script>

<template>
  <article class="post-card">
    <RouterLink :to="`/blog/${post.slug}`" class="post-link">
      <div class="post-cover-wrapper">
        <img
          v-if="post.coverImage"
          v-lazyload="post.coverImage"
          :alt="post.title"
          class="post-cover lazy"
        />
        <div v-else class="post-cover-placeholder">
          <span>{{ post.categoryName?.[0] || 'B' }}</span>
        </div>
      </div>
      <div class="post-content">
        <div class="post-meta">
          <span v-if="post.categoryName" class="post-category">{{ post.categoryName }}</span>
          <time v-if="formattedDate" class="post-date">{{ formattedDate }}</time>
        </div>
        <h3 class="post-title" v-html="highlightedTitle"></h3>
        <p v-if="highlightedExcerpt" class="post-excerpt" v-html="highlightedExcerpt"></p>
        <div v-if="post.tags?.length" class="post-tags">
          <span v-for="tag in post.tags.slice(0, 3)" :key="tag.id" class="post-tag">
            {{ tag.name }}
          </span>
        </div>
        <div class="post-footer">
          <span class="read-more">Read More →</span>
          <span v-if="post.viewCount" class="view-count">{{ post.viewCount }} views</span>
        </div>
      </div>
    </RouterLink>
  </article>
</template>

<style scoped>
.post-card {
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
}

.post-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.post-link {
  text-decoration: none;
  color: inherit;
  display: block;
}

.post-cover-wrapper {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.post-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.post-card:hover .post-cover {
  transform: scale(1.05);
}

.post-cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.post-cover-placeholder span {
  font-size: 48px;
  color: #fff;
  opacity: 0.8;
}

.post-content {
  padding: 20px;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 13px;
}

.post-category {
  background: #409eff;
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
}

.post-date {
  color: #999;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-excerpt {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.post-tag {
  background: #f0f0f0;
  color: #666;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.read-more {
  color: #409eff;
  font-size: 14px;
  font-weight: 500;
}

.view-count {
  color: #999;
  font-size: 12px;
}

:deep(mark) {
  background: #fef08a;
  color: #92400e;
  padding: 0 2px;
  border-radius: 2px;
}
</style>
