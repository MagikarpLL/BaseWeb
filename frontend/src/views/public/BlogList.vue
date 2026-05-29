<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useHead } from '@unhead/vue'
import { ElSkeleton, ElSelect, ElOption, ElPagination } from 'element-plus'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
import PostCard from '@/components/blog/PostCard.vue'
import { blogApi, categoryApi, type Post, type Category, type Tag } from '@/api'
import { useSiteStore } from '@/stores/site'

const siteStore = useSiteStore()
const route = useRoute()

const posts = ref<Post[]>([])
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const loading = ref(false)
const total = ref(0)

const currentPage = ref(1)
const pageSize = ref(10)
const selectedCategory = ref<string>('')
const selectedTag = ref<string>('')
const sortBy = ref<string>('publishedAt')

const categoryOptions = computed(() => [
  { value: '', label: 'All Categories' },
  ...categories.value.map(c => ({ value: String(c.id), label: c.name }))
])

const tagOptions = computed(() => [
  { value: '', label: 'All Tags' },
  ...tags.value.map(t => ({ value: String(t.id), label: t.name }))
])

const sortOptions = [
  { value: 'publishedAt', label: 'Latest' },
  { value: 'updatedAt', label: 'Recently Updated' },
  { value: 'viewCount', label: 'Most Viewed' }
]

useHead({
  title: `Blog - ${siteStore.siteName}`,
  meta: [
    { name: 'description', content: siteStore.siteDescription || 'Blog posts' }
  ]
})

async function fetchPosts() {
  loading.value = true
  try {
    const res = await blogApi.getPosts({
      page: currentPage.value,
      size: pageSize.value,
      category: selectedCategory.value || undefined,
      tag: selectedTag.value || undefined,
      sort: sortBy.value,
      status: 'published'
    })
    // Backend returns { posts: [], pagination: {...}, filters: {...} }
    const data = res.data.data
    posts.value = data.posts || []
    total.value = data.pagination?.total || 0
  } catch (e) {
    posts.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    const res = await categoryApi.getAll()
    categories.value = res.data.data
  } catch {
    categories.value = []
  }
}

async function fetchTags() {
  try {
    const res = await blogApi.getAllTags()
    tags.value = res.data.data
  } catch {
    tags.value = []
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  fetchPosts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function handleCategoryChange() {
  currentPage.value = 1
  fetchPosts()
}

function handleTagChange() {
  currentPage.value = 1
  fetchPosts()
}

function handleSortChange() {
  currentPage.value = 1
  fetchPosts()
}

watch(
  () => route.query,
  (query) => {
    if (query.category) selectedCategory.value = query.category as string
    if (query.tag) selectedTag.value = query.tag as string
    fetchPosts()
  },
  { immediate: false }
)

onMounted(() => {
  fetchCategories()
  fetchTags()
  fetchPosts()
})
</script>

<template>
  <DefaultLayout>
    <Header :title="siteStore.siteName" />
    <main class="blog-list">
      <div class="page-header">
        <h1 class="page-title">Blog</h1>
        <p class="page-description">Thoughts, tutorials, and discoveries</p>
      </div>

      <div class="filters">
        <ElSelect
          v-model="selectedCategory"
          placeholder="Category"
          clearable
          class="filter-select"
          @change="handleCategoryChange"
        >
          <ElOption
            v-for="cat in categoryOptions"
            :key="cat.value"
            :label="cat.label"
            :value="cat.value"
          />
        </ElSelect>

        <ElSelect
          v-model="selectedTag"
          placeholder="Tag"
          clearable
          class="filter-select"
          @change="handleTagChange"
        >
          <ElOption
            v-for="t in tagOptions"
            :key="t.value"
            :label="t.label"
            :value="t.value"
          />
        </ElSelect>

        <ElSelect
          v-model="sortBy"
          placeholder="Sort by"
          class="filter-select"
          @change="handleSortChange"
        >
          <ElOption
            v-for="opt in sortOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </ElSelect>
      </div>

      <ElSkeleton v-if="loading" :rows="6" animated />
      <template v-else>
        <div v-if="posts.length" class="posts-grid">
          <PostCard v-for="post in posts" :key="post.id" :post="post" />
        </div>
        <div v-else class="no-posts">
          <p>No posts found. Check back later!</p>
        </div>

        <div v-if="total > pageSize" class="pagination">
          <ElPagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            :pager-count="5"
            layout="prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>
      </template>
    </main>
    <Footer />
  </DefaultLayout>
</template>

<style scoped>
.blog-list {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  padding: 40px 20px;
}

.page-title {
  font-size: 36px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px;
}

.page-description {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.filters {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
  justify-content: center;
}

.filter-select {
  width: 180px;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.no-posts {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

@media (max-width: 768px) {
  .filters {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-select {
    width: 100%;
  }
}
</style>
