import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

export function useAuth() {
  const authStore = useAuthStore()
  const router = useRouter()

  const isAuthenticated = computed(() => authStore.isAuthenticated)
  const isAdmin = computed(() => authStore.isAdmin)
  const user = computed(() => authStore.user)

  async function login(username: string, password: string) {
    try {
      await authStore.login(username, password)
      router.push('/admin')
    } catch (error) {
      throw error
    }
  }

  function logout() {
    authStore.logout()
    router.push('/login')
  }

  return {
    isAuthenticated,
    isAdmin,
    user,
    login,
    logout
  }
}
