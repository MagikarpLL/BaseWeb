<script setup lang="ts">
import { ref, nextTick } from 'vue'
import QRCode from 'qrcode'

interface Props {
  title: string
  excerpt?: string
  url: string
}

const props = defineProps<Props>()

const showQR = ref(false)
const qrCanvas = ref<HTMLCanvasElement | null>(null)
const copySuccess = ref(false)

function shareToTwitter() {
  const encodedTitle = encodeURIComponent(props.title)
  const encodedUrl = encodeURIComponent(props.url)
  window.open(
    `https://twitter.com/intent/tweet?text=${encodedTitle}&url=${encodedUrl}`,
    '_blank',
    'noopener,noreferrer'
  )
}

function shareToWeibo() {
  const encodedUrl = encodeURIComponent(props.url)
  const encodedTitle = encodeURIComponent(props.title)
  window.open(
    `https://service.weibo.com/share/share.php?url=${encodedUrl}&title=${encodedTitle}`,
    '_blank',
    'noopener,noreferrer'
  )
}

function shareToQQ() {
  const encodedUrl = encodeURIComponent(props.url)
  const encodedTitle = encodeURIComponent(props.title)
  window.open(
    `https://connect.qq.com/widget/share/iframe/choose.html?url=${encodedUrl}&title=${encodedTitle}`,
    '_blank',
    'noopener,noreferrer'
  )
}

async function showWechatQR() {
  showQR.value = true
  await nextTick()
  if (qrCanvas.value) {
    QRCode.toCanvas(qrCanvas.value, props.url, {
      width: 200,
      margin: 2,
      color: {
        dark: '#000000',
        light: '#ffffff'
      }
    })
  }
}

function hideQR() {
  showQR.value = false
}

async function copyLink() {
  try {
    await navigator.clipboard.writeText(props.url)
    copySuccess.value = true
    setTimeout(() => {
      copySuccess.value = false
    }, 2000)
  } catch {
    // Fallback for older browsers
    const textarea = document.createElement('textarea')
    textarea.value = props.url
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    copySuccess.value = true
    setTimeout(() => {
      copySuccess.value = false
    }, 2000)
  }
}
</script>

<template>
  <div class="share-bar">
    <button class="share-btn twitter" @click="shareToTwitter" title="Twitter">
      <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
        <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"/>
      </svg>
    </button>
    <button class="share-btn weibo" @click="shareToWeibo" title="Weibo">
      <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
        <path d="M10.098 20.323c-3.977.391-7.414-1.406-7.672-4.02-.259-2.609 2.759-5.047 6.74-5.441 3.979-.394 7.413 1.404 7.671 4.018.259 2.6-2.76 5.049-6.739 5.443zM9.05 17.219c-.384.616-1.208.884-1.829.602-.612-.279-.793-.991-.406-1.593.379-.595 1.176-.861 1.793-.601.622.263.82.972.442 1.592zm1.27-1.627c-.141.237-.449.353-.689.253-.236-.09-.313-.361-.177-.586.138-.227.436-.346.672-.24.239.09.315.36.194.573zm.176-2.719c-1.893-.493-4.033.45-4.857 2.118-.836 1.704-.026 3.591 1.886 4.21 1.983.64 4.318-.341 5.132-2.179.8-1.793-.201-3.642-2.161-4.149zm7.563-1.224c-.346-.105-.579-.202-.405-.649.388-1.037.428-1.894.007-2.533-.992-1.501-3.625-1.627-5.956-.53-.478.225-.95-.052-1.051-.602-.096-.522.315-.925.788-1.145 2.51-.968 5.618-.684 7.398 1.338.836.95 1.057 2.04.687 3.143-.115.341-.477.496-.849.436l.381.542zm-2.524-4.677c-1.705-.478-3.538-.016-4.765 1.194-1.228 1.211-1.474 2.94-.746 4.469.739 1.546 2.516 2.47 4.258 2.179 1.742-.292 3.138-1.773 3.371-3.556.239-1.829-.933-3.45-2.624-3.968l.506-.318zm2.422-1.697c-2.05-.553-4.401.195-5.77 1.804-1.368 1.608-1.245 3.867.438 5.382 1.682 1.514 4.229 1.696 5.92.476 1.691-1.22 2.138-3.437 1.083-5.251-.98-1.686-3.164-2.51-5.171-2.059l-1.5-.352z"/>
      </svg>
    </button>
    <button class="share-btn qq" @click="shareToQQ" title="QQ">
      <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
        <path d="M12.003 2c-2.265 0-6.615.812-6.625 9.5-.01 6.5 5.688 9.688 6.375 9.938.278.101.51-.162.51-.387v-1.301c-1.625.25-3.062-.375-3.125-.5-.063-.125-.75-.938-.75-3.812 0-1 .438-2.062 1-2.75 1.188-1.438 3.062-2.312 4.25-2.312 1.25 0 2.062.812 2.062 1.937 0 2.188-1.375 5.688-2.062 5.688-.375 0-.625-.25-.625-.875 0-1.063.688-2.188 1.188-3.375.563-1.313 1.125-2.688 1.125-4.063 0-1.687-.625-3.25-1.813-3.25-1.438 0-2.625 1.25-3 2.313-.25.688-.188 1.625-.188 1.688 0 .125-.312.25-.5.125-.437-.313-.938-1.063-1.062-1.313-.188-.375-.062-.75.188-1 .25-.25.562-.125.562-.062.063.063.313.188.5.375.312.312.688.562 1.062.688.062 0 .125-.063.125-.125 0-.125-.063-.25-.188-.375-.25-.25-.312-.625-.188-.75.188-.188.625-.125.875.125.063.063.188.125.312.125.063 0 .125 0 .188-.063.125-.125.188-.375.25-.625 0-.062-.063-.125-.188-.125-.563-.125-1.375-.312-1.438-.812-.063-.313.188-.625.438-.688.187-.063 4 1.438 4.625 1.937.188.125.188.25.125.313-.125.188-.375.438-.625.688-.125.125-.188.188-.25.188-.063 0-.125-.063-.188-.188-.188-.563-1.438-2.063-1.938-2.75-.125-.188-.438-.188-.688 0-.188.125-.625.5-.938.812-.063.063-.125.063-.125 0-.063-.125.062-.25.25-.625.312-.688 1.25-2.188 1.375-2.375.25-.375.313-.875.125-1.25-.25-.5-.875-.875-1.625-.875h.875z"/>
      </svg>
    </button>
    <button class="share-btn wechat" @click="showWechatQR" title="WeChat">
      <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
        <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098c.74.272 1.555.425 2.382.425 4.8 0 8.691-3.289 8.691-7.343S13.491 2.188 8.691 2.188zm-3.262 4.977c.642 0 1.174.52 1.174 1.16 0 .642-.532 1.163-1.174 1.163-.641 0-1.173-.52-1.173-1.163 0-.64.532-1.16 1.173-1.16zm6.524 0c.641 0 1.173.52 1.173 1.16 0 .642-.532 1.163-1.173 1.163-.642 0-1.174-.52-1.174-1.163 0-.64.532-1.16 1.174-1.16zm3.262 6.157c-1.734-.926-2.847-2.328-2.847-3.876 0-1.385.815-2.732 2.121-3.476.667-.374 1.406-.59 2.139-.744-.42-.955-1.24-1.534-2.16-1.534-.64 0-1.173.52-1.173 1.162 0 .641.532 1.162 1.173 1.162.56 0 1.027.4 1.117.927a5.97 5.97 0 0 0-1.11.213c-.445-.296-.983-.468-1.553-.468-.642 0-1.174.52-1.174 1.163 0 .641.532 1.162 1.174 1.162.642 0 1.173-.52 1.173-1.162 0-.297-.115-.574-.312-.793l.001.001c-.64-.267-1.385-.422-2.139-.422-.49 0-.971.05-1.432.146-.12.026-.24.04-.361.04a.83.83 0 0 1-.622-.276.804.804 0 0 1-.168-.627c.038-.315.11-.622.213-.915-.09-.05-.18-.1-.274-.143-.26-.12-.538-.219-.826-.288a5.389 5.389 0 0 0-.54-.133c-.12-.025-.239-.04-.361-.04-1.734 0-3.142 1.394-3.142 3.109 0 .926.42 1.798 1.174 2.45-.05.016-.098.028-.148.04l-.168.042c-.48.112-.98.195-1.49.25-.06.006-.12.009-.18.009A3.624 3.624 0 0 1 2.1 8.87a3.629 3.629 0 0 1 5.347-3.476c.025.02.052.04.078.058-.08.06-.16.116-.237.178l-.186.146c-.07.056-.139.113-.206.173-.055.045-.11.09-.163.137a4.321 4.321 0 0 0-.24.223c-.055.054-.108.108-.16.165l-.093.102c-.032.035-.063.071-.093.108-.048.056-.093.113-.136.172-.03.04-.058.08-.085.122-.038.057-.073.115-.106.175-.025.045-.049.091-.071.138-.03.06-.056.122-.082.184-.02.048-.038.096-.055.145a3.02 3.02 0 0 0-.074.237c-.015.053-.028.106-.04.16a3.08 3.08 0 0 0-.054.305c-.005.044-.009.088-.012.133-.004.06-.005.12-.005.181 0 .026.001.052.003.078l.003.045c.004.06.01.12.018.179.006.04.013.08.021.12.01.053.023.106.036.158.015.058.032.115.05.172.015.045.03.091.048.136.02.05.043.1.066.149.026.053.053.106.082.158.023.041.047.082.072.123.03.049.063.098.096.147.027.04.054.08.083.12.035.048.072.096.11.143.032.04.065.08.099.12l.12.132c.03.032.06.064.092.096.047.048.096.096.145.143.034.032.068.064.104.096.052.048.106.095.16.142.036.03.072.06.109.09.057.048.115.095.174.142l.087.066c.064.048.13.096.196.142.04.029.08.057.122.085.06.04.121.08.183.119l.096.06c.07.042.14.083.212.123.045.025.09.05.136.075.07.038.142.075.214.111.048.024.096.048.144.072.074.035.148.069.224.103l.12.051c.076.032.153.063.23.094.05.02.1.04.152.06.08.03.162.06.244.089l.127.042c.087.028.176.055.265.081.054.016.108.032.163.048.086.024.174.047.262.069l.152.036c.093.022.187.042.282.062l.138.027c.096.018.194.034.292.05l.132.022c.1.016.201.03.303.043l.127.015c.108.012.217.022.327.03.08.007.16.013.241.018.11.007.222.012.333.016l.124.006c.118.004.237.006.356.008h.04c.114 0 .229-.002.344-.006l.132-.006c.113-.005.226-.012.339-.02.08-.005.16-.011.24-.018.113-.009.227-.019.34-.031l.127-.015c.118-.013.237-.028.355-.045l.116-.019c.128-.023.257-.048.385-.075.053-.011.106-.023.159-.036.13-.03.261-.062.39-.096l.098-.027c.138-.04.276-.083.413-.129.027-.009.054-.019.081-.029.143-.05.286-.102.427-.158.026-.01.052-.021.078-.032.147-.058.293-.119.438-.183.018-.008.036-.016.054-.024.15-.068.299-.138.447-.212l.06-.03c.15-.076.298-.155.445-.236.023-.013.046-.026.069-.04.144-.08.287-.162.429-.246.026-.016.052-.032.078-.048.14-.086.279-.174.416-.265.031-.02.062-.042.093-.063.135-.093.269-.188.402-.285.024-.018.048-.036.072-.054.13-.098.259-.198.386-.3.028-.023.056-.046.084-.069.126-.103.251-.208.375-.315.025-.022.05-.044.075-.066.122-.11.242-.222.361-.336.023-.022.046-.044.069-.066.118-.12.235-.241.35-.365.022-.023.044-.047.066-.07.112-.126.222-.253.33-.382.022-.026.044-.052.065-.078.104-.13.206-.262.306-.395.02-.027.04-.054.06-.081.098-.136.193-.273.287-.413.018-.027.036-.054.054-.081.09-.14.177-.282.263-.425.018-.03.036-.06.053-.09.082-.15.161-.301.239-.454.016-.031.032-.062.048-.093.074-.16.145-.321.213-.483.014-.033.027-.066.04-.099.063-.166.123-.333.181-.501.012-.036.024-.072.035-.108.053-.173.102-.347.148-.522.01-.038.02-.076.029-.114.044-.185.084-.37.121-.557.008-.039.016-.078.023-.117.033-.19.062-.381.088-.572.006-.042.012-.084.017-.127.024-.223.043-.447.058-.671l.006-.09c.012-.25.018-.5.018-.751 0-4.055-3.891-7.344-8.691-7.344z"/>
      </svg>
    </button>
    <button class="share-btn copy" :class="{ success: copySuccess }" @click="copyLink" :title="copySuccess ? 'Copied!' : 'Copy Link'">
      <svg v-if="!copySuccess" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
        <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
        <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
      </svg>
      <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
        <polyline points="20 6 9 17 4 12"/>
      </svg>
    </button>

    <!-- QR Code Modal for WeChat -->
    <Teleport to="body">
      <div v-if="showQR" class="qr-modal" @click="hideQR">
        <div class="qr-content" @click.stop>
          <div class="qr-header">
            <span class="qr-title">Scan with WeChat</span>
            <button class="qr-close" @click="hideQR">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <canvas ref="qrCanvas" class="qr-canvas"></canvas>
          <p class="qr-hint">Scan the QR code to share via WeChat</p>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.share-bar {
  position: fixed;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 100;
}

.share-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.share-btn:hover {
  transform: scale(1.1);
}

.share-btn:active {
  transform: scale(0.95);
}

.twitter {
  background: #000000;
  color: #fff;
}

.twitter:hover {
  background: #1a1a1a;
}

.weibo {
  background: #E6162D;
  color: #fff;
}

.weibo:hover {
  background: #c71428;
}

.qq {
  background: #1296DB;
  color: #fff;
}

.qq:hover {
  background: #1085c3;
}

.wechat {
  background: #07C160;
  color: #fff;
}

.wechat:hover {
  background: #06ae50;
}

.copy {
  background: #666666;
  color: #fff;
}

.copy:hover {
  background: #555555;
}

.copy.success {
  background: #07C160;
}

/* QR Modal */
.qr-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.qr-content {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.qr-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.qr-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.qr-close {
  background: none;
  border: none;
  cursor: pointer;
  color: #999;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.qr-close:hover {
  background: #f0f0f0;
  color: #666;
}

.qr-canvas {
  display: block;
  margin: 0 auto;
}

.qr-hint {
  margin: 16px 0 0;
  font-size: 14px;
  color: #666;
}

/* Responsive */
@media (max-width: 768px) {
  .share-bar {
    left: 10px;
  }

  .share-btn {
    width: 40px;
    height: 40px;
  }

  .qr-content {
    padding: 16px;
    margin: 0 20px;
  }
}
</style>