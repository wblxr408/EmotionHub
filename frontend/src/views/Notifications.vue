<template>
  <div class="notifications-page">
    <div class="notifications-container">
      <div class="page-header">
        <h2>NOTIFICATION CENTER</h2>
        <div class="header-actions">
          <button
            v-if="unreadCount > 0"
            class="archive-button-outline"
            @click="markAllRead"
          >
            MARK ALL READ
          </button>
          <button class="archive-button-outline" @click="router.back()">
            ← BACK
          </button>
        </div>
      </div>

      <!-- Tab切换 -->
      <div class="folder-tabs">
        <div class="tab" :class="{ active: mode === 'unread' }" @click="switchMode('unread')">
          UNREAD ({{ unreadCount }})
        </div>
        <div class="tab" :class="{ active: mode === 'all' }" @click="switchMode('all')">
          ALL MESSAGES
        </div>
      </div>

      <!-- 通知列表 -->
      <div class="notifications-list">
        <div v-if="loading" class="loading-state">
          <p class="meta">LOADING MESSAGES...</p>
        </div>

        <div v-else-if="notifications.length === 0" class="empty-state">
          <p class="meta">NO MESSAGES ON RECORD</p>
        </div>

        <div v-else>
          <div
            v-for="notification in notifications"
            :key="notification.id"
            class="archive-card notification-card"
            :class="{ unread: notification.isRead === 0 }"
            @click="handleNotificationClick(notification)"
          >
            <div class="notification-header">
              <div class="notification-type">
                <span class="stamp" :class="getTypeClass(notification.type)">
                  {{ formatType(notification.type) }}
                </span>
              </div>
              <div class="timestamp">{{ formatDate(notification.createdAt) }}</div>
            </div>

            <div class="notification-content">
              <h4>{{ notification.title }}</h4>
              <p>{{ notification.content }}</p>
            </div>

            <div v-if="notification.isRead === 0" class="unread-indicator">
              <span class="meta">UNREAD</span>
            </div>
          </div>

          <!-- 加载更多 -->
          <div v-if="hasMore" class="load-more">
            <button class="archive-button-outline" @click="loadMore">
              LOAD MORE
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  getUnreadNotifications,
  getAllNotifications,
  getUnreadCount,
  markAsRead,
  markAllAsRead as markAllReadApi
} from '@/api/notification'
import type { Notification } from '@/api/notification'
import { ElMessage } from 'element-plus'

const router = useRouter()

const mode = ref<'unread' | 'all'>('unread')
const notifications = ref<Notification[]>([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)
const unreadCount = ref(0)

const parseUnreadCount = (payload: unknown): number => {
  if (typeof payload === 'number') {
    return payload
  }

  if (payload && typeof payload === 'object' && 'count' in payload) {
    const count = (payload as { count?: unknown }).count
    if (typeof count === 'number') {
      return count
    }
  }

  return 0
}

const loadNotifications = async (reset = false) => {
  if (reset) {
    page.value = 1
    notifications.value = []
  }

  loading.value = true
  try {
    const fetchFunc = mode.value === 'unread' ? getUnreadNotifications : getAllNotifications
    const res = await fetchFunc(page.value, 10)

    if (reset) {
      notifications.value = res.data.records
    } else {
      notifications.value.push(...res.data.records)
    }

    hasMore.value = res.data.hasNext
  } catch (error) {
    console.error('Failed to load notifications:', error)
  } finally {
    loading.value = false
  }
}

const loadUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = parseUnreadCount(res.data)
  } catch (error) {
    console.error('Failed to load unread count:', error)
  }
}

const switchMode = (newMode: 'unread' | 'all') => {
  mode.value = newMode
  loadNotifications(true)
}

const loadMore = () => {
  page.value++
  loadNotifications()
}

const handleNotificationClick = async (notification: Notification) => {
  if (notification.isRead === 0) {
    try {
      await markAsRead(notification.id)
      notification.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('Failed to mark as read:', error)
    }
  }

  // 根据通知类型跳转
  if (notification.relatedId) {
    if (notification.type === 'LIKE' || notification.type === 'COMMENT') {
      router.push(`/post/${notification.relatedId}`)
    }
  }
}

const markAllRead = async () => {
  try {
    await markAllReadApi()
    notifications.value.forEach(n => n.isRead = 1)
    unreadCount.value = 0
    ElMessage.success('All messages marked as read')
  } catch (error: any) {
    ElMessage.error(error.message || 'Operation failed')
  }
}

const formatType = (type: string) => {
  const typeMap: Record<string, string> = {
    LIKE: 'LIKE',
    COMMENT: 'COMMENT',
    SYSTEM: 'SYSTEM',
    FOLLOW: 'FOLLOW'
  }
  return typeMap[type] || type
}

const getTypeClass = (type: string) => {
  const classMap: Record<string, string> = {
    LIKE: 'stamp-positive',
    COMMENT: 'stamp-logged',
    SYSTEM: 'stamp-pending',
    FOLLOW: 'stamp-logged'
  }
  return classMap[type] || 'stamp-pending'
}

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return 'JUST NOW'
  if (minutes < 60) return `${minutes}M AGO`
  if (hours < 24) return `${hours}H AGO`
  if (days < 7) return `${days}D AGO`

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

onMounted(() => {
  loadUnreadCount()
  loadNotifications(true)
})
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.notifications-page {
  min-height: 100vh;
  padding: 2rem;
}

.notifications-container {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;

  h2 {
    margin: 0;
  }
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.notifications-list {
  margin-top: 2rem;
}

.notification-card {
  margin-bottom: 1.5rem;
  cursor: pointer;
  position: relative;

  &.unread {
    border-left: 4px solid $color-bordeaux;
  }

  &:hover {
    transform: rotate(0deg) translateY(-2px);
  }
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.notification-type {
  .stamp {
    font-size: 0.7rem;
    padding: 0.2rem 0.6rem;
  }
}

.notification-content {
  h4 {
    font-size: 1.1rem;
    margin: 0 0 0.5rem 0;
    color: $color-charcoal;
  }

  p {
    margin: 0;
    line-height: 1.6;
  }
}

.unread-indicator {
  position: absolute;
  top: 1rem;
  right: 1rem;

  .meta {
    color: $color-bordeaux;
    font-weight: 700;
  }
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
