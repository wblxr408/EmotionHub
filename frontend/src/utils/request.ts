import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data

    // 如果返回的状态码不是200，说明接口有问题
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res
  },
  (error) => {
    console.error('响应错误:', error)

    if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，服务可能仍在启动中，请稍后重试')
    } else if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          // 跳转到登录页
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error('网络错误')
      }
    } else {
      ElMessage.error('网络连接失败，请检查后端或前端代理是否已就绪')
    }

    return Promise.reject(error)
  }
)

export default service
