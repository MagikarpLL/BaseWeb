<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElInput, ElForm, ElFormItem, ElCard, ElMessage, ElMessageBox, ElTag } from 'element-plus'
import { tagApi, type Tag } from '@/api'
import { useLocale } from '@/composables/useLocale'

const { t } = useLocale()

const tags = ref<Tag[]>([])
const loading = ref(false)

const newTag = reactive({
  name: '',
  slug: ''
})

const editingTag = ref<Tag | null>(null)
const isEditing = ref(false)

// Translations
const info = computed(() => ({
  tags: t('admin.tags'),
  addTag: t('admin.addTag') || 'Add Tag',
  editTag: t('admin.editTag') || 'Edit Tag',
  name: t('admin.name'),
  slug: t('admin.slug'),
  add: t('common.add'),
  update: t('common.edit') || 'Update',
  cancel: t('common.cancel'),
  actions: t('admin.actions'),
  edit: t('admin.edit'),
  delete: t('admin.delete'),
  pleaseEnterName: t('admin.pleaseEnterName') || 'Please enter tag name',
  deleteConfirm: t('admin.confirmDelete'),
  tagCreated: t('admin.tagCreated') || 'Tag created successfully',
  tagUpdated: t('admin.tagUpdated') || 'Tag updated successfully',
  tagDeleted: t('admin.tagDeleted') || 'Tag deleted successfully'
}))

function generateSlug() {
  newTag.slug = newTag.name
    .toLowerCase()
    .replace(/[^a-z0-9\u4e00-\u9fa5]+/g, '-')
    .replace(/^-|-$/g, '')
}

async function fetchTags() {
  loading.value = true
  try {
    const res = await tagApi.getAll()
    tags.value = res.data.data
  } catch {
    tags.value = []
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!newTag.name.trim()) {
    ElMessage.warning(info.value.pleaseEnterName)
    return
  }

  try {
    if (isEditing.value && editingTag.value) {
      await tagApi.update(editingTag.value.id, newTag)
      ElMessage.success(info.value.tagUpdated)
    } else {
      await tagApi.create(newTag)
      ElMessage.success(info.value.tagCreated)
    }
    resetForm()
    fetchTags()
  } catch {
    // Error handled by API interceptor
  }
}

function startEdit(tag: Tag) {
  editingTag.value = tag
  newTag.name = tag.name
  newTag.slug = tag.slug
  isEditing.value = true
}

function resetForm() {
  newTag.name = ''
  newTag.slug = ''
  editingTag.value = null
  isEditing.value = false
}

async function handleDelete(id: number, name: string) {
  try {
    await ElMessageBox.confirm(
      `"${name}"?`,
      info.value.delete,
      {
        confirmButtonText: info.value.delete,
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    await tagApi.delete(id)
    ElMessage.success(info.value.tagDeleted)
    fetchTags()
  } catch {
    // User cancelled
  }
}

onMounted(() => {
  fetchTags()
})
</script>

<template>
  <div class="tag-manage">
    <div class="header">
      <h1 class="page-title">{{ info.tags }}</h1>
    </div>

    <!-- Add/Edit Form -->
    <ElCard class="form-card">
      <template #header>
        <span>{{ isEditing ? info.editTag : info.addTag }}</span>
      </template>
      <ElForm :inline="true" :model="newTag">
        <ElFormItem :label="info.name">
          <ElInput
            v-model="newTag.name"
            :placeholder="info.name"
            @blur="!isEditing && !newTag.slug && generateSlug()"
          />
        </ElFormItem>
        <ElFormItem :label="info.slug">
          <ElInput v-model="newTag.slug" :placeholder="info.slug">
            <template #append>
              <ElButton @click="generateSlug" :disabled="isEditing">{{ t('common.add') }}</ElButton>
            </template>
          </ElInput>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleCreate">
            {{ isEditing ? info.update : info.add }}
          </ElButton>
          <ElButton v-if="isEditing" @click="resetForm">{{ info.cancel }}</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <!-- Tags Table -->
    <ElTable :data="tags" v-loading="loading" stripe class="tags-table">
      <ElTableColumn prop="id" label="ID" width="80" />
      <ElTableColumn prop="name" :label="info.name" min-width="150">
        <template #default="{ row }">
          <span class="tag-name">{{ row.name }}</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="slug" :label="info.slug" min-width="150">
        <template #default="{ row }">
          <ElTag size="small" type="info">{{ row.slug }}</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn :label="info.actions" width="150" fixed="right">
        <template #default="{ row }">
          <ElButton size="small" @click="startEdit(row)">{{ info.edit }}</ElButton>
          <ElButton size="small" type="danger" @click="handleDelete(row.id, row.name)">{{ info.delete }}</ElButton>
        </template>
      </ElTableColumn>
    </ElTable>
  </div>
</template>

<style scoped>
.tag-manage {
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

.form-card {
  margin-bottom: 24px;
  border-radius: 12px;
}

.tags-table {
  border-radius: 8px;
  overflow: hidden;
}

.tag-name {
  font-weight: 500;
}
</style>
