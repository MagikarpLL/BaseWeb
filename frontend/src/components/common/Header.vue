<script setup lang="ts">
import { RouterLink, useRoute } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { useLocale } from '@/composables/useLocale'
import { ElAvatar, ElDropdown, ElDropdownMenu, ElDropdownItem } from 'element-plus'
import LanguageSwitch from '@/components/LanguageSwitch.vue'

defineProps<{
  title: string
}>()

const { isAuthenticated, user, logout } = useAuth()
const { t } = useLocale()
const route = useRoute()

const navLinks = [
  { path: '/', labelKey: 'nav.home' },
  { path: '/blog', labelKey: 'nav.blog' },
  { path: '/tools', labelKey: 'nav.tools' },
  { path: '/about', labelKey: 'nav.about' }
]

const isActive = (path: string) => route.path === path

function handleLogout() {
  logout()
}
</script>

<template>
  <header class="site-header">
    <div class="header-content">
      <RouterLink to="/" class="site-title">{{ title }}</RouterLink>
      <nav class="nav-links">
        <RouterLink
          v-for="link in navLinks"
          :key="link.path"
          :to="link.path"
          class="nav-link"
          :class="{ active: isActive(link.path) }"
        >
          {{ t(link.labelKey) }}
        </RouterLink>
      </nav>
      <div class="header-actions">
        <LanguageSwitch />
        <template v-if="isAuthenticated">
          <ElDropdown @command="handleLogout">
            <span class="user-dropdown">
              <ElAvatar :size="32" style="background-color: #409eff">
                {{ user?.username?.[0]?.toUpperCase() || 'A' }}
              </ElAvatar>
            </span>
            <template #dropdown>
              <ElDropdownMenu>
                <ElDropdownItem command="dashboard">
                  <RouterLink to="/admin">{{ t('nav.dashboard') }}</RouterLink>
                </ElDropdownItem>
                <ElDropdownItem command="logout">{{ t('nav.logout') }}</ElDropdownItem>
              </ElDropdownMenu>
            </template>
          </ElDropdown>
        </template>
        <template v-else>
          <RouterLink to="/login" class="login-link">{{ t('nav.login') }}</RouterLink>
        </template>
      </div>
    </div>
  </header>
</template>

<style scoped>
.site-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.site-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  text-decoration: none;
}

.nav-links {
  display: flex;
  gap: 32px;
}

.nav-link {
  color: #666;
  text-decoration: none;
  font-size: 16px;
  transition: color 0.3s;
  padding: 4px 0;
  border-bottom: 2px solid transparent;
}

.nav-link:hover,
.nav-link.active {
  color: #409eff;
  border-bottom-color: #409eff;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-dropdown {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.login-link {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 16px;
  }

  .nav-links {
    gap: 20px;
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>