<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useHead } from '@unhead/vue'
import { ElCard, ElInput, ElButton, ElAlert } from 'element-plus'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
import { useSiteStore } from '@/stores/site'

const siteStore = useSiteStore()

// Color values
const hexValue = ref('#667eea')
const rgbValue = ref({ r: 102, g: 126, b: 234 })
const hslValue = ref({ h: 229, s: 76, l: 66 })

// Recent colors
const recentColors = ref<string[]>([])
const copiedFormat = ref<string | null>(null)

// Validation
const hexError = ref('')
const rgbError = ref('')
const hslError = ref('')

// Active input (to prevent circular updates)
const activeInput = ref<'hex' | 'rgb' | 'hsl' | null>(null)

// Parse HEX to RGB
function hexToRgb(hex: string): { r: number; g: number; b: number } | null {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  return result
    ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16)
      }
    : null
}

// Convert RGB to HEX
function rgbToHex(r: number, g: number, b: number): string {
  return '#' + [r, g, b].map(x => {
    const hex = Math.max(0, Math.min(255, Math.round(x))).toString(16)
    return hex.length === 1 ? '0' + hex : hex
  }).join('')
}

// Convert RGB to HSL
function rgbToHsl(r: number, g: number, b: number): { h: number; s: number; l: number } {
  r /= 255
  g /= 255
  b /= 255

  const max = Math.max(r, g, b)
  const min = Math.min(r, g, b)
  let h = 0
  let s = 0
  const l = (max + min) / 2

  if (max !== min) {
    const d = max - min
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min)

    switch (max) {
      case r:
        h = ((g - b) / d + (g < b ? 6 : 0)) / 6
        break
      case g:
        h = ((b - r) / d + 2) / 6
        break
      case b:
        h = ((r - g) / d + 4) / 6
        break
    }
  }

  return {
    h: Math.round(h * 360),
    s: Math.round(s * 100),
    l: Math.round(l * 100)
  }
}

// Convert HSL to RGB
function hslToRgb(h: number, s: number, l: number): { r: number; g: number; b: number } {
  h /= 360
  s /= 100
  l /= 100

  let r, g, b

  if (s === 0) {
    r = g = b = l
  } else {
    const hue2rgb = (p: number, q: number, t: number) => {
      if (t < 0) t += 1
      if (t > 1) t -= 1
      if (t < 1 / 6) return p + (q - p) * 6 * t
      if (t < 1 / 2) return q
      if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6
      return p
    }

    const q = l < 0.5 ? l * (1 + s) : l + s - l * s
    const p = 2 * l - q
    r = hue2rgb(p, q, h + 1 / 3)
    g = hue2rgb(p, q, h)
    b = hue2rgb(p, q, h - 1 / 3)
  }

  return {
    r: Math.round(r * 255),
    g: Math.round(g * 255),
    b: Math.round(b * 255)
  }
}

// Validate HEX
function isValidHex(hex: string): boolean {
  return /^#?([a-f\d]{6}|[a-f\d]{3})$/i.test(hex)
}

// Open color picker
function openColorPicker() {
  const input = document.querySelector('.color-input[type=color]') as HTMLInputElement
  if (input) {
    input.dispatchEvent(new MouseEvent('click'))
  }
}

// Handle HEX input
function onHexInput() {
  activeInput.value = 'hex'
  hexError.value = ''
  
  let hex = hexValue.value.trim()
  if (!hex.startsWith('#')) hex = '#' + hex
  
  if (!isValidHex(hex)) {
    hexError.value = 'Invalid HEX format'
    activeInput.value = null
    return
  }
  
  // Expand 3-char hex to 6-char
  if (hex.length === 4) {
    hex = '#' + hex[1] + hex[1] + hex[2] + hex[2] + hex[3] + hex[3]
  }
  
  hexValue.value = hex
  
  const rgb = hexToRgb(hex)
  if (rgb) {
    rgbValue.value = rgb
    hslValue.value = rgbToHsl(rgb.r, rgb.g, rgb.b)
    addToRecent(hex)
  }
  
  activeInput.value = null
}

// Handle RGB input
function onRgbInput() {
  activeInput.value = 'rgb'
  rgbError.value = ''
  
  const { r, g, b } = rgbValue.value
  
  if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
    rgbError.value = 'Values must be between 0-255'
    activeInput.value = null
    return
  }
  
  hexValue.value = rgbToHex(r, g, b)
  hslValue.value = rgbToHsl(r, g, b)
  addToRecent(hexValue.value)
  
  activeInput.value = null
}

// Handle HSL input
function onHslInput() {
  activeInput.value = 'hsl'
  hslError.value = ''
  
  const { h, s, l } = hslValue.value
  
  if (h < 0 || h > 360 || s < 0 || s > 100 || l < 0 || l > 100) {
    hslError.value = 'H: 0-360, S: 0-100, L: 0-100'
    activeInput.value = null
    return
  }
  
  const rgb = hslToRgb(h, s, l)
  rgbValue.value = rgb
  hexValue.value = rgbToHex(rgb.r, rgb.g, rgb.b)
  addToRecent(hexValue.value)
  
  activeInput.value = null
}

// Add to recent colors
function addToRecent(color: string) {
  const normalized = color.toLowerCase()
  recentColors.value = [normalized, ...recentColors.value.filter(c => c !== normalized)].slice(0, 12)
  sessionStorage.setItem('color-history', JSON.stringify(recentColors.value))
}

// Copy to clipboard
async function copyFormat(format: string) {
  let text = ''
  
  switch (format) {
    case 'hex':
      text = hexValue.value
      break
    case 'rgb':
      text = `rgb(${rgbValue.value.r}, ${rgbValue.value.g}, ${rgbValue.value.b})`
      break
    case 'hsl':
      text = `hsl(${hslValue.value.h}, ${hslValue.value.s}%, ${hslValue.value.l}%)`
      break
  }
  
  await navigator.clipboard.writeText(text)
  copiedFormat.value = format
  setTimeout(() => {
    copiedFormat.value = null
  }, 2000)
}

// Load recent colors
onMounted(() => {
  const saved = sessionStorage.getItem('color-history')
  if (saved) {
    try {
      recentColors.value = JSON.parse(saved)
    } catch {
      recentColors.value = []
    }
  }
})

useHead({
  title: `Color Converter - Tools - ${siteStore.siteName}`,
  meta: [
    { name: 'description', content: 'Convert between HEX, RGB, and HSL color formats' }
  ]
})
</script>

<template>
  <DefaultLayout>
    <Header :title="siteStore.siteName" />
    <main class="tool-detail">
      <div class="tool-header">
        <div class="tool-icon">🎨</div>
        <div class="tool-info">
          <h1 class="tool-name">Color Converter</h1>
          <p class="tool-description">Convert between HEX, RGB, and HSL color formats</p>
        </div>
      </div>

      <ElCard class="tool-card">
        <div class="tool-content">
          <!-- Color Preview -->
          <div class="color-preview-container">
          <div
            class="color-preview"
            :style="{ backgroundColor: hexValue }"
            @click="openColorPicker"
          ></div>
            <input
              type="color"
              class="color-input"
              v-model="hexValue"
              @input="onHexInput"
            />
            <span class="preview-label">Click swatch to pick</span>
          </div>

          <!-- HEX Input -->
          <div class="field-group">
            <label class="field-label">HEX</label>
            <div class="input-row">
              <ElInput
                v-model="hexValue"
                placeholder="#667eea"
                class="color-input-field"
                @blur="onHexInput"
                @keyup.enter="onHexInput"
              />
              <ElButton @click="copyFormat('hex')">
                {{ copiedFormat === 'hex' ? '✓' : '📋 Copy' }}
              </ElButton>
            </div>
            <ElAlert v-if="hexError" type="error" :title="hexError" show-icon closable class="field-error" />
          </div>

          <!-- RGB Input -->
          <div class="field-group">
            <label class="field-label">RGB</label>
            <div class="rgb-inputs">
              <div class="rgb-field">
                <span class="rgb-label">R</span>
                <ElInput
                  v-model.number="rgbValue.r"
                  type="number"
                  :min="0"
                  :max="255"
                  class="rgb-number"
                  @blur="onRgbInput"
                />
              </div>
              <div class="rgb-field">
                <span class="rgb-label">G</span>
                <ElInput
                  v-model.number="rgbValue.g"
                  type="number"
                  :min="0"
                  :max="255"
                  class="rgb-number"
                  @blur="onRgbInput"
                />
              </div>
              <div class="rgb-field">
                <span class="rgb-label">B</span>
                <ElInput
                  v-model.number="rgbValue.b"
                  type="number"
                  :min="0"
                  :max="255"
                  class="rgb-number"
                  @blur="onRgbInput"
                />
              </div>
            </div>
            <div class="input-row">
              <ElButton @click="copyFormat('rgb')">
                {{ copiedFormat === 'rgb' ? '✓' : '📋 Copy' }}
              </ElButton>
            </div>
            <ElAlert v-if="rgbError" type="error" :title="rgbError" show-icon closable class="field-error" />
          </div>

          <!-- HSL Input -->
          <div class="field-group">
            <label class="field-label">HSL</label>
            <div class="hsl-inputs">
              <div class="hsl-field">
                <span class="hsl-label">H</span>
                <ElInput
                  v-model.number="hslValue.h"
                  type="number"
                  :min="0"
                  :max="360"
                  class="hsl-number"
                  @blur="onHslInput"
                />
                <span class="unit">°</span>
              </div>
              <div class="hsl-field">
                <span class="hsl-label">S</span>
                <ElInput
                  v-model.number="hslValue.s"
                  type="number"
                  :min="0"
                  :max="100"
                  class="hsl-number"
                  @blur="onHslInput"
                />
                <span class="unit">%</span>
              </div>
              <div class="hsl-field">
                <span class="hsl-label">L</span>
                <ElInput
                  v-model.number="hslValue.l"
                  type="number"
                  :min="0"
                  :max="100"
                  class="hsl-number"
                  @blur="onHslInput"
                />
                <span class="unit">%</span>
              </div>
            </div>
            <div class="input-row">
              <ElButton @click="copyFormat('hsl')">
                {{ copiedFormat === 'hsl' ? '✓' : '📋 Copy' }}
              </ElButton>
            </div>
            <ElAlert v-if="hslError" type="error" :title="hslError" show-icon closable class="field-error" />
          </div>

          <!-- Recent Colors -->
          <div class="recent-section" v-if="recentColors.length > 0">
            <h3 class="recent-title">Recent Colors</h3>
            <div class="recent-palette">
              <div
                v-for="color in recentColors"
                :key="color"
                class="recent-swatch"
                :style="{ backgroundColor: color }"
                :title="color"
                @click="hexValue = color; onHexInput()"
              ></div>
            </div>
          </div>
        </div>
      </ElCard>
    </main>
    <Footer />
  </DefaultLayout>
</template>

<style scoped>
.tool-detail {
  max-width: 600px;
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

.color-preview-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
}

.color-preview {
  width: 120px;
  height: 120px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  transition: transform 0.2s;
}

.color-preview:hover {
  transform: scale(1.05);
}

.color-input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.preview-label {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

.field-group {
  margin-bottom: 20px;
}

.field-label {
  display: block;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  font-size: 14px;
}

.input-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.color-input-field {
  font-family: 'Fira Code', 'Consolas', monospace;
}

.color-input-field :deep(input) {
  font-family: 'Fira Code', 'Consolas', monospace;
}

.rgb-inputs,
.hsl-inputs {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.rgb-field,
.hsl-field {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.rgb-label,
.hsl-label {
  font-size: 12px;
  font-weight: 600;
  color: #666;
  min-width: 16px;
}

.rgb-number,
.hsl-number {
  width: 100%;
}

.rgb-number :deep(input),
.hsl-number :deep(input) {
  text-align: center;
  font-family: 'Fira Code', 'Consolas', monospace;
}

.unit {
  font-size: 14px;
  color: #999;
}

.field-error {
  margin-top: 8px;
}

.recent-section {
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.recent-title {
  font-size: 14px;
  font-weight: 600;
  color: #666;
  margin: 0 0 12px;
}

.recent-palette {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.recent-swatch {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.recent-swatch:hover {
  transform: scale(1.15);
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

  .rgb-inputs,
  .hsl-inputs {
    flex-direction: column;
    gap: 8px;
  }

  .rgb-field,
  .hsl-field {
    justify-content: center;
  }
}
</style>