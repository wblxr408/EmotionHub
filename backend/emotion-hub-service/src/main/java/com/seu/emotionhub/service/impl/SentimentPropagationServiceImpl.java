package com.seu.emotionhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seu.emotionhub.dao.mapper.*;
import com.seu.emotionhub.model.entity.*;
import com.seu.emotionhub.model.dto.response.SentimentPropagationVO;
import com.seu.emotionhub.model.dto.response.SentimentTimelineVO;
import com.seu.emotionhub.service.SentimentPropagationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 情感传播分析服务实现类
 *
 * @author EmotionHub Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentPropagationServiceImpl implements SentimentPropagationService {

    private final SentimentPropagationMapper propagationMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    public SentimentPropagationVO getPropagationAnalysis(Long postId) {
        // 1. 获取帖子信息
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 2. 获取传播统计数据
        Map<String, Object> stats = propagationMapper.selectPropagationStats(postId);

        // 3. 获取所有传播记录
        List<SentimentPropagation> propagations = propagationMapper.selectByPostId(postId);

        // 4. 获取情感转折点
        List<SentimentPropagation> shifts = propagationMapper.selectSentimentShifts(postId);

        // 5. 构建响应VO
        SentimentPropagationVO vo = new SentimentPropagationVO();
        vo.setPostId(postId);
        vo.setPostSentimentScore(post.getEmotionScore());
        vo.setPostEmotionLabel(post.getEmotionLabel());

        // 统计数据
        vo.setTotalComments(stats.get("total_comments") != null ?
                ((Number) stats.get("total_comments")).intValue() : 0);
        vo.setAvgConsistency(convertToBigDecimal(stats.get("avg_consistency")));
        vo.setAvgAmplification(convertToBigDecimal(stats.get("avg_amplification")));
        vo.setShiftCount(stats.get("shift_count") != null ?
                ((Number) stats.get("shift_count")).intValue() : 0);
        vo.setMaxDepth(stats.get("max_depth") != null ?
                ((Number) stats.get("max_depth")).intValue() : 0);

        // 判断传播类型
        vo.setPropagationType(determinePropagationType(vo.getAvgConsistency(),
                vo.getAvgAmplification(), vo.getShiftCount(), vo.getTotalComments()));

        // 6. 转换传播节点
        vo.setPropagationNodes(convertToPropagationNodes(propagations));
        vo.setShiftNodes(convertToPropagationNodes(shifts));

        return vo;
    }

    @Override
    public SentimentTimelineVO getSentimentTimeline(Long postId) {
        // 1. 获取帖子信息
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 2. 获取时间线数据
        List<Map<String, Object>> timelineData = propagationMapper.selectTimelineData(postId);

        // 3. 获取所有传播记录用于统计
        List<SentimentPropagation> propagations = propagationMapper.selectByPostId(postId);

        // 4. 构建响应VO
        SentimentTimelineVO vo = new SentimentTimelineVO();
        vo.setPostId(postId);
        vo.setPostCreatedAt(post.getCreatedAt());
        vo.setInitialSentiment(post.getEmotionScore());

        // 计算当前平均情感
        if (!propagations.isEmpty()) {
            BigDecimal avgSentiment = propagations.stream()
                    .map(SentimentPropagation::getCommentSentimentScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(propagations.size()), 4, RoundingMode.HALF_UP);
            vo.setCurrentAvgSentiment(avgSentiment);
        } else {
            vo.setCurrentAvgSentiment(post.getEmotionScore());
        }

        // 计算情感趋势和波动率
        calculateTrendAndVolatility(vo, propagations, post.getEmotionScore());

        // 5. 转换时间线数据点
        vo.setTimelinePoints(convertToTimelinePoints(timelineData, propagations));

        // 6. 计算按层级的情感统计
        vo.setDepthSentiments(calculateDepthSentiments(propagations));

        return vo;
    }

    @Override
    @Transactional
    public void recordCommentPropagation(Long postId, Long commentId) {
        try {
            // 1. 获取帖子和评论信息
            Post post = postMapper.selectById(postId);
            Comment comment = commentMapper.selectById(commentId);

            if (post == null || comment == null) {
                log.warn("帖子或评论不存在: postId={}, commentId={}", postId, commentId);
                return;
            }

            // 如果评论没有情感分数，暂时跳过
            if (comment.getEmotionScore() == null) {
                log.warn("评论情感分数未计算: commentId={}", commentId);
                return;
            }

            // 2. 计算评论层级
            Integer depthLevel = calculateDepthLevel(comment);

            // 3. 计算情感一致性
            BigDecimal consistency = calculateConsistency(
                    post.getEmotionScore(),
                    comment.getEmotionScore()
            );

            // 4. 计算情感放大系数
            BigDecimal amplification = calculateAmplification(
                    post.getEmotionScore(),
                    comment.getEmotionScore()
            );

            // 5. 判断是否发生情感转折
            boolean isSentimentShift = checkSentimentShift(
                    post.getEmotionScore(),
                    comment.getEmotionScore()
            );

            String shiftDirection = null;
            if (isSentimentShift) {
                shiftDirection = determineShiftDirection(
                        post.getEmotionScore(),
                        comment.getEmotionScore()
                );
            }

            // 6. 创建传播记录
            SentimentPropagation propagation = new SentimentPropagation();
            propagation.setPostId(postId);
            propagation.setCommentId(commentId);
            propagation.setUserId(comment.getUserId());
            propagation.setParentCommentId(comment.getParentId());
            propagation.setDepthLevel(depthLevel);
            propagation.setPostSentimentScore(post.getEmotionScore());
            propagation.setCommentSentimentScore(comment.getEmotionScore());
            propagation.setSentimentConsistency(consistency);
            propagation.setSentimentAmplification(amplification);
            propagation.setIsSentimentShift(isSentimentShift);
            propagation.setShiftDirection(shiftDirection);
            propagation.setCreatedAt(LocalDateTime.now());

            // 7. 保存记录
            propagationMapper.insert(propagation);

            log.info("情感传播记录已创建: postId={}, commentId={}, consistency={}, amplification={}",
                    postId, commentId, consistency, amplification);

        } catch (Exception e) {
            log.error("记录情感传播失败: postId={}, commentId={}", postId, commentId, e);
            throw new RuntimeException("记录情感传播失败", e);
        }
    }

    @Override
    @Transactional
    public void recalculatePropagation(Long postId) {
        // 1. 删除现有的传播记录
        LambdaQueryWrapper<SentimentPropagation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SentimentPropagation::getPostId, postId);
        propagationMapper.delete(wrapper);

        // 2. 获取该帖子的所有评论
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getPostId, postId);
        commentWrapper.orderByAsc(Comment::getCreatedAt);
        List<Comment> comments = commentMapper.selectList(commentWrapper);

        // 3. 重新记录每条评论的传播
        for (Comment comment : comments) {
            if (comment.getEmotionScore() != null) {
                recordCommentPropagation(postId, comment.getId());
            }
        }

        log.info("帖子情感传播数据已重新计算: postId={}, commentCount={}", postId, comments.size());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 计算评论层级
     */
    private Integer calculateDepthLevel(Comment comment) {
        if (comment.getParentId() == null) {
            return 1; // 一级评论
        }

        // 递归计算父评论的层级
        Comment parent = commentMapper.selectById(comment.getParentId());
        if (parent == null) {
            return 1;
        }

        return calculateDepthLevel(parent) + 1;
    }

    /**
     * 计算情感一致性
     * 公式: 1 - |postScore - commentScore| / 2
     * 返回值范围: -1 到 1，1表示完全一致
     */
    private BigDecimal calculateConsistency(BigDecimal postScore, BigDecimal commentScore) {
        BigDecimal diff = postScore.subtract(commentScore).abs();
        BigDecimal consistency = BigDecimal.ONE.subtract(
                diff.divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP)
        );
        return consistency;
    }

    /**
     * 计算情感放大系数
     * 公式: commentScore / postScore (当postScore != 0)
     */
    private BigDecimal calculateAmplification(BigDecimal postScore, BigDecimal commentScore) {
        if (postScore.compareTo(BigDecimal.ZERO) == 0) {
            // 如果原帖情感为0（中性），使用绝对值作为放大系数
            return commentScore.abs();
        }

        try {
            return commentScore.divide(postScore, 4, RoundingMode.HALF_UP);
        } catch (ArithmeticException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 检查是否发生情感转折
     * 转折定义：正面转负面或负面转正面
     */
    private boolean checkSentimentShift(BigDecimal postScore, BigDecimal commentScore) {
        return (postScore.compareTo(BigDecimal.ZERO) > 0 && commentScore.compareTo(BigDecimal.ZERO) < 0) ||
               (postScore.compareTo(BigDecimal.ZERO) < 0 && commentScore.compareTo(BigDecimal.ZERO) > 0);
    }

    /**
     * 确定转折方向
     */
    private String determineShiftDirection(BigDecimal postScore, BigDecimal commentScore) {
        if (postScore.compareTo(BigDecimal.ZERO) > 0 && commentScore.compareTo(BigDecimal.ZERO) < 0) {
            return "POSITIVE_TO_NEGATIVE";
        } else if (postScore.compareTo(BigDecimal.ZERO) < 0 && commentScore.compareTo(BigDecimal.ZERO) > 0) {
            return "NEGATIVE_TO_POSITIVE";
        }
        return null;
    }

    /**
     * 判断传播类型
     */
    private String determinePropagationType(BigDecimal avgConsistency, BigDecimal avgAmplification,
                                           Integer shiftCount, Integer totalComments) {
        if (totalComments == 0) {
            return "NO_PROPAGATION";
        }

        // 高一致性 (> 0.7)
        if (avgConsistency != null && avgConsistency.compareTo(BigDecimal.valueOf(0.7)) > 0) {
            // 检查是否有放大效果
            if (avgAmplification != null && avgAmplification.abs().compareTo(BigDecimal.ONE) > 0) {
                return "AMPLIFIED";
            }
            return "CONSISTENT";
        }

        // 低一致性且有较多转折点
        if (shiftCount != null && shiftCount > totalComments * 0.3) {
            return "CONTROVERSIAL";
        }

        // 一致性中等偏低，可能是衰减传播
        if (avgAmplification != null && avgAmplification.abs().compareTo(BigDecimal.ONE) < 0) {
            return "DAMPENED";
        }

        return "MIXED";
    }

    /**
     * 转换为传播节点VO列表
     */
    private List<SentimentPropagationVO.PropagationNodeVO> convertToPropagationNodes(
            List<SentimentPropagation> propagations) {

        if (propagations == null || propagations.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询评论和用户信息
        List<Long> commentIds = propagations.stream()
                .map(SentimentPropagation::getCommentId)
                .collect(Collectors.toList());

        Map<Long, Comment> commentMap = commentMapper.selectBatchIds(commentIds)
                .stream()
                .collect(Collectors.toMap(Comment::getId, c -> c));

        List<Long> userIds = propagations.stream()
                .map(SentimentPropagation::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userMapper.selectBatchIds(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return propagations.stream().map(p -> {
            SentimentPropagationVO.PropagationNodeVO node = new SentimentPropagationVO.PropagationNodeVO();

            Comment comment = commentMap.get(p.getCommentId());
            User user = userMap.get(p.getUserId());

            node.setCommentId(p.getCommentId());
            node.setUserId(p.getUserId());
            node.setParentCommentId(p.getParentCommentId());
            node.setDepthLevel(p.getDepthLevel());
            node.setCommentSentimentScore(p.getCommentSentimentScore());
            node.setSentimentConsistency(p.getSentimentConsistency());
            node.setSentimentAmplification(p.getSentimentAmplification());
            node.setIsSentimentShift(p.getIsSentimentShift());
            node.setShiftDirection(p.getShiftDirection());
            node.setCreatedAt(p.getCreatedAt());

            if (comment != null) {
                node.setContent(comment.getContent());
                node.setCommentEmotionLabel(comment.getEmotionLabel());
            }

            if (user != null) {
                node.setUserNickname(user.getNickname());
                node.setUserAvatar(user.getAvatar());
            }

            return node;
        }).collect(Collectors.toList());
    }

    /**
     * 计算情感趋势和波动率
     */
    private void calculateTrendAndVolatility(SentimentTimelineVO vo,
                                            List<SentimentPropagation> propagations,
                                            BigDecimal initialSentiment) {
        if (propagations.isEmpty()) {
            vo.setSentimentTrend("STABLE");
            vo.setVolatility(BigDecimal.ZERO);
            return;
        }

        // 计算标准差（波动率）
        List<BigDecimal> scores = propagations.stream()
                .map(SentimentPropagation::getCommentSentimentScore)
                .collect(Collectors.toList());

        BigDecimal mean = scores.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(scores.size()), 4, RoundingMode.HALF_UP);

        BigDecimal variance = scores.stream()
                .map(score -> score.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(scores.size()), 4, RoundingMode.HALF_UP);

        BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()))
                .setScale(4, RoundingMode.HALF_UP);

        vo.setVolatility(stdDev);

        // 判断趋势：比较前半段和后半段的平均值
        int midPoint = scores.size() / 2;
        BigDecimal firstHalfAvg = scores.subList(0, midPoint).stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(midPoint), 4, RoundingMode.HALF_UP);

        BigDecimal secondHalfAvg = scores.subList(midPoint, scores.size()).stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(scores.size() - midPoint), 4, RoundingMode.HALF_UP);

        BigDecimal trendDiff = secondHalfAvg.subtract(firstHalfAvg);

        if (stdDev.compareTo(BigDecimal.valueOf(0.3)) > 0) {
            vo.setSentimentTrend("VOLATILE");
        } else if (trendDiff.compareTo(BigDecimal.valueOf(0.1)) > 0) {
            vo.setSentimentTrend("RISING");
        } else if (trendDiff.compareTo(BigDecimal.valueOf(-0.1)) < 0) {
            vo.setSentimentTrend("FALLING");
        } else {
            vo.setSentimentTrend("STABLE");
        }
    }

    /**
     * 转换时间线数据点
     */
    private List<SentimentTimelineVO.TimelinePointVO> convertToTimelinePoints(
            List<Map<String, Object>> timelineData,
            List<SentimentPropagation> propagations) {

        List<SentimentTimelineVO.TimelinePointVO> points = new ArrayList<>();
        BigDecimal cumulativeSum = BigDecimal.ZERO;
        int count = 0;

        // 创建转折点集合以便快速查找
        Set<Long> shiftCommentIds = propagations.stream()
                .filter(SentimentPropagation::getIsSentimentShift)
                .map(SentimentPropagation::getCommentId)
                .collect(Collectors.toSet());

        for (Map<String, Object> data : timelineData) {
            SentimentTimelineVO.TimelinePointVO point = new SentimentTimelineVO.TimelinePointVO();

            point.setTimestamp((LocalDateTime) data.get("created_at"));
            point.setSentimentScore(convertToBigDecimal(data.get("comment_sentiment_score")));
            point.setDepthLevel(data.get("depth_level") != null ?
                    ((Number) data.get("depth_level")).intValue() : 1);

            // 计算累计平均
            cumulativeSum = cumulativeSum.add(point.getSentimentScore());
            count++;
            point.setCumulativeAvgSentiment(
                    cumulativeSum.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP)
            );

            // 检查是否为转折点
            Long commentId = data.get("comment_id") != null ?
                    ((Number) data.get("comment_id")).longValue() : null;
            point.setIsShiftPoint(commentId != null && shiftCommentIds.contains(commentId));

            points.add(point);
        }

        return points;
    }

    /**
     * 计算按层级的情感统计
     */
    private List<SentimentTimelineVO.DepthSentimentVO> calculateDepthSentiments(
            List<SentimentPropagation> propagations) {

        Map<Integer, List<SentimentPropagation>> depthGroups = propagations.stream()
                .collect(Collectors.groupingBy(SentimentPropagation::getDepthLevel));

        return depthGroups.entrySet().stream()
                .map(entry -> {
                    Integer depth = entry.getKey();
                    List<SentimentPropagation> group = entry.getValue();

                    SentimentTimelineVO.DepthSentimentVO depthVO = new SentimentTimelineVO.DepthSentimentVO();
                    depthVO.setDepthLevel(depth);
                    depthVO.setCommentCount(group.size());

                    // 计算平均情感
                    BigDecimal avgSentiment = group.stream()
                            .map(SentimentPropagation::getCommentSentimentScore)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(group.size()), 4, RoundingMode.HALF_UP);
                    depthVO.setAvgSentiment(avgSentiment);

                    // 计算标准差
                    BigDecimal variance = group.stream()
                            .map(p -> p.getCommentSentimentScore().subtract(avgSentiment).pow(2))
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(group.size()), 4, RoundingMode.HALF_UP);

                    BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()))
                            .setScale(4, RoundingMode.HALF_UP);
                    depthVO.setStdDeviation(stdDev);

                    // 确定主导情感标签
                    Map<String, Long> labelCounts = group.stream()
                            .collect(Collectors.groupingBy(
                                    p -> {
                                        BigDecimal score = p.getCommentSentimentScore();
                                        if (score.compareTo(BigDecimal.valueOf(0.3)) > 0) return "POSITIVE";
                                        else if (score.compareTo(BigDecimal.valueOf(-0.3)) < 0) return "NEGATIVE";
                                        else return "NEUTRAL";
                                    },
                                    Collectors.counting()
                            ));

                    String dominantLabel = labelCounts.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("NEUTRAL");
                    depthVO.setDominantLabel(dominantLabel);

                    return depthVO;
                })
                .sorted(Comparator.comparing(SentimentTimelineVO.DepthSentimentVO::getDepthLevel))
                .collect(Collectors.toList());
    }

    /**
     * 转换为BigDecimal
     */
    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.ZERO;
    }
}
