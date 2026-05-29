<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useHead } from '@unhead/vue'
import { ElButton } from 'element-plus'
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
import { useSiteStore } from '@/stores/site'

const router = useRouter()
const siteStore = useSiteStore()

useHead({
  title: `404 Not Found - ${siteStore.siteName}`,
  meta: [
    { name: 'description', content: 'Page not found' }
  ]
})

function goHome() {
  router.push('/')
}

function goBlog() {
  router.push('/blog')
}
</script>

<template>
  <div class="not-found-page">
    <Header :title="siteStore.siteName" />
    <main class="not-found-content">
      <div class="error-container">
        <h1 class="error-code">404</h1>
        <h2 class="error-title">Page Not Found</h2>
        <p class="error-message">
          The page you're looking for doesn't exist or has been moved.
        </p>
        <div class="error-actions">
          <ElButton type="primary" size="large" @click="goHome">
            Go Home
          </ElButton>
          <ElButton size="large" @click="goBlog">
            Go Blog
          </ElButton>
        </div>
      </div>
    </main>
    <Footer />
  </div>
</template>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

.not-found-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.error-container {
  text-align: center;
  max-width: 500px;
}

.error-code {
  font-size: 160px;
  font-weight: 800;
  margin: 0;
  line-height: 1;
  background: linear-gradient(135deg, #409eff 0%, #79bbff 50%, #a0cfff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 60px rgba(64, 158, 255, 0.4);
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.error-title {
  font-size: 32px;
  font-weight: 600;
  color: #fff;
  margin: 16px 0;
}

.error-message {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
  margin: 0 0 32px;
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

@media (max-width: 768px) {
  .error-code {
    font-size: 100px;
  }

  .error-title {
    font-size: 24px;
  }

  .error-message {
    font-size: 16px;
  }

  .error-actions {
    flex-direction: column;
    gap: 12px;
  }
}
</style>