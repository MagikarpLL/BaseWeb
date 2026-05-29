import request from './request'
import type { AxiosResponse } from 'axios'
import type { ApiResponse, PageResponse } from './request'

// Types
export interface User {
  id: number
  username: string
  email?: string
  role: string
  avatar?: string
  createdAt: string
}

export interface LoginData {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  refreshToken: string
  user: User
}

export interface Post {
  id: number
  title: string
  slug: string
  excerpt?: string
  content: string
  coverImage?: string
  status: 'draft' | 'published'
  categoryId: number
  categoryName?: string
  tags?: Tag[]
  viewCount: number
  commentCount: number
  createdAt: string
  updatedAt: string
  publishedAt?: string
}

export interface PostFormData {
  title: string
  slug?: string
  excerpt?: string
  content: string
  coverImage?: string
  status: 'draft' | 'published'
  isTop?: boolean
  categoryId: number
  tagIds?: number[]
}

export interface Category {
  id: number
  name: string
  slug: string
  description?: string
  postCount?: number
  createdAt: string
}

export interface Tag {
  id: number
  name: string
  slug: string
  createdAt: string
}

export interface Comment {
  id: number
  content: string
  authorName: string
  authorEmail?: string
  website?: string
  avatar?: string
  status: 'pending' | 'approved' | 'rejected'
  postId: number
  postTitle?: string
  parentId?: number
  children?: Comment[]
  createdAt: string
}

export interface CommentFormData {
  content: string
  author: string
  email?: string
  website?: string
  parentId?: number
  captcha: string
}

export interface Tool {
  id: number
  name: string
  slug: string
  description: string
  icon: string
  category: string
  config?: Record<string, unknown>
}

export interface HomeData {
  profile: {
    name: string
    title: string
    bio: string
    avatar: string
  }
  latestPosts: Post[]
  featuredTools: Tool[]
}

export interface AboutData {
  profile: {
    name: string
    title: string
    bio: string
    avatar: string
    skills: Array<{ name: string; level: number }>
  }
  timeline: Array<{
    date: string
    title: string
    description: string
    type: 'education' | 'work'
  }>
  projects: Array<{
    name: string
    description: string
    url?: string
    techs: string[]
  }>
}

export interface SiteSettings {
  siteName: string
  siteDescription: string
  seoTitle: string
  seoDescription: string
  allowComments: boolean
  requireApproval: boolean
  profile: {
    name: string
    title: string
    bio: string
    avatar: string
  }
  social: {
    github?: string
    twitter?: string
    email?: string
  }
  about: {
    skills: Array<{ name: string; level: number }>
    timeline: Array<{
      date: string
      title: string
      description: string
      type: 'education' | 'work'
    }>
    projects: Array<{
      name: string
      description: string
      url?: string
      techs: string[]
    }>
  }
}

export interface AnalyticsOverview {
  totalPosts: number
  totalComments: number
  totalViews: number
  totalUsers: number
  todayPV: number
  todayUV: number
  recentPosts: Array<{ id: number; title: string; viewCount: number }>
}

export interface AnalyticsTrend {
  dates: string[]
  pv: number[]
  uv: number[]
}

export interface AnalyticsPage {
  path: string
  title: string
  viewCount: number
  uniqueViewCount: number
}

export interface AnalyticsSource {
  source: string
  count: number
  percentage: number
}

export interface AdminComment {
  id: number
  content: string
  author: string
  email?: string
  status: 'pending' | 'approved' | 'rejected'
  postId: number
  postTitle: string
  createdAt: string
}

// Auth API
export const authApi = {
  login: (data: LoginData): Promise<AxiosResponse<ApiResponse<LoginResponse>>> =>
    request.post('/auth/login', data),
  logout: (): Promise<AxiosResponse<ApiResponse>> => request.post('/auth/logout'),
  refresh: (refreshToken: string): Promise<AxiosResponse<ApiResponse<{ token: string; refreshToken: string }>>> =>
    request.post('/auth/refresh', { refreshToken }),
  getCurrentUser: (): Promise<AxiosResponse<ApiResponse<User>>> =>
    request.get('/auth/me')
}

// Blog API
export const blogApi = {
  getPosts: (params?: {
    page?: number
    size?: number
    category?: string
    tag?: string
    status?: string
    sort?: string
  }): Promise<AxiosResponse<ApiResponse<any>>> =>
    request.get('/blog/posts', { params }),
  getPost: (slug: string): Promise<AxiosResponse<ApiResponse<Post>>> =>
    request.get(`/blog/posts/${slug}`),
  incrementView: (slug: string): Promise<AxiosResponse<ApiResponse<void>>> =>
    request.post(`/blog/posts/${slug}/view`),
  getAllTags: (): Promise<AxiosResponse<ApiResponse<Tag[]>>> =>
    request.get('/blog/tags'),
  getComments: (slug: string): Promise<AxiosResponse<ApiResponse<Comment[]>>> =>
    request.get(`/blog/posts/${slug}/comments`),
  createComment: (slug: string, data: CommentFormData): Promise<AxiosResponse<ApiResponse<Comment>>> =>
    request.post(`/blog/posts/${slug}/comments`, data)
}

// Public API
export const publicApi = {
  getHome: (): Promise<AxiosResponse<ApiResponse<HomeData>>> =>
    request.get('/public/home'),
  getAbout: (): Promise<AxiosResponse<ApiResponse<AboutData>>> =>
    request.get('/public/about'),
  getTools: (): Promise<AxiosResponse<ApiResponse<Tool[]>>> =>
    request.get('/public/tools')
}

// Category API
export const categoryApi = {
  getAll: (): Promise<AxiosResponse<ApiResponse<Category[]>>> => request.get('/categories'),
  create: (data: { name: string; slug: string; description?: string }): Promise<AxiosResponse<ApiResponse<Category>>> =>
    request.post('/categories', data),
  update: (id: number, data: { name: string; slug: string; description?: string }): Promise<AxiosResponse<ApiResponse<Category>>> =>
    request.put(`/categories/${id}`, data),
  delete: (id: number): Promise<AxiosResponse<ApiResponse>> =>
    request.delete(`/categories/${id}`)
}

// Tag API
export const tagApi = {
  getAll: (): Promise<AxiosResponse<ApiResponse<Tag[]>>> => request.get('/tags'),
  create: (data: { name: string; slug: string }): Promise<AxiosResponse<ApiResponse<Tag>>> =>
    request.post('/tags', data),
  update: (id: number, data: { name: string; slug: string }): Promise<AxiosResponse<ApiResponse<Tag>>> =>
    request.put(`/tags/${id}`, data),
  delete: (id: number): Promise<AxiosResponse<ApiResponse>> =>
    request.delete(`/tags/${id}`)
}

// Admin Post API
export const adminPostApi = {
  getPosts: (params?: {
    page?: number
    size?: number
    category?: string
    status?: string
    keyword?: string
  }): Promise<AxiosResponse<PageResponse<Post>>> =>
    request.get('/admin/posts', { params }),
  getPost: (id: number): Promise<AxiosResponse<ApiResponse<Post>>> =>
    request.get(`/admin/posts/${id}`),
  createPost: (data: PostFormData): Promise<AxiosResponse<ApiResponse<Post>>> =>
    request.post('/admin/posts', data),
  updatePost: (id: number, data: PostFormData): Promise<AxiosResponse<ApiResponse<Post>>> =>
    request.put(`/admin/posts/${id}`, data),
  deletePost: (id: number): Promise<AxiosResponse<ApiResponse>> =>
    request.delete(`/admin/posts/${id}`),
  publishPost: (id: number): Promise<AxiosResponse<ApiResponse>> =>
    request.post(`/admin/posts/${id}/publish`),
  draftPost: (id: number): Promise<AxiosResponse<ApiResponse>> =>
    request.post(`/admin/posts/${id}/draft`),
  toggleTop: (id: number): Promise<AxiosResponse<ApiResponse>> =>
    request.post(`/admin/posts/${id}/top`)
}

// Admin Comment API
export const adminCommentApi = {
  getComments: (params?: {
    page?: number
    size?: number
    status?: string
    postId?: number
  }): Promise<AxiosResponse<PageResponse<AdminComment>>> =>
    request.get('/admin/comments', { params }),
  updateStatus: (id: number, status: 'approved' | 'rejected'): Promise<AxiosResponse<ApiResponse>> =>
    request.put(`/admin/comments/${id}/status`, { status }),
  deleteComment: (id: number): Promise<AxiosResponse<ApiResponse>> =>
    request.delete(`/admin/comments/${id}`)
}

// Settings API
export const settingsApi = {
  get: (): Promise<AxiosResponse<ApiResponse<SiteSettings>>> =>
    request.get('/admin/settings'),
  update: (data: Partial<SiteSettings>): Promise<AxiosResponse<ApiResponse<SiteSettings>>> =>
    request.put('/admin/settings', data),
  uploadImage: (formData: FormData): Promise<AxiosResponse<ApiResponse<{ url: string }>>> =>
    request.post('/admin/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// Analytics API
export const analyticsApi = {
  getOverview: (): Promise<AxiosResponse<ApiResponse<AnalyticsOverview>>> =>
    request.get('/admin/analytics/overview'),
  getTrend: (days?: number): Promise<AxiosResponse<ApiResponse<AnalyticsTrend>>> =>
    request.get('/admin/analytics/trend', { params: { days } }),
  getTopPages: (limit?: number): Promise<AxiosResponse<ApiResponse<AnalyticsPage[]>>> =>
    request.get('/admin/analytics/pages', { params: { limit } }),
  getSources: (): Promise<AxiosResponse<ApiResponse<AnalyticsSource[]>>> =>
    request.get('/admin/analytics/sources')
}

export default {
  auth: authApi,
  blog: blogApi,
  public: publicApi,
  category: categoryApi,
  tag: tagApi,
  adminPost: adminPostApi,
  adminComment: adminCommentApi,
  settings: settingsApi,
  analytics: analyticsApi
}
