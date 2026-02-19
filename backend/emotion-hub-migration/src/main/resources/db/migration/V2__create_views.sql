-- ============================================
-- 创建视图
-- ============================================

-- 帖子详情视图（包含用户信息）
CREATE OR REPLACE VIEW `v_post_detail` AS
SELECT
  p.id,
  p.content,
  p.images,
  p.emotion_score,
  p.emotion_label,
  p.view_count,
  p.like_count,
  p.comment_count,
  p.status,
  p.created_at,
  u.id AS user_id,
  u.username,
  u.nickname,
  u.avatar
FROM `post` p
INNER JOIN `user` u ON p.user_id = u.id
WHERE p.status = 'PUBLISHED';

-- ============================================
-- 初始化完成
-- ============================================
SELECT 'Database initialization completed successfully!' AS message;
