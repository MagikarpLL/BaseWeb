<script setup lang="ts">
import { useSiteStore } from '@/stores/site'

const siteStore = useSiteStore()
const currentYear = new Date().getFullYear()
</script>

<template>
  <footer class="site-footer">
    <div class="footer-content">
      <div class="footer-info">
        <p>&copy; {{ currentYear }} {{ siteStore.siteName }}. All rights reserved.</p>
      </div>
      <div class="footer-links">
        <a href="https://github.com" target="_blank" rel="noopener noreferrer">GitHub</a>
        <a href="mailto:contact@example.com">Contact</a>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  background: #f5f5f5;
  border-top: 1px solid #e0e0e0;
  padding: 24px 20px;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.footer-links {
  display: flex;
  gap: 20px;
}

.footer-links a {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.footer-links a:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }
}
</style>
