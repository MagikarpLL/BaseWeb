<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useHead } from '@unhead/vue'
import { ElCard, ElButton, ElAlert } from 'element-plus'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
import { useSiteStore } from '@/stores/site'

const siteStore = useSiteStore()

// Current generated UUID
const currentUuid = ref('')
const history = ref<string[]>([])
const copiedId = ref<string | null>(null)

// Generate UUID v4
function generateUuid(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

function generateAndSave() {
  const uuid = generateUuid()
  currentUuid.value = uuid
  
  // Add to history (max 10)
  history.value.unshift(uuid)
  if (history.value.length > 10) {
    history.value = history.value.slice(0, 10)
  }
  
  // Save to session storage
  sessionStorage.setItem('uuid-history', JSON.stringify(history.value))
  copiedId.value = null
}

function copyToClipboard(text: string, id?: string) {
  navigator.clipboard.writeText(text)
  copiedId.value = id || 'current'
  setTimeout(() => {
    copiedId.value = null
  }, 2000)
}

function clearHistory() {
  history.value = []
  sessionStorage.removeItem('uuid-history')
}

// Load history from session storage
onMounted(() => {
  const saved = sessionStorage.getItem('uuid-history')
  if (saved) {
    try {
      history.value = JSON.parse(saved)
    } catch {
      history.value = []
    }
  }
  generateAndSave()
})

useHead({
  title: `UUID Generator - Tools - ${siteStore.siteName}`,
  meta: [
    { name: 'description', content: 'Generate RFC 4122 compliant UUID v4' }
  ]
})
</script>

<template>
  <DefaultLayout>
    <Header :title="siteStore.siteName" />
    <main class="tool-detail">
      <div class="tool-header">
        <div class="tool-icon">🎲</div>
        <div class="tool-info">
          <h1 class="tool-name">UUID Generator</h1>
          <p class="tool-description">Generate RFC 4122 compliant UUID v4</p>
        </div>
      </div>

      <ElCard class="tool-card">
        <div class="tool-content">
          <!-- Current UUID -->
          <div class="uuid-display" :class="{ copied: copiedId === 'current' }">
            <code class="uuid-text">{{ currentUuid }}</code>
            <div class="uuid-actions">
              <ElButton @click="copyToClipboard(currentUuid, 'current')">
                {{ copiedId === 'current' ? '✓ Copied' : '📋 Copy' }}
              </ElButton>
              <ElButton type="primary" @click="generateAndSave">
                🔄 Generate
              </ElButton>
            </div>
          </div>

          <!-- Version Badge -->
          <div class="version-badge">UUID v4</div>

          <!-- History Section -->
          <div class="history-section" v-if="history.length > 0">
            <div class="history-header">
              <h3 class="history-title">History</h3>
              <ElButton size="small" text @click="clearHistory">Clear</ElButton>
            </div>
            <div class="history-list">
              <div
                v-for="(uuid, index) in history"
                :key="index"
                class="history-item"
                :class="{ copied: copiedId === `history-${index}` }"
                @click="copyToClipboard(uuid, `history-${index}`)"
              >
                <code class="history-uuid">{{ uuid }}</code>
                <span class="copy-indicator">{{ copiedId === `history-${index}` ? '✓' : '📋' }}</span>
              </div>
            </div>
          </div>

          <!-- Info -->
          <ElAlert
            type="info"
            title="Click any UUID in history to copy it"
            :closable="false"
            class="info-alert"
          />
        </div>
      </ElCard>
    </main>
    <Footer />
  </DefaultLayout>
</template>

<style scoped>
.tool-detail {
  max-width: 700px;
  margin: 0 auto;
}

.tool-header {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  margin-bottom: 32px;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: #fff;
}

.tool-icon {
  font-size: 56px;
  line-height: 1;
}

.tool-info {
  flex: 1;
}

.tool-name {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
}

.tool-description {
  font-size: 16px;
  margin: 0;
  opacity: 0.9;
}

.tool-card {
  border-radius: 12px;
}

.tool-content {
  padding: 8px;
}

.uuid-display {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 2px solid transparent;
  transition: border-color 0.3s;
}

.uuid-display.copied {
  border-color: #22c55e;
}

.uuid-text {
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 22px;
  word-break: break-all;
  color: #333;
  text-align: center;
}

.uuid-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.version-badge {
  display: inline-block;
  margin: 16px auto 0;
  padding: 4px 12px;
  background: #e0e0e0;
  color: #666;
  font-size: 12px;
  border-radius: 12px;
  text-align: center;
}

.history-section {
  margin-top: 28px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.history-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s, border-color 0.2s;
  border: 1px solid transparent;
}

.history-item:hover {
  background: #f0f0f0;
  border-color: #ddd;
}

.history-item.copied {
  border-color: #22c55e;
  background: #f0fdf4;
}

.history-uuid {
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 14px;
  color: #666;
}

.copy-indicator {
  font-size: 14px;
}

.info-alert {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .tool-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .tool-name {
    font-size: 24px;
  }

  .uuid-text {
    font-size: 16px;
  }
}
</style>