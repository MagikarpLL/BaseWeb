import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createHead, useHead } from '@unhead/vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import 'highlight.js/styles/github-dark.css'
import router from './router'
import i18n from './i18n'
import App from './App.vue'
import '@/assets/styles/main.css'
import lazyload from '@/directives/lazyload'

const app = createApp(App)
const pinia = createPinia()
const head = createHead()

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.use(head)
app.use(i18n)

// Register lazyload directive
app.directive('lazyload', lazyload)

// Make useHead available globally
app.config.globalProperties.$useHead = useHead

app.mount('#app')
