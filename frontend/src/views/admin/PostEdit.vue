<script setup lang="ts">
import { ref, reactive, onMounted, computed, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElForm, ElFormItem, ElInput, ElButton, ElSelect, ElOption, ElMessage, ElCard } from 'element-plus'
import { marked } from 'marked'
import { adminPostApi, categoryApi, tagApi, settingsApi, type Category, type Tag, type PostFormData } from '@/api'
import { useLocale } from '@/composables/useLocale'

const { t } = useLocale()

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const postId = computed(() => route.params.id as string | undefined)

const formRef = ref()
const loading = ref(false)
const fetchingPost = ref(false)

// Image upload states
const isCompressing = ref(false)
const isUploading = ref(false)
const uploadProgress = ref(0)
const uploadStatus = ref<'idle' | 'compressing' | 'uploading' | 'success' | 'error'>('idle')
const statusMessage = ref('')

// Editor area ref for drag/drop
const editorAreaRef = ref<HTMLElement | null>(null)

const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])

// Translations - MUST be before rules that use info.value
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
  status: t('admin.status'),
  draft: t('admin.draft'),
  published: t('admin.published'),
  pinToTop: t('admin.pinToTop') || 'Pin to top',
  category: t('admin.category'),
  tags: t('admin.tags'),
  coverImage: t('admin.featuredImage'),
  preview: t('admin.preview'),
  history: t('admin.history') || 'History',
  pleaseEnterTitle: t('admin.pleaseEnterTitle') || 'Please enter title',
  titleLengthMessage: t('admin.titleLengthMessage') || 'Title must be 2-200 characters',
  pleaseEnterContent: t('admin.pleaseEnterContent') || 'Please enter content',
  pleaseSelectCategory: t('admin.pleaseSelectCategory') || 'Please select category',
  postUpdated: t('admin.postUpdated') || 'Post updated successfully',
  postCreated: t('admin.postCreated') || 'Post created successfully'
}))

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

const previewContent = computed(() => {
  if (!form.content) return ''
  return marked(form.content)
})

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

// ===== Image Upload with Compression =====

const MAX_SIZE_KB = 1024 // 1MB target
const MAX_SIZE_BYTES = MAX_SIZE_KB * 1024

/**
 * Compress image to target size using progressive JPEG quality reduction
 * then dimension reduction if still too large
 */
async function compressImage(file: File, maxSizeBytes: number = MAX_SIZE_BYTES): Promise<Blob> {
  // GIF/WebP: preserve original format without compression
  if (file.type === 'image/gif' || file.type === 'image/webp') {
    return file
  }

  return new Promise((resolve, reject) => {
    const img = new Image()
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')!

    img.onload = () => {
      URL.revokeObjectURL(img.src)
      
      // Compression presets: [maxDimension, quality]
      const presets = [
        { dim: 2000, qual: 0.9 },
        { dim: 1600, qual: 0.85 },
        { dim: 1200, qual: 0.8 },
        { dim: 1000, qual: 0.75 },
        { dim: 800, qual: 0.7 },
        { dim: 600, qual: 0.65 },
        { dim: 400, qual: 0.5 }
      ]

      let currentPresetIndex = 0
      let outputQuality = presets[0].qual
      let maxDim = presets[0].dim

      function compress(): void {
        // Calculate dimensions maintaining aspect ratio
        let { width, height } = img
        if (width > maxDim || height > maxDim) {
          if (width > height) {
            height = Math.round((height * maxDim) / width)
            width = maxDim
          } else {
            width = Math.round((width * maxDim) / height)
            height = maxDim
          }
        }

        canvas.width = width
        canvas.height = height
        ctx.drawImage(img, 0, 0, width, height)

        canvas.toBlob(
          (blob) => {
            if (!blob) {
              reject(new Error('Canvas toBlob failed'))
              return
            }

            if (blob.size <= maxSizeBytes) {
              resolve(blob)
              return
            }

            // Move to next preset if available
            currentPresetIndex++
            if (currentPresetIndex < presets.length) {
              maxDim = presets[currentPresetIndex].dim
              outputQuality = presets[currentPresetIndex].qual
              compress()
            } else {
              // Last resort: return smallest preset regardless of size
              resolve(blob)
            }
          },
          file.type.startsWith('image/png') ? 'image/png' : 'image/jpeg',
          outputQuality
        )
      }

      compress()
    }

    img.onerror = () => {
      URL.revokeObjectURL(img.src)
      reject(new Error('Failed to load image'))
    }

    img.src = URL.createObjectURL(file)
  })
}

/**
 * Insert markdown image syntax at cursor position
 */
function insertImageMarkdown(imageUrl: string) {
  const textarea = editorAreaRef.value?.querySelector('textarea')
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const content = form.content
  const imageMarkdown = `\n![](${imageUrl})\n`

  form.content = content.substring(0, start) + imageMarkdown + content.substring(end)

  // Move cursor after image
  requestAnimationFrame(() => {
    const newPos = start + imageMarkdown.length
    textarea.selectionStart = newPos
    textarea.selectionEnd = newPos
    textarea.focus()
  })
}

/**
 * Upload image with compression
 */
async function uploadImage(file: File) {
  // Validate type
  if (!file.type.startsWith('image/')) {
    ElMessage.error('Please select an image file')
    return
  }

  try {
    uploadStatus.value = 'compressing'
    uploadProgress.value = 0
    statusMessage.value = t('admin.compressingImage') || 'Compressing image...'
    isCompressing.value = true

    let fileToUpload: File | Blob = file

    // Compress if > 1MB
    if (file.size > MAX_SIZE_BYTES) {
      fileToUpload = await compressImage(file)
      uploadProgress.value = 50
    }

    isCompressing.value = false
    uploadStatus.value = 'uploading'
    statusMessage.value = t('admin.uploadingImage') || 'Uploading image...'
    isUploading.value = true
    uploadProgress.value = 70

    // Upload
    const formData = new FormData()
    const fileName = fileToUpload instanceof File ? fileToUpload.name : 'image.jpg'
    const uploadFile = new File([fileToUpload], fileName, { type: fileToUpload.type })
    formData.append('file', uploadFile)
    const res = await settingsApi.uploadImage(formData)
    const imageUrl = res.data.data?.url

    if (!imageUrl) {
      throw new Error('No URL returned from server')
    }

    uploadProgress.value = 100
    uploadStatus.value = 'success'
    statusMessage.value = '✓'

    // Insert markdown at cursor
    insertImageMarkdown(imageUrl)

    // Reset status after 2s
    setTimeout(() => {
      uploadStatus.value = 'idle'
      statusMessage.value = ''
    }, 2000)
  } catch (err) {
    uploadStatus.value = 'error'
    statusMessage.value = t('admin.uploadFailed') || 'Upload failed'
    ElMessage.error(statusMessage.value)
    setTimeout(() => {
      uploadStatus.value = 'idle'
      statusMessage.value = ''
    }, 3000)
  } finally {
    isCompressing.value = false
    isUploading.value = false
  }
}

/**
 * Handle toolbar image upload button click
 */
function handleImageUploadClick() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = (e) => {
    const file = (e.target as HTMLInputElement).files?.[0]
    if (file) {
      uploadImage(file)
    }
  }
  input.click()
}

// ===== Drag & Drop Handlers =====

function handleDragOver(e: DragEvent) {
  e.preventDefault()
  e.stopPropagation()
}

function handleDragEnter(e: DragEvent) {
  e.preventDefault()
  e.stopPropagation()
}

function handleDragLeave(e: DragEvent) {
  e.preventDefault()
  e.stopPropagation()
}

function handleDrop(e: DragEvent) {
  e.preventDefault()
  e.stopPropagation()

  const files = e.dataTransfer?.files
  if (files && files.length > 0) {
    const file = files[0]
    if (file.type.startsWith('image/')) {
      uploadImage(file)
    } else {
      ElMessage.warning('Please drop an image file')
    }
  }
}

// ===== Paste Handler =====

function handlePaste(e: ClipboardEvent) {
  const items = e.clipboardData?.items
  if (!items) return

  for (const item of items) {
    if (item.type.startsWith('image/')) {
      const file = item.getAsFile()
      if (file) {
        e.preventDefault()
        uploadImage(file)
        return
      }
    }
  }
}

// ===== Lifecycle =====

onMounted(async () => {
  await fetchCategories()
  await fetchTags()
  if (isEdit.value) {
    await fetchPost()
  }

  // Add paste listener to document
  document.addEventListener('paste', handlePaste)
})

onUnmounted(() => {
  document.removeEventListener('paste', handlePaste)
})
</script>

<template>
  <div class="post-edit">
    <div class="header">
      <h1 class="page-title">{{ isEdit ? info.editPost : info.createPost }}</h1>
      <div class="header-actions">
        <ElButton v-if="isEdit" @click="router.push(`/admin/posts/${postId}/history`)">
          {{ info.history || 'History' }}
        </ElButton>
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

            <!-- Toolbar -->
            <div class="editor-toolbar">
              <span class="toolbar-label">{{ info.content }}</span>
              <div class="toolbar-actions">
                <ElButton size="small" @click="handleImageUploadClick">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                    <circle cx="8.5" cy="8.5" r="1.5"/>
                    <polyline points="21,15 16,10 5,21"/>
                  </svg>
                  {{ t('admin.insertImage') || 'Insert Image' }}
                </ElButton>
                <span v-if="uploadStatus !== 'idle'" class="upload-status" :class="uploadStatus">
                  <span v-if="uploadStatus === 'compressing'">{{ statusMessage }}</span>
                  <span v-else-if="uploadStatus === 'uploading'">{{ statusMessage }}</span>
                  <span v-else-if="uploadStatus === 'success'" class="success-text">{{ statusMessage }}</span>
                  <span v-else-if="uploadStatus === 'error'" class="error-text">{{ statusMessage }}</span>
                </span>
              </div>
            </div>

            <ElFormItem prop="content" class="editor-item">
              <div
                ref="editorAreaRef"
                class="editor-area"
                @dragover="handleDragOver"
                @dragenter="handleDragEnter"
                @dragleave="handleDragLeave"
                @drop="handleDrop"
              >
                <ElInput
                  v-model="form.content"
                  type="textarea"
                  :rows="15"
                  placeholder="Write your content in Markdown..."
                  class="content-textarea"
                />
              </div>
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

/* Editor Toolbar */
.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  margin-bottom: 8px;
}

.toolbar-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar-actions svg {
  margin-right: 4px;
}

.upload-status {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  background: #f5f5f5;
  color: #666;
}

.upload-status.compressing,
.upload-status.uploading {
  color: #409eff;
}

.upload-status.success {
  color: #67c23a;
  background: #f0f9eb;
}

.upload-status.error {
  color: #f56c6c;
  background: #fef0f0;
}

.success-text {
  font-weight: 500;
}

.error-text {
  font-weight: 500;
}

/* Editor Area */
.editor-area {
  position: relative;
  transition: background-color 0.2s;
}

.editor-area:deep(.el-textarea__inner) {
  min-height: 400px;
}

.editor-item :deep(.el-form-item__content) {
  line-height: normal;
}

/* Drag & Drop visual feedback */
.editor-area.drag-over {
  background-color: #ecf5ff;
}

@media (max-width: 1024px) {
  .edit-layout {
    grid-template-columns: 1fr;
  }
}
</style>
