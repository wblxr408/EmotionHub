-- EmotionHub 内容情感标签库 - 数据表创建脚本
-- 版本: V7
-- 功能: Sprint 2.2 - 情感互补推荐引擎
-- ============================================

DROP TABLE IF EXISTS `content_emotion_tags`;
CREATE TABLE `content_emotion_tags` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签记录ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `primary_tag` VARCHAR(50) NOT NULL COMMENT '主标签',
  `tags` JSON DEFAULT NULL COMMENT '标签列表（JSON数组）',
  `sentiment_score` DECIMAL(5,4) DEFAULT NULL COMMENT '帖子情感分数',
  `controversy_score` DECIMAL(5,4) DEFAULT NULL COMMENT '争议性分数（标准差）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_id` (`post_id`),
  KEY `idx_primary_tag` (`primary_tag`),
  KEY `idx_controversy_score` (`controversy_score`),
  CONSTRAINT `fk_content_tag_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容情感标签库';

-- ============================================
-- 初始化说明
-- - 内容情感标签由定时任务自动生成
-- - 标签受帖子情感分数与评论区波动影响
-- ============================================
