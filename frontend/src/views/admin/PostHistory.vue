<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElButton, ElCard, ElTable, ElTableColumn, ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'
import { adminPostApi } from '@/api'
import { useLocale } from '@/composables/useLocale'

interface PostHistory {
  id: number
  postId: number
  title: string
  content: string
  excerpt: string
  status: string
  createdAt: string
  createdBy: number | null
}

interface CompareResult {
  from: PostHistory
  to: PostHistory
}

const { t } = useLocale()
const route = useRoute()
const router = useRouter()

const postId = computed(() => route.params.id as string)
const histories = ref<PostHistory[]>([])
const loading = ref(false)
const compareMode = ref(false)
const selectedFrom = ref<PostHistory | null>(null)
const selectedTo = ref<PostHistory | null>(null)
const compareResult = ref<CompareResult | null>(null)

const info = computed(() => ({
  history: t('admin.history') || 'History',
  compare: t('admin.compare') || 'Compare',
  restore: t('admin.restore') || 'Restore',
  back: t('common.back') || 'Back',
  version: t('admin.version') || 'Version',
  selectToCompare: t('admin.selectToCompare') || 'Select versions to compare',
  noHistory: t('admin.noHistory') || 'No history found',
  restored: t('admin.restored') || 'Version restored successfully',
  confirmRestore: t('admin.confirmRestore') || 'Are you sure you want to restore this version?',
  current: t('admin.current') || 'Current'
}))

async function fetchHistories() {
  loading.value = true
  try {
    const res = await adminPostApi.getPostHistory(Number(postId.value))
    histories.value = res.data.data || []
  } catch {
    ElMessage.error('Failed to load history')
    histories.value = []
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString()
}

function selectForCompare(history: PostHistory, position: 'from' | 'to') {
  if (position === 'from') {
    selectedFrom.value = history
  } else {
    selectedTo.value = history
  }
}

async function compareVersions() {
  if (!selectedFrom.value || !selectedTo.value) {
    ElMessage.warning(info.value.selectToCompare)
    return
  }
  try {
    const res = await adminPostApi.compareHistory(Number(postId.value), {
      from: selectedFrom.value.id,
      to: selectedTo.value.id
    })
    compareResult.value = res.data.data
    compareMode.value = true
  } catch {
    ElMessage.error('Failed to compare versions')
  }
}

async function restoreVersion(history: PostHistory) {
  try {
    await ElMessageBox.confirm(info.value.confirmRestore, info.value.restore, {
      confirmButtonText: t('common.confirm') || 'Confirm',
      cancelButtonText: t('common.cancel') || 'Cancel',
      type: 'warning'
    })
    await adminPostApi.restoreHistory(Number(postId.value), history.id)
    ElMessage.success(info.value.restored)
    router.push(`/admin/posts/${postId.value}/edit`)
  } catch {
    // Cancelled or error
  }
}

function goBack() {
  router.push(`/admin/posts/${postId.value}/edit`)
}

onMounted(() => {
  fetchHistories()
})
</script>

<template>
  <div class="post-history">
    <div class="header">
      <h1 class="page-title">{{ info.history }}</h1>
      <div class="header-actions">
        <ElButton @click="goBack">{{ info.back }}</ElButton>
      </div>
    </div>

    <!-- Compare Mode -->
    <div v-if="compareMode && compareResult" class="compare-view">
      <ElCard>
        <template #header>
          <div class="compare-header">
            <span>{{ info.compare }}</span>
            <ElButton @click="compareMode = false; compareResult = null">{{ info.back }}</ElButton>
          </div>
        </template>
        <div class="compare-container">
          <div class="compare-side old">
            <h3>{{ info.version }} #{{ selectedFrom?.id }} ({{ formatDate(selectedFrom?.createdAt || '') }})</h3>
            <div class="content-preview markdown-body" v-html="marked(selectedFrom?.content || '')"></div>
          </div>
          <div class="compare-side new">
            <h3>{{ info.version }} #{{ selectedTo?.id }} ({{ formatDate(selectedTo?.createdAt || '') }})</h3>
            <div class="content-preview markdown-body" v-html="marked(selectedTo?.content || '')"></div>
          </div>
        </div>
      </ElCard>
    </div>

    <!-- Selection Mode -->
    <div v-else class="history-list">
      <ElCard class="selection-actions" v-if="histories.length >= 2">
        <div class="selection-info">
          <span class="selection-label">{{ info.selectToCompare }}:</span>
          <span class="selection-from" v-if="selectedFrom">
            {{ info.version }} #{{ selectedFrom.id }} ({{ formatDate(selectedFrom.createdAt) }})
          </span>
          <span class="selection-to" v-if="selectedTo">
            → {{ info.version }} #{{ selectedTo.id }} ({{ formatDate(selectedTo.createdAt) }})
          </span>
        </div>
        <ElButton
          type="primary"
          :disabled="!selectedFrom || !selectedTo"
          @click="compareVersions"
        >
          {{ info.compare }}
        </ElButton>
      </ElCard>

      <ElTable :data="histories" v-loading="loading" stripe>
        <ElTableColumn prop="id" label="ID" width="80" />
        <ElTableColumn :label="info.version" width="180">
          <template #default="{ row }">
            <div class="version-cell">
              <span>#{{ row.id }}</span>
              <span class="version-date">{{ formatDate(row.createdAt) }}</span>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="title" :label="t('admin.title') || 'Title'" />
        <ElTableColumn :label="t('admin.status') || 'Status'" width="120">
          <template #default="{ row }">
            <span :class="['status-badge', row.status]">{{ row.status }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn :label="t('admin.actions') || 'Actions'" width="280" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <ElButton
                size="small"
                :type="selectedFrom?.id === row.id ? 'primary' : 'default'"
                @click="selectForCompare(row, 'from')"
                :disabled="selectedTo?.id === row.id"
              >
                {{ t('admin.selectAsFrom') || 'Select From' }}
              </ElButton>
              <ElButton
                size="small"
                :type="selectedTo?.id === row.id ? 'primary' : 'default'"
                @click="selectForCompare(row, 'to')"
                :disabled="selectedFrom?.id === row.id"
              >
                {{ t('admin.selectAsTo') || 'Select To' }}
              </ElButton>
              <ElButton
                size="small"
                type="warning"
                @click="restoreVersion(row)"
              >
                {{ info.restore }}
              </ElButton>
            </div>
          </template>
        </ElTableColumn>
      </ElTable>

      <div v-if="histories.length === 0 && !loading" class="empty-state">
        <p>{{ info.noHistory }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.post-history {
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

.header-actions {
  display: flex;
  gap: 12px;
}

.selection-actions {
  margin-bottom: 20px;
}

.selection-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.selection-label {
  font-weight: 500;
  color: #666;
}

.selection-from, .selection-to {
  padding: 4px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
}

.selection-from {
  background: #ecf5ff;
  color: #409eff;
}

.selection-to {
  background: #f0f9eb;
  color: #67c23a;
}

.version-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.version-date {
  font-size: 12px;
  color: #999;
}

.status-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-badge.published {
  background: #f0f9eb;
  color: #67c23a;
}

.status-badge.draft {
  background: #f5f7fa;
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.compare-view {
  margin-top: 20px;
}

.compare-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.compare-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.compare-side {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.compare-side.old {
  border-left: 4px solid #f56c6c;
}

.compare-side.new {
  border-left: 4px solid #67c23a;
}

.compare-side h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #333;
}

.content-preview {
  max-height: 500px;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.6;
}

.content-preview :deep(h1),
.content-preview :deep(h2),
.content-preview :deep(h3) {
  margin-top: 16px;
  margin-bottom: 8px;
}

.content-preview :deep(p) {
  margin-bottom: 12px;
}

.content-preview :deep(code) {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

@media (max-width: 1024px) {
  .compare-container {
    grid-template-columns: 1fr;
  }
}
</style>