-- API密钥配置表
-- 用于存储用户自主填入的LLM API密钥，支持多平台配置
-- user_id = NULL 表示平台级默认配置；user_id > 0 表示用户个人配置
CREATE TABLE `api_key_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID，NULL表示平台级配置',
  `provider` VARCHAR(30) NOT NULL COMMENT 'LLM提供商: qianwen/openai/wenxin/zhipu',
  `api_key` VARCHAR(500) NOT NULL COMMENT 'API密钥',
  `api_url` VARCHAR(500) DEFAULT NULL COMMENT '自定义API地址（可选）',
  `model` VARCHAR(100) DEFAULT NULL COMMENT '使用的模型名称',
  `is_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用: 0-禁用 1-启用',
  `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认: 0-否 1-是',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_provider` (`user_id`, `provider`),
  KEY `idx_user_enabled` (`user_id`, `is_enabled`),
  KEY `idx_provider_enabled` (`provider`, `is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM API密钥配置表';

-- 插入平台默认通义千问API Key（user_id=NULL 表示平台级配置，优先级最低）
-- 业务优先级：用户个人配置(user_id>0) > 平台默认配置(user_id=NULL) > 配置文件
INSERT INTO `api_key_config` (`user_id`, `provider`, `api_key`, `is_enabled`, `is_default`, `created_at`, `updated_at`)
VALUES (NULL, 'qianwen', 'sk-4c76aa29e3e44fa6aff8454546ba9925', 1, 1, NOW(), NOW());
