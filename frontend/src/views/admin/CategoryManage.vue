<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElInput, ElForm, ElFormItem, ElCard, ElMessage, ElMessageBox, ElTag } from 'element-plus'
import { categoryApi, type Category } from '@/api'
import { useLocale } from '@/composables/useLocale'

const { t } = useLocale()

const categories = ref<Category[]>([])
const loading = ref(false)

const newCategory = reactive({
  name: '',
  slug: '',
  description: ''
})

const editingCategory = ref<Category | null>(null)
const isEditing = ref(false)

// Translations
const info = computed(() => ({
  categories: t('admin.categories'),
  addCategory: t('admin.addCategory') || 'Add Category',
  editCategory: t('admin.editCategory') || 'Edit Category',
  name: t('admin.name'),
  slug: t('admin.slug'),
  description: t('admin.description') || 'Description',
  add: t('common.add'),
  update: t('common.edit') || 'Update',
  cancel: t('common.cancel'),
  posts: t('admin.posts'),
  actions: t('admin.actions'),
  edit: t('admin.edit'),
  delete: t('admin.delete'),
  pleaseEnterName: t('admin.pleaseEnterName') || 'Please enter category name',
  deleteConfirm: t('admin.confirmDelete'),
  categoryCreated: t('admin.categoryCreated') || 'Category created successfully',
  categoryUpdated: t('admin.categoryUpdated') || 'Category updated successfully',
  categoryDeleted: t('admin.categoryDeleted') || 'Category deleted successfully'
}))

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
    ElMessage.warning(info.value.pleaseEnterName)
    return
  }

  try {
    if (isEditing.value && editingCategory.value) {
      await categoryApi.update(editingCategory.value.id, newCategory)
      ElMessage.success(info.value.categoryUpdated)
    } else {
      await categoryApi.create(newCategory)
      ElMessage.success(info.value.categoryCreated)
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
      `"${name}"?`,
      info.value.delete,
      {
        confirmButtonText: info.value.delete,
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    await categoryApi.delete(id)
    ElMessage.success(info.value.categoryDeleted)
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
      <h1 class="page-title">{{ info.categories }}</h1>
    </div>

    <!-- Add/Edit Form -->
    <ElCard class="form-card">
      <template #header>
        <span>{{ isEditing ? info.editCategory : info.addCategory }}</span>
      </template>
      <ElForm :inline="true" :model="newCategory">
        <ElFormItem :label="info.name">
          <ElInput
            v-model="newCategory.name"
            :placeholder="info.name"
            @blur="!isEditing && !newCategory.slug && generateSlug()"
          />
        </ElFormItem>
        <ElFormItem :label="info.slug">
          <ElInput v-model="newCategory.slug" :placeholder="info.slug">
            <template #append>
              <ElButton @click="generateSlug" :disabled="isEditing">{{ t('common.add') }}</ElButton>
            </template>
          </ElInput>
        </ElFormItem>
        <ElFormItem :label="info.description">
          <ElInput v-model="newCategory.description" :placeholder="info.description" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleCreate">
            {{ isEditing ? info.update : info.add }}
          </ElButton>
          <ElButton v-if="isEditing" @click="resetForm">{{ info.cancel }}</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <!-- Categories Table -->
    <ElTable :data="categories" v-loading="loading" stripe class="categories-table">
      <ElTableColumn prop="id" label="ID" width="80" />
      <ElTableColumn prop="name" :label="info.name" min-width="150">
        <template #default="{ row }">
          <span class="category-name">{{ row.name }}</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="slug" :label="info.slug" min-width="150">
        <template #default="{ row }">
          <ElTag size="small">{{ row.slug }}</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="description" :label="info.description" min-width="200" />
      <ElTableColumn prop="postCount" :label="info.posts" width="80" align="center" />
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
