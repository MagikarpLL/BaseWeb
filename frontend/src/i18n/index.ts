import { createI18n } from 'vue-i18n'
import en from '@/locales/en.json'
import zhCN from '@/locales/zh-CN.json'

type MessageSchema = typeof en

const getDefaultLocale = (): 'en' | 'zh-CN' => {
  const stored = localStorage.getItem('locale')
  if (stored === 'en' || stored === 'zh-CN') return stored

  const browserLang = navigator.language || navigator.languages[0]
  if (browserLang.toLowerCase().startsWith('zh')) return 'zh-CN'
  return 'en'
}

const i18n = createI18n<[MessageSchema], 'en' | 'zh-CN'>({
  legacy: false,
  locale: getDefaultLocale(),
  fallbackLocale: 'en',
  messages: {
    'en': en,
    'zh-CN': zhCN
  }
})

export default i18n