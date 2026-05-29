<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useHead } from '@unhead/vue'
import { ElSkeleton, ElProgress, ElTimeline, ElTimelineItem, ElCard } from 'element-plus'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
import { publicApi, type AboutData } from '@/api'
import { useSiteStore } from '@/stores/site'
import { useLocale } from '@/composables/useLocale'

const siteStore = useSiteStore()
const { t } = useLocale()

const aboutData = ref<AboutData | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

useHead({
  title: `${siteStore.siteName}`,
  meta: [
    { name: 'description', content: aboutData.value?.profile?.bio || siteStore.siteDescription || 'About me' }
  ]
})

const skills = computed(() => aboutData.value?.profile?.skills || [])
const timeline = computed(() => aboutData.value?.timeline || [])
const projects = computed(() => aboutData.value?.projects || [])

async function fetchAboutData() {
  loading.value = true
  error.value = null
  try {
    const res = await publicApi.getAbout()
    aboutData.value = res.data.data
  } catch (e) {
    error.value = 'Failed to load about data'
  } finally {
    loading.value = false
  }
}

function getSkillColor(level: number): string {
  if (level >= 80) return '#67c23a'
  if (level >= 60) return '#409eff'
  if (level >= 40) return '#e6a23c'
  return '#909399'
}

onMounted(() => {
  fetchAboutData()
})
</script>

<template>
  <DefaultLayout>
    <Header :title="siteStore.siteName" />
    <main class="about">
      <ElSkeleton v-if="loading" :rows="10" animated />
      <template v-else-if="aboutData?.profile">
        <!-- Profile Section -->
        <section class="profile-section">
          <div class="profile-card">
            <div class="avatar-wrapper">
              <img
                v-if="aboutData.profile.avatar"
                :src="aboutData.profile.avatar"
                :alt="aboutData.profile.name"
                class="avatar"
              />
              <div v-else class="avatar-placeholder">
                {{ aboutData.profile.name[0]?.toUpperCase() || 'U' }}
              </div>
            </div>
            <div class="profile-info">
              <h1 class="name">{{ aboutData.profile.name }}</h1>
              <p class="title">{{ aboutData.profile.title }}</p>
              <p class="bio">{{ aboutData.profile.bio }}</p>
            </div>
          </div>
        </section>

        <!-- Skills Section -->
        <section v-if="skills.length" class="section">
          <h2 class="section-title">{{ t('about.skills') }}</h2>
          <div class="skills-grid">
            <div v-for="skill in skills" :key="skill.name" class="skill-item">
              <div class="skill-header">
                <span class="skill-name">{{ skill.name }}</span>
                <span class="skill-level">{{ skill.level }}%</span>
              </div>
              <ElProgress
                :percentage="skill.level"
                :color="getSkillColor(skill.level)"
                :show-text="false"
                :stroke-width="8"
              />
            </div>
          </div>
        </section>

        <!-- Timeline Section -->
        <section v-if="timeline.length" class="section">
          <h2 class="section-title">{{ t('about.experience') }}</h2>
          <div class="timeline-container">
            <ElTimeline>
              <ElTimelineItem
                v-for="item in timeline"
                :key="item.date + item.title"
                :timestamp="item.date"
                :type="item.type === 'education' ? 'primary' : 'success'"
                placement="top"
              >
                <ElCard class="timeline-card">
                  <h4 class="timeline-title">{{ item.title }}</h4>
                  <p class="timeline-description">{{ item.description }}</p>
                </ElCard>
              </ElTimelineItem>
            </ElTimeline>
          </div>
        </section>

        <!-- Projects Section -->
        <section v-if="projects.length" class="section">
          <h2 class="section-title">{{ t('about.projects') }}</h2>
          <div class="projects-grid">
            <ElCard
              v-for="project in projects"
              :key="project.name"
              class="project-card"
              :body-style="{ padding: '20px' }"
            >
              <h3 class="project-name">{{ project.name }}</h3>
              <p class="project-description">{{ project.description }}</p>
              <div class="project-techs">
                <span v-for="tech in project.techs" :key="tech" class="tech-tag">
                  {{ tech }}
                </span>
              </div>
              <a
                v-if="project.url"
                :href="project.url"
                target="_blank"
                rel="noopener noreferrer"
                class="project-link"
              >
                {{ t('about.viewProject') }}
              </a>
            </ElCard>
          </div>
        </section>
      </template>
    </main>
    <Footer />
  </DefaultLayout>
</template>

<style scoped>
.about {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.profile-section {
  margin-bottom: 40px;
}

.profile-card {
  display: flex;
  gap: 32px;
  align-items: flex-start;
  padding: 32px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.avatar-wrapper {
  flex-shrink: 0;
}

.avatar {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56px;
  font-weight: bold;
  color: #fff;
}

.profile-info {
  flex: 1;
}

.name {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px;
}

.title {
  font-size: 18px;
  color: #409eff;
  margin: 0 0 16px;
}

.bio {
  font-size: 15px;
  color: #666;
  line-height: 1.7;
  margin: 0;
}

.section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0 0 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid #409eff;
}

.skills-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.skill-item {
  background: #fff;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.skill-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.skill-name {
  font-weight: 500;
  color: #333;
}

.skill-level {
  color: #999;
  font-size: 14px;
}

.timeline-container {
  padding: 0 20px;
}

.timeline-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.timeline-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px;
}

.timeline-description {
  font-size: 14px;
  color: #666;
  margin: 0;
  line-height: 1.6;
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.project-card {
  border-radius: 12px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.project-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.project-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px;
}

.project-description {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0 0 16px;
}

.project-techs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.tech-tag {
  background: #f0f0f0;
  color: #666;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.project-link {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
}

.project-link:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .profile-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .name {
    font-size: 24px;
  }

  .skills-grid {
    grid-template-columns: 1fr;
  }
}
</style>