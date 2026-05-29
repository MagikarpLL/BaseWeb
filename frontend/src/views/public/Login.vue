<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@unhead/vue'
import { ElForm, ElFormItem, ElInput, ElButton, ElCard, ElAlert } from 'element-plus'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { login } = useAuth()

const formRef = ref()
const loading = ref(false)
const errorMessage = ref('')

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: 'Please enter username', trigger: 'blur' },
    { min: 3, max: 50, message: 'Username must be 3-50 characters', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
  ]
}

useHead({
  title: 'Login'
})

async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    errorMessage.value = ''
    loading.value = true

    await login(form.username, form.password)
  } catch (error: unknown) {
    if (error && typeof error === 'object' && 'response' in error) {
      const err = error as { response?: { data?: { message?: string } } }
      errorMessage.value = err.response?.data?.message || 'Login failed. Please check your credentials.'
    } else {
      errorMessage.value = 'Login failed. Please check your credentials.'
    }
  } finally {
    loading.value = false
  }
}

function goHome() {
  router.push('/')
}
</script>

<template>
  <div class="login-page">
    <ElCard class="login-card">
      <template #header>
        <div class="card-header">
          <h2>Welcome Back</h2>
          <p class="subtitle">Sign in to your account</p>
        </div>
      </template>

      <ElAlert
        v-if="errorMessage"
        type="error"
        :title="errorMessage"
        show-icon
        closable
        class="error-alert"
        @close="errorMessage = ''"
      />

      <ElForm
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleSubmit"
      >
        <ElFormItem label="Username" prop="username">
          <ElInput
            v-model="form.username"
            placeholder="Enter your username"
            size="large"
            prefix-icon="User"
          />
        </ElFormItem>

        <ElFormItem label="Password" prop="password">
          <ElInput
            v-model="form.password"
            type="password"
            placeholder="Enter your password"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </ElFormItem>

        <ElFormItem>
          <ElButton
            type="primary"
            native-type="submit"
            :loading="loading"
            size="large"
            class="submit-btn"
          >
            Sign In
          </ElButton>
        </ElFormItem>
      </ElForm>

      <div class="card-footer">
        <span>Don't have an account?</span>
        <ElButton text @click="goHome">Back to Home</ElButton>
      </div>
    </ElCard>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.card-header {
  text-align: center;
}

.card-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px;
}

.subtitle {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.error-alert {
  margin-bottom: 20px;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
}

.card-footer {
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  color: #666;
  font-size: 14px;
}

.card-footer span {
  margin-right: 8px;
}
</style>
