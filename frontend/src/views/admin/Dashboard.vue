<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElCard, ElRow, ElCol, ElStatistic, ElSkeleton } from 'element-plus'
import { analyticsApi, type AnalyticsOverview } from '@/api'

const router = useRouter()

const stats = ref<AnalyticsOverview | null>(null)
const loading = ref(true)

// Dashboard text translations
const info = computed(() => ({
  dashboard: t('admin.dashboard'),
  totalPosts: t('admin.totalPosts') || 'Total Posts',
  totalComments: t('admin.totalComments') || 'Total Comments',
  totalViews: t('admin.totalViews') || 'Total Views',
  todayVisitors: t('admin.todayVisitors') || 'Today Visitors',
  quickActions: t('admin.quickActions') || 'Quick Actions',
  writePost: t('admin.createPost'),
  manageCategories: t('admin.manageCategories') || 'Manage Categories',
  manageTags: t('admin.manageTags') || 'Manage Tags',
  settings: t('admin.settings'),
  recentPosts: t('admin.recentPosts') || 'Recent Posts',
  viewAll: t('home.viewAll'),
  noPosts: t('admin.noPosts') || 'No posts yet. Start writing!'
}))

async function fetchStats() {
  loading.value = true
  try {
    const res = await analyticsApi.getOverview()
    stats.value = res.data.data
  } catch {
    // Use empty stats on error
    stats.value = {
      totalPosts: 0,
      totalComments: 0,
      totalViews: 0,
      totalUsers: 0,
      todayPV: 0,
      todayUV: 0,
      recentPosts: []
    }
  } finally {
    loading.value = false
  }
}

function goToPosts() {
  router.push('/admin/posts')
}

function goToComments() {
  router.push('/admin/comments')
}

function goToAnalytics() {
  router.push('/admin/analytics')
}

onMounted(() => {
  fetchStats()
})
</script>

<template>
  <div class="dashboard">
    <h1 class="page-title">{{ info.dashboard }}</h1>

    <ElSkeleton v-if="loading" :rows="6" animated />

    <template v-else-if="stats">
      <!-- Stats Cards -->
      <ElRow :gutter="20" class="stats-row">
        <ElCol :xs="24" :sm="12" :md="6">
          <ElCard class="stat-card" @click="goToPosts">
            <ElStatistic :title="info.totalPosts" :value="stats.totalPosts">
              <template #prefix>
                <span class="stat-icon">📝</span>
              </template>
            </ElStatistic>
          </ElCard>
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="6">
          <ElCard class="stat-card" @click="goToComments">
            <ElStatistic :title="info.totalComments" :value="stats.totalComments">
              <template #prefix>
                <span class="stat-icon">💬</span>
              </template>
            </ElStatistic>
          </ElCard>
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="6">
          <ElCard class="stat-card" @click="goToAnalytics">
            <ElStatistic :title="info.totalViews" :value="stats.totalViews">
              <template #prefix>
                <span class="stat-icon">👁️</span>
              </template>
            </ElStatistic>
          </ElCard>
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="6">
          <ElCard class="stat-card">
            <ElStatistic :title="info.todayVisitors" :value="stats.todayPV + stats.todayUV">
              <template #prefix>
                <span class="stat-icon">📊</span>
              </template>
            </ElStatistic>
          </ElCard>
        </ElCol>
      </ElRow>

      <!-- Quick Actions -->
      <ElRow :gutter="20" class="actions-row">
        <ElCol :span="24">
          <ElCard>
            <template #header>
              <span>{{ info.quickActions }}</span>
            </template>
            <div class="quick-actions">
              <ElCard
                class="action-card"
                shadow="hover"
                @click="router.push('/admin/posts/new')"
              >
                <div class="action-icon">✍️</div>
                <div class="action-text">{{ info.writePost }}</div>
              </ElCard>
              <ElCard
                class="action-card"
                shadow="hover"
                @click="router.push('/admin/categories')"
              >
                <div class="action-icon">📁</div>
                <div class="action-text">{{ info.manageCategories }}</div>
              </ElCard>
              <ElCard
                class="action-card"
                shadow="hover"
                @click="router.push('/admin/tags')"
              >
                <div class="action-icon">🏷️</div>
                <div class="action-text">{{ info.manageTags }}</div>
              </ElCard>
              <ElCard
                class="action-card"
                shadow="hover"
                @click="router.push('/admin/settings')"
              >
                <div class="action-icon">⚙️</div>
                <div class="action-text">{{ info.settings }}</div>
              </ElCard>
            </div>
          </ElCard>
        </ElCol>
      </ElRow>

      <!-- Recent Posts -->
      <ElRow :gutter="20" class="recent-row">
        <ElCol :span="24">
          <ElCard>
            <template #header>
              <div class="card-header">
                <span>{{ info.recentPosts }}</span>
                <ElCard
                  class="view-all-btn"
                  shadow="hover"
                  @click="goToPosts"
                >
                  {{ info.viewAll }}
                </ElCard>
              </div>
            </template>
            <div v-if="stats.recentPosts?.length" class="recent-posts">
              <div
                v-for="post in stats.recentPosts"
                :key="post.id"
                class="recent-post-item"
                @click="router.push(`/admin/posts/${post.id}/edit`)"
              >
                <span class="post-title">{{ post.title }}</span>
                <span class="post-views">{{ post.viewCount }} {{ t('admin.views') || 'views' }}</span>
              </div>
            </div>
            <div v-else class="no-posts">
              <p>{{ info.noPosts }}</p>
            </div>
          </ElCard>
        </ElCol>
      </ElRow>
    </template>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 20px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0 0 24px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: 12px;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 24px;
}

.actions-row {
  margin-bottom: 20px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}

.action-card {
  cursor: pointer;
  text-align: center;
  border-radius: 12px;
  transition: transform 0.2s;
}

.action-card:hover {
  transform: translateY(-2px);
}

.action-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.action-text {
  font-size: 14px;
  color: #666;
}

.recent-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.view-all-btn {
  cursor: pointer;
  border-radius: 8px;
  font-size: 12px;
  padding: 4px 12px;
}

.recent-posts {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recent-post-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f9f9f9;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.recent-post-item:hover {
  background: #f0f0f0;
}

.post-title {
  font-weight: 500;
  color: #333;
}

.post-views {
  color: #999;
  font-size: 13px;
}

.no-posts {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.no-posts p {
  margin: 0;
}
</style>
