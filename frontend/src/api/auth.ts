/**
 * 认证相关API
 */
import request from './request'

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams {
  username: string
  nickname: string
  email: string
  password: string
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  email: string
  avatar?: string
  bio?: string
  role: string
  status: string
  createdAt: string
}

export interface LoginResponse {
  token: string
  userInfo: UserInfo
}

/**
 * 登录
 */
export function login(data: LoginParams) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 注册
 */
export function register(data: RegisterParams) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

/**
 * 获取当前用户信息
 */
export function getUserInfo(userId?: number) {
  return request({
    url: userId ? `/user/${userId}` : '/user/info',
    method: 'get'
  })
}

/**
 * 退出登录
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}
