<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useHead } from '@unhead/vue'
import { ElCard, ElInput, ElSelect, ElOption, ElAlert } from 'element-plus'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
import { useSiteStore } from '@/stores/site'

const siteStore = useSiteStore()

// Cron expression input
const cronExpression = ref('* * * * *')
const error = ref('')
const highlightedField = ref<number | null>(null)

// Examples
const examples = [
  { value: '* * * * *', label: 'Every minute' },
  { value: '0 * * * *', label: 'Every hour' },
  { value: '0 0 * * *', label: 'Every day at midnight' },
  { value: '0 9 * * 1', label: 'Every Monday at 9 AM' },
  { value: '0 0 1 * *', label: 'First day of month' },
  { value: '*/5 * * * *', label: 'Every 5 minutes' },
  { value: '0 */2 * * *', label: 'Every 2 hours' },
  { value: '30 4 1,15 * *', label: '4:30 AM on 1st and 15th' }
]

// Field names and descriptions
const fieldInfo = [
  { name: 'Minute', range: '0-59', description: 'Minute of the hour' },
  { name: 'Hour', range: '0-23', description: 'Hour of the day (24h)' },
  { name: 'Day', range: '1-31', description: 'Day of the month' },
  { name: 'Month', range: '1-12', description: 'Month of the year' },
  { name: 'Weekday', range: '0-6', description: 'Day of the week (0=Sun)' }
]

// Parse a single field
function parseField(value: string, fieldName: string): string[] {
  if (value === '*') return [`Every ${fieldName.toLowerCase()}`]
  
  const parts: string[] = []
  
  // Handle step values (*/5, 1-10/2)
  const stepMatch = value.match(/^(.+)\/(\d+)$/)
  let baseValue = value
  let step = 1
  
  if (stepMatch) {
    baseValue = stepMatch[1]
    step = parseInt(stepMatch[2])
  }
  
  // Handle range (1-5)
  if (baseValue.includes('-')) {
    const [start, end] = baseValue.split('-').map(Number)
    if (step === 1) {
      parts.push(`${fieldName} ${start} through ${end}`)
    } else {
      parts.push(`Every ${step} ${fieldName.toLowerCase()}s from ${start} to ${end}`)
    }
  }
  // Handle list (1,3,5)
  else if (baseValue.includes(',')) {
    const vals = baseValue.split(',')
    parts.push(`${fieldName}: ${vals.join(', ')}`)
  }
  // Handle single value
  else {
    parts.push(`${fieldName} ${baseValue}`)
  }
  
  // Add step info if applicable
  if (step > 1) {
    parts.push(`step of ${step}`)
  }
  
  return parts
}

// Human readable explanation
const explanation = computed(() => {
  if (!cronExpression.value) return []
  
  const fields = cronExpression.value.trim().split(/\s+/)
  if (fields.length !== 5) {
    return []
  }
  
  const result: { field: string; range: string; description: string; value: string; parts: string[] }[] = []
  
  for (let i = 0; i < 5; i++) {
    result.push({
      field: fieldInfo[i].name,
      range: fieldInfo[i].range,
      description: fieldInfo[i].description,
      value: fields[i],
      parts: parseField(fields[i], fieldInfo[i].name)
    })
  }
  
  return result
})

// Calculate next N execution times
function getNextExecutions(count: number = 5): string[] {
  if (!cronExpression.value) return []
  
  const fields = cronExpression.value.trim().split(/\s+/)
  if (fields.length !== 5) return []
  
  const [minute, hour, day, month, dayOfWeek] = fields
  const executions: string[] = []
  const now = new Date()
  
  // Simple cron parser for common cases
  const parseFieldValues = (field: string, min: number, max: number): number[] => {
    if (field === '*') {
      const values: number[] = []
      for (let i = min; i <= max; i++) values.push(i)
      return values
    }
    
    const values = new Set<number>()
    
    // Handle step (e.g., */5)
    const stepMatch = field.match(/^\*\/(\d+)$/)
    if (stepMatch) {
      const step = parseInt(stepMatch[1])
      for (let i = min; i <= max; i += step) values.add(i)
      return Array.from(values).sort((a, b) => a - b)
    }
    
    // Handle list
    const parts = field.split(',')
    for (const part of parts) {
      // Handle range
      const rangeMatch = part.match(/^(\d+)-(\d+)$/)
      if (rangeMatch) {
        const start = parseInt(rangeMatch[1])
        const end = parseInt(rangeMatch[2])
        for (let i = start; i <= end; i++) values.add(i)
      }
      // Handle single
      else if (/^\d+$/.test(part)) {
        values.add(parseInt(part))
      }
    }
    
    return Array.from(values).sort((a, b) => a - b)
  }
  
  const minutes = parseFieldValues(minute, 0, 59)
  const hours = parseFieldValues(hour, 0, 23)
  const days = parseFieldValues(day, 1, 31)
  const months = parseFieldValues(month, 1, 12)
  const weekdays = parseFieldValues(dayOfWeek, 0, 6)
  
  // Find next execution times
  const current = new Date(now)
  current.setSeconds(0)
  current.setMilliseconds(0)
  
  let iterations = 0
  const maxIterations = 100000 // Prevent infinite loop
  
  while (executions.length < count && iterations < maxIterations) {
    iterations++
    current.setMinutes(current.getMinutes() + 1)
    
    const m = current.getMinutes()
    const h = current.getHours()
    const d = current.getDate()
    const mo = current.getMonth() + 1
    const dw = current.getDay()
    
    if (minutes.includes(m) &&
        hours.includes(h) &&
        days.includes(d) &&
        months.includes(mo) &&
        weekdays.includes(dw)) {
      executions.push(current.toLocaleString('en-US', {
        weekday: 'short',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      }))
    }
  }
  
  return executions
}

const nextExecutions = computed(() => getNextExecutions(5))

// Validate cron expression
function validateCron() {
  if (!cronExpression.value) {
    error.value = ''
    return
  }
  
  const fields = cronExpression.value.trim().split(/\s+/)
  if (fields.length !== 5) {
    error.value = 'Cron expression must have exactly 5 fields'
    return
  }
  
  // Basic validation
  const patterns = [
    /^(\*|[0-5]?\d)(-\d+)?(\/\d+)?(,[0-5]?\d(-\d+)?(\/\d+)?)*$/, // minute
    /^(\*|[01]?\d|2[0-3])(-\d+)?(\/\d+)?(,[01]?\d|2[0-3](-\d+)?(\/\d+)?)*$/, // hour
    /^(\*|[12]?\d|3[01])(-\d+)?(\/\d+)?(,[12]?\d|3[01](-\d+)?(\/\d+)?)*$/, // day
    /^(\*|[1-9]|1[0-2])(-\d+)?(\/\d+)?(,[1-9]|1[0-2](-\d+)?(\/\d+)?)*$/, // month
    /^(\*|[0-6])(-\d+)?(\/\d+)?(,[0-6](-\d+)?(\/\d+)?)*$/ // weekday
  ]
  
  for (let i = 0; i < 5; i++) {
    if (!patterns[i].test(fields[i])) {
      error.value = `Invalid ${fieldInfo[i].name.toLowerCase()} field: ${fields[i]}`
      return
    }
  }
  
  error.value = ''
}

// Watch for changes
watch(cronExpression, () => {
  validateCron()
})

// Load default
onMounted(() => {
  validateCron()
})

useHead({
  title: `Cron Parser - Tools - ${siteStore.siteName}`,
  meta: [
    { name: 'description', content: 'Parse and explain cron expressions with next execution times' }
  ]
})
</script>

<template>
  <DefaultLayout>
    <Header :title="siteStore.siteName" />
    <main class="tool-detail">
      <div class="tool-header">
        <div class="tool-icon">⏰</div>
        <div class="tool-info">
          <h1 class="tool-name">Cron Parser</h1>
          <p class="tool-description">Parse and explain cron expressions with next execution times</p>
        </div>
      </div>

      <ElCard class="tool-card">
        <div class="tool-content">
          <!-- Cron Input -->
          <div class="field-group">
            <label class="field-label">Cron Expression</label>
            <div class="cron-input-row">
              <ElInput
                v-model="cronExpression"
                placeholder="* * * * *"
                class="cron-input"
              />
              <ElSelect
                v-model="cronExpression"
                placeholder="Examples"
                class="examples-select"
              >
                <ElOption
                  v-for="ex in examples"
                  :key="ex.value"
                  :value="ex.value"
                  :label="ex.label"
                />
              </ElSelect>
            </div>
          </div>

          <!-- Error Alert -->
          <ElAlert
            v-if="error"
            type="error"
            :title="error"
            show-icon
            closable
            class="error-alert"
          />

          <!-- Field Explanation -->
          <div class="explanation-section" v-if="explanation.length > 0 && !error">
            <h3 class="section-title">Expression Breakdown</h3>
            <div class="fields-grid">
              <div
                v-for="(field, index) in explanation"
                :key="index"
                class="field-card"
                :class="{ highlighted: highlightedField === index }"
                @mouseenter="highlightedField = index"
                @mouseleave="highlightedField = null"
              >
                <div class="field-header">
                  <span class="field-name">{{ field.field }}</span>
                  <span class="field-range">({{ field.range }})</span>
                </div>
                <code class="field-value">{{ field.value }}</code>
                <div class="field-description">{{ field.description }}</div>
                <div class="field-meaning">
                  <span v-for="(part, pIdx) in field.parts" :key="pIdx" class="meaning-part">
                    {{ part }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Next Executions -->
          <div class="executions-section" v-if="nextExecutions.length > 0 && !error">
            <h3 class="section-title">Next 5 Executions</h3>
            <div class="executions-list">
              <div
                v-for="(time, index) in nextExecutions"
                :key="index"
                class="execution-item"
              >
                <span class="execution-index">{{ index + 1 }}</span>
                <span class="execution-time">{{ time }}</span>
              </div>
            </div>
          </div>

          <!-- Quick Reference -->
          <div class="reference-section">
            <h3 class="section-title">Quick Reference</h3>
            <div class="reference-grid">
              <div class="ref-item">
                <code>*</code>
                <span>Any value</span>
              </div>
              <div class="ref-item">
                <code>,</code>
                <span>Value list (1,3,5)</span>
              </div>
              <div class="ref-item">
                <code>-</code>
                <span>Range (1-5)</span>
              </div>
              <div class="ref-item">
                <code>/</code>
                <span>Step (*/5)</span>
              </div>
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
  max-width: 800px;
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

.field-label {
  display: block;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  font-size: 14px;
}

.cron-input-row {
  display: flex;
  gap: 12px;
}

.cron-input {
  flex: 1;
  font-family: 'Fira Code', 'Consolas', monospace;
}

.cron-input :deep(input) {
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 18px;
  letter-spacing: 2px;
}

.examples-select {
  width: 200px;
}

.error-alert {
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px;
}

.fields-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin-bottom: 28px;
}

.field-card {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.field-card.highlighted {
  border-color: #667eea;
  background: #f0f0ff;
}

.field-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.field-name {
  font-weight: 600;
  font-size: 13px;
  color: #333;
}

.field-range {
  font-size: 11px;
  color: #999;
}

.field-value {
  display: block;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 16px;
  color: #667eea;
  text-align: center;
  padding: 8px;
  background: #fff;
  border-radius: 4px;
  margin-bottom: 8px;
}

.field-description {
  font-size: 11px;
  color: #666;
  margin-bottom: 8px;
}

.field-meaning {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meaning-part {
  font-size: 11px;
  color: #888;
  background: #fff;
  padding: 2px 6px;
  border-radius: 3px;
  text-align: center;
}

.executions-section {
  margin-bottom: 28px;
}

.executions-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.execution-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.execution-index {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #667eea;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  border-radius: 50%;
}

.execution-time {
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 14px;
  color: #333;
}

.reference-section {
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.reference-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.ref-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.ref-item code {
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 16px;
  color: #667eea;
  background: #fff;
  padding: 4px 8px;
  border-radius: 4px;
}

.ref-item span {
  font-size: 12px;
  color: #666;
  text-align: center;
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

  .cron-input-row {
    flex-direction: column;
  }

  .examples-select {
    width: 100%;
  }

  .fields-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .reference-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>