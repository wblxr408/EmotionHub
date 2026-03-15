package com.seu.emotionhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seu.emotionhub.dao.mapper.*;
import com.seu.emotionhub.model.dto.response.UserInfluenceVO;
import com.seu.emotionhub.model.entity.*;
import com.seu.emotionhub.service.UserInfluenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户情感影响力服务实现类
 *
 * 核心算法：
 * 1. PageRank 变体：基于用户互动关系图计算综合影响力
 * 2. 正面/负面影响力：分析用户帖子引发的评论情感倾向
 * 3. 争议性评分：评论区情感分化程度（标准差）
 * 4. 互动深度：评论树的平均深度
 * 5. 情感改变率：评论情感与帖子情感不一致的比例
 *
 * @author EmotionHub Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfluenceServiceImpl implements UserInfluenceService {

    private final UserInfluenceScoreMapper influenceMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final LikeRecordMapper likeRecordMapper;

    /** PageRank 阻尼系数 */
    private static final double DAMPING_FACTOR = 0.85;

    /** PageRank 最大迭代次数 */
    private static final int MAX_ITERATIONS = 20;

    /** PageRank 收敛阈值 */
    private static final double CONVERGENCE_THRESHOLD = 0.0001;

    /** 统计天数（最近30天的数据） */
    private static final int STATS_DAYS = 30;

    // ==================== 公共接口实现 ====================

    @Override
    public UserInfluenceVO getLatestInfluence(Long userId) {
        UserInfluenceScore score = influenceMapper.selectLatestByUserId(userId);
        if (score == null) {
            return null;
        }
        return buildInfluenceVO(score);
    }

    @Override
    public UserInfluenceVO getInfluenceByDate(Long userId, LocalDate date) {
        UserInfluenceScore score = influenceMapper.selectByUserIdAndDate(userId, date);
        if (score == null) {
            return null;
        }
        return buildInfluenceVO(score);
    }

    @Override
    public List<UserInfluenceVO> getTopInfluential(Integer limit) {
        if (limit == null || limit <= 0) limit = 20;
        LocalDate latestDate = getLatestCalculationDate();
        if (latestDate == null) {
            return Collections.emptyList();
        }

        List<UserInfluenceScore> scores = influenceMapper.selectTopInfluential(latestDate, limit);
        return buildRankedList(scores);
    }

    @Override
    public List<UserInfluenceVO> getTopPositiveInfluence(Integer limit) {
        if (limit == null || limit <= 0) limit = 20;
        LocalDate latestDate = getLatestCalculationDate();
        if (latestDate == null) {
            return Collections.emptyList();
        }

        List<UserInfluenceScore> scores = influenceMapper.selectTopPositiveInfluence(latestDate, limit);
        return buildRankedList(scores);
    }

    @Override
    public List<UserInfluenceVO> getTopControversial(Integer limit) {
        if (limit == null || limit <= 0) limit = 20;
        LocalDate latestDate = getLatestCalculationDate();
        if (latestDate == null) {
            return Collections.emptyList();
        }

        List<UserInfluenceScore> scores = influenceMapper.selectTopControversial(latestDate, limit);
        return buildRankedList(scores);
    }

    @Override
    public List<UserInfluenceVO> getInfluenceTrend(Long userId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        List<UserInfluenceScore> scores = influenceMapper.selectHistoryByUserId(userId, startDate, endDate);
        return scores.stream()
                .map(this::buildInfluenceVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int calculateAllUserInfluence() {
        log.info("开始全量用户影响力计算...");
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(STATS_DAYS);

        // 1. 获取所有活跃用户（最近30天有发帖的用户）
        List<Long> activeUserIds = getActiveUserIds(startDate, today);
        log.info("活跃用户数量: {}", activeUserIds.size());

        if (activeUserIds.isEmpty()) {
            log.info("没有活跃用户，跳过计算");
            return 0;
        }

        // 2. 删除今天已有的旧记录
        LambdaQueryWrapper<UserInfluenceScore> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(UserInfluenceScore::getCalculationDate, today);
        influenceMapper.delete(deleteWrapper);

        // 3. 计算每个用户的影响力
        int successCount = 0;
        for (Long userId : activeUserIds) {
            try {
                UserInfluenceScore score = calculateUserInfluence(userId);
                score.setCalculationDate(today);
                influenceMapper.insert(score);
                successCount++;
            } catch (Exception e) {
                log.error("计算用户影响力失败: userId={}", userId, e);
            }
        }

        log.info("用户影响力计算完成，成功 {}/{} 个用户", successCount, activeUserIds.size());
        return successCount;
    }

    @Override
    public UserInfluenceScore calculateUserInfluence(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(STATS_DAYS);

        UserInfluenceScore score = new UserInfluenceScore();
        score.setUserId(userId);

        // 1. 统计基础数据
        Map<String, Object> basicStats = calculateBasicStats(userId, startDate, today);
        score.setPostCount((Integer) basicStats.get("postCount"));
        score.setCommentCount((Integer) basicStats.get("commentCount"));

        // 如果没有帖子，返回默认值
        if (score.getPostCount() == 0) {
            score.setInfluenceScore(BigDecimal.ZERO);
            score.setPositiveImpact(BigDecimal.ZERO);
            score.setNegativeImpact(BigDecimal.ZERO);
            score.setControversialScore(BigDecimal.ZERO);
            score.setAvgEngagementDepth(BigDecimal.ZERO);
            score.setSentimentChangeRate(BigDecimal.ZERO);
            return score;
        }

        // 2. 计算综合影响力（PageRank 变体）
        double influenceScore = calculatePageRankScore(userId, startDate, today);
        score.setInfluenceScore(toBD(influenceScore * 100)); // 归一化到 0-100

        // 3. 计算正面/负面影响力
        Map<String, Double> sentimentImpact = calculateSentimentImpact(userId, startDate, today);
        score.setPositiveImpact(toBD(sentimentImpact.get("positiveImpact") * 100));
        score.setNegativeImpact(toBD(sentimentImpact.get("negativeImpact") * 100));

        // 4. 计算争议性分数
        double controversialScore = calculateControversialScore(userId, startDate, today);
        score.setControversialScore(toBD(controversialScore * 100));

        // 5. 计算平均互动深度
        double avgDepth = calculateAvgEngagementDepth(userId, startDate, today);
        score.setAvgEngagementDepth(toBD(avgDepth));

        // 6. 计算情感改变率
        double changeRate = calculateSentimentChangeRate(userId, startDate, today);
        score.setSentimentChangeRate(toBD(changeRate));

        return score;
    }

    // ==================== 定时任务 ====================

    /**
     * 每日凌晨 3:00 自动计算所有用户影响力
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduledInfluenceCalculation() {
        log.info("定时任务：开始每日用户影响力计算...");
        try {
            int count = calculateAllUserInfluence();
            log.info("定时任务：用户影响力计算完成，成功计算 {} 个用户", count);
        } catch (Exception e) {
            log.error("定时任务：用户影响力计算失败", e);
        }
    }

    // ==================== 核心算法 ====================

    /**
     * PageRank 变体算法计算综合影响力
     * 基于用户互动图：用户A评论用户B的帖子 → A向B投票
     */
    private double calculatePageRankScore(Long userId, LocalDate startDate, LocalDate endDate) {
        // 获取所有活跃用户
        List<Long> allUsers = getActiveUserIds(startDate, endDate);
        if (allUsers.size() < 2) {
            return 0.0;
        }

        // 构建互动图：用户 -> 被评论的用户 -> 评论次数
        Map<Long, Map<Long, Integer>> graph = buildInteractionGraph(allUsers, startDate, endDate);

        // 初始化 PageRank 值
        Map<Long, Double> pageRank = new HashMap<>();
        double initValue = 1.0 / allUsers.size();
        for (Long uid : allUsers) {
            pageRank.put(uid, initValue);
        }

        // 迭代计算 PageRank
        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            Map<Long, Double> newPageRank = new HashMap<>();
            double maxDiff = 0.0;

            for (Long uid : allUsers) {
                double rank = (1.0 - DAMPING_FACTOR) / allUsers.size();

                // 累加所有指向当前用户的投票
                for (Map.Entry<Long, Map<Long, Integer>> entry : graph.entrySet()) {
                    Long fromUser = entry.getKey();
                    Map<Long, Integer> toUsers = entry.getValue();

                    if (toUsers.containsKey(uid)) {
                        int outDegree = toUsers.values().stream().mapToInt(Integer::intValue).sum();
                        if (outDegree > 0) {
                            rank += DAMPING_FACTOR * pageRank.get(fromUser) * toUsers.get(uid) / outDegree;
                        }
                    }
                }

                newPageRank.put(uid, rank);
                maxDiff = Math.max(maxDiff, Math.abs(rank - pageRank.get(uid)));
            }

            pageRank = newPageRank;

            // 检查收敛
            if (maxDiff < CONVERGENCE_THRESHOLD) {
                log.debug("PageRank 收敛于第 {} 次迭代", iter + 1);
                break;
            }
        }

        return pageRank.getOrDefault(userId, 0.0);
    }

    /**
     * 计算正面/负面影响力
     * 分析用户帖子引发的评论情感倾向
     */
    private Map<String, Double> calculateSentimentImpact(Long userId, LocalDate startDate, LocalDate endDate) {
        // 获取用户的所有帖子
        LambdaQueryWrapper<Post> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.eq(Post::getUserId, userId)
                .ge(Post::getCreatedAt, startDate.atStartOfDay())
                .lt(Post::getCreatedAt, endDate.plusDays(1).atStartOfDay())
                .eq(Post::getDeleted, false);
        List<Post> posts = postMapper.selectList(postWrapper);

        if (posts.isEmpty()) {
            return Map.of("positiveImpact", 0.0, "negativeImpact", 0.0);
        }

        List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());

        // 获取所有评论
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.in(Comment::getPostId, postIds)
                .eq(Comment::getDeleted, false);
        List<Comment> comments = commentMapper.selectList(commentWrapper);

        if (comments.isEmpty()) {
            return Map.of("positiveImpact", 0.0, "negativeImpact", 0.0);
        }

        // 统计正面/负面评论数量
        long positiveCount = comments.stream()
                .filter(c -> "POSITIVE".equals(c.getEmotionLabel()))
                .count();
        long negativeCount = comments.stream()
                .filter(c -> "NEGATIVE".equals(c.getEmotionLabel()))
                .count();

        double total = comments.size();
        double positiveImpact = positiveCount / total;
        double negativeImpact = negativeCount / total;

        return Map.of("positiveImpact", positiveImpact, "negativeImpact", negativeImpact);
    }

    /**
     * 计算争议性分数
     * 评论区情感分化程度（情感分数的标准差）
     */
    private double calculateControversialScore(Long userId, LocalDate startDate, LocalDate endDate) {
        // 获取用户的所有帖子
        LambdaQueryWrapper<Post> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.eq(Post::getUserId, userId)
                .ge(Post::getCreatedAt, startDate.atStartOfDay())
                .lt(Post::getCreatedAt, endDate.plusDays(1).atStartOfDay())
                .eq(Post::getDeleted, false);
        List<Post> posts = postMapper.selectList(postWrapper);

        if (posts.isEmpty()) {
            return 0.0;
        }

        List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());

        // 获取所有评论
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.in(Comment::getPostId, postIds)
                .eq(Comment::getDeleted, false)
                .isNotNull(Comment::getEmotionScore);
        List<Comment> comments = commentMapper.selectList(commentWrapper);

        if (comments.size() < 2) {
            return 0.0;
        }

        // 计算情感分数的标准差
        List<Double> scores = comments.stream()
                .map(c -> c.getEmotionScore().doubleValue())
                .collect(Collectors.toList());

        double mean = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = scores.stream()
                .mapToDouble(s -> Math.pow(s - mean, 2))
                .average()
                .orElse(0.0);
        double stdDev = Math.sqrt(variance);

        // 标准差归一化到 [0, 1]（情感分数范围是 [-1, 1]，最大标准差约为1）
        return Math.min(stdDev, 1.0);
    }

    /**
     * 计算平均互动深度
     * 评论树的平均深度
     */
    private double calculateAvgEngagementDepth(Long userId, LocalDate startDate, LocalDate endDate) {
        // 获取用户的所有帖子
        LambdaQueryWrapper<Post> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.eq(Post::getUserId, userId)
                .ge(Post::getCreatedAt, startDate.atStartOfDay())
                .lt(Post::getCreatedAt, endDate.plusDays(1).atStartOfDay())
                .eq(Post::getDeleted, false);
        List<Post> posts = postMapper.selectList(postWrapper);

        if (posts.isEmpty()) {
            return 0.0;
        }

        List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());

        // 获取所有评论
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.in(Comment::getPostId, postIds)
                .eq(Comment::getDeleted, false);
        List<Comment> comments = commentMapper.selectList(commentWrapper);

        if (comments.isEmpty()) {
            return 0.0;
        }

        // 计算每条评论的深度
        Map<Long, Integer> depthMap = new HashMap<>();
        for (Comment comment : comments) {
            int depth = calculateCommentDepth(comment, comments, depthMap);
            depthMap.put(comment.getId(), depth);
        }

        // 计算平均深度
        return depthMap.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    /**
     * 计算评论的深度（递归）
     */
    private int calculateCommentDepth(Comment comment, List<Comment> allComments, Map<Long, Integer> cache) {
        if (cache.containsKey(comment.getId())) {
            return cache.get(comment.getId());
        }

        if (comment.getParentId() == null || comment.getParentId() == 0) {
            return 1;
        }

        Comment parent = allComments.stream()
                .filter(c -> c.getId().equals(comment.getParentId()))
                .findFirst()
                .orElse(null);

        if (parent == null) {
            return 1;
        }

        return 1 + calculateCommentDepth(parent, allComments, cache);
    }

    /**
     * 计算情感改变率
     * 评论情感与帖子情感不一致的比例
     */
    private double calculateSentimentChangeRate(Long userId, LocalDate startDate, LocalDate endDate) {
        // 获取用户的所有帖子
        LambdaQueryWrapper<Post> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.eq(Post::getUserId, userId)
                .ge(Post::getCreatedAt, startDate.atStartOfDay())
                .lt(Post::getCreatedAt, endDate.plusDays(1).atStartOfDay())
                .eq(Post::getDeleted, false)
                .isNotNull(Post::getEmotionLabel);
        List<Post> posts = postMapper.selectList(postWrapper);

        if (posts.isEmpty()) {
            return 0.0;
        }

        // 统计情感不一致的评论数
        int totalComments = 0;
        int changedComments = 0;

        for (Post post : posts) {
            LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
            commentWrapper.eq(Comment::getPostId, post.getId())
                    .eq(Comment::getDeleted, false)
                    .isNotNull(Comment::getEmotionLabel);
            List<Comment> comments = commentMapper.selectList(commentWrapper);

            for (Comment comment : comments) {
                totalComments++;
                if (!post.getEmotionLabel().equals(comment.getEmotionLabel())) {
                    changedComments++;
                }
            }
        }

        if (totalComments == 0) {
            return 0.0;
        }

        return (double) changedComments / totalComments;
    }

    // ==================== 辅助方法 ====================

    /**
     * 构建用户互动图
     * 图结构：fromUser -> toUser -> 评论次数
     */
    private Map<Long, Map<Long, Integer>> buildInteractionGraph(List<Long> users, LocalDate startDate, LocalDate endDate) {
        Map<Long, Map<Long, Integer>> graph = new HashMap<>();

        // 获取所有用户的帖子
        LambdaQueryWrapper<Post> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.in(Post::getUserId, users)
                .ge(Post::getCreatedAt, startDate.atStartOfDay())
                .lt(Post::getCreatedAt, endDate.plusDays(1).atStartOfDay())
                .eq(Post::getDeleted, false);
        List<Post> posts = postMapper.selectList(postWrapper);

        if (posts.isEmpty()) {
            return graph;
        }

        // 帖子ID -> 用户ID 映射
        Map<Long, Long> postUserMap = posts.stream()
                .collect(Collectors.toMap(Post::getId, Post::getUserId));

        List<Long> postIds = new ArrayList<>(postUserMap.keySet());

        // 获取所有评论
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.in(Comment::getPostId, postIds)
                .eq(Comment::getDeleted, false);
        List<Comment> comments = commentMapper.selectList(commentWrapper);

        // 构建图
        for (Comment comment : comments) {
            Long fromUser = comment.getUserId();
            Long toUser = postUserMap.get(comment.getPostId());

            if (fromUser != null && toUser != null && !fromUser.equals(toUser)) {
                graph.computeIfAbsent(fromUser, k -> new HashMap<>())
                        .merge(toUser, 1, Integer::sum);
            }
        }

        return graph;
    }

    /**
     * 获取活跃用户ID列表（最近有发帖的用户）
     */
    private List<Long> getActiveUserIds(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Post::getCreatedAt, startDate.atStartOfDay())
                .lt(Post::getCreatedAt, endDate.plusDays(1).atStartOfDay())
                .eq(Post::getDeleted, false)
                .select(Post::getUserId);

        return postMapper.selectList(wrapper).stream()
                .map(Post::getUserId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 计算基础统计数据
     */
    private Map<String, Object> calculateBasicStats(Long userId, LocalDate startDate, LocalDate endDate) {
        // 统计帖子数
        LambdaQueryWrapper<Post> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.eq(Post::getUserId, userId)
                .ge(Post::getCreatedAt, startDate.atStartOfDay())
                .lt(Post::getCreatedAt, endDate.plusDays(1).atStartOfDay())
                .eq(Post::getDeleted, false);
        int postCount = Math.toIntExact(postMapper.selectCount(postWrapper));

        // 统计获得的评论数
        List<Long> postIds = postMapper.selectList(postWrapper).stream()
                .map(Post::getId)
                .collect(Collectors.toList());

        int commentCount = 0;
        if (!postIds.isEmpty()) {
            LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
            commentWrapper.in(Comment::getPostId, postIds)
                    .eq(Comment::getDeleted, false);
            commentCount = Math.toIntExact(commentMapper.selectCount(commentWrapper));
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("postCount", postCount);
        stats.put("commentCount", commentCount);
        return stats;
    }

    /**
     * 获取最新的计算日期
     */
    private LocalDate getLatestCalculationDate() {
        LambdaQueryWrapper<UserInfluenceScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(UserInfluenceScore::getCalculationDate)
                .last("LIMIT 1");
        UserInfluenceScore latest = influenceMapper.selectOne(wrapper);
        return latest != null ? latest.getCalculationDate() : null;
    }

    /**
     * 构建影响力 VO
     */
    private UserInfluenceVO buildInfluenceVO(UserInfluenceScore score) {
        UserInfluenceVO vo = new UserInfluenceVO();
        vo.setUserId(score.getUserId());
        vo.setInfluenceScore(score.getInfluenceScore());
        vo.setPositiveImpact(score.getPositiveImpact());
        vo.setNegativeImpact(score.getNegativeImpact());
        vo.setControversialScore(score.getControversialScore());
        vo.setPostCount(score.getPostCount());
        vo.setCommentCount(score.getCommentCount());
        vo.setAvgEngagementDepth(score.getAvgEngagementDepth());
        vo.setSentimentChangeRate(score.getSentimentChangeRate());
        vo.setCalculationDate(score.getCalculationDate());

        // 查询用户信息
        User user = userMapper.selectById(score.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }

        // 设置影响力等级
        setInfluenceLevel(vo);

        return vo;
    }

    /**
     * 构建排行榜列表（带排名）
     */
    private List<UserInfluenceVO> buildRankedList(List<UserInfluenceScore> scores) {
        List<UserInfluenceVO> result = new ArrayList<>();
        int rank = 1;
        for (UserInfluenceScore score : scores) {
            UserInfluenceVO vo = buildInfluenceVO(score);
            vo.setRank(rank++);
            result.add(vo);
        }
        return result;
    }

    /**
     * 设置影响力等级
     */
    private void setInfluenceLevel(UserInfluenceVO vo) {
        double score = vo.getInfluenceScore() != null ? vo.getInfluenceScore().doubleValue() : 0;

        if (score >= 80) {
            vo.setInfluenceLevel("LEGENDARY");
            vo.setInfluenceLevelDesc("传奇影响者");
        } else if (score >= 60) {
            vo.setInfluenceLevel("EXPERT");
            vo.setInfluenceLevelDesc("专家影响者");
        } else if (score >= 40) {
            vo.setInfluenceLevel("ADVANCED");
            vo.setInfluenceLevelDesc("进阶影响者");
        } else if (score >= 20) {
            vo.setInfluenceLevel("INTERMEDIATE");
            vo.setInfluenceLevelDesc("中级影响者");
        } else {
            vo.setInfluenceLevel("NOVICE");
            vo.setInfluenceLevelDesc("新手影响者");
        }
    }

    /**
     * 转换为 BigDecimal（保留4位小数）
     */
    private BigDecimal toBD(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);
    }
}
