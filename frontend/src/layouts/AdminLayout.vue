<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRouter, useRoute } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { ElMenu, ElMenuItem, ElContainer, ElHeader, ElMain, ElAvatar, ElDropdown, ElDropdownMenu, ElDropdownItem } from 'element-plus'

const router = useRouter()
const route = useRoute()
const { logout, user } = useAuth()

const menuItems = [
  { path: '/admin', label: 'Dashboard', icon: '📊' },
  { path: '/admin/posts', label: 'Posts', icon: '📝' },
  { path: '/admin/categories', label: 'Categories', icon: '📁' },
  { path: '/admin/tags', label: 'Tags', icon: '🏷️' },
  { path: '/admin/comments', label: 'Comments', icon: '💬' },
  { path: '/admin/analytics', label: 'Analytics', icon: '📈' },
  { path: '/admin/settings', label: 'Settings', icon: '⚙️' }
]

const activeMenu = computed(() => route.path)

function handleLogout() {
  logout()
}

function handleMenuSelect(path: string) {
  router.push(path)
}
</script>

<template>
  <ElContainer class="admin-layout">
    <ElHeader class="admin-header">
      <div class="header-left">
        <span class="logo">Admin Panel</span>
      </div>
      <div class="header-right">
        <RouterLink to="/" class="view-site-btn">View Site</RouterLink>
        <ElDropdown @command="handleLogout">
          <span class="user-info">
            <ElAvatar :size="32" style="background-color: #409eff">
              {{ user?.username?.[0]?.toUpperCase() || 'A' }}
            </ElAvatar>
            <span class="username">{{ user?.username || 'Admin' }}</span>
          </span>
          <template #dropdown>
            <ElDropdownMenu>
              <ElDropdownItem command="logout">Logout</ElDropdownItem>
            </ElDropdownMenu>
          </template>
        </ElDropdown>
      </div>
    </ElHeader>
    <ElContainer>
      <aside class="admin-sidebar">
        <ElMenu
          :default-active="activeMenu"
          :router="false"
          @select="handleMenuSelect"
          class="admin-menu"
        >
          <ElMenuItem v-for="item in menuItems" :key="item.path" :index="item.path">
            <span class="menu-icon">{{ item.icon }}</span>
            <span>{{ item.label }}</span>
          </ElMenuItem>
        </ElMenu>
      </aside>
      <ElMain class="admin-main">
        <RouterView />
      </ElMain>
    </ElContainer>
  </ElContainer>
</template>

<style scoped>
.admin-layout {
  height: 100vh;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #2c3e50;
  color: #fff;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  font-size: 18px;
  font-weight: bold;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.view-site-btn {
  color: #fff;
  text-decoration: none;
  font-size: 14px;
  opacity: 0.8;
  transition: opacity 0.3s;
}

.view-site-btn:hover {
  opacity: 1;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-size: 14px;
}

.admin-sidebar {
  width: 220px;
  background: #f5f5f5;
  border-right: 1px solid #e0e0e0;
}

.admin-menu {
  border-right: none;
  background: transparent;
}

.menu-icon {
  margin-right: 8px;
}

.admin-main {
  background: #fff;
  padding: 20px;
  overflow-y: auto;
}
</style>
