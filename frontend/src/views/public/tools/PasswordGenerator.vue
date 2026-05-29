<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useHead } from '@unhead/vue'
import { ElCard, ElButton, ElSlider, ElCheckbox, ElAlert } from 'element-plus'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
import { useSiteStore } from '@/stores/site'

const siteStore = useSiteStore()

// Options
const length = ref(16)
const includeUppercase = ref(true)
const includeLowercase = ref(true)
const includeNumbers = ref(true)
const includeSymbols = ref(true)
const excludeAmbiguous = ref(false)

// Generated password
const generatedPassword = ref('')
const copied = ref(false)

// Password character sets
const uppercaseChars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
const lowercaseChars = 'abcdefghijklmnopqrstuvwxyz'
const numberChars = '0123456789'
const symbolChars = '!@#$%^&*()_+-=[]{}|;:,.<>?'
const ambiguousChars = '0O1lI'

function getAvailableChars(): string {
  let chars = ''
  
  if (includeUppercase.value) chars += uppercaseChars
  if (includeLowercase.value) chars += lowercaseChars
  if (includeNumbers.value) chars += numberChars
  if (includeSymbols.value) chars += symbolChars
  
  if (excludeAmbiguous.value) {
    for (const char of ambiguousChars) {
      chars = chars.replace(new RegExp(char, 'g'), '')
    }
  }
  
  return chars || 'abcdefghijklmnopqrstuvwxyz'
}

// Generate cryptographically secure random password
function generatePassword() {
  const chars = getAvailableChars()
  const array = new Uint32Array(length.value)
  crypto.getRandomValues(array)
  
  let password = ''
  for (let i = 0; i < length.value; i++) {
    password += chars[array[i] % chars.length]
  }
  
  generatedPassword.value = password
  copied.value = false
}

// Calculate password strength
const passwordStrength = computed(() => {
  const pwd = generatedPassword.value
  if (!pwd) return { level: 0, label: 'None', color: '#999' }
  
  let score = 0
  
  // Length score
  if (pwd.length >= 8) score++
  if (pwd.length >= 12) score++
  if (pwd.length >= 16) score++
  if (pwd.length >= 24) score++
  
  // Character variety
  if (/[a-z]/.test(pwd)) score++
  if (/[A-Z]/.test(pwd)) score++
  if (/[0-9]/.test(pwd)) score++
  if (/[^a-zA-Z0-9]/.test(pwd)) score++
  
  // Penalty for sequential or repeated chars
  if (/(.)\1{2,}/.test(pwd)) score--
  if (/012|123|234|345|456|567|678|789/.test(pwd)) score--
  if (/abc|bcd|cde|def|efg|fgh|ghi|hij|ijk|jkl|klm|lmn|mno|nop|opq|pqr|qrs|rst|stu|tuv|uvw|vwx|wxy|xyz/i.test(pwd)) score--
  
  if (score <= 2) return { level: 1, label: 'Weak', color: '#ef4444' }
  if (score <= 4) return { level: 2, label: 'Fair', color: '#f59e0b' }
  if (score <= 6) return { level: 3, label: 'Strong', color: '#22c55e' }
  return { level: 4, label: 'Very Strong', color: '#10b981' }
})

// Strength bar width
const strengthWidth = computed(() => {
  return `${(passwordStrength.value.level / 4) * 100}%`
})

// Copy to clipboard
async function copyPassword() {
  if (generatedPassword.value) {
    await navigator.clipboard.writeText(generatedPassword.value)
    copied.value = true
    setTimeout(() => {
      copied.value = false
    }, 2000)
  }
}

// Watch options and regenerate on change
watch([length, includeUppercase, includeLowercase, includeNumbers, includeSymbols, excludeAmbiguous], () => {
  generatePassword()
}, { immediate: false })

// At least one option must be selected
const hasValidOption = computed(() => {
  return includeUppercase.value || includeLowercase.value || includeNumbers.value || includeSymbols.value
})

onMounted(() => {
  generatePassword()
})

useHead({
  title: `Password Generator - Tools - ${siteStore.siteName}`,
  meta: [
    { name: 'description', content: 'Generate secure random passwords with customizable options' }
  ]
})
</script>

<template>
  <DefaultLayout>
    <Header :title="siteStore.siteName" />
    <main class="tool-detail">
      <div class="tool-header">
        <div class="tool-icon">🔐</div>
        <div class="tool-info">
          <h1 class="tool-name">Password Generator</h1>
          <p class="tool-description">Generate secure random passwords with customizable options</p>
        </div>
      </div>

      <ElCard class="tool-card">
        <div class="tool-content">
          <!-- Password Display -->
          <div class="password-display" :class="{ copied }">
            <code class="password-text">{{ generatedPassword || 'Click Generate' }}</code>
            <ElButton type="primary" @click="copyPassword" :disabled="!generatedPassword">
              {{ copied ? '✓ Copied' : '📋 Copy' }}
            </ElButton>
          </div>

          <!-- Strength Indicator -->
          <div class="strength-container" v-if="generatedPassword">
            <div class="strength-label">
              <span>Strength:</span>
              <span :style="{ color: passwordStrength.color, fontWeight: 600 }">
                {{ passwordStrength.label }}
              </span>
            </div>
            <div class="strength-bar">
              <div
                class="strength-fill"
                :style="{ width: strengthWidth, backgroundColor: passwordStrength.color }"
              ></div>
            </div>
          </div>

          <!-- Length Slider -->
          <div class="field-group">
            <label class="field-label">
              Length: <strong>{{ length }}</strong> characters
            </label>
            <ElSlider v-model="length" :min="8" :max="64" :step="1" />
            <div class="slider-marks">
              <span>8</span>
              <span>16</span>
              <span>32</span>
              <span>48</span>
              <span>64</span>
            </div>
          </div>

          <!-- Character Options -->
          <div class="field-group">
            <label class="field-label">Character Types</label>
            <div class="checkbox-group">
              <ElCheckbox v-model="includeUppercase">Uppercase (A-Z)</ElCheckbox>
              <ElCheckbox v-model="includeLowercase">Lowercase (a-z)</ElCheckbox>
              <ElCheckbox v-model="includeNumbers">Numbers (0-9)</ElCheckbox>
              <ElCheckbox v-model="includeSymbols">Symbols (!@#$%...)</ElCheckbox>
            </div>
          </div>

          <!-- Exclude Ambiguous -->
          <div class="field-group">
            <ElCheckbox v-model="excludeAmbiguous">
              Exclude ambiguous characters (0, O, l, 1, I)
            </ElCheckbox>
          </div>

          <!-- Validation Alert -->
          <ElAlert
            v-if="!hasValidOption"
            type="warning"
            title="Please select at least one character type"
            show-icon
            closable
            class="warning-alert"
          />

          <!-- Action Buttons -->
          <div class="action-buttons">
            <ElButton type="primary" size="large" @click="generatePassword">
              🔄 Generate
            </ElButton>
          </div>
        </div>
      </ElCard>
    </main>
    <Footer />
  </DefaultLayout>
</template>

<style scoped>
.tool-detail {
  max-width: 700px;
  margin: 0 auto;
}

.tool-header {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  margin-bottom: 32px;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: #fff;
}

.tool-icon {
  font-size: 56px;
  line-height: 1;
}

.tool-info {
  flex: 1;
}

.tool-name {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
}

.tool-description {
  font-size: 16px;
  margin: 0;
  opacity: 0.9;
}

.tool-card {
  border-radius: 12px;
}

.tool-content {
  padding: 8px;
}

.password-display {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 2px solid transparent;
  transition: border-color 0.3s;
}

.password-display.copied {
  border-color: #22c55e;
}

.password-text {
  flex: 1;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 20px;
  word-break: break-all;
  color: #333;
}

.strength-container {
  margin-top: 20px;
}

.strength-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.strength-bar {
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s, background-color 0.3s;
}

.field-group {
  margin-top: 24px;
}

.field-label {
  display: block;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  font-size: 14px;
}

.slider-marks {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.checkbox-group {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.checkbox-group :deep(.el-checkbox) {
  margin-right: 0;
}

.warning-alert {
  margin-top: 20px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

.action-buttons :deep(.el-button) {
  min-width: 200px;
}

@media (max-width: 768px) {
  .tool-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .tool-name {
    font-size: 24px;
  }

  .checkbox-group {
    grid-template-columns: 1fr;
  }

  .password-text {
    font-size: 16px;
  }
}
</style>