import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createHead, useHead } from '@unhead/vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'highlight.js/styles/github-dark.css'
import router from './router'
import App from './App.vue'
import '@/assets/styles/main.css'

const app = createApp(App)
const pinia = createPinia()
const head = createHead()

app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.use(head)

// Make useHead available globally
app.config.globalProperties.$useHead = useHead

app.mount('#app')
