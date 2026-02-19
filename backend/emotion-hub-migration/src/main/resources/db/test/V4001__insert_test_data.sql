-- ============================================
-- 插入测试数据
-- ============================================

-- 插入管理员账号（密码: admin123，需要BCrypt加密）
-- BCrypt加密后的密码示例（实际使用时需要通过程序生成）
INSERT INTO `user` (`username`, `password`, `email`, `nickname`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@emotionhub.com', '系统管理员', 'ADMIN', 'ACTIVE');

-- 插入测试用户（密码: user123）
INSERT INTO `user` (`username`, `password`, `email`, `nickname`, `role`, `status`) VALUES
('testuser1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'user1@test.com', '测试用户1', 'USER', 'ACTIVE'),
('testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'user2@test.com', '测试用户2', 'USER', 'ACTIVE');

-- 插入测试帖子
INSERT INTO `post` (`user_id`, `content`, `emotion_score`, `emotion_label`, `status`) VALUES
(2, '今天天气真好，心情也很愉快！准备去公园散步。', 0.85, 'POSITIVE', 'PUBLISHED'),
(2, '工作压力好大，感觉有点累...', -0.45, 'NEGATIVE', 'PUBLISHED'),
(3, '刚看完一部电影，剧情一般般。', 0.10, 'NEUTRAL', 'PUBLISHED');

-- 插入测试评论
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `emotion_score`, `emotion_label`) VALUES
(1, 3, '是啊，今天确实很适合出去玩！', 0.75, 'POSITIVE'),
(2, 3, '加油！休息一下就好了。', 0.60, 'POSITIVE');