-- EmotionHub 用户情感状态追踪系统 - 数据表创建脚本
-- 版本: V5
-- 功能: Sprint 2.1 - 用户情感状态追踪
-- ============================================

-- ============================================
-- 用户情感历史表 (user_emotion_history)
-- 用于存储用户的情感状态历史记录，支持滑动窗口统计
-- ============================================
DROP TABLE IF EXISTS `user_emotion_history`;
CREATE TABLE `user_emotion_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '情感历史记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `timestamp` BIGINT NOT NULL COMMENT '时间戳（毫秒）',
  `sentiment_score` INT NOT NULL COMMENT '情感分数（-100到100）',
  `source` VARCHAR(50) NOT NULL COMMENT '情感来源: POST/COMMENT/INTERACTION',
  `source_id` BIGINT DEFAULT NULL COMMENT '来源ID（帖子ID或评论ID）',
  `emotion_state` VARCHAR(20) DEFAULT NULL COMMENT '情感状态: HAPPY/CALM/LOW/ANXIOUS/FLUCTUANT',
  `volatility` DECIMAL(5,4) DEFAULT NULL COMMENT '波动性（标准差/均值）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_timestamp` (`user_id`, `timestamp` DESC),
  KEY `idx_user_state` (`user_id`, `emotion_state`),
  KEY `idx_source` (`source`, `source_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_emotion_history_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户情感历史表';

-- ============================================
-- 创建索引以优化滑动窗口查询
-- ============================================

-- 时间范围查询优化（支持 1小时、24小时、7天窗口）
CREATE INDEX `idx_emotion_time_window` ON `user_emotion_history` (`user_id`, `timestamp` DESC, `sentiment_score`);

-- 情感趋势分析优化
CREATE INDEX `idx_emotion_trend` ON `user_emotion_history` (`user_id`, `created_at` DESC);

-- ============================================
-- 初始化说明
-- ============================================
-- 本迁移脚本创建了用户情感状态追踪所需的核心表：
-- 1. user_emotion_history: 存储用户的情感历史记录
--    - 支持滑动窗口统计（1小时、24小时、7天）
--    - 记录情感来源（帖子/评论/互动）
--    - 存储计算后的情感状态和波动性
--
-- 配合 Redis 使用：
-- - Redis 存储最近7天的实时数据（快速查询）
-- - MySQL 存储完整历史数据（长期分析和归档）
--
-- 相关功能：
-- - 实时情感状态计算（滑动窗口）
-- - 情感趋势判断（上升/下降/稳定）
-- - 情感状态机（高兴/平静/低落/焦虑/波动）
-- - Spring Event 发布情感变化事件
