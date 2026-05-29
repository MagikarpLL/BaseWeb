<script setup lang="ts">
import { RouterLink } from 'vue-router'

interface Tool {
  id: number
  name: string
  slug: string
  description: string
  icon: string
  category: string
}

defineProps<{
  tool: Tool
}>()
</script>

<template>
  <RouterLink :to="`/tools/${tool.slug}`" class="tool-card">
    <div class="tool-icon">{{ tool.icon || '🔧' }}</div>
    <h3 class="tool-name">{{ tool.name }}</h3>
    <p class="tool-description">{{ tool.description }}</p>
    <span class="tool-category">{{ tool.category }}</span>
  </RouterLink>
</template>

<style scoped>
.tool-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 20px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  text-decoration: none;
  transition: transform 0.3s, box-shadow 0.3s;
}

.tool-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.tool-icon {
  font-size: 48px;
  margin-bottom: 16px;
  line-height: 1;
}

.tool-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px;
  text-align: center;
}

.tool-description {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 16px;
  text-align: center;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.tool-category {
  display: inline-block;
  padding: 4px 12px;
  background: #f0f0f0;
  color: #666;
  font-size: 12px;
  border-radius: 16px;
}
</style>
