-- V6: 给 comment 表添加 deleted 标记列
-- 在评论被软删除时标记为 1，未删除为 0。默认 0 保证旧数据兼容。

ALTER TABLE `comment`
  ADD COLUMN `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已删除: 0-未删除, 1-已删除';

-- 可选：为现有索引或查询优化创建索引（如果经常按 deleted 查询）
-- CREATE INDEX IF NOT EXISTS `idx_comment_deleted` ON `comment` (`deleted`);
