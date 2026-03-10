/**
 * 帖子相关API
 */
import request from './request'

export interface PostCreateParams {
  content: string
  images?: string[]
}

export interface PostQueryParams {
  page?: number
  size?: number
  emotionLabel?: string
  orderBy?: 'LATEST' | 'HOT'
  userId?: number
}

export interface Post {
  id: number
  userId: number
  username: string
  nickname: string
  avatar?: string
  content: string
  images?: string[]
  emotionScore?: number
  emotionLabel?: string
  viewCount: number
  likeCount: number
  commentCount: number
  liked?: boolean
  status: string
  createdAt: string
  updatedAt: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
  pages: number
  hasNext: boolean
}

/**
 * 发布帖子
 */
export function createPost(data: PostCreateParams) {
  return request({
    url: '/post/create',
    method: 'post',
    data
  })
}

/**
 * 查询帖子列表
 */
export function listPosts(params: PostQueryParams) {
  return request({
    url: '/post/list',
    method: 'get',
    params
  })
}

/**
 * 获取帖子详情
 */
export function getPostDetail(postId: number) {
  return request({
    url: `/post/${postId}`,
    method: 'get'
  })
}

/**
 * 删除帖子
 */
export function deletePost(postId: number) {
  return request({
    url: `/post/${postId}`,
    method: 'delete'
  })
}

/**
 * 获取用户帖子
 */
export function getUserPosts(userId: number, page = 1, size = 10) {
  return request({
    url: `/post/user/${userId}`,
    method: 'get',
    params: { page, size }
  })
}
