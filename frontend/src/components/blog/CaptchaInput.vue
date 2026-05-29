<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElInput, ElButton } from 'element-plus'

const emit = defineEmits<{
  (e: 'verified', valid: boolean): void
}>()

const props = defineProps<{
  code?: string
}>()

const canvasRef = ref<HTMLCanvasElement | null>(null)
const inputValue = ref('')
const localCode = ref('')

const characters = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789'

function generateCode(): string {
  let code = ''
  for (let i = 0; i < 4; i++) {
    code += characters.charAt(Math.floor(Math.random() * characters.length))
  }
  return code
}

function drawNoise(ctx: CanvasRenderingContext2D, width: number, height: number) {
  for (let i = 0; i < 8; i++) {
    ctx.strokeStyle = `rgba(${Math.random() * 100 + 150}, ${Math.random() * 100 + 150}, ${Math.random() * 100 + 180}, 0.3)`
    ctx.lineWidth = Math.random() * 2 + 0.5
    ctx.beginPath()
    ctx.moveTo(Math.random() * width, Math.random() * height)
    ctx.bezierCurveTo(
      Math.random() * width, Math.random() * height,
      Math.random() * width, Math.random() * height,
      Math.random() * width, Math.random() * height
    )
    ctx.stroke()
  }
}

function drawDots(ctx: CanvasRenderingContext2D, width: number, height: number) {
  for (let i = 0; i < 30; i++) {
    ctx.fillStyle = `rgba(${Math.random() * 100 + 100}, ${Math.random() * 100 + 100}, ${Math.random() * 100 + 150}, 0.4)`
    ctx.beginPath()
    ctx.arc(Math.random() * width, Math.random() * height, Math.random() * 1.5 + 0.5, 0, Math.PI * 2)
    ctx.fill()
  }
}

function generateCaptcha() {
  const canvas = canvasRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  // Use provided code or generate new one
  localCode.value = generateCode()
  inputValue.value = ''

  const width = 120
  const height = 40

  canvas.width = width
  canvas.height = height

  ctx.fillStyle = `rgb(${Math.random() * 20 + 230}, ${Math.random() * 20 + 230}, ${Math.random() * 20 + 240})`
  ctx.fillRect(0, 0, width, height)

  drawNoise(ctx, width, height)
  drawDots(ctx, width, height)

  ctx.font = 'bold 24px Arial, sans-serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'

  const chars = localCode.value.split('')
  const spacing = width / (chars.length + 1)

  chars.forEach((char, i) => {
    ctx.save()
    ctx.translate(spacing * (i + 1), height / 2)
    ctx.rotate((Math.random() - 0.5) * 0.4)
    ctx.fillStyle = `rgb(${Math.random() * 50 + 30}, ${Math.random() * 50 + 30}, ${Math.random() * 50 + 50})`
    ctx.fillText(char, 0, 0)
    ctx.restore()
  })
}

function verify(): boolean {
  const valid = inputValue.value.toLowerCase() === localCode.value.toLowerCase()
  emit('verified', valid)
  return valid
}

function getCode(): string {
  return localCode.value
}

defineExpose({
  verify,
  regenerate: generateCaptcha,
  getCode
})

watch(() => props.code, () => {
  generateCaptcha()
}, { immediate: true })

onMounted(() => {
  if (!props.code) {
    generateCaptcha()
  }
})
</script>

<template>
  <div class="captcha-input">
    <div class="captcha-display">
      <canvas ref="canvasRef" class="captcha-canvas" />
      <ElButton
        size="small"
        text
        class="refresh-btn"
        @click="generateCaptcha"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M23 4v6h-6M1 20v-6h6"/>
          <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
        </svg>
      </ElButton>
    </div>
    <ElInput
      v-model="inputValue"
      placeholder="Enter captcha"
      maxlength="4"
      class="captcha-field"
      @keyup.enter="verify"
    />
  </div>
</template>

<style scoped>
.captcha-input {
  display: flex;
  align-items: center;
  gap: 12px;
}

.captcha-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.captcha-canvas {
  border-radius: 4px;
  display: block;
}

.refresh-btn {
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.refresh-btn svg {
  transition: transform 0.3s ease;
}

.refresh-btn:hover svg {
  transform: rotate(180deg);
}

.captcha-field {
  width: 120px;
}

.captcha-field :deep(.el-input__inner) {
  text-transform: uppercase;
  letter-spacing: 2px;
  font-weight: 600;
}
</style>