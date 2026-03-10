/**
 * 通知相关API
 */
import request from './request'
import type { PageResult } from './post'

export interface Notification {
  id: number
  userId: number
  type: string
  title: string
  content: string
  relatedId?: number
  isRead: number
  createdAt: string
}

/**
 * 获取未读通知
 */
export function getUnreadNotifications(page = 1, size = 10) {
  return request({
    url: '/notification/unread',
    method: 'get',
    params: { page, size }
  })
}

/**
 * 获取所有通知
 */
export function getAllNotifications(page = 1, size = 10) {
  return request({
    url: '/notification/list',
    method: 'get',
    params: { page, size }
  })
}

/**
 * 获取未读数量
 */
export function getUnreadCount() {
  return request({
    url: '/notification/unread/count',
    method: 'get'
  })
}

/**
 * 标记已读
 */
export function markAsRead(notificationId: number) {
  return request({
    url: `/notification/read/${notificationId}`,
    method: 'put'
  })
}

/**
 * 全部标记已读
 */
export function markAllAsRead() {
  return request({
    url: '/notification/read/all',
    method: 'put'
  })
}

/**
 * 删除通知
 */
export function deleteNotification(notificationId: number) {
  return request({
    url: `/notification/${notificationId}`,
    method: 'delete'
  })
}
