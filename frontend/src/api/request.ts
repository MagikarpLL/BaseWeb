import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const api: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor with token refresh
api.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error: AxiosError) => {
    const authStore = useAuthStore()
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    if (error.response?.status === 401 && !originalRequest._retry && authStore.refreshToken) {
      originalRequest._retry = true
      try {
        const res = await axios.post(`${import.meta.env.VITE_API_URL || 'http://localhost:8080/api'}/auth/refresh`, {
          refreshToken: authStore.refreshToken
        })
        const { token, refreshToken: newRefreshToken } = res.data.data as { token: string; refreshToken: string }
        authStore.setToken(token, newRefreshToken)
        originalRequest.headers.Authorization = `Bearer ${token}`
        return api(originalRequest)
      } catch (refreshError) {
        authStore.clearAuth()
        router.push('/login')
        return Promise.reject(refreshError)
      }
    }

    if (error.response?.status === 401) {
      authStore.clearAuth()
      router.push('/login')
    }

    return Promise.reject(error)
  }
)

export default api

export interface ApiResponse<T = unknown> {
  data: T
  message?: string
  code?: number
}

export interface PageResponse<T> {
  data: {
    content: T[]
    totalElements: number
    totalPages: number
    page: number
    size: number
  }
  message?: string
  code?: number
}
