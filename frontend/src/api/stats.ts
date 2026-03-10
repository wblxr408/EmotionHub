/**
 * 统计相关API
 */
import request from './request'

export interface UserStats {
  userId: number
  username: string
  nickname: string
  postCount: number
  commentCount: number
  totalLikes: number
  emotionStats: {
    positive: number
    negative: number
    neutral: number
  }
}

export interface PlatformStats {
  totalUsers: number
  totalPosts: number
  totalComments: number
  totalLikes: number
  emotionDistribution: {
    positive: number
    negative: number
    neutral: number
  }
}

/**
 * 获取我的统计
 */
export function getMyStats() {
  return request({
    url: '/stats/my',
    method: 'get'
  })
}

/**
 * 获取用户统计
 */
export function getUserStats(userId: number) {
  return request({
    url: `/stats/user/${userId}`,
    method: 'get'
  })
}

/**
 * 获取平台统计
 */
export function getPlatformStats() {
  return request({
    url: '/stats/platform',
    method: 'get'
  })
}
