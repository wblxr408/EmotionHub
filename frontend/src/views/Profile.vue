<template>
  <div class="profile-page">
    <div class="profile-container">
      <!-- 个人信息卡片 -->
      <div class="archive-card profile-card">
        <div class="profile-header">
          <div class="avatar-large">{{ userInfo?.nickname.charAt(0).toUpperCase() || 'U' }}</div>
          <div class="profile-info">
            <h2>{{ userInfo?.nickname || 'User' }}</h2>
            <p class="meta">@{{ userInfo?.username || 'username' }}</p>
            <p class="timestamp">MEMBER SINCE: {{ formatDate(userInfo?.createdAt || '') }}</p>
          </div>
          <div v-if="isOwnProfile" class="profile-header-actions">
            <router-link to="/settings/llm" class="archive-button">LLM KEYS</router-link>
            <button class="archive-button-outline" @click="handleLogout">LOGOUT</button>
          </div>
        </div>

        <div class="divider"></div>

        <div class="profile-bio">
          <p>{{ userInfo?.bio || 'No biographical information on file.' }}</p>
        </div>

        <div class="divider"></div>

        <!-- 统计信息 -->
        <div class="profile-stats">
          <div class="stat-item">
            <div class="stat-value">{{ statsSummary.totalPosts }}</div>
            <div class="meta">ENTRIES</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ statsSummary.totalLikes }}</div>
            <div class="meta">LIKES RECEIVED</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ statsSummary.totalComments }}</div>
            <div class="meta">COMMENTS</div>
          </div>
        </div>
      </div>

      <!-- 情感分析卡片 -->
      <div v-if="isOwnProfile && stats" class="archive-card emotion-card">
        <h3>EMOTIONAL ANALYSIS</h3>
        <div class="divider"></div>

        <div class="emotion-distribution">
          <div class="emotion-item">
            <div class="stamp stamp-positive">POSITIVE</div>
            <div class="emotion-bar">
              <div
                class="emotion-fill positive"
                :style="{ width: `${statsSummary.positivePercent}%` }"
              ></div>
            </div>
            <span class="meta">{{ statsSummary.positivePercent.toFixed(1) }}%</span>
          </div>

          <div class="emotion-item">
            <div class="stamp stamp-neutral">NEUTRAL</div>
            <div class="emotion-bar">
              <div
                class="emotion-fill neutral"
                :style="{ width: `${statsSummary.neutralPercent}%` }"
              ></div>
            </div>
            <span class="meta">{{ statsSummary.neutralPercent.toFixed(1) }}%</span>
          </div>

          <div class="emotion-item">
            <div class="stamp stamp-negative">NEGATIVE</div>
            <div class="emotion-bar">
              <div
                class="emotion-fill negative"
                :style="{ width: `${statsSummary.negativePercent}%` }"
              ></div>
            </div>
            <span class="meta">{{ statsSummary.negativePercent.toFixed(1) }}%</span>
          </div>
        </div>
      </div>

      <!-- 用户帖子 -->
      <div class="user-posts">
        <h3>FILED ENTRIES</h3>
        <div class="divider"></div>

        <div v-if="loadingPosts" class="loading-state">
          <p class="meta">LOADING ENTRIES...</p>
        </div>

        <div v-else-if="posts.length === 0" class="empty-state">
          <p class="meta">NO ENTRIES ON RECORD</p>
        </div>

        <div v-else class="posts-list">
          <div
            v-for="post in posts"
            :key="post.id"
            class="archive-card post-card"
            @click="viewPost(post.id)"
          >
            <div class="post-header">
              <div class="timestamp">{{ formatDate(post.createdAt) }}</div>
              <div v-if="post.emotionLabel" class="stamp" :class="`stamp-${post.emotionLabel.toLowerCase()}`">
                {{ post.emotionLabel }}
              </div>
            </div>

            <div class="post-content">
              <p>{{ post.content }}</p>
            </div>

            <div class="post-stats">
              <span class="meta">{{ post.viewCount }} VIEWS</span>
              <span class="meta">{{ post.likeCount }} LIKES</span>
              <span class="meta">{{ post.commentCount }} COMMENTS</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserInfo } from '@/api/auth'
import { getUserStats } from '@/api/stats'
import { getUserPosts } from '@/api/post'
import type { UserInfo } from '@/api/auth'
import type { UserStats } from '@/api/stats'
import type { Post } from '@/api/post'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userInfo = ref<UserInfo | null>(null)
const stats = ref<UserStats | null>(null)
const posts = ref<Post[]>([])
const loadingPosts = ref(false)

const statsSummary = computed(() => {
  if (!stats.value) {
    return {
      totalPosts: 0,
      totalComments: 0,
      totalLikes: 0,
      positivePercent: 0,
      neutralPercent: 0,
      negativePercent: 0
    }
  }

  const positive = Number(stats.value.emotionStats?.positive || 0)
  const neutral = Number(stats.value.emotionStats?.neutral || 0)
  const negative = Number(stats.value.emotionStats?.negative || 0)
  const totalEmotionCount = positive + neutral + negative

  return {
    totalPosts: Number(stats.value.postCount || 0),
    totalComments: Number(stats.value.commentCount || 0),
    totalLikes: Number(stats.value.totalLikes || 0),
    positivePercent: totalEmotionCount > 0 ? (positive / totalEmotionCount) * 100 : 0,
    neutralPercent: totalEmotionCount > 0 ? (neutral / totalEmotionCount) * 100 : 0,
    negativePercent: totalEmotionCount > 0 ? (negative / totalEmotionCount) * 100 : 0
  }
})

const userId = computed(() => {
  const id = route.params.id
  return id ? Number(id) : userStore.userInfo?.id
})

const isOwnProfile = computed(() => {
  return !route.params.id || userId.value === userStore.userInfo?.id
})

const loadUserInfo = async () => {
  try {
    if (isOwnProfile.value) {
      await userStore.getUserInfo()
      userInfo.value = userStore.userInfo
    } else {
      const res = await getUserInfo(userId.value!)
      userInfo.value = res.data
    }
  } catch (error: any) {
    ElMessage.error(error.message || 'Failed to load user info')
  }
}

const loadStats = async () => {
  if (!userId.value) return

  try {
    const res = await getUserStats(userId.value)
    stats.value = res.data
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
}

const loadPosts = async () => {
  if (!userId.value) return

  loadingPosts.value = true
  try {
    const res = await getUserPosts(userId.value)
    posts.value = res.data.records
  } catch (error) {
    console.error('Failed to load posts:', error)
  } finally {
    loadingPosts.value = false
  }
}

const viewPost = (postId: number) => {
  router.push(`/post/${postId}`)
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('Logged out successfully')
  router.push('/login')
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return 'UNKNOWN'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

onMounted(() => {
  loadUserInfo()
  loadStats()
  loadPosts()
})
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.profile-page {
  min-height: 100vh;
  padding: 2rem;
}

.profile-container {
  max-width: 900px;
  margin: 0 auto;
}

.profile-card {
  margin-bottom: 2rem;
}

.profile-header {
  display: flex;
  gap: 2rem;
  align-items: flex-start;
}

.profile-header-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: stretch;
  flex-shrink: 0;

  a.archive-button {
    text-align: center;
    text-decoration: none;
  }
}

.avatar-large {
  width: 100px;
  height: 100px;
  background: $color-bordeaux;
  color: $color-parchment;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: $font-heading;
  font-size: 3rem;
  font-weight: 700;
  flex-shrink: 0;
}

.profile-info {
  flex: 1;

  h2 {
    margin: 0 0 0.5rem 0;
  }

  p {
    margin: 0.25rem 0;
  }
}

.profile-bio {
  margin: 1.5rem 0;
  line-height: 1.8;
}

.profile-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2rem;
  text-align: center;
}

.stat-item {
  .stat-value {
    font-family: $font-heading;
    font-size: 2.5rem;
    color: $color-bordeaux;
    margin-bottom: 0.5rem;
  }
}

.emotion-card {
  margin-bottom: 2rem;

  h3 {
    margin-bottom: 1rem;
  }
}

.emotion-distribution {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.emotion-item {
  display: flex;
  align-items: center;
  gap: 1rem;

  .stamp {
    width: 120px;
    flex-shrink: 0;
  }
}

.emotion-bar {
  flex: 1;
  height: 30px;
  background: rgba(166, 158, 133, 0.3);
  border: 1px solid $color-border;
  position: relative;
  overflow: hidden;
}

.emotion-fill {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  transition: width 0.5s ease;

  &.positive {
    background: rgba(45, 80, 22, 0.5);
  }

  &.neutral {
    background: rgba(166, 158, 133, 0.7);
  }

  &.negative {
    background: rgba(92, 1, 32, 0.5);
  }
}

.user-posts {
  h3 {
    margin-bottom: 1rem;
  }
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-top: 2rem;
}

.post-card {
  cursor: pointer;

  &:hover {
    transform: rotate(0deg) translateY(-2px);
  }
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.post-content {
  margin-bottom: 1rem;
  line-height: 1.8;
}

.post-stats {
  display: flex;
  gap: 2rem;
  padding-top: 1rem;
  border-top: 1px solid $color-border;
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

.loading-state,
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
}
</style>
