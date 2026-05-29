<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElTag, ElSelect, ElOption, ElPagination, ElMessage, ElMessageBox } from 'element-plus'
import { adminCommentApi, type AdminComment } from '@/api'

const comments = ref<AdminComment[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const selectedStatus = ref('')

const statusOptions = [
  { value: '', label: 'All Status' },
  { value: 'pending', label: 'Pending' },
  { value: 'approved', label: 'Approved' },
  { value: 'rejected', label: 'Rejected' }
]

async function fetchComments() {
  loading.value = true
  try {
    const res = await adminCommentApi.getComments({
      page: currentPage.value,
      size: pageSize.value,
      status: selectedStatus.value || undefined
    })
    comments.value = res.data.data.content
    total.value = res.data.data.totalElements
  } catch {
    comments.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  fetchComments()
}

function handleStatusFilter() {
  currentPage.value = 1
  fetchComments()
}

async function approveComment(id: number) {
  try {
    await adminCommentApi.updateStatus(id, 'approved')
    ElMessage.success('Comment approved')
    fetchComments()
  } catch {
    // Error handled by API interceptor
  }
}

async function rejectComment(id: number) {
  try {
    await adminCommentApi.updateStatus(id, 'rejected')
    ElMessage.success('Comment rejected')
    fetchComments()
  } catch {
    // Error handled by API interceptor
  }
}

async function deleteComment(id: number, author: string) {
  try {
    await ElMessageBox.confirm(
      `Are you sure you want to delete the comment by "${author}"?`,
      'Delete Comment',
      {
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )
    await adminCommentApi.deleteComment(id)
    ElMessage.success('Comment deleted successfully')
    fetchComments()
  } catch {
    // User cancelled
  }
}

function getStatusType(status: string): 'success' | 'warning' | 'danger' | 'info' {
  switch (status) {
    case 'approved': return 'success'
    case 'pending': return 'warning'
    case 'rejected': return 'danger'
    default: return 'info'
  }
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchComments()
})
</script>

<template>
  <div class="comment-manage">
    <div class="header">
      <h1 class="page-title">Comments</h1>
    </div>

    <div class="filters">
      <ElSelect
        v-model="selectedStatus"
        placeholder="Status"
        clearable
        class="filter-select"
        @change="handleStatusFilter"
      >
        <ElOption
          v-for="opt in statusOptions"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </ElSelect>
    </div>

    <ElTable :data="comments" v-loading="loading" stripe class="comments-table">
      <ElTableColumn prop="id" label="ID" width="80" />
      <ElTableColumn prop="author" label="Author" width="120">
        <template #default="{ row }">
          <div class="author-cell">
            <span class="author-name">{{ row.author }}</span>
            <span v-if="row.email" class="author-email">{{ row.email }}</span>
          </div>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="content" label="Content" min-width="250">
        <template #default="{ row }">
          <div class="content-cell">
            <p class="comment-content">{{ row.content }}</p>
          </div>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="postTitle" label="Post" min-width="150">
        <template #default="{ row }">
          <span class="post-title">{{ row.postTitle }}</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="status" label="Status" width="100">
        <template #default="{ row }">
          <ElTag :type="getStatusType(row.status)" size="small">
            {{ row.status }}
          </ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="createdAt" label="Date" width="160">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </ElTableColumn>
      <ElTableColumn label="Actions" width="200" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 'pending'">
            <ElButton size="small" type="success" @click="approveComment(row.id)">Approve</ElButton>
            <ElButton size="small" type="warning" @click="rejectComment(row.id)">Reject</ElButton>
          </template>
          <ElButton size="small" type="danger" @click="deleteComment(row.id, row.author)">Delete</ElButton>
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
.comment-manage {
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

.comments-table {
  border-radius: 8px;
  overflow: hidden;
}

.author-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-weight: 500;
}

.author-email {
  font-size: 12px;
  color: #999;
}

.content-cell {
  max-width: 300px;
}

.comment-content {
  margin: 0;
  font-size: 14px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-title {
  font-size: 13px;
  color: #409eff;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
