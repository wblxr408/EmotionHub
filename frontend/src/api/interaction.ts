/**
 * 互动相关API（点赞、评论）
 */
import request from './request'

export interface LikeParams {
  targetId: number
  targetType: 'POST' | 'COMMENT'
}

export interface CommentCreateParams {
  postId: number
  content: string
  parentId?: number
}

export interface Comment {
  id: number
  postId: number
  userId: number
  username: string
  nickname: string
  avatar?: string
  parentId?: number
  content: string
  likeCount: number
  liked: boolean
  createdAt: string
  children?: Comment[]
}

/**
 * 点赞/取消点赞
 */
export function toggleLike(data: LikeParams) {
  return request({
    url: '/interaction/like',
    method: 'post',
    data
  })
}

/**
 * 检查是否已点赞
 */
export function checkLike(targetId: number, targetType: string) {
  return request({
    url: '/interaction/like/check',
    method: 'get',
    params: { targetId, targetType }
  })
}

/**
 * 发表评论
 */
export function createComment(data: CommentCreateParams) {
  return request({
    url: '/interaction/comment',
    method: 'post',
    data
  })
}

/**
 * 查询评论列表
 */
export function listComments(postId: number) {
  return request({
    url: '/interaction/comment/list',
    method: 'get',
    params: { postId }
  })
}

/**
 * 删除评论
 */
export function deleteComment(commentId: number) {
  return request({
    url: `/interaction/comment/${commentId}`,
    method: 'delete'
  })
}
