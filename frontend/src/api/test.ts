import request from '@/utils/request'

/**
 * 测试API连接
 */
export function testConnection() {
  return request({
    url: '/api/test/hello',
    method: 'get'
  })
}
