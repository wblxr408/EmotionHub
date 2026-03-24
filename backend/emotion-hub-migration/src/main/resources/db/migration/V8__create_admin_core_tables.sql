CREATE TABLE `report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `reporter_id` BIGINT NOT NULL COMMENT '举报人ID',
  `target_type` VARCHAR(20) NOT NULL COMMENT '举报目标类型: POST/COMMENT',
  `target_id` BIGINT NOT NULL COMMENT '举报目标ID',
  `reason` VARCHAR(200) NOT NULL COMMENT '举报原因',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '举报状态: PENDING/PROCESSED/REJECTED',
  `handler_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
  `action` VARCHAR(50) DEFAULT NULL COMMENT '联动处理动作',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
  `handled_at` DATETIME DEFAULT NULL COMMENT '处理时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_report_status` (`status`),
  KEY `idx_report_target` (`target_type`, `target_id`),
  KEY `idx_report_reporter` (`reporter_id`),
  KEY `idx_report_handler` (`handler_id`),
  CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_report_handler` FOREIGN KEY (`handler_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

CREATE TABLE `admin_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `operator_id` BIGINT NOT NULL COMMENT '管理员ID',
  `action` VARCHAR(50) NOT NULL COMMENT '操作动作',
  `target_type` VARCHAR(20) NOT NULL COMMENT '目标类型',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `before_state` VARCHAR(500) DEFAULT NULL COMMENT '操作前状态摘要',
  `after_state` VARCHAR(500) DEFAULT NULL COMMENT '操作后状态摘要',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_log_operator` (`operator_id`),
  KEY `idx_admin_log_action` (`action`),
  KEY `idx_admin_log_created_at` (`created_at`),
  CONSTRAINT `fk_admin_log_operator` FOREIGN KEY (`operator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表';

INSERT INTO `user` (`username`, `nickname`, `email`, `password`, `bio`, `role`, `status`, `created_at`, `updated_at`)
SELECT
  'admin_ops',
  'Admin Ops',
  'admin@example.com',
  '$2a$10$Zp2AGn7fWqj8NwBi9bBT4OWGWr5wIUQYrz/wHcj35/a4uBwLyOZHy',
  'System administrator account for EmotionHub moderation and operations.',
  'ADMIN',
  'ACTIVE',
  NOW(),
  NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `user` WHERE `username` = 'admin_ops'
);

INSERT INTO `report` (`reporter_id`, `target_type`, `target_id`, `reason`, `status`, `created_at`, `updated_at`)
SELECT
  2,
  'POST',
  4,
  '内容带有明显攻击倾向，希望管理员关注',
  'PENDING',
  NOW(),
  NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `report` WHERE `reporter_id` = 2 AND `target_type` = 'POST' AND `target_id` = 4
);
