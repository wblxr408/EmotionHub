/**
 * Feed 流 API
 */
import request from './request'
import type { Post } from './post'

export interface FeedParams {
  userId: number
  strategy?: string
  page?: number
  size?: number
}

export interface FeedResponse {
  userId: number
  strategy: string
  emotionState: string
  page: number
  size: number
  items: Post[]
}

/**
 * 获取个性化 Feed 流
 */
export function getFeed(params: FeedParams) {
  return request<FeedResponse>({
    url: '/feed',
    method: 'get',
    params
  })
}

/**
 * 记录帖子点击（A/B 测试 CTR 统计）
 */
export function recordFeedClick(logId: number) {
  return request({
    url: `/feed/click/${logId}`,
    method: 'post'
  })
}
