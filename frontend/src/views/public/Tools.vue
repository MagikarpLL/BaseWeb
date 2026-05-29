<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useHead } from '@unhead/vue'
import { ElSkeleton, ElTabs, ElTabPane } from 'element-plus'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
import ToolCard from '@/components/tools/ToolCard.vue'
import { publicApi, type Tool } from '@/api'
import { useSiteStore } from '@/stores/site'
import { useLocale } from '@/composables/useLocale'

const siteStore = useSiteStore()
const { t } = useLocale()

// Static tools not from API
const staticTools: Tool[] = [
  { id: 1001, name: 'Timestamp Converter', slug: 'timestamp-converter', description: 'Convert between Unix timestamp and human-readable date', icon: '🕐', category: 'Converter' },
  { id: 1002, name: 'Password Generator', slug: 'password-generator', description: 'Generate secure random passwords with customizable options', icon: '🔐', category: 'Generator' },
  { id: 1003, name: 'UUID Generator', slug: 'uuid-generator', description: 'Generate RFC 4122 compliant UUID v4', icon: '🎲', category: 'Generator' },
  { id: 1004, name: 'Color Converter', slug: 'color-converter', description: 'Convert between HEX, RGB, and HSL color formats', icon: '🎨', category: 'Converter' },
  { id: 1005, name: 'Cron Parser', slug: 'cron-parser', description: 'Parse and explain cron expressions with next execution times', icon: '⏰', category: 'Parser' }
]

const tools = ref<Tool[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const activeCategory = ref<string>('all')

useHead({
  title: `${t('tools.title')} - ${siteStore.siteName}`,
  meta: [
    { name: 'description', content: 'Free online tools for developers' }
  ]
})

const categories = computed(() => {
  const cats = new Set(tools.value.map(t => t.category))
  return ['all', ...Array.from(cats)]
})

const filteredTools = computed(() => {
  if (activeCategory.value === 'all') return tools.value
  return tools.value.filter(t => t.category === activeCategory.value)
})

async function fetchTools() {
  loading.value = true
  error.value = null
  try {
    const res = await publicApi.getTools()
    // Combine API tools with static tools
    tools.value = [...staticTools, ...res.data.data]
  } catch (e) {
    // Fallback to static tools only on error
    tools.value = staticTools
    error.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchTools()
})
</script>

<template>
  <DefaultLayout>
    <Header :title="siteStore.siteName" />
    <main class="tools-page">
      <div class="page-header">
        <h1 class="page-title">{{ t('tools.title') }}</h1>
        <p class="page-description">{{ t('tools.description') }}</p>
      </div>

      <ElSkeleton v-if="loading" :rows="6" animated />
      <template v-else>
        <ElTabs v-model="activeCategory" class="category-tabs">
          <ElTabPane
            v-for="cat in categories"
            :key="cat"
            :label="cat === 'all' ? t('tools.all') : cat"
            :name="cat"
          />
        </ElTabs>

        <div v-if="filteredTools.length" class="tools-grid">
          <ToolCard v-for="tool in filteredTools" :key="tool.id" :tool="tool" />
        </div>
        <div v-else class="no-tools">
          <p>{{ t('blog.noResults') }}</p>
        </div>
      </template>
    </main>
    <Footer />
  </DefaultLayout>
</template>

<style scoped>
.tools-page {
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

.category-tabs {
  margin-bottom: 32px;
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.no-tools {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}
</style>