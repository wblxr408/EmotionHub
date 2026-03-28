<template>
  <header class="app-header">
    <div class="header-container">
      <div class="header-left">
        <router-link to="/" class="logo">
          <h1>EMOTIONHUB</h1>
          <p class="meta">ARCHIVE DIVISION</p>
        </router-link>
      </div>

      <nav class="header-nav">
        <router-link to="/" class="nav-link">
          <span class="meta">PLAZA</span>
        </router-link>

        <template v-if="userStore.isLoggedIn">
          <router-link to="/settings/llm" class="nav-link llm-nav-link">
            <span class="meta">LLM KEYS</span>
          </router-link>
        </template>

        <template v-if="userStore.isLoggedIn && !userStore.isAdmin">
          <router-link to="/feed" class="nav-link">
            <span class="meta">MY FEED</span>
          </router-link>

          <router-link to="/profile" class="nav-link">
            <span class="meta">PROFILE</span>
          </router-link>

          <router-link to="/notifications" class="nav-link notification-link">
            <span class="meta">MESSAGES</span>
            <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount }}</span>
          </router-link>
        </template>

        <template v-else-if="!userStore.isLoggedIn">
          <router-link to="/login" class="nav-link">
            <span class="meta">LOGIN</span>
          </router-link>
        </template>

        <template v-if="userStore.isAdmin">
          <router-link to="/admin" class="nav-link admin-nav-link">
            <span class="meta">ADMIN PANEL</span>
          </router-link>
        </template>
      </nav>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getUnreadCount } from '@/api/notification'

const userStore = useUserStore()
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

const loadUnreadCount = async () => {
  if (!userStore.isLoggedIn) return

  try {
    const res = await getUnreadCount()
    unreadCount.value = parseUnreadCount(res.data)
  } catch (error) {
    console.error('Failed to load unread count:', error)
  }
}

onMounted(() => {
  userStore.initUserInfo()
  loadUnreadCount()

  // 每分钟刷新未读数
  setInterval(() => {
    loadUnreadCount()
  }, 60000)
})
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.app-header {
  background: $color-parchment;
  border-bottom: 2px solid $color-border;
  padding: 1.5rem 2rem;
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 3px 0 rgba(45, 41, 38, 0.15);
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  text-decoration: none;
  color: inherit;

  h1 {
    font-size: 1.75rem;
    margin: 0 0 0.25rem 0;
    letter-spacing: 0.15em;
  }

  .meta {
    margin: 0;
  }

  &:hover h1 {
    color: $color-bordeaux;
  }
}

.header-nav {
  display: flex;
  gap: 2rem;
  align-items: center;
}

.nav-link {
  text-decoration: none;
  color: $color-charcoal;
  position: relative;
  transition: color 0.2s;

  &:hover {
    color: $color-bordeaux;
  }

  &.router-link-active {
    color: $color-bordeaux;

    &::after {
      content: '';
      position: absolute;
      bottom: -0.5rem;
      left: 0;
      right: 0;
      height: 2px;
      background: $color-bordeaux;
    }
  }
}

.llm-nav-link {
  font-weight: 700;
  color: $color-bordeaux;
}

.admin-nav-link {
  background-color: rgba(198, 40, 40, 0.1);
  border-color: $color-bordeaux;
  font-weight: 700;

  &.router-link-active {
    background-color: $color-bordeaux;
    color: $color-parchment;
    border-color: $color-bordeaux;
  }
}

.notification-link {
  position: relative;
}

.notification-badge {
  position: absolute;
  top: -0.5rem;
  right: -0.75rem;
  background: $color-bordeaux;
  color: $color-parchment;
  font-family: $font-mono;
  font-size: 0.7rem;
  padding: 0.15rem 0.4rem;
  min-width: 20px;
  text-align: center;
  box-shadow: 2px 2px 0 rgba(0, 0, 0, 0.3);
}
</style>
