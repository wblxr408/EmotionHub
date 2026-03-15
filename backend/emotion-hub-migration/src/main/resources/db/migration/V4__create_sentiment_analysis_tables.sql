-- EmotionHub 情感传播分析系统 - 数据表创建脚本
-- 版本: V4
-- 功能: 情感传染分析、用户影响力、情感共鸣网络
-- ============================================

-- ============================================
-- 1. 情感传播关系表 (sentiment_propagation)
-- 用于追踪帖子和评论之间的情感传播路径
-- ============================================
DROP TABLE IF EXISTS `sentiment_propagation`;
CREATE TABLE `sentiment_propagation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '传播记录ID',
  `post_id` BIGINT NOT NULL COMMENT '原始帖子ID',
  `comment_id` BIGINT NOT NULL COMMENT '评论ID',
  `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
  `parent_comment_id` BIGINT DEFAULT NULL COMMENT '父评论ID（用于追踪评论链）',
  `depth_level` INT NOT NULL DEFAULT 1 COMMENT '评论层级（1表示一级评论）',
  `post_sentiment_score` DECIMAL(3,2) NOT NULL COMMENT '原帖情感分数',
  `comment_sentiment_score` DECIMAL(3,2) NOT NULL COMMENT '评论情感分数',
  `sentiment_consistency` DECIMAL(5,4) DEFAULT NULL COMMENT '情感一致性（-1到1，1表示完全一致）',
  `sentiment_amplification` DECIMAL(5,4) DEFAULT NULL COMMENT '情感放大系数（评论强度/原帖强度）',
  `is_sentiment_shift` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否发生情感转折（0-否，1-是）',
  `shift_direction` VARCHAR(20) DEFAULT NULL COMMENT '转折方向: POSITIVE_TO_NEGATIVE/NEGATIVE_TO_POSITIVE/NULL',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_comment_id` (`comment_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_comment_id` (`parent_comment_id`),
  KEY `idx_depth_level` (`depth_level`),
  KEY `idx_sentiment_shift` (`is_sentiment_shift`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_propagation_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_propagation_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_propagation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情感传播关系表';

-- ============================================
-- 2. 用户情感影响力表 (user_influence_score)
-- 用于存储用户的情感影响力评分
-- ============================================
DROP TABLE IF EXISTS `user_influence_score`;
CREATE TABLE `user_influence_score` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '影响力记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `influence_score` DECIMAL(10,4) NOT NULL DEFAULT 0.0000 COMMENT '综合影响力分数（PageRank变体算法计算）',
  `positive_impact` DECIMAL(10,4) NOT NULL DEFAULT 0.0000 COMMENT '正面影响力（引发正面评论的能力）',
  `negative_impact` DECIMAL(10,4) NOT NULL DEFAULT 0.0000 COMMENT '负面影响力（引发负面评论的能力）',
  `controversial_score` DECIMAL(10,4) NOT NULL DEFAULT 0.0000 COMMENT '争议性分数（引发情感分化的程度）',
  `post_count` INT NOT NULL DEFAULT 0 COMMENT '统计期内帖子数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '统计期内评论数',
  `avg_engagement_depth` DECIMAL(5,2) DEFAULT NULL COMMENT '平均互动深度（评论链平均层级）',
  `sentiment_change_rate` DECIMAL(5,4) DEFAULT NULL COMMENT '情感改变率（能够改变他人情感的比例）',
  `calculation_date` DATE NOT NULL COMMENT '计算日期',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `calculation_date`),
  KEY `idx_influence_score` (`influence_score` DESC),
  KEY `idx_positive_impact` (`positive_impact` DESC),
  KEY `idx_controversial_score` (`controversial_score` DESC),
  KEY `idx_calculation_date` (`calculation_date`),
  CONSTRAINT `fk_influence_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户情感影响力表';

-- ============================================
-- 3. 情感共鸣关系表 (sentiment_resonance)
-- 用于存储用户之间的情感相似度和共鸣关系
-- ============================================
DROP TABLE IF EXISTS `sentiment_resonance`;
CREATE TABLE `sentiment_resonance` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '共鸣关系ID',
  `user_a_id` BIGINT NOT NULL COMMENT '用户A的ID',
  `user_b_id` BIGINT NOT NULL COMMENT '用户B的ID',
  `resonance_score` DECIMAL(5,4) NOT NULL COMMENT '共鸣分数（0到1，基于余弦相似度）',
  `interaction_count` INT NOT NULL DEFAULT 0 COMMENT '互动次数（评论、点赞等）',
  `sentiment_similarity` DECIMAL(5,4) DEFAULT NULL COMMENT '情感相似度（基于历史情感向量）',
  `avg_sentiment_diff` DECIMAL(5,4) DEFAULT NULL COMMENT '平均情感差异（绝对值）',
  `common_emotion_label` VARCHAR(20) DEFAULT NULL COMMENT '共同主导情感标签',
  `community_id` INT DEFAULT NULL COMMENT '所属情感社区ID（通过Louvain算法计算）',
  `calculation_date` DATE NOT NULL COMMENT '计算日期',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_pair_date` (`user_a_id`, `user_b_id`, `calculation_date`),
  KEY `idx_user_a` (`user_a_id`),
  KEY `idx_user_b` (`user_b_id`),
  KEY `idx_resonance_score` (`resonance_score` DESC),
  KEY `idx_community_id` (`community_id`),
  KEY `idx_calculation_date` (`calculation_date`),
  CONSTRAINT `fk_resonance_user_a` FOREIGN KEY (`user_a_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_resonance_user_b` FOREIGN KEY (`user_b_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_user_pair` CHECK (`user_a_id` < `user_b_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情感共鸣关系表';

-- ============================================
-- 4. 创建索引以优化查询性能
-- ============================================

-- 情感传播查询优化
CREATE INDEX `idx_propagation_composite` ON `sentiment_propagation` (`post_id`, `depth_level`, `created_at`);

-- 影响力排行查询优化
CREATE INDEX `idx_influence_ranking` ON `user_influence_score` (`calculation_date`, `influence_score` DESC);

-- 共鸣网络查询优化
CREATE INDEX `idx_resonance_network` ON `sentiment_resonance` (`calculation_date`, `resonance_score` DESC);

-- ============================================
-- 5. 初始化说明
-- ============================================
-- 本迁移脚本创建了情感传播分析所需的三个核心表：
-- 1. sentiment_propagation: 实时追踪每条评论的情感传播特征
-- 2. user_influence_score: 定期计算（建议每日）用户的影响力指标
-- 3. sentiment_resonance: 定期计算（建议每周）用户间的情感共鸣关系
--
-- 后续需要实现的定时任务：
-- - 每次评论创建时：更新 sentiment_propagation 表
-- - 每日凌晨：计算 user_influence_score
-- - 每周日凌晨：计算 sentiment_resonance 和社区发现
