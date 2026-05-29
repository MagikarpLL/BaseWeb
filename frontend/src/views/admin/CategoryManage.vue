<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElInput, ElForm, ElFormItem, ElCard, ElMessage, ElMessageBox, ElTag } from 'element-plus'
import { categoryApi, type Category } from '@/api'

const categories = ref<Category[]>([])
const loading = ref(false)

const newCategory = reactive({
  name: '',
  slug: '',
  description: ''
})

const editingCategory = ref<Category | null>(null)
const isEditing = ref(false)

function generateSlug() {
  newCategory.slug = newCategory.name
    .toLowerCase()
    .replace(/[^a-z0-9\u4e00-\u9fa5]+/g, '-')
    .replace(/^-|-$/g, '')
}

async function fetchCategories() {
  loading.value = true
  try {
    const res = await categoryApi.getAll()
    categories.value = res.data.data
  } catch {
    categories.value = []
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!newCategory.name.trim()) {
    ElMessage.warning('Please enter category name')
    return
  }

  try {
    if (isEditing.value && editingCategory.value) {
      await categoryApi.update(editingCategory.value.id, newCategory)
      ElMessage.success('Category updated successfully')
    } else {
      await categoryApi.create(newCategory)
      ElMessage.success('Category created successfully')
    }
    resetForm()
    fetchCategories()
  } catch {
    // Error handled by API interceptor
  }
}

function startEdit(category: Category) {
  editingCategory.value = category
  newCategory.name = category.name
  newCategory.slug = category.slug
  newCategory.description = category.description || ''
  isEditing.value = true
}

function resetForm() {
  newCategory.name = ''
  newCategory.slug = ''
  newCategory.description = ''
  editingCategory.value = null
  isEditing.value = false
}

async function handleDelete(id: number, name: string) {
  try {
    await ElMessageBox.confirm(
      `Are you sure you want to delete "${name}"?`,
      'Delete Category',
      {
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )
    await categoryApi.delete(id)
    ElMessage.success('Category deleted successfully')
    fetchCategories()
  } catch {
    // User cancelled
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<template>
  <div class="category-manage">
    <div class="header">
      <h1 class="page-title">Categories</h1>
    </div>

    <!-- Add/Edit Form -->
    <ElCard class="form-card">
      <template #header>
        <span>{{ isEditing ? 'Edit Category' : 'Add Category' }}</span>
      </template>
      <ElForm :inline="true" :model="newCategory">
        <ElFormItem label="Name">
          <ElInput
            v-model="newCategory.name"
            placeholder="Category name"
            @blur="!isEditing && !newCategory.slug && generateSlug()"
          />
        </ElFormItem>
        <ElFormItem label="Slug">
          <ElInput v-model="newCategory.slug" placeholder="category-slug">
            <template #append>
              <ElButton @click="generateSlug" :disabled="isEditing">Generate</ElButton>
            </template>
          </ElInput>
        </ElFormItem>
        <ElFormItem label="Description">
          <ElInput v-model="newCategory.description" placeholder="Description (optional)" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleCreate">
            {{ isEditing ? 'Update' : 'Add' }}
          </ElButton>
          <ElButton v-if="isEditing" @click="resetForm">Cancel</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <!-- Categories Table -->
    <ElTable :data="categories" v-loading="loading" stripe class="categories-table">
      <ElTableColumn prop="id" label="ID" width="80" />
      <ElTableColumn prop="name" label="Name" min-width="150">
        <template #default="{ row }">
          <span class="category-name">{{ row.name }}</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="slug" label="Slug" min-width="150">
        <template #default="{ row }">
          <ElTag size="small">{{ row.slug }}</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="description" label="Description" min-width="200" />
      <ElTableColumn prop="postCount" label="Posts" width="80" align="center" />
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
.category-manage {
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

.categories-table {
  border-radius: 8px;
  overflow: hidden;
}

.category-name {
  font-weight: 500;
}
</style>
