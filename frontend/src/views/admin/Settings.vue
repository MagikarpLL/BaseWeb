<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElForm, ElFormItem, ElInput, ElButton, ElTabs, ElTabPane, ElMessage, ElCard, ElSwitch, ElUpload, ElAvatar } from 'element-plus'
import { settingsApi, type SiteSettings } from '@/api'
import { useSiteStore } from '@/stores/site'

const siteStore = useSiteStore()

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
    ElMessage.success('Settings saved successfully')
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
    ElMessage.success('Image uploaded successfully')
  } catch {
    ElMessage.error('Failed to upload image')
  }
  return false
}

onMounted(() => {
  fetchSettings()
})
</script>

<template>
  <div class="settings">
    <div class="header">
      <h1 class="page-title">Settings</h1>
      <ElButton type="primary" :loading="loading" @click="handleSave">Save Changes</ElButton>
    </div>

    <ElCard>
      <ElTabs v-model="activeTab" class="settings-tabs">
        <!-- Basic Settings -->
        <ElTabPane label="Basic" name="basic">
          <ElForm label-position="top" style="max-width: 600px">
            <ElFormItem label="Site Name">
              <ElInput v-model="form.siteName" placeholder="My Blog" />
            </ElFormItem>
            <ElFormItem label="Site Description">
              <ElInput v-model="form.siteDescription" type="textarea" :rows="3" />
            </ElFormItem>
            <ElFormItem label="Allow Comments">
              <ElSwitch v-model="form.allowComments" />
            </ElFormItem>
            <ElFormItem label="Require Approval for Comments">
              <ElSwitch v-model="form.requireApproval" />
            </ElFormItem>
          </ElForm>
        </ElTabPane>

        <!-- Profile Settings -->
        <ElTabPane label="Profile" name="profile">
          <ElForm label-position="top" style="max-width: 600px">
            <ElFormItem label="Name">
              <ElInput v-model="form.profile.name" placeholder="John Doe" />
            </ElFormItem>
            <ElFormItem label="Title">
              <ElInput v-model="form.profile.title" placeholder="Software Developer" />
            </ElFormItem>
            <ElFormItem label="Bio">
              <ElInput v-model="form.profile.bio" type="textarea" :rows="4" />
            </ElFormItem>
            <ElFormItem label="Avatar">
              <div class="avatar-upload">
                <ElAvatar v-if="form.profile.avatar" :src="form.profile.avatar" :size="80" />
                <ElUpload
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="(file: any) => handleImageUpload(file)"
                  accept="image/*"
                >
                  <ElButton size="small">Upload Image</ElButton>
                </ElUpload>
              </div>
            </ElFormItem>
          </ElForm>
        </ElTabPane>

        <!-- Social Settings -->
        <ElTabPane label="Social" name="social">
          <ElForm label-position="top" style="max-width: 600px">
            <ElFormItem label="GitHub">
              <ElInput v-model="form.social.github" placeholder="https://github.com/username" />
            </ElFormItem>
            <ElFormItem label="Twitter">
              <ElInput v-model="form.social.twitter" placeholder="https://twitter.com/username" />
            </ElFormItem>
            <ElFormItem label="Email">
              <ElInput v-model="form.social.email" placeholder="contact@example.com" />
            </ElFormItem>
          </ElForm>
        </ElTabPane>

        <!-- SEO Settings -->
        <ElTabPane label="SEO" name="seo">
          <ElForm label-position="top" style="max-width: 600px">
            <ElFormItem label="SEO Title">
              <ElInput v-model="form.seoTitle" placeholder="My Blog - Personal Website" />
            </ElFormItem>
            <ElFormItem label="SEO Description">
              <ElInput v-model="form.seoDescription" type="textarea" :rows="3" />
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
</style>
