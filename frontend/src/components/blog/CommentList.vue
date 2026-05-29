<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElAvatar, ElButton, ElInput, ElForm, ElFormItem, ElMessage } from 'element-plus'
import { timeAgo } from '@/utils/formatters'
import { getGravatarUrl } from '@/utils/md5'
import CaptchaInput from './CaptchaInput.vue'
import axios from 'axios'

interface Comment {
  id: number
  content: string
  authorName: string
  authorEmail?: string
  avatar?: string
  createdAt: string
  children?: Comment[]
}

const props = defineProps<{
  comments: Comment[]
  postId: number
}>()

const emit = defineEmits<{
  (e: 'submit', data: { content: string; author: string; email?: string; parentId?: number; captcha: string }): void
}>()

const newComment = ref({
  author: '',
  email: '',
  content: ''
})
const replyingTo = ref<number | null>(null)
const submitting = ref(false)
const captchaRef = ref<InstanceType<typeof CaptchaInput> | null>(null)
const captchaCode = ref('')

const hasComments = computed(() => props.comments.length > 0)

async function generateCaptchaCode() {
  try {
    const res = await axios.get('/captcha')
    if (res.data.data?.code) {
      captchaCode.value = res.data.data.code
    }
  } catch {
    // Generate fallback code client-side if server fails
    captchaCode.value = ''
  }
}

function formatCommentDate(dateStr: string): string {
  return timeAgo(dateStr)
}

function getAvatarUrl(authorEmail?: string): string {
  if (!authorEmail) return ''
  return getGravatarUrl(authorEmail, 48)
}

function handleReply(commentId: number) {
  replyingTo.value = replyingTo.value === commentId ? null : commentId
}

function cancelReply() {
  replyingTo.value = null
  newComment.value.content = ''
}

function handleSubmit(parentId?: number) {
  if (!newComment.value.content.trim() || !newComment.value.author.trim()) {
    ElMessage.warning('Please fill in name and comment content')
    return
  }

  if (!captchaRef.value?.verify()) {
    ElMessage.error('Captcha verification failed')
    captchaRef.value?.regenerate()
    return
  }

  submitting.value = true
  emit('submit', {
    content: newComment.value.content,
    author: newComment.value.author,
    email: newComment.value.email || undefined,
    parentId,
    captcha: captchaCode.value || newComment.value.content // Fallback to content for client-side captcha
  })

  newComment.value.content = ''
  replyingTo.value = null
  submitting.value = false
  captchaRef.value?.regenerate()
}
</script>

<template>
  <div class="comment-list">
    <h3 class="comment-title">
      Comments {{ hasComments ? `(${comments.length})` : '' }}
    </h3>

    <div v-if="!hasComments" class="no-comments">
      <p>No comments yet. Be the first to share your thoughts!</p>
    </div>

    <div v-else class="comments-container">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <div class="comment-header">
          <ElAvatar :size="40">
            <img v-if="getAvatarUrl(comment.authorEmail)" :src="getAvatarUrl(comment.authorEmail)" :alt="comment.authorName" loading="lazy" />
            <template v-else>{{ comment.authorName[0]?.toUpperCase() || 'A' }}</template>
          </ElAvatar>
          <div class="comment-info">
            <span class="comment-author">{{ comment.authorName }}</span>
            <time class="comment-time">{{ formatCommentDate(comment.createdAt) }}</time>
          </div>
        </div>
        <div class="comment-body">
          <p class="comment-content">{{ comment.content }}</p>
          <div class="comment-actions">
            <ElButton size="small" text @click="handleReply(comment.id)">Reply</ElButton>
          </div>
        </div>

        <!-- Reply form -->
        <div v-if="replyingTo === comment.id" class="reply-form">
          <ElForm label-position="top">
            <ElFormItem label="Name">
              <ElInput v-model="newComment.author" placeholder="Your name" />
            </ElFormItem>
            <ElFormItem label="Email (optional)">
              <ElInput v-model="newComment.email" placeholder="Your email" />
            </ElFormItem>
            <ElFormItem label="Comment">
              <ElInput
                v-model="newComment.content"
                type="textarea"
                :rows="3"
                placeholder="Write a reply..."
              />
            </ElFormItem>
            <ElFormItem label="Captcha">
              <CaptchaInput ref="captchaRef" />
            </ElFormItem>
            <div class="reply-actions">
              <ElButton size="small" @click="cancelReply">Cancel</ElButton>
              <ElButton
                size="small"
                type="primary"
                :loading="submitting"
                @click="handleSubmit(comment.id)"
              >
                Submit Reply
              </ElButton>
            </div>
          </ElForm>
        </div>

        <!-- Nested replies -->
        <div v-if="comment.children?.length" class="comment-children">
          <div v-for="reply in comment.children" :key="reply.id" class="comment-item comment-reply">
            <div class="comment-header">
              <ElAvatar :size="32">
                <img v-if="getAvatarUrl(reply.authorEmail)" :src="getAvatarUrl(reply.authorEmail)" :alt="reply.authorName" loading="lazy" />
                <template v-else>{{ reply.authorName[0]?.toUpperCase() || 'A' }}</template>
              </ElAvatar>
              <div class="comment-info">
                <span class="comment-author">{{ reply.authorName }}</span>
                <time class="comment-time">{{ formatCommentDate(reply.createdAt) }}</time>
              </div>
            </div>
            <div class="comment-body">
              <p class="comment-content">{{ reply.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- New comment form -->
    <div class="new-comment-form">
      <h4>Leave a Comment</h4>
      <ElForm label-position="top">
        <ElFormItem label="Name">
          <ElInput v-model="newComment.author" placeholder="Your name" />
        </ElFormItem>
        <ElFormItem label="Email (optional)">
          <ElInput v-model="newComment.email" placeholder="Your email for Gravatar" />
        </ElFormItem>
        <ElFormItem label="Comment">
          <ElInput
            v-model="newComment.content"
            type="textarea"
            :rows="4"
            placeholder="Write your comment..."
          />
        </ElFormItem>
        <ElFormItem label="Captcha">
          <CaptchaInput ref="captchaRef" />
        </ElFormItem>
        <ElFormItem>
          <ElButton
            type="primary"
            :loading="submitting"
            @click="handleSubmit()"
          >
            Submit Comment
          </ElButton>
        </ElFormItem>
      </ElForm>
    </div>
  </div>
</template>

<style scoped>
.comment-list {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}

.comment-title {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
}

.no-comments {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 30px;
}

.no-comments p {
  margin: 0;
}

.comments-container {
  margin-bottom: 40px;
}

.comment-item {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-reply {
  padding-left: 20px;
  border-left: 2px solid #e0e0e0;
  margin-left: 20px;
  margin-top: 16px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.comment-info {
  display: flex;
  flex-direction: column;
}

.comment-author {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.comment-time {
  color: #999;
  font-size: 12px;
}

.comment-body {
  padding-left: 52px;
}

.comment-content {
  color: #555;
  line-height: 1.6;
  margin: 0 0 12px;
  white-space: pre-wrap;
}

.comment-actions {
  padding-left: 0;
}

.reply-form {
  margin-top: 16px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-left: 52px;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.comment-children {
  margin-top: 16px;
}

.new-comment-form {
  margin-top: 40px;
  padding: 24px;
  background: #f9f9f9;
  border-radius: 12px;
}

.new-comment-form h4 {
  margin: 0 0 20px;
  font-size: 18px;
  color: #333;
}

:deep(.el-avatar) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

:deep(.el-avatar img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

@media (max-width: 768px) {
  .comment-body {
    padding-left: 0;
  }

  .reply-form {
    margin-left: 0;
  }
}
</style>
