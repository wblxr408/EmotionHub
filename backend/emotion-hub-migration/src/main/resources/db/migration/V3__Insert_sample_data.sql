-- =============================================
-- EmotionHub 示例数据
-- =============================================

-- 插入测试用户
INSERT INTO `user` (`username`, `nickname`, `email`, `password`, `avatar`, `bio`, `created_at`, `updated_at`) VALUES
('alice_chen', 'Alice Chen', 'alice@example.com', '$2a$10$Zp2AGn7fWqj8NwBi9bBT4OWGWr5wIUQYrz/wHcj35/a4uBwLyOZHy', NULL, 'Passionate about sharing emotions and connecting with others through meaningful conversations.', NOW(), NOW()),
('bob_wang', 'Bob Wang', 'bob@example.com', '$2a$10$Zp2AGn7fWqj8NwBi9bBT4OWGWr5wIUQYrz/wHcj35/a4uBwLyOZHy', NULL, 'Tech enthusiast, coffee lover, and amateur philosopher. Always curious about human emotions.', NOW(), NOW()),
('carol_liu', 'Carol Liu', 'carol@example.com', '$2a$10$Zp2AGn7fWqj8NwBi9bBT4OWGWr5wIUQYrz/wHcj35/a4uBwLyOZHy', NULL, 'Writer and dreamer. Exploring the depths of human feelings through words.', NOW(), NOW()),
('david_zhang', 'David Zhang', 'david@example.com', '$2a$10$Zp2AGn7fWqj8NwBi9bBT4OWGWr5wIUQYrz/wHcj35/a4uBwLyOZHy', NULL, 'Data scientist by day, emotional explorer by night. Fascinated by the intersection of AI and human psychology.', NOW(), NOW()),
('emma_li', 'Emma Li', 'emma@example.com', '$2a$10$Zp2AGn7fWqj8NwBi9bBT4OWGWr5wIUQYrz/wHcj35/a4uBwLyOZHy', NULL, 'Artist and mindfulness practitioner. Painting emotions with words and colors.', NOW(), NOW());

-- 注意：所有用户的密码都是 "password123"（已使用BCrypt加密）

-- 插入示例帖子
INSERT INTO `post` (`user_id`, `content`, `images`, `emotion_label`, `emotion_score`, `status`, `view_count`, `like_count`, `comment_count`, `created_at`, `updated_at`) VALUES
(1, 'Just finished a wonderful book today! It made me reflect on so many things about life and relationships. Sometimes the best moments are the quiet ones spent with a good story.', '[]', 'POSITIVE', 0.85, 'PUBLISHED', 127, 23, 8, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

(2, 'Had a challenging day at work. Debugging that critical issue took hours, but finally solved it. The satisfaction of fixing complex problems never gets old!', '[]', 'POSITIVE', 0.72, 'PUBLISHED', 89, 15, 5, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

(3, 'Rainy Sunday afternoon. Perfect weather for introspection and tea. Writing down thoughts and watching the world slow down through the window.', '[]', 'NEUTRAL', 0.45, 'PUBLISHED', 156, 31, 12, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR)),

(1, 'Feeling overwhelmed by deadlines lately. Need to remind myself to take breaks and not let stress consume everything. Self-care is not selfish.', '[]', 'NEGATIVE', -0.63, 'PUBLISHED', 203, 42, 18, DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR)),

(4, 'Discovered an amazing new café today! The atmosphere was perfect for coding and the barista actually remembered my order. Small joys matter.', '[]', 'POSITIVE', 0.78, 'PUBLISHED', 94, 19, 6, DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 6 HOUR)),

(5, 'Just completed my first oil painting in months! The creative process was therapeutic. Art really does heal the soul in ways words cannot express.', '[]', 'POSITIVE', 0.91, 'PUBLISHED', 178, 38, 14, DATE_SUB(NOW(), INTERVAL 8 HOUR), DATE_SUB(NOW(), INTERVAL 8 HOUR)),

(2, 'Sometimes I wonder if we are too connected digitally and not enough in person. Missing the days of long conversations without phone distractions.', '[]', 'NEUTRAL', 0.12, 'PUBLISHED', 142, 27, 9, DATE_SUB(NOW(), INTERVAL 10 HOUR), DATE_SUB(NOW(), INTERVAL 10 HOUR)),

(3, 'Anxiety has been high this week. But I am learning to sit with uncomfortable feelings instead of running from them. Growth is not always comfortable.', '[]', 'NEGATIVE', -0.48, 'PUBLISHED', 167, 35, 11, DATE_SUB(NOW(), INTERVAL 12 HOUR), DATE_SUB(NOW(), INTERVAL 12 HOUR)),

(4, 'Finally took that leap and signed up for the public speaking course. Terrified but excited! Stepping out of comfort zones is where the magic happens.', '[]', 'POSITIVE', 0.68, 'PUBLISHED', 112, 24, 7, DATE_SUB(NOW(), INTERVAL 14 HOUR), DATE_SUB(NOW(), INTERVAL 14 HOUR)),

(5, 'Meditation practice today was profound. In the silence, I found answers I did not even know I was looking for. The mind is both chaos and sanctuary.', '[]', 'POSITIVE', 0.82, 'PUBLISHED', 198, 41, 16, DATE_SUB(NOW(), INTERVAL 16 HOUR), DATE_SUB(NOW(), INTERVAL 16 HOUR)),

(1, 'Received unexpected criticism at work today. It stung, but trying to see it as an opportunity for growth rather than a personal attack. Easier said than done.', '[]', 'NEGATIVE', -0.55, 'PUBLISHED', 134, 28, 13, DATE_SUB(NOW(), INTERVAL 18 HOUR), DATE_SUB(NOW(), INTERVAL 18 HOUR)),

(2, 'Nothing special today, just existing. Sometimes that is enough. We do not always need to be productive or extraordinary.', '[]', 'NEUTRAL', 0.28, 'PUBLISHED', 176, 33, 10, DATE_SUB(NOW(), INTERVAL 20 HOUR), DATE_SUB(NOW(), INTERVAL 20 HOUR)),

(3, 'Had a deep conversation with an old friend tonight. Reminded me why genuine connections are worth maintaining even when life gets busy.', '[]', 'POSITIVE', 0.87, 'PUBLISHED', 145, 29, 8, DATE_SUB(NOW(), INTERVAL 22 HOUR), DATE_SUB(NOW(), INTERVAL 22 HOUR)),

(4, 'The imposter syndrome is real today. Questioning everything I have accomplished. Need to remember that everyone struggles with self-doubt sometimes.', '[]', 'NEGATIVE', -0.71, 'PUBLISHED', 189, 39, 15, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

(5, 'Watching the sunset paint the sky in impossible colors. Nature is the greatest artist. Feeling grateful for moments of pure beauty.', '[]', 'POSITIVE', 0.94, 'PUBLISHED', 221, 47, 19, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 插入示例评论
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `parent_id`, `like_count`, `created_at`, `updated_at`) VALUES
-- 第1个帖子的评论
(1, 2, 'What book was it? Always looking for good recommendations!', NULL, 5, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 1, 'It was "The Midnight Library" by Matt Haig. Highly recommend!', 1, 3, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 3, 'Love that book! Changed my perspective on life choices.', NULL, 7, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- 第4个帖子的评论（压力相关）
(4, 2, 'Totally understand this feeling. Remember to breathe and take it one step at a time.', NULL, 8, DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(4, 5, 'Self-care is definitely not selfish! You got this!', NULL, 6, DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(4, 3, 'Have you tried the Pomodoro technique? Really helps with managing overwhelming workloads.', NULL, 4, DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 4 HOUR)),

-- 第6个帖子的评论（绘画相关）
(6, 1, 'Would love to see your painting! Art is such a wonderful outlet.', NULL, 9, DATE_SUB(NOW(), INTERVAL 8 HOUR), DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(6, 4, 'This is inspiring! Maybe I should pick up my old hobby too.', NULL, 5, DATE_SUB(NOW(), INTERVAL 7 HOUR), DATE_SUB(NOW(), INTERVAL 7 HOUR)),

-- 第8个帖子的评论（焦虑相关）
(8, 2, 'Sitting with discomfort is one of the hardest but most valuable skills. Proud of you!', NULL, 11, DATE_SUB(NOW(), INTERVAL 12 HOUR), DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(8, 5, 'You are stronger than you think. Keep going!', NULL, 7, DATE_SUB(NOW(), INTERVAL 11 HOUR), DATE_SUB(NOW(), INTERVAL 11 HOUR)),

-- 第15个帖子的评论（日落相关）
(15, 1, 'Beautiful sentiment. Nature always knows how to remind us what matters.', NULL, 12, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(15, 3, 'Sunsets never get old. Each one is unique and fleeting.', NULL, 8, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 插入示例点赞记录
INSERT INTO `like_record` (`user_id`, `target_id`, `target_type`, `created_at`) VALUES
-- Alice点赞
(1, 6, 'POST', DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(1, 10, 'POST', DATE_SUB(NOW(), INTERVAL 16 HOUR)),
(1, 15, 'POST', DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- Bob点赞
(2, 1, 'POST', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 3, 'POST', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(2, 13, 'POST', DATE_SUB(NOW(), INTERVAL 22 HOUR)),

-- Carol点赞
(3, 4, 'POST', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(3, 8, 'POST', DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(3, 15, 'POST', DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- David点赞
(4, 3, 'POST', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(4, 5, 'POST', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(4, 10, 'POST', DATE_SUB(NOW(), INTERVAL 16 HOUR)),

-- Emma点赞
(5, 1, 'POST', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 4, 'POST', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(5, 13, 'POST', DATE_SUB(NOW(), INTERVAL 22 HOUR));

-- 插入示例通知
INSERT INTO `notification` (`user_id`, `type`, `title`, `content`, `related_id`, `is_read`, `created_at`) VALUES
(1, 'LIKE', 'New Like', 'Bob Wang liked your post', 4, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(1, 'COMMENT', 'New Comment', 'Emma Li commented on your post', 6, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(1, 'LIKE', 'New Like', 'Carol Liu liked your post', 11, 1, DATE_SUB(NOW(), INTERVAL 5 HOUR)),

(2, 'COMMENT', 'New Comment', 'Alice Chen replied to your comment', 1, 0, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(2, 'LIKE', 'New Like', 'David Zhang liked your post', 7, 1, DATE_SUB(NOW(), INTERVAL 8 HOUR)),

(3, 'LIKE', 'New Like', 'Alice Chen liked your post', 3, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(3, 'COMMENT', 'New Comment', 'Bob Wang commented on your post', 8, 0, DATE_SUB(NOW(), INTERVAL 4 HOUR)),

(5, 'LIKE', 'New Like', 'Alice Chen liked your post', 10, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(5, 'COMMENT', 'New Comment', 'David Zhang commented on your post', 6, 1, DATE_SUB(NOW(), INTERVAL 6 HOUR));

