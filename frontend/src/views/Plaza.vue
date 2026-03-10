<template>
  <div class="plaza-page">
    <div class="plaza-container">
      <!-- 顶部工具栏 -->
      <div class="plaza-toolbar">
        <h2>EMOTION ARCHIVE</h2>
        <div class="toolbar-actions">
          <button class="archive-button-outline" @click="showCreatePost = true">
            + NEW ENTRY
          </button>
        </div>
      </div>

      <!-- 筛选标签 -->
      <div class="folder-tabs">
        <div
          class="tab"
          :class="{ active: filterEmotion === null }"
          @click="filterEmotion = null"
        >
          ALL ENTRIES
        </div>
        <div
          class="tab"
          :class="{ active: filterEmotion === 'POSITIVE' }"
          @click="filterEmotion = 'POSITIVE'"
        >
          POSITIVE
        </div>
        <div
          class="tab"
          :class="{ active: filterEmotion === 'NEGATIVE' }"
          @click="filterEmotion = 'NEGATIVE'"
        >
          NEGATIVE
        </div>
        <div
          class="tab"
          :class="{ active: filterEmotion === 'NEUTRAL' }"
          @click="filterEmotion = 'NEUTRAL'"
        >
          NEUTRAL
        </div>
      </div>

      <!-- 帖子列表 -->
      <div class="posts-container">
        <div v-if="loading" class="loading-state">
          <p class="meta">LOADING ARCHIVES...</p>
        </div>

        <div v-else-if="posts.length === 0" class="empty-state">
          <p class="meta">NO ENTRIES FOUND</p>
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
                <div class="avatar">{{ post.nickname.charAt(0).toUpperCase() }}</div>
                <div class="user-details">
                  <div class="meta username">{{ post.nickname }}</div>
                  <div class="timestamp">{{ formatDate(post.createdAt) }}</div>
                </div>
              </div>

              <!-- 情感标签 -->
              <div v-if="post.emotionLabel" class="stamp" :class="`stamp-${post.emotionLabel.toLowerCase()}`">
                {{ post.emotionLabel }}
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

        <!-- 加载更多 -->
        <div v-if="hasMore && !loading" class="load-more">
          <button class="archive-button-outline" @click="loadMore">
            LOAD MORE
          </button>
        </div>
      </div>
    </div>

    <!-- 发帖对话框 -->
    <CreatePostDialog v-model="showCreatePost" @success="refreshPosts" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { listPosts } from '@/api/post'
import type { Post } from '@/api/post'
import CreatePostDialog from '@/components/CreatePostDialog.vue'

const router = useRouter()

const posts = ref<Post[]>([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)
const filterEmotion = ref<string | null>(null)
const showCreatePost = ref(false)

const loadPosts = async (reset = false) => {
  if (reset) {
    page.value = 1
    posts.value = []
  }

  loading.value = true
  try {
    const res = await listPosts({
      page: page.value,
      size: 10,
      emotionLabel: filterEmotion.value || undefined,
      orderBy: 'LATEST'
    })

    if (reset) {
      posts.value = res.data.records
    } else {
      posts.value.push(...res.data.records)
    }

    hasMore.value = res.data.hasNext
  } catch (error) {
    console.error('Failed to load posts:', error)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  page.value++
  loadPosts()
}

const refreshPosts = () => {
  loadPosts(true)
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

watch(filterEmotion, () => {
  loadPosts(true)
})

onMounted(() => {
  loadPosts(true)
})
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.plaza-page {
  min-height: 100vh;
  padding: 2rem;
}

.plaza-container {
  max-width: 900px;
  margin: 0 auto;
}

.plaza-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;

  h2 {
    margin: 0;
  }
}

.posts-container {
  margin-top: 2rem;
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
</style>
