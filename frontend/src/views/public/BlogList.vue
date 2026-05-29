<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useHead } from '@unhead/vue'
import { ElSkeleton, ElSelect, ElOption, ElPagination, ElInput } from 'element-plus'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
import PostCard from '@/components/blog/PostCard.vue'
import TagCloud from '@/components/blog/TagCloud.vue'
import { blogApi, categoryApi, type Post, type Category, type Tag } from '@/api'
import { useSiteStore } from '@/stores/site'
import { useLocale } from '@/composables/useLocale'

const siteStore = useSiteStore()
const { t } = useLocale()
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

// Search state
const searchQuery = ref('')
const searchKeyword = ref('')
const isSearching = ref(false)

const categoryOptions = computed(() => [
  { value: '', label: t('tools.all') },
  ...categories.value.map(c => ({ value: String(c.id), label: c.name }))
])

const tagOptions = computed(() => [
  { value: '', label: t('tools.all') },
  ...tags.value.map(t => ({ value: String(t.id), label: t.name }))
])

const sortOptions = computed(() => [
  { value: 'publishedAt', label: t('blog.latest') || 'Latest' },
  { value: 'updatedAt', label: t('blog.recentlyUpdated') || 'Recently Updated' },
  { value: 'viewCount', label: t('blog.mostViewed') || 'Most Viewed' }
])

useHead({
  title: `Blog - ${siteStore.siteName}`,
  meta: [
    { name: 'description', content: siteStore.siteDescription || 'Blog posts' }
  ]
})

async function fetchPosts() {
  if (isSearching.value) return
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

async function fetchSearchResults() {
  if (!searchQuery.value.trim()) {
    isSearching.value = false
    fetchPosts()
    return
  }
  loading.value = true
  isSearching.value = true
  searchKeyword.value = searchQuery.value.trim()
  try {
    const res = await blogApi.searchPosts({
      q: searchKeyword.value,
      page: currentPage.value,
      size: pageSize.value
    })
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

function handleSearch() {
  currentPage.value = 1
  if (searchQuery.value.trim()) {
    fetchSearchResults()
  } else {
    isSearching.value = false
    fetchPosts()
  }
}

function handleSearchKeydown(e: Event | KeyboardEvent) {
  if ('key' in e && e.key === 'Enter') {
    handleSearch()
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
  if (isSearching.value) {
    fetchSearchResults()
  } else {
    fetchPosts()
  }
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function handleCategoryChange() {
  currentPage.value = 1
  if (!isSearching.value) {
    fetchPosts()
  }
}

function handleTagChange() {
  currentPage.value = 1
  if (!isSearching.value) {
    fetchPosts()
  }
}

function handleSortChange() {
  currentPage.value = 1
  if (!isSearching.value) {
    fetchPosts()
  }
}

function handleTagClick(tag: Tag) {
  selectedTag.value = String(tag.id)
  currentPage.value = 1
  if (!isSearching.value) {
    fetchPosts()
  }
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
        <h1 class="page-title">{{ t('nav.blog') }}</h1>
        <p class="page-description">Thoughts, tutorials, and discoveries</p>
      </div>

      <div class="search-box">
        <ElInput
          v-model="searchQuery"
          :placeholder="t('blog.searchPlaceholder') || 'Search articles...'"
          class="search-input"
          clearable
          @keydown="handleSearchKeydown"
        >
          <template #append>
            <ElButton @click="handleSearch">{{ t('blog.search') || 'Search' }}</ElButton>
          </template>
        </ElInput>
      </div>

      <div v-if="isSearching" class="search-result-info">
        {{ t('blog.searchResult', { count: total, keyword: searchKeyword }) || `Found ${total} articles for keyword: ${searchKeyword}` }}
      </div>

      <TagCloud :tags="tags" @click="handleTagClick" />

      <div class="filters">
        <ElSelect
          v-model="selectedCategory"
          :placeholder="t('admin.category')"
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
          :placeholder="t('admin.tags')"
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
          :placeholder="t('blog.sort')"
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
          <PostCard v-for="post in posts" :key="post.id" :post="post" :highlight-keyword="isSearching ? searchKeyword : undefined" />
        </div>
        <div v-else class="no-posts">
          <p>{{ t('blog.noResults') }}</p>
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

.search-box {
  max-width: 600px;
  margin: 0 auto 24px;
}

.search-input {
  width: 100%;
}

.search-result-info {
  text-align: center;
  padding: 12px 20px;
  background: #f0f9ff;
  border-radius: 8px;
  margin-bottom: 24px;
  color: #409eff;
  font-size: 14px;
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