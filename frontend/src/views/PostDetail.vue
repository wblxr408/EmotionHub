<template>
  <div class="post-detail-page">
    <div class="post-container">
      <!-- 返回按钮 -->
      <div class="back-nav">
        <button class="archive-button-outline" @click="router.back()">
          ← BACK TO ARCHIVE
        </button>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="loading-state">
        <p class="meta">RETRIEVING ENTRY...</p>
      </div>

      <!-- 帖子详情 -->
      <div v-else-if="post" class="archive-card post-detail">
        <!-- 帖子头部 -->
        <div class="post-header">
          <div class="user-info">
            <div class="avatar">{{ post.nickname.charAt(0).toUpperCase() }}</div>
            <div class="user-details">
              <div class="meta username">{{ post.nickname }}</div>
              <div class="timestamp">FILED: {{ formatDate(post.createdAt) }}</div>
            </div>
          </div>

          <div v-if="post.emotionLabel" class="stamp" :class="`stamp-${post.emotionLabel.toLowerCase()}`">
            {{ post.emotionLabel }}
            <div v-if="post.emotionScore" class="meta" style="margin-top: 0.25rem">
              SCORE: {{ post.emotionScore.toFixed(2) }}
            </div>
          </div>
        </div>

        <div class="divider"></div>

        <!-- 帖子内容 -->
        <div class="post-content">
          <p>{{ post.content }}</p>
        </div>

        <!-- 帖子图片 -->
        <div v-if="post.images && post.images.length > 0" class="post-images">
          <img
            v-for="(img, idx) in post.images"
            :key="idx"
            :src="img"
            alt="Entry image"
            class="post-image"
          />
        </div>

        <div class="divider"></div>

        <!-- 交互按钮 -->
        <div class="post-actions">
          <button class="archive-button-outline" @click="toggleLike">
            {{ post.liked ? '♥' : '♡' }} {{ post.likeCount }} LIKES
          </button>
          <span class="meta">{{ post.viewCount }} VIEWS</span>
          <span class="meta">{{ post.commentCount }} COMMENTS</span>
        </div>

        <!-- 评论区 -->
        <div class="comments-section">
          <h3>CASE NOTES</h3>
          <div class="divider"></div>

          <!-- 发表评论 -->
          <div class="comment-form">
            <textarea
              v-model="commentContent"
              class="archive-input"
              rows="3"
              placeholder="Add your analysis..."
            ></textarea>
            <button class="archive-button" @click="submitComment" :disabled="!commentContent.trim()">
              SUBMIT NOTE
            </button>
          </div>

          <!-- 评论列表 -->
          <div v-if="comments.length === 0" class="empty-state">
            <p class="meta">NO NOTES YET</p>
          </div>

          <div v-else class="comments-list">
            <CommentItem
              v-for="comment in comments"
              :key="comment.id"
              :comment="comment"
              @reply="handleReply"
            />
          </div>
        </div>
      </div>

      <!-- 错误状态 -->
      <div v-else class="error-state">
        <p class="meta">ENTRY NOT FOUND</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPostDetail } from '@/api/post'
import { likePost, getComments, createComment } from '@/api/interaction'
import type { Post } from '@/api/post'
import type { Comment } from '@/api/interaction'
import { ElMessage } from 'element-plus'
import CommentItem from '@/components/CommentItem.vue'

const route = useRoute()
const router = useRouter()

const post = ref<Post | null>(null)
const comments = ref<Comment[]>([])
const loading = ref(false)
const commentContent = ref('')

const loadPost = async () => {
  loading.value = true
  try {
    const postId = Number(route.params.id)
    const res = await getPostDetail(postId)
    post.value = res.data
  } catch (error) {
    console.error('Failed to load post:', error)
    ElMessage.error('Failed to retrieve entry')
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  try {
    const postId = Number(route.params.id)
    const res = await getComments(postId)
    comments.value = res.data
  } catch (error) {
    console.error('Failed to load comments:', error)
  }
}

const toggleLike = async () => {
  if (!post.value) return

  try {
    await likePost(post.value.id)
    post.value.liked = !post.value.liked
    post.value.likeCount += post.value.liked ? 1 : -1
  } catch (error: any) {
    ElMessage.error(error.message || 'Operation failed')
  }
}

const submitComment = async () => {
  if (!post.value || !commentContent.value.trim()) return

  try {
    await createComment({
      postId: post.value.id,
      content: commentContent.value
    })

    ElMessage.success('Note submitted')
    commentContent.value = ''
    loadComments()

    if (post.value) {
      post.value.commentCount++
    }
  } catch (error: any) {
    ElMessage.error(error.message || 'Submission failed')
  }
}

const handleReply = (parentComment: Comment) => {
  // 简单处理：将回复内容预填充到评论框
  commentContent.value = `@${parentComment.username} `
}

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

onMounted(() => {
  loadPost()
  loadComments()
})
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.post-detail-page {
  min-height: 100vh;
  padding: 2rem;
}

.post-container {
  max-width: 800px;
  margin: 0 auto;
}

.back-nav {
  margin-bottom: 2rem;
}

.post-detail {
  padding: 2rem;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
}

.user-info {
  display: flex;
  gap: 1rem;
}

.avatar {
  width: 60px;
  height: 60px;
  background: $color-bordeaux;
  color: $color-parchment;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: $font-heading;
  font-size: 1.75rem;
  font-weight: 700;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.username {
  font-weight: 700;
  color: $color-charcoal;
  font-size: 1.1rem;
}

.stamp-positive {
  color: #2d5016;
  border-color: #2d5016;
}

.stamp-negative {
  color: $color-bordeaux;
  border-color: $color-bordeaux;
}

.stamp-neutral {
  color: $color-border;
  border-color: $color-border;
}

.post-content {
  margin: 2rem 0;
  line-height: 1.8;
  font-size: 1.1rem;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1rem;
  margin: 2rem 0;
}

.post-image {
  width: 100%;
  height: 250px;
  object-fit: cover;
  border: 1px solid $color-border;
}

.post-actions {
  display: flex;
  gap: 2rem;
  align-items: center;
  margin: 2rem 0;
}

.comments-section {
  margin-top: 3rem;

  h3 {
    margin-bottom: 1rem;
  }
}

.comment-form {
  margin: 2rem 0;

  .archive-input {
    width: 100%;
    margin-bottom: 1rem;
  }
}

.comments-list {
  margin-top: 2rem;
}

.loading-state,
.error-state,
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
}
</style>
