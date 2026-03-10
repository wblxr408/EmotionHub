/**
 * User Store - 用户状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo as getUserInfoApi } from '@/api/auth'
import type { LoginParams, RegisterParams, UserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  /**
   * 登录
   */
  async function login(params: LoginParams) {
    const res = await loginApi(params)
    token.value = res.data.token
    userInfo.value = res.data.userInfo
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data.userInfo))
  }

  /**
   * 注册
   */
  async function register(params: RegisterParams) {
    const res = await registerApi(params)
    token.value = res.data.token
    userInfo.value = res.data.userInfo
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data.userInfo))
  }

  /**
   * 获取用户信息
   */
  async function getUserInfo() {
    const res = await getUserInfoApi()
    userInfo.value = res.data
    localStorage.setItem('userInfo', JSON.stringify(res.data))
  }

  /**
   * 退出登录
   */
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  /**
   * 从localStorage初始化用户信息
   */
  function initUserInfo() {
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo) {
      try {
        userInfo.value = JSON.parse(storedUserInfo)
      } catch (e) {
        console.error('Failed to parse userInfo from localStorage', e)
      }
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    register,
    getUserInfo,
    logout,
    initUserInfo
  }
})
