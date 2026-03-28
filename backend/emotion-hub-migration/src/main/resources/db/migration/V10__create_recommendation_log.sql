-- 推荐日志表 (recommendation_log)
-- 用于 A/B 测试指标采集：CTR、停留时长、策略对比
CREATE TABLE `recommendation_log` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id`      BIGINT       NOT NULL                COMMENT '用户ID',
  `post_id`      BIGINT       NOT NULL                COMMENT '推荐的帖子ID',
  `strategy`     VARCHAR(50)  NOT NULL                COMMENT '推荐策略: emotional_adaptive / traditional',
  `emotion_state` VARCHAR(30) DEFAULT NULL            COMMENT '推荐时用户情感状态',
  `score`        DOUBLE       DEFAULT NULL            COMMENT '排序分数',
  `position`     INT          DEFAULT NULL            COMMENT '在 Feed 中的位置（从1开始）',
  `impressed_at` DATETIME     NOT NULL                COMMENT '曝光时间',
  `clicked`      TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '是否点击: 0-未点击, 1-已点击',
  `clicked_at`   DATETIME     DEFAULT NULL            COMMENT '点击时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id`      (`user_id`),
  KEY `idx_post_id`      (`post_id`),
  KEY `idx_strategy`     (`strategy`),
  KEY `idx_impressed_at` (`impressed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐日志表（A/B测试）';
