-- EmotionHub 情感社交媒体分析平台 - 数据库初始化脚本
-- 数据库版本: MySQL 8.0+
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- ============================================
-- 1. 用户表 (user)
-- ============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `bio` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
  `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/BANNED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 帖子表 (post)
-- ============================================
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `content` TEXT NOT NULL COMMENT '帖子内容',
  `images` JSON DEFAULT NULL COMMENT '图片URLs（JSON数组）',
  `emotion_score` DECIMAL(3,2) DEFAULT NULL COMMENT '情感分数: -1.00 到 1.00',
  `emotion_label` VARCHAR(20) DEFAULT NULL COMMENT '情感标签: POSITIVE/NEUTRAL/NEGATIVE',
  `llm_analysis` JSON DEFAULT NULL COMMENT 'LLM分析结果（JSON）',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览数',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ANALYZING' COMMENT '状态: ANALYZING/PUBLISHED/DELETED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_emotion_label` (`emotion_label`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- ============================================
-- 3. 评论表 (comment)
-- ============================================
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID（支持嵌套）',
  `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
  `emotion_score` DECIMAL(3,2) DEFAULT NULL COMMENT '情感分数',
  `emotion_label` VARCHAR(20) DEFAULT NULL COMMENT '情感标签',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ============================================
-- 4. 点赞表 (like_record)
-- ============================================
DROP TABLE IF EXISTS `like_record`;
CREATE TABLE `like_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '点赞记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `target_id` BIGINT NOT NULL COMMENT '目标ID（帖子或评论）',
  `target_type` VARCHAR(20) NOT NULL COMMENT '目标类型: POST/COMMENT',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`),
  KEY `idx_target` (`target_id`, `target_type`),
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';

-- ============================================
-- 5. 情感分析记录表 (emotion_analysis)
-- ============================================
DROP TABLE IF EXISTS `emotion_analysis`;
CREATE TABLE `emotion_analysis` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分析记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `content_type` VARCHAR(20) NOT NULL COMMENT '内容类型: POST/COMMENT',
  `content_id` BIGINT NOT NULL COMMENT '内容ID',
  `llm_provider` VARCHAR(50) NOT NULL COMMENT 'LLM提供商: openai/qianwen/wenxin/zhipu',
  `request_data` JSON DEFAULT NULL COMMENT '请求数据（JSON）',
  `response_data` JSON DEFAULT NULL COMMENT '响应数据（JSON）',
  `emotion_score` DECIMAL(3,2) NOT NULL COMMENT '情感分数',
  `emotion_label` VARCHAR(20) NOT NULL COMMENT '情感标签',
  `keywords` JSON DEFAULT NULL COMMENT '关键词（JSON数组）',
  `analysis` VARCHAR(200) DEFAULT NULL COMMENT '分析说明',
  `analysis_time` INT NOT NULL COMMENT '分析耗时（毫秒）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_content` (`content_type`, `content_id`),
  KEY `idx_llm_provider` (`llm_provider`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_analysis_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情感分析记录表';

-- ============================================
-- 6. 用户情感统计表 (user_emotion_stats)
-- ============================================
DROP TABLE IF EXISTS `user_emotion_stats`;
CREATE TABLE `user_emotion_stats` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `date` DATE NOT NULL COMMENT '统计日期',
  `positive_count` INT NOT NULL DEFAULT 0 COMMENT '积极情绪数',
  `neutral_count` INT NOT NULL DEFAULT 0 COMMENT '中性情绪数',
  `negative_count` INT NOT NULL DEFAULT 0 COMMENT '消极情绪数',
  `avg_emotion_score` DECIMAL(3,2) DEFAULT NULL COMMENT '平均情感分数',
  `total_posts` INT NOT NULL DEFAULT 0 COMMENT '总帖子数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `date`),
  KEY `idx_date` (`date`),
  CONSTRAINT `fk_stats_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户情感统计表';

-- ============================================
-- 7. 通知表 (notification)
-- ============================================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT NOT NULL COMMENT '接收者ID',
  `type` VARCHAR(20) NOT NULL COMMENT '通知类型: LIKE/COMMENT/SYSTEM/ANALYSIS_COMPLETE',
  `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
  `content` VARCHAR(500) NOT NULL COMMENT '通知内容',
  `related_id` BIGINT DEFAULT NULL COMMENT '关联ID（帖子/评论ID）',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';