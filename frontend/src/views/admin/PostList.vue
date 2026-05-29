<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElTable, ElTableColumn, ElButton, ElTag, ElSelect, ElOption, ElPagination, ElMessage, ElMessageBox } from 'element-plus'
import { adminPostApi, categoryApi, type Post, type Category } from '@/api'
import { useLocale } from '@/composables/useLocale'

const { t } = useLocale()

const router = useRouter()

const posts = ref<Post[]>([])
const categories = ref<Category[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const selectedCategory = ref('')
const selectedStatus = ref('')

// Translations
const info = computed(() => ({
  posts: t('admin.posts'),
  newPost: t('common.add'),
  title: t('admin.title'),
  allStatus: t('admin.allStatus') || 'All Status',
  draft: t('admin.draft'),
  published: t('admin.published'),
  category: t('admin.category'),
  status: t('admin.status'),
  views: t('admin.views') || 'Views',
  comments: t('admin.comments'),
  created: t('admin.createdAt'),
  actions: t('admin.actions'),
  edit: t('admin.edit'),
  delete: t('admin.delete'),
  deleteConfirmTitle: t('admin.deletePost'),
  deleteConfirm: t('admin.confirmDelete'),
  deleteSuccess: t('admin.deleteSuccess') || 'Post deleted successfully'
}))

const statusOptions = computed(() => [
  { value: '', label: info.value.allStatus },
  { value: 'draft', label: info.value.draft },
  { value: 'published', label: info.value.published }
])

async function fetchPosts() {
  loading.value = true
  try {
    const res = await adminPostApi.getPosts({
      page: currentPage.value,
      size: pageSize.value,
      category: selectedCategory.value || undefined,
      status: selectedStatus.value || undefined
    })
    posts.value = res.data.data.content
    total.value = res.data.data.totalElements
  } catch {
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

function handlePageChange(page: number) {
  currentPage.value = page
  fetchPosts()
}

function handleFilterChange() {
  currentPage.value = 1
  fetchPosts()
}

function createPost() {
  router.push('/admin/posts/new')
}

function editPost(id: number) {
  router.push(`/admin/posts/${id}/edit`)
}

async function deletePost(id: number, title: string) {
  try {
    await ElMessageBox.confirm(
      `"${title}"?`,
      info.value.deleteConfirmTitle,
      {
        confirmButtonText: info.value.delete,
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    await adminPostApi.deletePost(id)
    ElMessage.success(info.value.deleteSuccess)
    fetchPosts()
  } catch {
    // User cancelled
  }
}

function getStatusType(status: string): 'success' | 'warning' | 'info' {
  switch (status) {
    case 'published': return 'success'
    case 'draft': return 'warning'
    default: return 'info'
  }
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

onMounted(() => {
  fetchCategories()
  fetchPosts()
})
</script>

<template>
  <div class="post-list">
    <div class="header">
      <h1 class="page-title">{{ info.posts }}</h1>
      <ElButton type="primary" @click="createPost">{{ info.newPost }}</ElButton>
    </div>

    <div class="filters">
      <ElSelect
        v-model="selectedCategory"
        :placeholder="info.category"
        clearable
        class="filter-select"
        @change="handleFilterChange"
      >
        <ElOption
          v-for="cat in categories"
          :key="cat.id"
          :label="cat.name"
          :value="cat.slug"
        />
      </ElSelect>

      <ElSelect
        v-model="selectedStatus"
        :placeholder="info.status"
        clearable
        class="filter-select"
        @change="handleFilterChange"
      >
        <ElOption
          v-for="opt in statusOptions"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </ElSelect>
    </div>

    <ElTable :data="posts" v-loading="loading" stripe class="posts-table">
      <ElTableColumn prop="id" label="ID" width="80" />
      <ElTableColumn prop="title" :label="info.title || 'Title'" min-width="200">
        <template #default="{ row }">
          <div class="post-title-cell">
            <span class="post-title">{{ row.title }}</span>
            <span v-if="row.excerpt" class="post-excerpt">{{ row.excerpt }}</span>
          </div>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="categoryName" :label="info.category" width="120" />
      <ElTableColumn prop="status" :label="info.status" width="100">
        <template #default="{ row }">
          <ElTag :type="getStatusType(row.status)" size="small">
            {{ row.status }}
          </ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="viewCount" :label="info.views" width="80" align="center" />
      <ElTableColumn prop="commentCount" :label="info.comments" width="100" align="center" />
      <ElTableColumn prop="createdAt" :label="info.created" width="120">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </ElTableColumn>
      <ElTableColumn :label="info.actions" width="160" fixed="right">
        <template #default="{ row }">
          <ElButton size="small" @click="editPost(row.id)">{{ info.edit }}</ElButton>
          <ElButton size="small" type="danger" @click="deletePost(row.id, row.title)">{{ info.delete }}</ElButton>
        </template>
      </ElTableColumn>
    </ElTable>

    <div class="pagination">
      <ElPagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.post-list {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.filters {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.filter-select {
  width: 180px;
}

.posts-table {
  border-radius: 8px;
  overflow: hidden;
}

.post-title-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.post-title {
  font-weight: 500;
  color: #333;
}

.post-excerpt {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 300px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .filters {
    flex-direction: column;
  }

  .filter-select {
    width: 100%;
  }
}
</style>
