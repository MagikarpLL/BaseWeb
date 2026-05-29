<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import type { Tag as ApiTag } from '@/api'

export interface Tag extends ApiTag {
  postCount?: number
}

const props = defineProps<{
  tags: Tag[]
}>()

const emit = defineEmits<{
  (e: 'click', tag: Tag): void
}>()

const router = useRouter()
const isMobile = ref(window.innerWidth <= 768)

const displayTags = computed(() => {
  if (isMobile.value) {
    return [...props.tags]
      .sort((a, b) => (b.postCount || 0) - (a.postCount || 0))
      .slice(0, 10)
  }
  return props.tags
})

function calculateFontSize(postCount: number): number {
  const minSize = 12
  const maxSize = 28
  const maxPostCount = Math.max(...props.tags.map(t => t.postCount || 0), 1)
  return minSize + ((postCount || 0) / maxPostCount) * (maxSize - minSize)
}

function handleClick(tag: Tag) {
  router.push(`/blog?tag=${tag.slug}`)
  emit('click', tag)
}
</script>

<template>
  <div class="tag-cloud" v-if="tags.length > 0">
    <span
      v-for="tag in displayTags"
      :key="tag.id"
      class="tag-item"
      :style="{ fontSize: calculateFontSize(tag.postCount || 0) + 'px' }"
      :title="`${tag.postCount || 0} 篇文章`"
      @click="handleClick(tag)"
    >
      {{ tag.name }}
    </span>
  </div>
  <div v-else class="no-tags">暂无标签</div>
</template>

<style scoped>
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  padding: 16px;
}

.tag-item {
  color: var(--el-color-primary);
  cursor: pointer;
  transition: transform 0.2s, color 0.2s;
  padding: 4px 8px;
}

.tag-item:hover {
  transform: scale(1.1);
  color: var(--el-color-primary-light-3);
}

.no-tags {
  color: #999;
  font-size: 14px;
  text-align: center;
  padding: 16px;
}
</style>