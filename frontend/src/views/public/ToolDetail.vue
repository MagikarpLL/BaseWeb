<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useHead } from '@unhead/vue'
import { ElCard, ElInput, ElButton, ElAlert } from 'element-plus'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
import { publicApi, type Tool } from '@/api'
import { useSiteStore } from '@/stores/site'

const route = useRoute()
const router = useRouter()
const siteStore = useSiteStore()

const tool = ref<Tool | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

// Tool processing state
const input = ref('')
const output = ref('')

// Tool-specific state
const jsonError = ref<string | undefined>(undefined)
const urlError = ref<string | undefined>(undefined)
const base64Error = ref<string | undefined>(undefined)

const currentError = computed(() => jsonError.value || urlError.value || base64Error.value)

const slug = computed(() => route.params.slug as string)

useHead({
  title: () => tool.value ? `${tool.value.name} - Tools - ${siteStore.siteName}` : 'Loading...',
  meta: [
    { name: 'description', content: tool.value?.description || 'Online tool' }
  ]
})

async function fetchTool() {
  loading.value = true
  error.value = null
  try {
    const res = await publicApi.getTools()
    const tools = res.data.data as Tool[]
    tool.value = tools.find(t => t.slug === slug.value) || null
    if (!tool.value) {
      error.value = 'Tool not found'
    }
  } catch {
    error.value = 'Failed to load tool'
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/tools')
}

// JSON Formatter
function formatJson() {
  jsonError.value = undefined
  try {
    const parsed = JSON.parse(input.value)
    output.value = JSON.stringify(parsed, null, 2)
  } catch (e) {
    jsonError.value = (e as Error).message
    output.value = ''
  }
}

function minifyJson() {
  jsonError.value = undefined
  try {
    const parsed = JSON.parse(input.value)
    output.value = JSON.stringify(parsed)
  } catch (e) {
    jsonError.value = (e as Error).message
    output.value = ''
  }
}

// URL Encoder/Decoder
function encodeUrl() {
  urlError.value = undefined
  try {
    output.value = encodeURIComponent(input.value)
  } catch {
    urlError.value = 'Failed to encode URL'
    output.value = ''
  }
}

function decodeUrl() {
  urlError.value = undefined
  try {
    output.value = decodeURIComponent(input.value)
  } catch {
    urlError.value = 'Invalid URL encoding'
    output.value = ''
  }
}

// Base64
function encodeBase64() {
  base64Error.value = undefined
  try {
    output.value = btoa(input.value)
  } catch {
    base64Error.value = 'Failed to encode Base64'
    output.value = ''
  }
}

function decodeBase64() {
  base64Error.value = undefined
  try {
    output.value = atob(input.value)
  } catch {
    base64Error.value = 'Invalid Base64 string'
    output.value = ''
  }
}

// HTML Encoder
function encodeHtml() {
  output.value = input.value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
}

function decodeHtml() {
  output.value = input.value
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&#039;/g, "'")
}

function copyOutput() {
  navigator.clipboard.writeText(output.value)
}

function swapInputOutput() {
  const temp = input.value
  input.value = output.value
  output.value = temp
}

function clearAll() {
  input.value = ''
  output.value = ''
  jsonError.value = undefined
  urlError.value = undefined
  base64Error.value = undefined
}

onMounted(() => {
  fetchTool()
})
</script>

<template>
  <DefaultLayout>
    <Header :title="siteStore.siteName" />
    <main class="tool-detail">
      <ElSkeleton v-if="loading" :rows="8" animated />
      <template v-else-if="error">
        <ElAlert type="error" :title="error" show-icon />
        <div class="back-btn">
          <ElButton @click="goBack">← Back to Tools</ElButton>
        </div>
      </template>
      <template v-else-if="tool">
        <div class="tool-header">
          <div class="tool-icon">{{ tool.icon || '🔧' }}</div>
          <div class="tool-info">
            <h1 class="tool-name">{{ tool.name }}</h1>
            <p class="tool-description">{{ tool.description }}</p>
          </div>
        </div>

        <ElCard class="tool-card">
          <div class="tool-content">
            <!-- Input Section -->
            <div class="input-section">
              <label class="section-label">Input</label>
              <ElInput
                v-model="input"
                type="textarea"
                :rows="8"
                placeholder="Enter text to process..."
                class="io-textarea"
              />
            </div>

            <!-- Action Buttons -->
            <div class="action-buttons">
              <template v-if="tool.slug === 'json-formatter'">
                <ElButton type="primary" @click="formatJson">Format</ElButton>
                <ElButton @click="minifyJson">Minify</ElButton>
              </template>
              <template v-else-if="tool.slug === 'url-encoder'">
                <ElButton type="primary" @click="encodeUrl">Encode URL</ElButton>
                <ElButton @click="decodeUrl">Decode URL</ElButton>
              </template>
              <template v-else-if="tool.slug === 'base64'">
                <ElButton type="primary" @click="encodeBase64">Encode Base64</ElButton>
                <ElButton @click="decodeBase64">Decode Base64</ElButton>
              </template>
              <template v-else-if="tool.slug === 'html-encoder'">
                <ElButton type="primary" @click="encodeHtml">Encode HTML</ElButton>
                <ElButton @click="decodeHtml">Decode HTML</ElButton>
              </template>
              <ElButton @click="swapInputOutput">⇅ Swap</ElButton>
              <ElButton @click="copyOutput">📋 Copy</ElButton>
              <ElButton @click="clearAll">🗑️ Clear</ElButton>
            </div>

            <!-- Error Display -->
            <ElAlert
              v-if="currentError"
              type="error"
              :title="currentError"
              show-icon
              closable
              class="error-alert"
            />

            <!-- Output Section -->
            <div class="output-section">
              <label class="section-label">Output</label>
              <ElInput
                v-model="output"
                type="textarea"
                :rows="8"
                readonly
                placeholder="Output will appear here..."
                class="io-textarea"
              />
            </div>
          </div>
        </ElCard>

        <div class="back-btn">
          <ElButton text @click="goBack">← Back to Tools</ElButton>
        </div>
      </template>
    </main>
    <Footer />
  </DefaultLayout>
</template>

<style scoped>
.tool-detail {
  max-width: 900px;
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

.section-label {
  display: block;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  font-size: 14px;
}

.io-textarea {
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 14px;
}

.io-textarea :deep(textarea) {
  font-family: 'Fira Code', 'Consolas', monospace;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 20px 0;
  justify-content: center;
}

.error-alert {
  margin: 16px 0;
}

.output-section {
  margin-top: 20px;
}

.back-btn {
  margin-top: 24px;
  text-align: center;
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

  .action-buttons {
    justify-content: center;
  }
}
</style>
