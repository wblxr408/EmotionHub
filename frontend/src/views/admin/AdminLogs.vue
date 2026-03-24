<template>
  <section class="admin-page">
    <div class="admin-toolbar">
      <div>
        <p class="meta">AUDIT TRAIL</p>
        <h3>管理员操作日志</h3>
      </div>
    </div>

    <div class="archive-card admin-panel">
      <div class="admin-filter-row">
        <el-input v-model.number="filters.operatorId" placeholder="操作人 ID" clearable />
        <el-select v-model="filters.action" placeholder="全部动作" clearable>
          <el-option label="BAN_USER" value="BAN_USER" />
          <el-option label="UNBAN_USER" value="UNBAN_USER" />
          <el-option label="HIDE_POST" value="HIDE_POST" />
          <el-option label="RESTORE_POST" value="RESTORE_POST" />
          <el-option label="DELETE_COMMENT" value="DELETE_COMMENT" />
          <el-option label="HANDLE_REPORT" value="HANDLE_REPORT" />
        </el-select>
        <button class="archive-button" @click="handleSearch">SEARCH</button>
      </div>
    </div>

    <div class="archive-card admin-panel">
      <el-table :data="logs" v-loading="loading" stripe>
        <el-table-column prop="createdAt" label="时间" min-width="180" />
        <el-table-column label="操作人" min-width="150">
          <template #default="{ row }">
            {{ row.operatorNickname || row.operatorUsername || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="action" label="动作" min-width="140" />
        <el-table-column label="目标" min-width="140">
          <template #default="{ row }">
            {{ row.targetType }} #{{ row.targetId }}
          </template>
        </el-table-column>
        <el-table-column prop="beforeState" label="变更前" min-width="180" />
        <el-table-column prop="afterState" label="变更后" min-width="180" />
        <el-table-column prop="remark" label="备注" min-width="220" />
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
import { ElMessage } from 'element-plus'
import { getAdminLogs, type AdminOperationLogItem } from '@/api/admin'

const loading = ref(false)
const total = ref(0)
const logs = ref<AdminOperationLogItem[]>([])

const filters = reactive({
  page: 1,
  size: 10,
  operatorId: undefined as number | undefined,
  action: ''
})

const loadLogs = async () => {
  loading.value = true
  try {
    const res = await getAdminLogs({
      ...filters,
      operatorId: filters.operatorId || undefined,
      action: filters.action || undefined
    })
    logs.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('加载操作日志失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  filters.page = 1
  loadLogs()
}

const handlePageChange = (page: number) => {
  filters.page = page
  loadLogs()
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped lang="scss">
@import '@/styles/admin.scss';
</style>
