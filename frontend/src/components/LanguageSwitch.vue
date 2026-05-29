<script setup lang="ts">
import { ElDropdown, ElDropdownMenu, ElDropdownItem } from 'element-plus'
import { useLocale } from '@/composables/useLocale'

const { locale, setLocale } = useLocale()

const languages = [
  { code: 'en', label: 'English' },
  { code: 'zh-CN', label: '中文' }
] as const

function handleCommand(command: 'en' | 'zh-CN') {
  setLocale(command)
}
</script>

<template>
  <ElDropdown @command="handleCommand" trigger="click">
    <span class="language-switch">
      <span class="language-icon">🌐</span>
      <span class="language-code">{{ locale === 'zh-CN' ? '中文' : 'EN' }}</span>
    </span>
    <template #dropdown>
      <ElDropdownMenu>
        <ElDropdownItem
          v-for="lang in languages"
          :key="lang.code"
          :command="lang.code"
          :class="{ 'is-active': locale === lang.code }"
        >
          {{ lang.label }}
        </ElDropdownItem>
      </ElDropdownMenu>
    </template>
  </ElDropdown>
</template>

<style scoped>
.language-switch {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.language-switch:hover {
  background-color: #f5f5f5;
}

.language-icon {
  font-size: 16px;
}

.language-code {
  font-size: 14px;
  color: #666;
}

:deep(.el-dropdown-menu__item.is-active) {
  color: #409eff;
  font-weight: 500;
}
</style>