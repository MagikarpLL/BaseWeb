import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/public/Home.vue')
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/public/About.vue')
  },
  {
    path: '/blog',
    name: 'BlogList',
    component: () => import('@/views/public/BlogList.vue')
  },
  {
    path: '/blog/:slug',
    name: 'BlogDetail',
    component: () => import('@/views/public/BlogDetail.vue')
  },
  {
    path: '/tools',
    name: 'Tools',
    component: () => import('@/views/public/Tools.vue')
  },
  {
    path: '/tools/:slug',
    name: 'ToolDetail',
    component: () => import('@/views/public/ToolDetail.vue')
  },
  {
    path: '/tools/timestamp-converter',
    name: 'TimestampConverter',
    component: () => import('@/views/public/tools/TimestampConverter.vue')
  },
  {
    path: '/tools/password-generator',
    name: 'PasswordGenerator',
    component: () => import('@/views/public/tools/PasswordGenerator.vue')
  },
  {
    path: '/tools/uuid-generator',
    name: 'UuidGenerator',
    component: () => import('@/views/public/tools/UuidGenerator.vue')
  },
  {
    path: '/tools/color-converter',
    name: 'ColorConverter',
    component: () => import('@/views/public/tools/ColorConverter.vue')
  },
  {
    path: '/tools/cron-parser',
    name: 'CronParser',
    component: () => import('@/views/public/tools/CronParser.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/public/Login.vue')
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'posts',
        name: 'PostList',
        component: () => import('@/views/admin/PostList.vue')
      },
      {
        path: 'posts/new',
        name: 'PostCreate',
        component: () => import('@/views/admin/PostEdit.vue')
      },
      {
        path: 'posts/:id/edit',
        name: 'PostEdit',
        component: () => import('@/views/admin/PostEdit.vue')
      },
      {
        path: 'posts/:id/history',
        name: 'PostHistory',
        component: () => import('@/views/admin/PostHistory.vue')
      },
      {
        path: 'categories',
        name: 'CategoryManage',
        component: () => import('@/views/admin/CategoryManage.vue')
      },
      {
        path: 'tags',
        name: 'TagManage',
        component: () => import('@/views/admin/TagManage.vue')
      },
      {
        path: 'comments',
        name: 'CommentManage',
        component: () => import('@/views/admin/CommentManage.vue')
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/admin/Settings.vue')
      },
      {
        path: 'analytics',
        name: 'Analytics',
        component: () => import('@/views/admin/Analytics.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/public/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.name === 'Login' && authStore.isAuthenticated) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router