#!/bin/bash

# EmotionHub API 测试脚本
# 用于快速测试所有核心功能

BASE_URL="http://localhost:8080/api"
TOKEN=""

echo "=========================================="
echo "EmotionHub API 测试脚本"
echo "=========================================="
echo ""

# 1. 健康检查
echo "1. 测试健康检查..."
curl -s "${BASE_URL}/test/hello" | jq '.'
echo ""

# 2. 用户注册
echo "2. 测试用户注册..."
REGISTER_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com",
    "nickname": "测试用户"
  }')
echo $REGISTER_RESPONSE | jq '.'
echo ""

# 3. 用户登录
echo "3. 测试用户登录..."
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }')
echo $LOGIN_RESPONSE | jq '.'

# 提取Token
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.token')
echo "Token: $TOKEN"
echo ""

if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
  echo "❌ 登录失败，无法获取Token，后续测试中止"
  exit 1
fi

# 4. 获取当前用户信息
echo "4. 测试获取当前用户信息..."
curl -s "${BASE_URL}/auth/current" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# 5. 发布帖子
echo "5. 测试发布帖子..."
POST_RESPONSE=$(curl -s -X POST "${BASE_URL}/post/create" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "今天天气真好，心情很开心！感恩生活中的每一天，希望未来更美好。",
    "images": ["https://example.com/image1.jpg"]
  }')
echo $POST_RESPONSE | jq '.'

POST_ID=$(echo $POST_RESPONSE | jq -r '.data.id')
echo "帖子ID: $POST_ID"
echo ""

# 6. 等待情感分析完成
echo "6. 等待情感分析完成（3秒）..."
sleep 3
echo ""

# 7. 获取帖子详情
echo "7. 测试获取帖子详情..."
curl -s "${BASE_URL}/post/${POST_ID}" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# 8. 查询帖子列表
echo "8. 测试查询帖子列表..."
curl -s "${BASE_URL}/post/list?page=1&size=10&orderBy=LATEST" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# 9. 点赞帖子
echo "9. 测试点赞帖子..."
curl -s -X POST "${BASE_URL}/interaction/like" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"targetId\": ${POST_ID},
    \"targetType\": \"POST\"
  }" | jq '.'
echo ""

# 10. 发表评论
echo "10. 测试发表评论..."
COMMENT_RESPONSE=$(curl -s -X POST "${BASE_URL}/interaction/comment" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"postId\": ${POST_ID},
    \"content\": \"很棒的分享！\"
  }")
echo $COMMENT_RESPONSE | jq '.'

COMMENT_ID=$(echo $COMMENT_RESPONSE | jq -r '.data.id')
echo "评论ID: $COMMENT_ID"
echo ""

# 11. 查询评论列表
echo "11. 测试查询评论列表..."
curl -s "${BASE_URL}/interaction/comment/list?postId=${POST_ID}" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# 12. 获取我的统计
echo "12. 测试获取我的统计..."
curl -s "${BASE_URL}/stats/my" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# 13. 获取平台统计
echo "13. 测试获取平台统计..."
curl -s "${BASE_URL}/stats/platform" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# 14. 获取未读通知数量
echo "14. 测试获取未读通知数量..."
curl -s "${BASE_URL}/notification/unread/count" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

echo "=========================================="
echo "✅ 所有测试完成！"
echo "=========================================="
echo ""
echo "提示："
echo "- 访问 API 文档: http://localhost:8080/api/doc.html"
echo "- 使用 Token 进行后续测试: $TOKEN"
echo ""
