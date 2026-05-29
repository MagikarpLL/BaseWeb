<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElForm, ElFormItem, ElInput, ElButton, ElTabs, ElTabPane, ElMessage, ElCard, ElSwitch, ElUpload, ElAvatar, ElSelect, ElOption } from 'element-plus'
import { settingsApi, type SiteSettings } from '@/api'
import { useSiteStore } from '@/stores/site'
import { useLocale } from '@/composables/useLocale'

const siteStore = useSiteStore()
const { t, locale, setLocale } = useLocale()

const loading = ref(false)
const activeTab = ref('basic')

const form = reactive<SiteSettings>({
  siteName: '',
  siteDescription: '',
  seoTitle: '',
  seoDescription: '',
  allowComments: true,
  requireApproval: false,
  profile: {
    name: '',
    title: '',
    bio: '',
    avatar: ''
  },
  social: {},
  about: {
    skills: [],
    timeline: [],
    projects: []
  }
})

const languages = [
  { value: 'en', label: 'English' },
  { value: 'zh-CN', label: '中文' }
] as const

async function fetchSettings() {
  loading.value = true
  try {
    const res = await settingsApi.get()
    Object.assign(form, res.data.data)
    siteStore.setSettings(res.data.data)
  } catch {
    // Use default settings
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  loading.value = true
  try {
    await settingsApi.update(form)
    siteStore.setSettings(form)
    ElMessage.success(t('common.success'))
  } catch {
    // Error handled by API interceptor
  } finally {
    loading.value = false
  }
}

async function handleImageUpload(options: { file: File }) {
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const res = await settingsApi.uploadImage(formData)
    form.profile.avatar = res.data.data.url
    ElMessage.success(t('common.success'))
  } catch {
    ElMessage.error(t('common.error'))
  }
  return false
}

function handleLanguageChange(newLocale: 'en' | 'zh-CN') {
  setLocale(newLocale)
  ElMessage.success(t('common.success'))
}

onMounted(() => {
  fetchSettings()
})
</script>

<template>
  <div class="settings">
    <div class="header">
      <h1 class="page-title">{{ t('admin.settings') }}</h1>
      <ElButton type="primary" :loading="loading" @click="handleSave">{{ t('common.save') }}</ElButton>
    </div>

    <ElCard>
      <ElTabs v-model="activeTab" class="settings-tabs">
        <!-- Basic Settings -->
        <ElTabPane :label="t('admin.generalSettings')" name="basic">
          <ElForm label-position="top" style="max-width: 600px">
            <ElFormItem :label="t('admin.siteName')">
              <ElInput v-model="form.siteName" placeholder="My Blog" />
            </ElFormItem>
            <ElFormItem :label="t('admin.siteDescription')">
              <ElInput v-model="form.siteDescription" type="textarea" :rows="3" />
            </ElFormItem>
            <ElFormItem :label="t('admin.allowComments')">
              <ElSwitch v-model="form.allowComments" />
            </ElFormItem>
            <ElFormItem :label="t('admin.requireApproval')">
              <ElSwitch v-model="form.requireApproval" />
            </ElFormItem>
          </ElForm>
        </ElTabPane>

        <!-- Profile Settings -->
        <ElTabPane :label="t('admin.profileSettings')" name="profile">
          <ElForm label-position="top" style="max-width: 600px">
            <ElFormItem :label="t('admin.name')">
              <ElInput v-model="form.profile.name" placeholder="John Doe" />
            </ElFormItem>
            <ElFormItem :label="t('admin.jobTitle')">
              <ElInput v-model="form.profile.title" placeholder="Software Developer" />
            </ElFormItem>
            <ElFormItem :label="t('admin.bio')">
              <ElInput v-model="form.profile.bio" type="textarea" :rows="4" />
            </ElFormItem>
            <ElFormItem :label="t('admin.avatar')">
              <div class="avatar-upload">
                <ElAvatar v-if="form.profile.avatar" :src="form.profile.avatar" :size="80" />
                <ElUpload
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="(file: any) => handleImageUpload(file)"
                  accept="image/*"
                >
                  <ElButton size="small">{{ t('common.upload') }}</ElButton>
                </ElUpload>
              </div>
            </ElFormItem>
          </ElForm>
        </ElTabPane>

        <!-- Social Settings -->
        <ElTabPane :label="t('admin.socialSettings')" name="social">
          <ElForm label-position="top" style="max-width: 600px">
            <ElFormItem :label="t('admin.github')">
              <ElInput v-model="form.social.github" placeholder="https://github.com/username" />
            </ElFormItem>
            <ElFormItem label="Twitter">
              <ElInput v-model="form.social.twitter" placeholder="https://twitter.com/username" />
            </ElFormItem>
            <ElFormItem :label="t('admin.email')">
              <ElInput v-model="form.social.email" placeholder="contact@example.com" />
            </ElFormItem>
          </ElForm>
        </ElTabPane>

        <!-- SEO Settings -->
        <ElTabPane label="SEO" name="seo">
          <ElForm label-position="top" style="max-width: 600px">
            <ElFormItem :label="t('admin.metaTitle')">
              <ElInput v-model="form.seoTitle" placeholder="My Blog - Personal Website" />
            </ElFormItem>
            <ElFormItem :label="t('admin.metaDescription')">
              <ElInput v-model="form.seoDescription" type="textarea" :rows="3" />
            </ElFormItem>
          </ElForm>
        </ElTabPane>

        <!-- Language Settings -->
        <ElTabPane :label="t('admin.languageSettings')" name="language">
          <ElForm label-position="top" style="max-width: 600px">
            <ElFormItem :label="t('admin.currentLanguage')">
              <div class="language-info">
                <p class="language-desc">{{ t('admin.languageDescription') }}</p>
                <ElSelect :model-value="locale" @change="handleLanguageChange" style="width: 200px">
                  <ElOption
                    v-for="lang in languages"
                    :key="lang.value"
                    :label="lang.label"
                    :value="lang.value"
                  />
                </ElSelect>
              </div>
            </ElFormItem>
          </ElForm>
        </ElTabPane>
      </ElTabs>
    </ElCard>
  </div>
</template>

<style scoped>
.settings {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.settings-tabs {
  margin-top: 16px;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}

.language-info {
  padding: 16px 0;
}

.language-desc {
  margin: 0 0 16px;
  color: #666;
  font-size: 14px;
}
</style>