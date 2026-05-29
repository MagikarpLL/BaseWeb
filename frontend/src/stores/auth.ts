import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, type User, type LoginResponse } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
  const user = ref<User | null>(null)

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'admin')

  function setToken(newToken: string, newRefreshToken?: string) {
    token.value = newToken
    if (newRefreshToken) {
      refreshToken.value = newRefreshToken
    }
  }

  function setUser(newUser: User) {
    user.value = newUser
  }

  function clearAuth() {
    token.value = null
    refreshToken.value = null
    user.value = null
  }

  async function login(username: string, password: string): Promise<LoginResponse> {
    const res = await authApi.login({ username, password })
    const { token: newToken, refreshToken: newRefreshToken, user: userData } = res.data.data
    setToken(newToken, newRefreshToken)
    setUser(userData)
    return res.data.data
  }

  function logout() {
    clearAuth()
  }

  async function refreshAccessToken(): Promise<boolean> {
    if (!refreshToken.value) return false
    try {
      const res = await authApi.refresh(refreshToken.value)
      setToken(res.data.data.token, res.data.data.refreshToken)
      return true
    } catch {
      clearAuth()
      return false
    }
  }

  async function fetchCurrentUser(): Promise<User | null> {
    if (!token.value) return null
    try {
      const res = await authApi.getCurrentUser()
      user.value = res.data.data
      return user.value
    } catch {
      return null
    }
  }

  return {
    token,
    refreshToken,
    user,
    isAuthenticated,
    isAdmin,
    setToken,
    setUser,
    clearAuth,
    login,
    logout,
    refreshAccessToken,
    fetchCurrentUser
  }
})
