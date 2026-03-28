<template>
  <div class="feed-page">
    <div class="feed-container">

      <!-- 顶部标题栏 -->
      <div class="feed-toolbar">
        <h2>RECOMMENDED FEED</h2>
      </div>

      <!-- 情感状态 + 策略切换 -->
      <div class="feed-status-bar">
        <div class="emotion-status" v-if="emotionState">
          <span class="meta status-label">CURRENT STATE</span>
          <span class="stamp" :class="emotionStampClass">{{ emotionState }}</span>
        </div>

        <div class="strategy-tabs folder-tabs">
          <div
            class="tab"
            :class="{ active: activeStrategy === 'emotional_adaptive' }"
            @click="switchStrategy('emotional_adaptive')"
          >
            EMOTIONAL ADAPTIVE
          </div>
          <div
            class="tab"
            :class="{ active: activeStrategy === 'traditional' }"
            @click="switchStrategy('traditional')"
          >
            TRADITIONAL
          </div>
        </div>
      </div>

      <!-- 帖子列表 -->
      <div class="posts-container">
        <div v-if="loading && posts.length === 0" class="loading-state">
          <p class="meta">LOADING RECOMMENDATIONS...</p>
        </div>

        <div v-else-if="!loading && posts.length === 0" class="empty-state">
          <p class="meta">NO RECOMMENDATIONS AVAILABLE</p>
          <p class="meta" style="margin-top: 0.5rem; opacity: 0.6;">POST MORE ENTRIES TO IMPROVE YOUR FEED</p>
        </div>

        <div v-else class="posts-list">
          <div
            v-for="post in posts"
            :key="post.id"
            class="archive-card post-card"
            @click="viewPost(post.id)"
          >
            <!-- 帖子头部 -->
            <div class="post-header">
              <div class="user-info">
                <div class="avatar">{{ (post.nickname || post.username || '?').charAt(0).toUpperCase() }}</div>
                <div class="user-details">
                  <div class="meta username">{{ post.nickname || post.username }}</div>
                  <div class="timestamp">{{ formatDate(post.createdAt) }}</div>
                </div>
              </div>

              <div class="post-stamps">
                <!-- 情感标签 -->
                <div v-if="post.emotionLabel" class="stamp" :class="`stamp-${post.emotionLabel.toLowerCase()}`">
                  {{ post.emotionLabel }}
                </div>
              </div>
            </div>

            <!-- 帖子内容 -->
            <div class="post-content">
              <p>{{ post.content }}</p>
            </div>

            <!-- 帖子图片 -->
            <div v-if="post.images && post.images.length > 0" class="post-images">
              <img
                v-for="(img, idx) in post.images.slice(0, 3)"
                :key="idx"
                :src="img"
                alt="Post image"
                class="post-image"
              />
            </div>

            <!-- 帖子底部统计 -->
            <div class="post-footer">
              <div class="post-stats">
                <span class="meta">{{ post.viewCount }} VIEWS</span>
                <span class="meta">{{ post.likeCount }} LIKES</span>
                <span class="meta">{{ post.commentCount }} COMMENTS</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 加载更多 / 没有更多 -->
        <div class="load-more" v-if="posts.length > 0">
          <button
            v-if="hasMore"
            class="archive-button-outline"
            :disabled="loading"
            @click="loadMore"
          >
            {{ loading ? 'LOADING...' : 'LOAD MORE ENTRIES' }}
          </button>
          <p v-else class="meta no-more">— NO MORE ENTRIES —</p>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getFeed } from '@/api/feed'
import type { Post } from '@/api/post'

const router = useRouter()
const userStore = useUserStore()

const posts = ref<Post[]>([])
const loading = ref(false)
const page = ref(0)
const hasMore = ref(true)
const emotionState = ref('')
const activeStrategy = ref('emotional_adaptive')

// 情感状态对应的 stamp 样式
const emotionStampClass = computed(() => {
  const map: Record<string, string> = {
    HAPPY: 'stamp-positive',
    CALM: 'stamp-neutral',
    LOW: 'stamp-negative',
    ANXIOUS: 'stamp-negative',
    FLUCTUANT: 'stamp-neutral'
  }
  return map[emotionState.value] || 'stamp-neutral'
})

const loadFeed = async (reset = false) => {
  if (!userStore.userInfo) return

  if (reset) {
    page.value = 0
    posts.value = []
    hasMore.value = true
  }

  if (!hasMore.value) return

  loading.value = true
  try {
    const res = await getFeed({
      userId: userStore.userInfo.id,
      strategy: activeStrategy.value,
      page: page.value,
      size: 10
    })

    const data = res.data
    emotionState.value = data.emotionState

    if (reset) {
      posts.value = data.items
    } else {
      posts.value.push(...data.items)
    }

    hasMore.value = data.items.length >= 10
  } catch (error) {
    console.error('Failed to load feed:', error)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  page.value++
  loadFeed()
}

const switchStrategy = (strategy: string) => {
  if (activeStrategy.value === strategy) return
  activeStrategy.value = strategy
  loadFeed(true)
}

const viewPost = (postId: number) => {
  router.push(`/post/${postId}`)
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

// 登录状态就绪后加载
watch(() => userStore.userInfo, (info) => {
  if (info) loadFeed(true)
}, { immediate: true })

onMounted(() => {
  if (userStore.userInfo) loadFeed(true)
})
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.feed-page {
  min-height: 100vh;
  padding: 2rem;
}

.feed-container {
  max-width: 900px;
  margin: 0 auto;
}

.feed-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;

  h2 {
    margin: 0;
  }
}

// 情感状态栏
.feed-status-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding: 1rem 1.5rem;
  border: 1px solid $color-border;
  background: rgba($color-parchment, 0.5);
}

.emotion-status {
  display: flex;
  align-items: center;
  gap: 1rem;

  .status-label {
    opacity: 0.7;
    letter-spacing: 0.1em;
  }

  .stamp {
    font-size: 0.75rem;
    padding: 0.2rem 0.6rem;
  }
}

.strategy-tabs {
  margin-bottom: 0;
  border-bottom: none;
}

// 帖子列表
.posts-container {
  margin-top: 1.5rem;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.post-card {
  cursor: pointer;
  transition: transform 0.2s;

  &:hover {
    transform: rotate(0deg) translateY(-2px);
  }
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.user-info {
  display: flex;
  gap: 1rem;
}

.avatar {
  width: 50px;
  height: 50px;
  background: $color-bordeaux;
  color: $color-parchment;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: $font-heading;
  font-size: 1.5rem;
  font-weight: 700;
  flex-shrink: 0;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.username {
  font-weight: 700;
  color: $color-charcoal;
}

.post-stamps {
  display: flex;
  gap: 0.5rem;
  align-items: flex-start;
  flex-wrap: wrap;
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
  margin-bottom: 1rem;
  line-height: 1.8;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

.post-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border: 1px solid $color-border;
}

.post-footer {
  border-top: 1px solid $color-border;
  padding-top: 1rem;
}

.post-stats {
  display: flex;
  gap: 2rem;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
}

.load-more {
  text-align: center;
  margin-top: 2rem;
}

.no-more {
  opacity: 0.5;
  letter-spacing: 0.15em;
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.timestamp {
  font-family: $font-mono;
  font-size: 0.75rem;
  color: darken($color-border, 10%);
}
</style>
