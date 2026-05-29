<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@unhead/vue'
import { ElButton, ElSkeleton, ElEmpty } from 'element-plus'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
import PostCard from '@/components/blog/PostCard.vue'
import ToolCard from '@/components/tools/ToolCard.vue'
import { publicApi, type HomeData } from '@/api'
import { useSiteStore } from '@/stores/site'
import { useWebsiteSchema } from '@/composables/useSeo'
import { useLocale } from '@/composables/useLocale'

const router = useRouter()
const siteStore = useSiteStore()
const { t } = useLocale()

const homeData = ref<HomeData | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

// SEO Schema
const websiteSchema = useWebsiteSchema()

useHead(() => ({
  title: `${siteStore.siteName}`,
  meta: [
    { name: 'description', content: siteStore.siteDescription || 'Personal blog and tools' }
  ],
  script: [{
    type: 'application/ld+json',
    innerHTML: JSON.stringify(websiteSchema.value)
  }]
}))

async function fetchHomeData() {
  loading.value = true
  error.value = null
  try {
    const res = await publicApi.getHome()
    homeData.value = res.data.data
    if (homeData.value.profile) {
      siteStore.updateSettings({
        profile: homeData.value.profile
      })
    }
  } catch (e) {
    error.value = 'Failed to load home data'
  } finally {
    loading.value = false
  }
}

function goToBlog() {
  router.push('/blog')
}

function goToTools() {
  router.push('/tools')
}

onMounted(() => {
  fetchHomeData()
})
</script>

<template>
  <DefaultLayout>
    <Header :title="siteStore.siteName" />
    <main class="home">
      <!-- Hero Section -->
      <section class="hero">
        <ElSkeleton v-if="loading" :rows="5" animated />
        <template v-else-if="homeData?.profile">
          <div class="hero-content">
            <div class="avatar-wrapper">
              <img
                v-if="homeData.profile.avatar"
                :src="homeData.profile.avatar"
                :alt="homeData.profile.name"
                class="avatar"
              />
              <div v-else class="avatar-placeholder">
                {{ homeData.profile.name[0]?.toUpperCase() || 'U' }}
              </div>
            </div>
            <h1 class="hero-name">{{ homeData.profile.name }}</h1>
            <p class="hero-title">{{ homeData.profile.title }}</p>
            <p class="hero-bio">{{ homeData.profile.bio }}</p>
            <div class="hero-actions">
              <ElButton type="primary" size="large" @click="goToBlog">{{ t('home.readBlog') }}</ElButton>
              <ElButton size="large" @click="goToTools">{{ t('home.exploreTools') }}</ElButton>
            </div>
          </div>
        </template>
        <ElEmpty v-else-if="error" :description="error" />
      </section>

      <!-- Latest Posts Section -->
      <section v-if="homeData?.latestPosts?.length" class="section">
        <div class="section-header">
          <h2 class="section-title">{{ t('home.latestPosts') }}</h2>
          <RouterLink to="/blog" class="view-all">{{ t('home.viewAll') }}</RouterLink>
        </div>
        <div class="posts-grid">
          <PostCard v-for="post in homeData.latestPosts.slice(0, 3)" :key="post.id" :post="post" />
        </div>
      </section>

      <!-- Featured Tools Section -->
      <section v-if="homeData?.featuredTools?.length" class="section">
        <div class="section-header">
          <h2 class="section-title">{{ t('home.featuredTools') }}</h2>
          <RouterLink to="/tools" class="view-all">{{ t('home.viewAll') }}</RouterLink>
        </div>
        <div class="tools-grid">
          <ToolCard v-for="tool in homeData.featuredTools.slice(0, 3)" :key="tool.id" :tool="tool" />
        </div>
      </section>
    </main>
    <Footer />
  </DefaultLayout>
</template>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
}

.hero {
  padding: 60px 20px;
  text-align: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  margin-bottom: 40px;
}

.hero-content {
  max-width: 600px;
  margin: 0 auto;
}

.avatar-wrapper {
  margin-bottom: 24px;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #fff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.avatar-placeholder {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: bold;
  color: #667eea;
  margin: 0 auto;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.hero-name {
  font-size: 36px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 8px;
}

.hero-title {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 16px;
}

.hero-bio {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
  margin: 0 0 24px;
}

.hero-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.section {
  padding: 20px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.view-all {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.view-all:hover {
  text-decoration: underline;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

@media (max-width: 768px) {
  .hero {
    padding: 40px 20px;
  }

  .hero-name {
    font-size: 28px;
  }

  .hero-title {
    font-size: 18px;
  }

  .hero-actions {
    flex-direction: column;
  }
}
</style>