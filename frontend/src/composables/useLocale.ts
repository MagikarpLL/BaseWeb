import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const LOCALE_KEY = 'locale'

export const useLocale = () => {
  const { locale, t } = useI18n()

  const currentLocale = computed(() => locale.value)
  const isZhCN = computed(() => locale.value === 'zh-CN')

  const setLocale = (newLocale: 'en' | 'zh-CN') => {
    locale.value = newLocale
    localStorage.setItem(LOCALE_KEY, newLocale)
    document.documentElement.lang = newLocale === 'zh-CN' ? 'zh-Hans' : 'en'
  }

  const toggleLocale = () => {
    setLocale(locale.value === 'en' ? 'zh-CN' : 'en')
  }

  return {
    locale: currentLocale,
    isZhCN,
    setLocale,
    toggleLocale,
    t
  }
}