<template>
  <section class="admin-page">
    <div class="admin-toolbar">
      <div>
        <p class="meta">REPORT CENTER</p>
        <h3>举报处理</h3>
      </div>
    </div>

    <div class="archive-card admin-panel">
      <div class="admin-filter-row">
        <el-select v-model="filters.status" placeholder="全部状态" clearable>
          <el-option label="PENDING" value="PENDING" />
          <el-option label="PROCESSED" value="PROCESSED" />
          <el-option label="REJECTED" value="REJECTED" />
        </el-select>
        <el-select v-model="filters.targetType" placeholder="全部目标" clearable>
          <el-option label="POST" value="POST" />
          <el-option label="COMMENT" value="COMMENT" />
        </el-select>
        <button class="archive-button" @click="handleSearch">SEARCH</button>
      </div>
    </div>

    <div class="archive-card admin-panel">
      <el-table :data="reports" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column label="举报人" min-width="150">
          <template #default="{ row }">
            {{ row.reporterNickname || row.reporterUsername || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="目标" min-width="120">
          <template #default="{ row }">
            {{ row.targetType }} #{{ row.targetId }}
          </template>
        </el-table-column>
        <el-table-column label="内容摘要" min-width="260">
          <template #default="{ row }">
            <div class="table-preview">{{ row.targetPreview || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="举报原因" min-width="220" />
        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <span class="meta-pill" :class="statusClass(row.status)">{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" min-width="180" />
        <el-table-column label="操作" min-width="320" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row.id)">查看</el-button>
            <template v-if="row.status === 'PENDING'">
              <el-button size="small" type="success" @click="promptHandle(row.id, 'PROCESSED', 'NONE')">
                标记处理
              </el-button>
              <el-button
                v-if="row.targetType === 'POST'"
                size="small"
                type="danger"
                @click="promptHandle(row.id, 'PROCESSED', 'HIDE_POST')"
              >
                下架帖子
              </el-button>
              <el-button
                v-if="row.targetType === 'COMMENT'"
                size="small"
                type="danger"
                @click="promptHandle(row.id, 'PROCESSED', 'DELETE_COMMENT')"
              >
                删除评论
              </el-button>
              <el-button size="small" @click="promptHandle(row.id, 'REJECTED', 'NONE')">驳回</el-button>
            </template>
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

    <el-dialog v-model="detailVisible" title="举报详情" width="680px">
      <dl v-if="detail" class="detail-grid">
        <dt>举报 ID</dt>
        <dd>{{ detail.id }}</dd>
        <dt>举报人</dt>
        <dd>{{ detail.reporterNickname || detail.reporterUsername || '-' }}</dd>
        <dt>目标</dt>
        <dd>{{ detail.targetType }} #{{ detail.targetId }}</dd>
        <dt>目标摘要</dt>
        <dd>{{ detail.targetPreview || '-' }}</dd>
        <dt>举报原因</dt>
        <dd>{{ detail.reason }}</dd>
        <dt>状态</dt>
        <dd>{{ detail.status }}</dd>
        <dt>处理动作</dt>
        <dd>{{ detail.action || '-' }}</dd>
        <dt>处理人</dt>
        <dd>{{ detail.handlerNickname || '-' }}</dd>
        <dt>处理时间</dt>
        <dd>{{ detail.handledAt || '-' }}</dd>
        <dt>处理备注</dt>
        <dd>{{ detail.remark || '-' }}</dd>
      </dl>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdminReportDetail,
  getAdminReports,
  handleAdminReport,
  type AdminReportItem
} from '@/api/admin'

const loading = ref(false)
const total = ref(0)
const reports = ref<AdminReportItem[]>([])
const detailVisible = ref(false)
const detail = ref<AdminReportItem | null>(null)

const filters = reactive({
  page: 1,
  size: 10,
  status: '',
  targetType: ''
})

const loadReports = async () => {
  loading.value = true
  try {
    const res = await getAdminReports({
      ...filters,
      status: filters.status || undefined,
      targetType: filters.targetType || undefined
    })
    reports.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('加载举报列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  filters.page = 1
  loadReports()
}

const handlePageChange = (page: number) => {
  filters.page = page
  loadReports()
}

const openDetail = async (reportId: number) => {
  try {
    const res = await getAdminReportDetail(reportId)
    detail.value = res.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('加载举报详情失败')
  }
}

const promptHandle = async (reportId: number, status: string, action: string) => {
  const titleMap: Record<string, string> = {
    NONE: status === 'REJECTED' ? '请输入驳回备注' : '请输入处理备注',
    HIDE_POST: '请输入下架备注',
    DELETE_COMMENT: '请输入删除备注'
  }

  try {
    const result = await ElMessageBox.prompt(titleMap[action] || '请输入备注', '处理举报', {
      inputPlaceholder: '可选备注',
      confirmButtonText: '提交',
      cancelButtonText: '取消'
    })
    await handleAdminReport(reportId, {
      status,
      action,
      remark: result.value?.trim() || ''
    })
    ElMessage.success('举报已处理')
    if (detailVisible.value && detail.value?.id === reportId) {
      openDetail(reportId)
    }
    loadReports()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('举报处理失败')
    }
  }
}

const statusClass = (status: string) => {
  if (status === 'PROCESSED') return 'processed'
  if (status === 'REJECTED') return 'rejected'
  return 'pending'
}

onMounted(() => {
  loadReports()
})
</script>

<style scoped lang="scss">
@import '@/styles/admin.scss';
</style>
