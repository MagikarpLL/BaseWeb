import { beforeEach, describe, expect, it, vi } from 'vitest'

const requestMock = {
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  delete: vi.fn()
}

vi.mock('../../src/api/request', () => ({
  default: requestMock
}))

describe('api route contracts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('uses the public home endpoint from the API spec', async () => {
    const { publicApi } = await import('../../src/api')

    publicApi.getHome()

    expect(requestMock.get).toHaveBeenCalledWith('/public/home')
  })

  it('uses the auth login endpoint from the API spec', async () => {
    const { authApi } = await import('../../src/api')
    const payload = { username: 'admin', password: 'test-password' }

    authApi.login(payload)

    expect(requestMock.post).toHaveBeenCalledWith('/auth/login', payload)
  })

  it('passes pagination and filter params for blog listing', async () => {
    const { blogApi } = await import('../../src/api')
    const params = { page: 1, size: 10, category: 'frontend', tag: 'vue', sort: 'latest' }

    blogApi.getPosts(params)

    expect(requestMock.get).toHaveBeenCalledWith('/blog/posts', { params })
  })
})
