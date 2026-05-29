<script setup lang="ts">
import { ref, computed } from 'vue'
import { useHead } from '@unhead/vue'
import { ElCard, ElInput, ElButton, ElSelect, ElOption, ElAlert, ElDatePicker } from 'element-plus'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
import { useSiteStore } from '@/stores/site'

const siteStore = useSiteStore()

// Input state
const timestampInput = ref('')
const dateInput = ref('')
const timezone = ref('local')
const error = ref<string | null>(null)

// Timezone options
const timezones = [
  { value: 'local', label: 'Local Time' },
  { value: 'utc', label: 'UTC' },
  { value: 'America/New_York', label: 'Eastern Time' },
  { value: 'America/Chicago', label: 'Central Time' },
  { value: 'America/Denver', label: 'Mountain Time' },
  { value: 'America/Los_Angeles', label: 'Pacific Time' },
  { value: 'Europe/London', label: 'London' },
  { value: 'Europe/Paris', label: 'Paris' },
  { value: 'Asia/Tokyo', label: 'Tokyo' },
  { value: 'Asia/Shanghai', label: 'Shanghai' },
  { value: 'Asia/Singapore', label: 'Singapore' },
  { value: 'Australia/Sydney', label: 'Sydney' }
]

// Detect if input is milliseconds or seconds
function detectTimestampUnit(ts: string): 'ms' | 's' | null {
  if (!ts || !/^\d+$/.test(ts)) return null
  const num = parseInt(ts, 10)
  // If it's 13 digits or less but greater than 10000000000, likely milliseconds
  if (ts.length >= 13 && num > 1000000000000) return 'ms'
  // If it's 10 digits or less and reasonable for seconds, likely seconds
  if (ts.length <= 10 && num < 10000000000) return 's'
  // Default: check if greater than year 2001 in seconds
  if (num > 1000000000) return 's'
  return 'ms'
}

// Convert timestamp to date
const timestampToDate = computed(() => {
  if (!timestampInput.value) return ''
  error.value = null
  
  const unit = detectTimestampUnit(timestampInput.value)
  if (!unit) {
    error.value = 'Invalid timestamp format'
    return ''
  }
  
  try {
    const ts = parseInt(timestampInput.value, 10)
    const ms = unit === 's' ? ts * 1000 : ts
    const date = new Date(ms)
    
    if (isNaN(date.getTime())) {
      error.value = 'Invalid timestamp value'
      return ''
    }
    
    return formatDateByTimezone(date)
  } catch {
    error.value = 'Invalid timestamp'
    return ''
  }
})

// Convert date to timestamp
const dateToTimestamp = computed(() => {
  if (!dateInput.value) return ''
  error.value = null
  
  try {
    const date = new Date(dateInput.value)
    if (isNaN(date.getTime())) {
      error.value = 'Invalid date'
      return ''
    }
    return date.getTime().toString()
  } catch {
    error.value = 'Invalid date'
    return ''
  }
})

// Get current timestamp
const currentTimestamp = computed(() => {
  return Date.now().toString()
})

function formatDateByTimezone(date: Date): string {
  if (timezone.value === 'utc') {
    return date.toISOString()
  }
  return date.toLocaleString('en-US', { timeZone: timezone.value })
}

function insertCurrentTimestamp() {
  timestampInput.value = Math.floor(Date.now() / 1000).toString()
}

function copyTimestamp() {
  if (dateToTimestamp.value) {
    navigator.clipboard.writeText(dateToTimestamp.value)
  }
}

function copyDate() {
  if (timestampToDate.value) {
    navigator.clipboard.writeText(timestampToDate.value)
  }
}

function clearAll() {
  timestampInput.value = ''
  dateInput.value = ''
  error.value = null
}

useHead({
  title: `Timestamp Converter - Tools - ${siteStore.siteName}`,
  meta: [
    { name: 'description', content: 'Convert between Unix timestamp and human-readable date' }
  ]
})
</script>

<template>
  <DefaultLayout>
    <Header :title="siteStore.siteName" />
    <main class="tool-detail">
      <div class="tool-header">
        <div class="tool-icon">🕐</div>
        <div class="tool-info">
          <h1 class="tool-name">Timestamp Converter</h1>
          <p class="tool-description">Convert between Unix timestamp and human-readable date</p>
        </div>
      </div>

      <ElCard class="tool-card">
        <div class="tool-content">
          <!-- Timezone Selector -->
          <div class="field-group">
            <label class="field-label">Timezone</label>
            <ElSelect v-model="timezone" class="timezone-select">
              <ElOption
                v-for="tz in timezones"
                :key="tz.value"
                :value="tz.value"
                :label="tz.label"
              />
            </ElSelect>
          </div>

          <!-- Timestamp to Date -->
          <div class="field-group">
            <div class="field-header">
              <label class="field-label">Timestamp</label>
              <ElButton size="small" @click="insertCurrentTimestamp">Now</ElButton>
            </div>
            <ElInput
              v-model="timestampInput"
              placeholder="Enter Unix timestamp (seconds or milliseconds)"
              class="input-field"
            />
            <p class="field-hint">Supports both 10-digit (seconds) and 13-digit (milliseconds)</p>
          </div>

          <div class="arrow-down">↓</div>

          <!-- Date Display -->
          <div class="field-group result-group">
            <label class="field-label">Date & Time</label>
            <div class="result-display">
              <span class="result-value">{{ timestampToDate || '—' }}</span>
              <ElButton
                v-if="timestampToDate"
                size="small"
                @click="copyDate"
              >
                📋 Copy
              </ElButton>
            </div>
          </div>

          <div class="divider"></div>

          <!-- Date to Timestamp -->
          <div class="field-group">
            <label class="field-label">Select Date & Time</label>
            <ElDatePicker
              v-model="dateInput"
              type="datetime"
              placeholder="Pick a date and time"
              class="date-picker"
            />
          </div>

          <div class="arrow-down">↓</div>

          <!-- Timestamp Display -->
          <div class="field-group result-group">
            <label class="field-label">Timestamp</label>
            <div class="result-display">
              <span class="result-value">{{ dateToTimestamp || '—' }}</span>
              <ElButton
                v-if="dateToTimestamp"
                size="small"
                @click="copyTimestamp"
              >
                📋 Copy
              </ElButton>
            </div>
          </div>

          <!-- Error Display -->
          <ElAlert
            v-if="error"
            type="error"
            :title="error"
            show-icon
            closable
            class="error-alert"
          />

          <!-- Current Time -->
          <div class="current-time">
            <span class="current-label">Current timestamp:</span>
            <code class="current-value" @click="insertCurrentTimestamp">{{ currentTimestamp }}</code>
            <ElButton size="small" @click="insertCurrentTimestamp">Use</ElButton>
          </div>

          <div class="action-buttons">
            <ElButton @click="clearAll">🗑️ Clear</ElButton>
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

.field-group {
  margin-bottom: 20px;
}

.field-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.field-label {
  display: block;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  font-size: 14px;
}

.field-header .field-label {
  margin-bottom: 0;
}

.field-hint {
  color: #999;
  font-size: 12px;
  margin: 6px 0 0;
}

.input-field {
  font-family: 'Fira Code', 'Consolas', monospace;
}

.input-field :deep(input) {
  font-family: 'Fira Code', 'Consolas', monospace;
}

.timezone-select {
  width: 100%;
  max-width: 300px;
}

.date-picker {
  width: 100%;
}

.arrow-down {
  text-align: center;
  font-size: 20px;
  color: #667eea;
  margin: 8px 0;
}

.result-group {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
}

.result-display {
  display: flex;
  align-items: center;
  gap: 12px;
}

.result-value {
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 16px;
  color: #333;
  flex: 1;
}

.divider {
  height: 1px;
  background: #eee;
  margin: 24px 0;
}

.current-time {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f0f0f0;
  border-radius: 8px;
  margin-top: 16px;
}

.current-label {
  font-size: 14px;
  color: #666;
}

.current-value {
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 14px;
  background: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
}

.current-value:hover {
  background: #e0e0e0;
}

.error-alert {
  margin: 16px 0;
}

.action-buttons {
  display: flex;
  justify-content: center;
  margin-top: 24px;
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

  .current-time {
    flex-wrap: wrap;
  }
}
</style>