<template>
  <section class="admin-page">
    <div class="admin-toolbar">
      <div>
        <p class="meta">CONTENT REVIEW</p>
        <h3>帖子管理</h3>
      </div>
    </div>

    <div class="archive-card admin-panel">
      <div class="admin-filter-row">
        <el-input
          v-model="filters.keyword"
          placeholder="内容关键词 / 作者信息"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-select v-model="filters.status" placeholder="全部状态" clearable>
          <el-option label="PUBLISHED" value="PUBLISHED" />
          <el-option label="HIDDEN" value="HIDDEN" />
          <el-option label="ANALYZING" value="ANALYZING" />
        </el-select>
        <el-input v-model.number="filters.userId" placeholder="作者 ID" clearable />
        <button class="archive-button" @click="handleSearch">SEARCH</button>
      </div>
    </div>

    <div class="archive-card admin-panel">
      <el-table :data="posts" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column label="作者" min-width="150">
          <template #default="{ row }">
            {{ row.nickname || row.username }}
          </template>
        </el-table-column>
        <el-table-column label="内容" min-width="360">
          <template #default="{ row }">
            <div class="table-preview">{{ preview(row.content) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <span class="meta-pill" :class="statusClass(row.status)">{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column label="互动" min-width="160">
          <template #default="{ row }">
            <span class="meta">{{ row.viewCount }} / {{ row.likeCount }} / {{ row.commentCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" min-width="180" />
        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="router.push(`/post/${row.id}`)">查看</el-button>
            <el-button
              v-if="row.status === 'PUBLISHED'"
              size="small"
              type="danger"
              @click="changeStatus(row.id, 'HIDDEN')"
            >
              下架
            </el-button>
            <el-button
              v-else-if="row.status === 'HIDDEN'"
              size="small"
              type="success"
              @click="changeStatus(row.id, 'PUBLISHED')"
            >
              恢复
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :current-page="filters.page"
          :page-size="filters.size"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminPosts, updateAdminPostStatus, type AdminPost } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const total = ref(0)
const posts = ref<AdminPost[]>([])

const filters = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: '',
  userId: undefined as number | undefined
})

const loadPosts = async () => {
  loading.value = true
  try {
    const res = await getAdminPosts({
      ...filters,
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
      userId: filters.userId || undefined
    })
    posts.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('加载帖子列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  filters.page = 1
  loadPosts()
}

const handlePageChange = (page: number) => {
  filters.page = page
  loadPosts()
}

const changeStatus = async (postId: number, status: 'PUBLISHED' | 'HIDDEN') => {
  const message = status === 'HIDDEN' ? '确认下架该帖子？' : '确认恢复该帖子？'
  try {
    await ElMessageBox.confirm(message, '帖子状态更新', { type: 'warning' })
    await updateAdminPostStatus(postId, { status })
    ElMessage.success('帖子状态已更新')
    loadPosts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('帖子状态更新失败')
    }
  }
}

const preview = (content: string) => {
  if (content.length <= 90) return content
  return `${content.slice(0, 90)}...`
}

const statusClass = (status: string) => {
  if (status === 'PUBLISHED') return 'published'
  if (status === 'HIDDEN') return 'hidden'
  return 'pending'
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped lang="scss">
@import '@/styles/admin.scss';
</style>
