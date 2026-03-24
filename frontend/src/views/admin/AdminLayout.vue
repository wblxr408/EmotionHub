<template>
  <section class="admin-shell">
    <div class="archive-card admin-panel admin-hero">
      <div>
        <p class="meta">ADMIN CONSOLE</p>
        <h2>Operations Backroom</h2>
        <p>封禁用户、下架帖子、处理举报和追踪关键操作都收口在这里。</p>
      </div>
      <div class="admin-user-info">
        <span class="stamp stamp-logged">{{ userStore.userInfo?.username }}</span>
        <button class="admin-logout-btn" @click="handleLogout">LOGOUT</button>
      </div>
    </div>

    <nav class="admin-nav">
      <router-link
        v-for="item in navItems"
        :key="item.to"
        :to="item.to"
        class="admin-nav-link"
      >
        {{ item.label }}
      </router-link>
    </nav>

    <router-view />
  </section>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const navItems = [
  { label: 'Overview', to: '/admin' },
  { label: 'Users', to: '/admin/users' },
  { label: 'Posts', to: '/admin/posts' },
  { label: 'Reports', to: '/admin/reports' },
  { label: 'Logs', to: '/admin/logs' }
]

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped lang="scss">
@import '@/styles/admin.scss';
</style>
