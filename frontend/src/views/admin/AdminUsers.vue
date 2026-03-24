<template>
  <section class="admin-page">
    <div class="admin-toolbar">
      <div>
        <p class="meta">USER CONTROL</p>
        <h3>用户管理</h3>
      </div>
    </div>

    <div class="archive-card admin-panel">
      <div class="admin-filter-row">
        <el-input
          v-model="filters.keyword"
          placeholder="用户名 / 昵称 / 邮箱"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-select v-model="filters.status" placeholder="全部状态" clearable>
          <el-option label="ACTIVE" value="ACTIVE" />
          <el-option label="BANNED" value="BANNED" />
        </el-select>
        <button class="archive-button" @click="handleSearch">SEARCH</button>
      </div>
    </div>

    <div class="archive-card admin-panel">
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="nickname" label="昵称" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="220" />
        <el-table-column label="角色" min-width="100">
          <template #default="{ row }">
            <span class="meta-pill" :class="row.role === 'ADMIN' ? 'processed' : 'pending'">
              {{ row.role }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <span class="meta-pill" :class="row.status === 'BANNED' ? 'banned' : 'active'">
              {{ row.status }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" min-width="180" />
        <el-table-column label="操作" min-width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== 'BANNED'"
              size="small"
              type="danger"
              :disabled="row.id === userStore.userInfo?.id"
              @click="changeStatus(row.id, 'BANNED')"
            >
              封禁
            </el-button>
            <el-button
              v-else
              size="small"
              type="success"
              @click="changeStatus(row.id, 'ACTIVE')"
            >
              解封
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminUsers, updateAdminUserStatus, type AdminUser } from '@/api/admin'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const total = ref(0)
const users = ref<AdminUser[]>([])

const filters = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: ''
})

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getAdminUsers({
      ...filters,
      keyword: filters.keyword || undefined,
      status: filters.status || undefined
    })
    users.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  filters.page = 1
  loadUsers()
}

const handlePageChange = (page: number) => {
  filters.page = page
  loadUsers()
}

const changeStatus = async (userId: number, status: 'ACTIVE' | 'BANNED') => {
  const title = status === 'BANNED' ? '确认封禁该用户？' : '确认解封该用户？'
  try {
    await ElMessageBox.confirm(title, '用户状态更新', { type: 'warning' })
    await updateAdminUserStatus(userId, { status })
    ElMessage.success('用户状态已更新')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('用户状态更新失败')
    }
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped lang="scss">
@import '@/styles/admin.scss';
</style>
