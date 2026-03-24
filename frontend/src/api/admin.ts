import request from './request'
import type { UserInfo } from './auth'
import type { PageResult, Post } from './post'

export interface DashboardOverview {
  totalUsers: number
  todayUsers: number
  totalPosts: number
  todayPosts: number
  pendingReports: number
  bannedUsers: number
}

export interface AdminReportItem {
  id: number
  reporterId: number
  reporterUsername?: string
  reporterNickname?: string
  targetType: 'POST' | 'COMMENT'
  targetId: number
  targetPreview?: string
  reason: string
  status: string
  handlerId?: number
  handlerNickname?: string
  handledAt?: string
  action?: string
  remark?: string
  createdAt: string
}

export interface AdminOperationLogItem {
  id: number
  operatorId: number
  operatorUsername?: string
  operatorNickname?: string
  action: string
  targetType: string
  targetId: number
  beforeState?: string
  afterState?: string
  remark?: string
  createdAt: string
}

export interface AdminUserQuery {
  page?: number
  size?: number
  keyword?: string
  status?: string
}

export interface AdminPostQuery {
  page?: number
  size?: number
  keyword?: string
  status?: string
  userId?: number
}

export interface AdminReportQuery {
  page?: number
  size?: number
  status?: string
  targetType?: string
}

export interface AdminLogQuery {
  page?: number
  size?: number
  operatorId?: number
  action?: string
}

export interface StatusUpdatePayload {
  status: string
}

export interface HandleReportPayload {
  status: string
  action?: string
  remark?: string
}

export interface CreateReportPayload {
  targetType: 'POST' | 'COMMENT'
  targetId: number
  reason: string
}

export function getAdminOverview() {
  return request({
    url: '/admin/dashboard/overview',
    method: 'get'
  })
}

export function getAdminUsers(params: AdminUserQuery) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

export function getAdminUserDetail(userId: number) {
  return request({
    url: `/admin/users/${userId}`,
    method: 'get'
  })
}

export function updateAdminUserStatus(userId: number, data: StatusUpdatePayload) {
  return request({
    url: `/admin/users/${userId}/status`,
    method: 'put',
    data
  })
}

export function getAdminPosts(params: AdminPostQuery) {
  return request({
    url: '/admin/posts',
    method: 'get',
    params
  })
}

export function updateAdminPostStatus(postId: number, data: StatusUpdatePayload) {
  return request({
    url: `/admin/posts/${postId}/status`,
    method: 'put',
    data
  })
}

export function deleteAdminComment(commentId: number) {
  return request({
    url: `/admin/comments/${commentId}`,
    method: 'delete'
  })
}

export function getAdminReports(params: AdminReportQuery) {
  return request({
    url: '/admin/reports',
    method: 'get',
    params
  })
}

export function getAdminReportDetail(reportId: number) {
  return request({
    url: `/admin/reports/${reportId}`,
    method: 'get'
  })
}

export function handleAdminReport(reportId: number, data: HandleReportPayload) {
  return request({
    url: `/admin/reports/${reportId}/handle`,
    method: 'put',
    data
  })
}

export function getAdminLogs(params: AdminLogQuery) {
  return request({
    url: '/admin/operation-logs',
    method: 'get',
    params
  })
}

export function submitReport(data: CreateReportPayload) {
  return request({
    url: '/report',
    method: 'post',
    data
  })
}

export type AdminUser = UserInfo
export type AdminPost = Post
export type AdminPageResult<T> = PageResult<T>
