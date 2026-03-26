/**
 * LLM API密钥配置API
 */
import request from './request'

export interface ApiKeyConfigVO {
  id: number
  userId: number | null
  isPlatformDefault: boolean
  provider: string
  maskedApiKey: string
  apiUrl: string | null
  model: string | null
  isEnabled: boolean
  isDefault: boolean
  createdAt: string
  updatedAt: string
}

export interface ApiKeyConfigRequest {
  provider: string
  apiKey: string
  apiUrl?: string
  model?: string
  isDefault?: boolean
}

export const PROVIDERS = [
  {
    code: 'qianwen',
    name: '通义千问',
    description: '阿里云通义千问系列模型',
    icon: '🔮',
    keyPlaceholder: 'sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
    urlPlaceholder: 'https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation'
  },
  {
    code: 'openai',
    name: 'OpenAI',
    description: 'GPT系列模型',
    icon: '🤖',
    keyPlaceholder: 'sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
    urlPlaceholder: 'https://api.openai.com/v1/chat/completions'
  },
  {
    code: 'wenxin',
    name: '文心一言',
    description: '百度文心一言模型',
    icon: '📝',
    keyPlaceholder: '您的文心一言API Key',
    urlPlaceholder: 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions'
  },
  {
    code: 'zhipu',
    name: '智谱AI',
    description: 'ChatGLM系列模型',
    icon: '✨',
    keyPlaceholder: '您的智谱AI API Key',
    urlPlaceholder: 'https://open.bigmodel.cn/api/paas/v4/chat/completions'
  }
]

/**
 * 获取当前用户的API密钥配置列表
 */
export function getMyApiKeyConfigs() {
  return request<ApiKeyConfigVO[]>({
    url: '/apikey/list',
    method: 'get'
  })
}

/**
 * 获取平台默认API密钥配置（仅管理员）
 */
export function getPlatformApiKeyConfigs() {
  return request<ApiKeyConfigVO[]>({
    url: '/apikey/platform',
    method: 'get'
  })
}

/**
 * 获取指定提供商的生效API配置
 */
export function getEffectiveApiKey(provider: string) {
  return request<ApiKeyConfigVO>({
    url: '/apikey/effective',
    method: 'get',
    params: { provider }
  })
}

/**
 * 保存API密钥配置
 */
export function saveApiKeyConfig(data: ApiKeyConfigRequest) {
  return request<void>({
    url: '/apikey/save',
    method: 'post',
    data
  })
}

/**
 * 删除API密钥配置
 */
export function deleteApiKeyConfig(provider: string) {
  return request<void>({
    url: '/apikey/delete',
    method: 'delete',
    params: { provider }
  })
}

/**
 * 验证API Key格式
 */
export function validateApiKey(provider: string, apiKey: string) {
  return request<boolean>({
    url: '/apikey/validate',
    method: 'post',
    params: { provider, apiKey }
  })
}
