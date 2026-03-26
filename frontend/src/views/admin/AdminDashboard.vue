<template>
  <section class="admin-page">
    <div class="admin-toolbar">
      <div>
        <p class="meta">OVERVIEW</p>
        <h3>后台概览</h3>
      </div>
      <button class="archive-button archive-button-outline" @click="loadOverview">
        REFRESH
      </button>
    </div>

    <div class="admin-stats">
      <article v-for="card in cards" :key="card.label" class="archive-card stat-card">
        <p class="meta">{{ card.label }}</p>
        <div class="stat-value">{{ card.value }}</div>
      </article>
    </div>

    <div class="archive-card admin-panel">
      <div class="admin-toolbar">
        <div>
          <p class="meta">QUICK ACTIONS</p>
          <h3>常用入口</h3>
        </div>
      </div>
      <div class="admin-actions">
        <button class="archive-button llm-action" @click="router.push('/admin/apikeys')">
          LLM / API KEYS
        </button>
        <button class="archive-button" @click="router.push('/admin/users')">MANAGE USERS</button>
        <button class="archive-button" @click="router.push('/admin/posts')">REVIEW POSTS</button>
        <button class="archive-button" @click="router.push('/admin/reports')">HANDLE REPORTS</button>
        <button class="archive-button archive-button-outline" @click="router.push('/admin/logs')">
          VIEW LOGS
        </button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAdminOverview, type DashboardOverview } from '@/api/admin'

const router = useRouter()
const overview = ref<DashboardOverview | null>(null)

const cards = computed(() => [
  { label: 'TOTAL USERS', value: overview.value?.totalUsers ?? 0 },
  { label: 'TODAY USERS', value: overview.value?.todayUsers ?? 0 },
  { label: 'TOTAL POSTS', value: overview.value?.totalPosts ?? 0 },
  { label: 'TODAY POSTS', value: overview.value?.todayPosts ?? 0 },
  { label: 'PENDING REPORTS', value: overview.value?.pendingReports ?? 0 },
  { label: 'BANNED USERS', value: overview.value?.bannedUsers ?? 0 }
])

const loadOverview = async () => {
  try {
    const res = await getAdminOverview()
    overview.value = res.data
  } catch (error) {
    ElMessage.error('加载后台概览失败')
  }
}

onMounted(() => {
  loadOverview()
})
</script>

<style scoped lang="scss">
@import '@/styles/admin.scss';

.llm-action {
  border: 2px solid $color-bordeaux;
  box-shadow: 4px 4px 0 rgba($color-bordeaux, 0.35);
}
</style>
