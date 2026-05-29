<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElForm, ElFormItem, ElInput, ElButton, ElSelect, ElOption, ElMessage, ElCard } from 'element-plus'
import { marked } from 'marked'
import { adminPostApi, categoryApi, tagApi, type Category, type Tag, type PostFormData } from '@/api'
import { useLocale } from '@/composables/useLocale'

const { t } = useLocale()

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const postId = computed(() => route.params.id as string | undefined)

const formRef = ref()
const loading = ref(false)
const fetchingPost = ref(false)

const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])

const form = reactive<PostFormData>({
  title: '',
  slug: '',
  excerpt: '',
  content: '',
  coverImage: '',
  status: 'draft',
  isTop: false,
  categoryId: 0,
  tagIds: []
})

const rules = {
  title: [
    { required: true, message: info.value.pleaseEnterTitle, trigger: 'blur' },
    { min: 2, max: 200, message: info.value.titleLengthMessage, trigger: 'blur' }
  ],
  content: [
    { required: true, message: info.value.pleaseEnterContent, trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: info.value.pleaseSelectCategory, trigger: 'change' }
  ]
}

const previewContent = computed(() => {
  if (!form.content) return ''
  return marked(form.content)
})

// Translations
const info = computed(() => ({
  createPost: t('admin.createPost'),
  editPost: t('admin.editPost'),
  cancel: t('common.cancel'),
  update: t('common.edit') || 'Update',
  create: t('common.add') || 'Create',
  title: t('admin.title'),
  slug: t('admin.slug'),
  generateSlug: t('admin.generateSlug') || 'Generate',
  excerpt: t('admin.excerpt'),
  content: t('admin.content'),
  contentMarkdown: t('admin.contentMarkdown') || 'Content (Markdown)',
  publish: t('admin.publish'),
  draft: t('admin.draft'),
  published: t('admin.published'),
  pinToTop: t('admin.pinToTop') || 'Pin to top',
  category: t('admin.category'),
  tags: t('admin.tags'),
  coverImage: t('admin.featuredImage'),
  preview: t('admin.preview'),
  pleaseEnterTitle: t('admin.pleaseEnterTitle') || 'Please enter title',
  titleLengthMessage: t('admin.titleLengthMessage') || 'Title must be 2-200 characters',
  pleaseEnterContent: t('admin.pleaseEnterContent') || 'Please enter content',
  pleaseSelectCategory: t('admin.pleaseSelectCategory') || 'Please select category',
  postUpdated: t('admin.postUpdated') || 'Post updated successfully',
  postCreated: t('admin.postCreated') || 'Post created successfully'
}))

async function fetchCategories() {
  try {
    const res = await categoryApi.getAll()
    categories.value = res.data.data
  } catch {
    categories.value = []
  }
}

async function fetchTags() {
  try {
    const res = await tagApi.getAll()
    tags.value = res.data.data
  } catch {
    tags.value = []
  }
}

async function fetchPost() {
  if (!postId.value) return
  fetchingPost.value = true
  try {
    const res = await adminPostApi.getPost(Number(postId.value))
    const post = res.data.data as any
    form.title = post.title
    form.slug = post.slug
    form.excerpt = post.excerpt || ''
    form.content = post.content
    form.coverImage = post.coverImage || ''
    form.status = post.status
    form.isTop = post.isTop || false
    form.categoryId = post.categoryId
    form.tagIds = post.tags?.map((t: Tag) => t.id) || []
  } catch {
    ElMessage.error('Failed to load post')
  } finally {
    fetchingPost.value = false
  }
}

function generateSlug() {
  form.slug = form.title
    .toLowerCase()
    .replace(/[^a-z0-9\u4e00-\u9fa5]+/g, '-')
    .replace(/^-|-$/g, '')
}

async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    if (isEdit.value && postId.value) {
      await adminPostApi.updatePost(Number(postId.value), form)
      ElMessage.success('Post updated successfully')
    } else {
      await adminPostApi.createPost(form)
      ElMessage.success('Post created successfully')
    }

    router.push('/admin/posts')
  } catch {
    // Error handled by API interceptor
  } finally {
    loading.value = false
  }
}

function handleCancel() {
  router.back()
}

onMounted(async () => {
  await fetchCategories()
  await fetchTags()
  if (isEdit.value) {
    await fetchPost()
  }
})
</script>

<template>
  <div class="post-edit">
    <div class="header">
      <h1 class="page-title">{{ isEdit ? info.editPost : info.createPost }}</h1>
      <div class="header-actions">
        <ElButton @click="handleCancel">{{ info.cancel }}</ElButton>
        <ElButton type="primary" :loading="loading" @click="handleSubmit">
          {{ isEdit ? info.update : info.create }}
        </ElButton>
      </div>
    </div>

    <div class="edit-layout">
      <div class="main-form">
        <ElCard>
          <ElForm
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
          >
            <ElFormItem :label="info.title" prop="title">
              <ElInput
                v-model="form.title"
                :placeholder="info.pleaseEnterTitle || 'Enter post title'"
                size="large"
                @blur="!form.slug && generateSlug()"
              />
            </ElFormItem>

            <ElFormItem :label="info.slug" prop="slug">
              <ElInput
                v-model="form.slug"
                placeholder="post-url-slug"
              >
                <template #append>
                  <ElButton @click="generateSlug">{{ info.generateSlug }}</ElButton>
                </template>
              </ElInput>
            </ElFormItem>

            <ElFormItem :label="info.excerpt" prop="excerpt">
              <ElInput
                v-model="form.excerpt"
                type="textarea"
                :rows="3"
                :placeholder="info.pleaseEnterTitle || 'Brief description for list view'"
              />
            </ElFormItem>

            <ElFormItem :label="info.contentMarkdown" prop="content">
              <ElInput
                v-model="form.content"
                type="textarea"
                :rows="15"
                placeholder="Write your content in Markdown..."
                class="content-textarea"
              />
            </ElFormItem>
          </ElForm>
        </ElCard>
      </div>

      <div class="side-panel">
        <ElCard>
          <template #header>
            <span>{{ info.publish }}</span>
          </template>
          <ElForm label-position="top">
            <ElFormItem :label="info.status">
              <ElSelect v-model="form.status" style="width: 100%">
                <ElOption :label="info.draft" value="draft" />
                <ElOption :label="info.published" value="published" />
              </ElSelect>
            </ElFormItem>
            <ElFormItem>
              <label class="top-checkbox">
                <input type="checkbox" v-model="form.isTop" />
                <span>{{ info.pinToTop }}</span>
              </label>
            </ElFormItem>
          </ElForm>
          <div class="publish-actions">
            <ElButton type="primary" :loading="loading" @click="handleSubmit" style="width: 100%">
              {{ isEdit ? info.update : info.publish }}
            </ElButton>
          </div>
        </ElCard>

        <ElCard>
          <template #header>
            <span>{{ info.category }}</span>
          </template>
          <ElForm label-position="top">
            <ElFormItem prop="categoryId">
              <ElSelect v-model="form.categoryId" :placeholder="info.pleaseSelectCategory" style="width: 100%">
                <ElOption
                  v-for="cat in categories"
                  :key="cat.id"
                  :label="cat.name"
                  :value="cat.id"
                />
              </ElSelect>
            </ElFormItem>
          </ElForm>
        </ElCard>

        <ElCard>
          <template #header>
            <span>{{ info.coverImage }}</span>
          </template>
          <ElForm label-position="top">
            <ElFormItem :label="t('admin.imageUrl') || 'Image URL'">
              <ElInput v-model="form.coverImage" placeholder="https://..." />
            </ElFormItem>
          </ElForm>
        </ElCard>

        <ElCard>
          <template #header>
            <span>{{ info.tags }}</span>
          </template>
          <ElForm label-position="top">
            <ElFormItem>
              <ElSelect
                v-model="form.tagIds"
                multiple
                :placeholder="t('admin.selectTags') || 'Select tags'"
                style="width: 100%"
              >
                <ElOption
                  v-for="tag in tags"
                  :key="tag.id"
                  :label="tag.name"
                  :value="tag.id"
                />
              </ElSelect>
            </ElFormItem>
          </ElForm>
        </ElCard>

        <!-- Preview Card -->
        <ElCard>
          <template #header>
            <span>{{ info.preview }}</span>
          </template>
          <div class="preview-content markdown-body" v-html="previewContent"></div>
        </ElCard>
      </div>
    </div>
  </div>
</template>

<style scoped>
.post-edit {
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

.header-actions {
  display: flex;
  gap: 12px;
}

.edit-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
}

.main-form {
  min-width: 0;
}

.side-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.content-textarea :deep(textarea) {
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 14px;
}

.publish-actions {
  margin-top: 16px;
}

.top-checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
}

.top-checkbox input {
  width: 16px;
  height: 16px;
}

.preview-content {
  max-height: 400px;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.6;
}

.preview-content:deep(h1),
.preview-content:deep(h2),
.preview-content:deep(h3) {
  margin-top: 16px;
  margin-bottom: 8px;
}

.preview-content:deep(p) {
  margin-bottom: 12px;
}

.preview-content:deep(code) {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
}

@media (max-width: 1024px) {
  .edit-layout {
    grid-template-columns: 1fr;
  }
}
</style>
