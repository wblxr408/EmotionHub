-- 为推荐日志表补充曝光上下文特征列
-- 在曝光时记录用户情感快照，避免训练时 JOIN 带来的时间偏差

ALTER TABLE `recommendation_log`
    ADD COLUMN `user_avg_score`  DOUBLE       DEFAULT NULL COMMENT '曝光时用户24h平均情感分（来自2.1滑动窗口）',
    ADD COLUMN `user_volatility` DOUBLE       DEFAULT NULL COMMENT '曝光时用户情感波动性（来自2.1）',
    ADD COLUMN `trend_type`      VARCHAR(20)  DEFAULT NULL COMMENT '曝光时情感趋势: RISING/FALLING/STABLE（来自2.1）',
    ADD COLUMN `author_influence` DOUBLE      DEFAULT NULL COMMENT '作者归一化影响力分（来自2.3）';
