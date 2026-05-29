<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElInput, ElForm, ElFormItem, ElCard, ElMessage, ElMessageBox, ElTag } from 'element-plus'
import { tagApi, type Tag } from '@/api'

const tags = ref<Tag[]>([])
const loading = ref(false)

const newTag = reactive({
  name: '',
  slug: ''
})

const editingTag = ref<Tag | null>(null)
const isEditing = ref(false)

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
    ElMessage.warning('Please enter tag name')
    return
  }

  try {
    if (isEditing.value && editingTag.value) {
      await tagApi.update(editingTag.value.id, newTag)
      ElMessage.success('Tag updated successfully')
    } else {
      await tagApi.create(newTag)
      ElMessage.success('Tag created successfully')
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
      `Are you sure you want to delete "${name}"?`,
      'Delete Tag',
      {
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )
    await tagApi.delete(id)
    ElMessage.success('Tag deleted successfully')
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
      <h1 class="page-title">Tags</h1>
    </div>

    <!-- Add/Edit Form -->
    <ElCard class="form-card">
      <template #header>
        <span>{{ isEditing ? 'Edit Tag' : 'Add Tag' }}</span>
      </template>
      <ElForm :inline="true" :model="newTag">
        <ElFormItem label="Name">
          <ElInput
            v-model="newTag.name"
            placeholder="Tag name"
            @blur="!isEditing && !newTag.slug && generateSlug()"
          />
        </ElFormItem>
        <ElFormItem label="Slug">
          <ElInput v-model="newTag.slug" placeholder="tag-slug">
            <template #append>
              <ElButton @click="generateSlug" :disabled="isEditing">Generate</ElButton>
            </template>
          </ElInput>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleCreate">
            {{ isEditing ? 'Update' : 'Add' }}
          </ElButton>
          <ElButton v-if="isEditing" @click="resetForm">Cancel</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <!-- Tags Table -->
    <ElTable :data="tags" v-loading="loading" stripe class="tags-table">
      <ElTableColumn prop="id" label="ID" width="80" />
      <ElTableColumn prop="name" label="Name" min-width="150">
        <template #default="{ row }">
          <span class="tag-name">{{ row.name }}</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="slug" label="Slug" min-width="150">
        <template #default="{ row }">
          <ElTag size="small" type="info">{{ row.slug }}</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn label="Actions" width="150" fixed="right">
        <template #default="{ row }">
          <ElButton size="small" @click="startEdit(row)">Edit</ElButton>
          <ElButton size="small" type="danger" @click="handleDelete(row.id, row.name)">Delete</ElButton>
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
