import request from './request'

interface TestConnectionResult {
  code: number
  message: string
  data: string
  timestamp: number
}

/**
 * 测试API连接
 */
export function testConnection() {
  return request({
    url: '/test/hello',
    method: 'get'
  }) as Promise<TestConnectionResult>
}
