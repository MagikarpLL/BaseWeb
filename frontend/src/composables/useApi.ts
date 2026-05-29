import { ref, shallowRef } from 'vue'
import type { AxiosResponse, AxiosError } from 'axios'
import { ElMessage } from 'element-plus'

interface UseApiOptions {
  onError?: (error: AxiosError) => void
  onSuccess?: (data: unknown) => void
  showSuccess?: boolean
  successMessage?: string
}

export function useApi<T = unknown>(options?: UseApiOptions) {
  const data = shallowRef<T | null>(null)
  const loading = ref(false)
  const error = ref<AxiosError | null>(null)

  async function request(
    apiCall: () => Promise<AxiosResponse>
  ): Promise<T | null> {
    loading.value = true
    error.value = null
    try {
      const response = await apiCall()
      data.value = response.data.data as T
      if (options?.showSuccess && options?.successMessage) {
        ElMessage.success(options.successMessage)
      }
      options?.onSuccess?.(data.value)
      return data.value
    } catch (err) {
      const axiosError = err as AxiosError
      error.value = axiosError
      options?.onError?.(axiosError)
      if (axiosError.response?.status !== 401) {
        const message = (axiosError.response?.data as { message?: string })?.message || 'Request failed'
        ElMessage.error(message)
      }
      return null
    } finally {
      loading.value = false
    }
  }

  function reset() {
    data.value = null
    error.value = null
    loading.value = false
  }

  return {
    data,
    loading,
    error,
    request,
    reset
  }
}

export function useApiWithPagination<T = unknown>(options?: UseApiOptions) {
  const { data, loading, error, request, reset } = useApi<T>(options)
  const total = ref(0)
  const page = ref(1)
  const pageSize = ref(10)

  async function requestWithPagination(
    apiCall: (params: { page: number; size: number }) => Promise<AxiosResponse>,
    pageNum: number = 1,
    size: number = 10
  ): Promise<T | null> {
    page.value = pageNum
    pageSize.value = size
    return request(() => apiCall({ page: pageNum, size }))
  }

  function setTotal(totalCount: number) {
    total.value = totalCount
  }

  return {
    data,
    loading,
    error,
    request,
    requestWithPagination,
    reset,
    total,
    page,
    pageSize,
    setTotal
  }
}
