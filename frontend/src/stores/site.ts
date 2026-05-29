import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { settingsApi, type SiteSettings } from '@/api'

export const useSiteStore = defineStore('site', () => {
  const settings = ref<SiteSettings>({
    siteName: 'My Blog',
    siteDescription: '',
    seoTitle: '',
    seoDescription: '',
    allowComments: true,
    requireApproval: false,
    profile: {
      name: '',
      title: '',
      bio: '',
      avatar: ''
    },
    social: {},
    about: {
      skills: [],
      timeline: [],
      projects: []
    }
  })

  const siteName = computed(() => settings.value.siteName)
  const siteDescription = computed(() => settings.value.siteDescription)
  const seoTitle = computed(() => settings.value.seoTitle)

  function setSettings(newSettings: SiteSettings) {
    settings.value = newSettings
  }

  function updateSettings(partial: Partial<SiteSettings>) {
    settings.value = { ...settings.value, ...partial }
  }

  async function fetchSettings(): Promise<void> {
    try {
      const res = await settingsApi.get()
      setSettings(res.data.data)
    } catch {
      // Use default settings if fetch fails
    }
  }

  async function saveSettings(): Promise<void> {
    await settingsApi.update(settings.value)
  }

  return {
    settings,
    siteName,
    siteDescription,
    seoTitle,
    setSettings,
    updateSettings,
    fetchSettings,
    saveSettings
  }
})
