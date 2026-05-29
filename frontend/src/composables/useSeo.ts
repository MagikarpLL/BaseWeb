import { computed } from 'vue'
import type { Post } from '@/api'
import { useSiteStore } from '@/stores/site'

interface BreadcrumbItem {
  name: string
  url: string
}

/**
 * Generates JSON-LD schema for a blog post (BlogPosting type)
 */
export function useBlogPostSchema(post: () => Post | null) {
  const siteStore = useSiteStore()

  return computed(() => {
    const p = post()
    if (!p) return null

    const baseUrl = typeof window !== 'undefined' ? window.location.origin : ''

    const schema: Record<string, unknown> = {
      '@context': 'https://schema.org',
      '@type': 'BlogPosting',
      headline: p.title,
      description: p.excerpt || '',
      datePublished: p.publishedAt || p.createdAt,
      dateModified: p.updatedAt,
      author: {
        '@type': 'Person',
        name: siteStore.settings.profile.name || 'Anonymous'
      },
      publisher: {
        '@type': 'Organization',
        name: siteStore.siteName
      },
      mainEntityOfPage: {
        '@type': 'WebPage',
        '@id': `${baseUrl}/blog/${p.slug}`
      }
    }

    if (siteStore.settings.profile.avatar) {
      (schema.publisher as Record<string, unknown>).logo = {
        '@type': 'ImageObject',
        url: siteStore.settings.profile.avatar.startsWith('http') 
          ? siteStore.settings.profile.avatar 
          : `${baseUrl}${siteStore.settings.profile.avatar}`
      }
    }

    if (p.coverImage) {
      schema.image = p.coverImage.startsWith('http') ? p.coverImage : `${baseUrl}${p.coverImage}`
    }

    return schema
  })
}

/**
 * Generates WebSite schema with searchAction for site-wide SEO
 */
export function useWebsiteSchema() {
  const siteStore = useSiteStore()

  return computed(() => {
    const baseUrl = typeof window !== 'undefined' ? window.location.origin : ''

    return {
      '@context': 'https://schema.org',
      '@type': 'WebSite',
      name: siteStore.siteName,
      url: baseUrl,
      description: siteStore.siteDescription || siteStore.settings.seoDescription || '',
      potentialAction: {
        '@type': 'SearchAction',
        target: {
          '@type': 'EntryPoint',
          urlTemplate: `${baseUrl}/blog?search={search_term_string}`
        },
        'query-input': 'required name=search_term_string'
      }
    }
  })
}

/**
 * Generates BreadcrumbList schema
 */
export function useBreadcrumbSchema(items: () => BreadcrumbItem[]) {
  return computed(() => {
    const itemList = items().map((item, index) => ({
      '@type': 'ListItem',
      position: index + 1,
      name: item.name,
      item: item.url.startsWith('http') ? item.url : `${typeof window !== 'undefined' ? window.location.origin : ''}${item.url}`
    }))

    return {
      '@context': 'https://schema.org',
      '@type': 'BreadcrumbList',
      itemListElement: itemList
    }
  })
}