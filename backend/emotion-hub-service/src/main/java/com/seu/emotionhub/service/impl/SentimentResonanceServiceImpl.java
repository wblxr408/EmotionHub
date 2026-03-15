package com.seu.emotionhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seu.emotionhub.dao.mapper.SentimentResonanceMapper;
import com.seu.emotionhub.dao.mapper.UserEmotionStatsMapper;
import com.seu.emotionhub.dao.mapper.UserMapper;
import com.seu.emotionhub.model.dto.response.SentimentCommunityVO;
import com.seu.emotionhub.model.dto.response.SentimentNetworkVO;
import com.seu.emotionhub.model.entity.SentimentResonance;
import com.seu.emotionhub.model.entity.User;
import com.seu.emotionhub.service.SentimentResonanceService;
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
 * 情感共鸣网络服务实现类
 *
 * 核心算法：
 * 1. 情感特征向量：[avgScore, positiveRatio, neutralRatio, negativeRatio]
 * 2. 余弦相似度：dot(v1,v2) / (|v1| * |v2|)，归一化到 [0,1]
 * 3. Louvain 社区发现：贪心迭代，将用户分配到使社区内聚度最大的社区
 *
 * @author EmotionHub Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentResonanceServiceImpl implements SentimentResonanceService {

    private final SentimentResonanceMapper resonanceMapper;
    private final UserEmotionStatsMapper statsMapper;
    private final UserMapper userMapper;

    /** 建立共鸣边的最低分数阈值 */
    static final double RESONANCE_THRESHOLD = 0.3;

    /** Louvain 算法最大迭代次数，防止死循环 */
    static final int MAX_LOUVAIN_ITERATIONS = 30;

    /** BFS 深度上限（防止图过大） */
    private static final int MAX_DEPTH = 3;

    /** 每个用户在网络图中最多取的邻居数 */
    private static final int MAX_NEIGHBORS_PER_NODE = 50;

    // ==================== 公共接口实现 ====================

    @Override
    public SentimentNetworkVO getResonanceNetwork(Long userId, Integer depth) {
        if (depth == null || depth < 1) depth = 2;
        if (depth > MAX_DEPTH) depth = MAX_DEPTH;

        // 获取最新计算日期，如果没有数据则先触发计算
        LocalDate latestDate = getLatestCalculationDate();
        if (latestDate == null) {
            log.info("情感共鸣数据不存在，触发首次计算...");
            recalculateResonance();
            latestDate = LocalDate.now();
        }

        // BFS 扩展网络
        Set<Long> visitedUsers = new LinkedHashSet<>();
        Set<String> addedEdgeKeys = new HashSet<>();
        List<SentimentNetworkVO.NetworkEdgeVO> edges = new ArrayList<>();

        // 队列存储 (userId, currentDepth)
        Queue<long[]> queue = new LinkedList<>();
        queue.add(new long[]{userId, 0});
        visitedUsers.add(userId);

        final LocalDate finalLatestDate = latestDate;
        while (!queue.isEmpty()) {
            long[] item = queue.poll();
            Long currentUserId = item[0];
            int currentDepth = (int) item[1];

            if (currentDepth >= depth) continue;

            List<SentimentResonance> neighbors = resonanceMapper.selectResonanceNetwork(
                    currentUserId, finalLatestDate, RESONANCE_THRESHOLD, MAX_NEIGHBORS_PER_NODE);

            for (SentimentResonance r : neighbors) {
                Long neighborId = r.getUserAId().equals(currentUserId) ? r.getUserBId() : r.getUserAId();

                // 去重边：始终以 minId-maxId 作为 key
                String edgeKey = Math.min(currentUserId, neighborId) + "-" + Math.max(currentUserId, neighborId);
                if (!addedEdgeKeys.contains(edgeKey)) {
                    addedEdgeKeys.add(edgeKey);
                    edges.add(buildEdgeVO(r));
                }

                if (!visitedUsers.contains(neighborId)) {
                    visitedUsers.add(neighborId);
                    queue.add(new long[]{neighborId, currentDepth + 1});
                }
            }
        }

        // 构建节点列表
        List<SentimentNetworkVO.NetworkNodeVO> nodes = buildNodeList(visitedUsers, userId, finalLatestDate);

        SentimentNetworkVO vo = new SentimentNetworkVO();
        vo.setCenterUserId(userId);
        vo.setDepth(depth);
        vo.setNodes(nodes);
        vo.setEdges(edges);
        vo.setStats(buildNetworkStats(nodes, edges, userId));
        return vo;
    }

    @Override
    public List<SentimentCommunityVO> getSentimentCommunities() {
        LocalDate latestDate = getLatestCalculationDate();
        if (latestDate == null) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> communityStats = resonanceMapper.selectCommunityStats(latestDate);
        List<SentimentCommunityVO> result = new ArrayList<>();

        for (Map<String, Object> stat : communityStats) {
            Integer communityId = toInt(stat.get("community_id"));
            if (communityId == null) continue;

            // 获取该社区的所有关系记录，提取成员 ID
            List<SentimentResonance> relations = resonanceMapper.selectByCommunityId(communityId, latestDate);
            Set<Long> memberIds = new LinkedHashSet<>();
            for (SentimentResonance r : relations) {
                memberIds.add(r.getUserAId());
                memberIds.add(r.getUserBId());
            }

            // 构建成员详情
            List<SentimentCommunityVO.CommunityMemberVO> members = buildMemberList(memberIds);

            double avgEmotionScore = members.stream()
                    .filter(m -> m.getAvgEmotionScore() != null)
                    .mapToDouble(SentimentCommunityVO.CommunityMemberVO::getAvgEmotionScore)
                    .average()
                    .orElse(0.0);

            String dominantEmotion = (String) stat.get("common_emotion_label");
            String communityType = determineCommunityType(avgEmotionScore, dominantEmotion);

            SentimentCommunityVO communityVO = new SentimentCommunityVO();
            communityVO.setCommunityId(communityId);
            communityVO.setCommunityType(communityType);
            communityVO.setCommunityLabel(getCommunityLabel(communityType));
            communityVO.setMemberCount(memberIds.size());
            communityVO.setAvgResonanceScore(toDouble(stat.get("avg_resonance")));
            communityVO.setDominantEmotion(dominantEmotion);
            communityVO.setAvgEmotionScore(round4(avgEmotionScore));
            communityVO.setMembers(members);
            result.add(communityVO);
        }

        return result;
    }

    @Override
    @Transactional
    public int recalculateResonance() {
        log.info("开始全量情感共鸣计算...");
        LocalDate today = LocalDate.now();

        // 1. 获取所有活跃用户
        List<Long> activeUserIds = statsMapper.selectActiveUserIds();
        log.info("活跃用户数量: {}", activeUserIds.size());

        if (activeUserIds.size() < 2) {
            log.info("活跃用户数量不足 2，跳过计算");
            return 0;
        }

        // 2. 提取每个用户的情感特征向量
        Map<Long, double[]> featureVectors = new HashMap<>();
        for (Long uid : activeUserIds) {
            double[] vector = extractFeatureVector(uid);
            if (vector != null) {
                featureVectors.put(uid, vector);
            }
        }
        List<Long> validUsers = new ArrayList<>(featureVectors.keySet());
        log.info("有效情感特征用户数量: {}", validUsers.size());

        if (validUsers.size() < 2) {
            return 0;
        }

        // 3. 计算所有用户对的共鸣分数（O(n²)）
        List<SentimentResonance> resonancePairs = new ArrayList<>();
        for (int i = 0; i < validUsers.size(); i++) {
            for (int j = i + 1; j < validUsers.size(); j++) {
                Long uidA = validUsers.get(i);
                Long uidB = validUsers.get(j);
                // 保证 user_a_id < user_b_id
                Long userA = Math.min(uidA, uidB);
                Long userB = Math.max(uidA, uidB);

                double[] vecA = featureVectors.get(userA);
                double[] vecB = featureVectors.get(userB);

                double cosSim = calculateCosineSimilarity(vecA, vecB);
                // 余弦相似度 [-1,1] → 归一化到 [0,1]
                double resonanceScore = (cosSim + 1.0) / 2.0;

                if (resonanceScore >= RESONANCE_THRESHOLD) {
                    SentimentResonance r = new SentimentResonance();
                    r.setUserAId(userA);
                    r.setUserBId(userB);
                    r.setResonanceScore(toBD(resonanceScore));
                    r.setSentimentSimilarity(toBD(resonanceScore));
                    r.setAvgSentimentDiff(toBD(Math.abs(featureVectors.get(userA)[0]
                            - featureVectors.get(userB)[0])));
                    r.setInteractionCount(0);
                    r.setCommonEmotionLabel(resolveCommonEmotionLabel(
                            featureVectors.get(userA)[0], featureVectors.get(userB)[0]));
                    r.setCalculationDate(today);
                    resonancePairs.add(r);
                }
            }
        }
        log.info("共鸣对数量（阈值 >= {}）: {}", RESONANCE_THRESHOLD, resonancePairs.size());

        // 4. Louvain 社区发现
        Map<Long, Integer> communityMap = detectCommunities(resonancePairs, validUsers);

        // 5. 将社区 ID 写回 resonancePairs（同一社区的边才写入 communityId）
        for (SentimentResonance r : resonancePairs) {
            Integer commA = communityMap.get(r.getUserAId());
            Integer commB = communityMap.get(r.getUserBId());
            if (commA != null && commA.equals(commB)) {
                r.setCommunityId(commA);
            }
        }

        // 6. 删除今天已有的旧记录，重新插入
        LambdaQueryWrapper<SentimentResonance> del = new LambdaQueryWrapper<>();
        del.eq(SentimentResonance::getCalculationDate, today);
        resonanceMapper.delete(del);

        for (SentimentResonance r : resonancePairs) {
            resonanceMapper.insert(r);
        }

        log.info("情感共鸣计算完成，写入 {} 对共鸣关系，发现 {} 个社区",
                resonancePairs.size(), new HashSet<>(communityMap.values()).size());
        return resonancePairs.size();
    }

    @Override
    public List<SentimentNetworkVO.NetworkNodeVO> getEmotionalPartners(Long userId, Integer limit) {
        if (limit == null || limit <= 0) limit = 10;

        LocalDate latestDate = getLatestCalculationDate();
        if (latestDate == null) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> partners = resonanceMapper.selectEmotionalPartners(userId, latestDate, limit);
        if (partners.isEmpty()) {
            return Collections.emptyList();
        }

        // 提取伙伴 ID
        List<Long> partnerIds = partners.stream()
                .map(p -> toLong(p.get("partner_id")))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 批量查询用户信息
        Map<Long, User> userMap = userMapper.selectBatchIds(partnerIds)
                .stream().collect(Collectors.toMap(User::getId, u -> u));

        // 批量查询情感特征
        Map<Long, Map<String, Object>> featureMap = new HashMap<>();
        for (Long pid : partnerIds) {
            Map<String, Object> feature = statsMapper.selectUserEmotionFeature(pid);
            if (feature != null) featureMap.put(pid, feature);
        }

        // 查询所属社区
        Map<Long, Integer> communityMap = new HashMap<>();
        for (Long pid : partnerIds) {
            Integer cid = resonanceMapper.selectCommunityIdByUser(pid, latestDate);
            if (cid != null) communityMap.put(pid, cid);
        }

        return partners.stream().map(p -> {
            Long partnerId = toLong(p.get("partner_id"));
            if (partnerId == null) return null;

            SentimentNetworkVO.NetworkNodeVO node = new SentimentNetworkVO.NetworkNodeVO();
            node.setUserId(partnerId);
            node.setIsCenterUser(false);

            User user = userMap.get(partnerId);
            if (user != null) {
                node.setNickname(user.getNickname());
                node.setAvatar(user.getAvatar());
            }

            Map<String, Object> feature = featureMap.get(partnerId);
            if (feature != null) {
                double avgScore = toDouble(feature.get("avg_score"));
                int totalPosts = toInt(feature.get("total_posts")) == null ? 0 : toInt(feature.get("total_posts"));
                node.setAvgEmotionScore(round4(avgScore));
                node.setDominantEmotion(scoreToDominantEmotion(avgScore));
                node.setPostCount(totalPosts);
                node.setNodeSize(calcNodeSize(totalPosts));
            }

            node.setCommunityId(communityMap.get(partnerId));
            if (node.getCommunityId() != null) {
                node.setCommunityType(determineCommunityType(
                        node.getAvgEmotionScore() != null ? node.getAvgEmotionScore() : 0.0,
                        node.getDominantEmotion()));
            }
            return node;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    // ==================== 定时任务 ====================

    /**
     * 每周日凌晨 2:00 自动全量重算情感共鸣
     */
    @Scheduled(cron = "0 0 2 ? * SUN")
    public void scheduledResonanceCalculation() {
        log.info("定时任务：开始每周情感共鸣计算...");
        try {
            int count = recalculateResonance();
            log.info("定时任务：情感共鸣计算完成，共创建 {} 对共鸣关系", count);
        } catch (Exception e) {
            log.error("定时任务：情感共鸣计算失败", e);
        }
    }

    // ==================== 核心算法 ====================

    /**
     * 从 user_emotion_stats 提取用户情感特征向量
     * 向量维度：[avgScore, positiveRatio, neutralRatio, negativeRatio]
     *
     * @param userId 用户ID
     * @return 4维特征向量，若无数据则返回 null
     */
    double[] extractFeatureVector(Long userId) {
        Map<String, Object> stats = statsMapper.selectUserEmotionFeature(userId);
        if (stats == null || stats.isEmpty()) return null;

        Number totalPostsNum = (Number) stats.get("total_posts");
        if (totalPostsNum == null || totalPostsNum.intValue() == 0) return null;

        double total = totalPostsNum.doubleValue();
        double avgScore = toDouble(stats.get("avg_score"));
        double pos = stats.get("total_positive") != null
                ? ((Number) stats.get("total_positive")).doubleValue() / total : 0.0;
        double neu = stats.get("total_neutral") != null
                ? ((Number) stats.get("total_neutral")).doubleValue() / total : 0.0;
        double neg = stats.get("total_negative") != null
                ? ((Number) stats.get("total_negative")).doubleValue() / total : 0.0;

        return new double[]{avgScore, pos, neu, neg};
    }

    /**
     * 计算两个向量的余弦相似度
     * cos(θ) = dot(v1, v2) / (|v1| * |v2|)
     * 返回值范围：[-1, 1]；若任意向量为零向量则返回 0
     */
    double calculateCosineSimilarity(double[] v1, double[] v2) {
        double dot = 0, norm1 = 0, norm2 = 0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        if (norm1 == 0 || norm2 == 0) return 0;
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Louvain 社区发现算法（贪心变体）
     * 迭代地将每个节点分配到使社区内边权最大的社区，直到稳定
     *
     * @param resonances  共鸣边列表
     * @param users       所有用户ID
     * @return 用户 → 社区ID 的映射（社区ID 从 0 开始连续递增）
     */
    Map<Long, Integer> detectCommunities(List<SentimentResonance> resonances, List<Long> users) {
        // 初始化：每个用户自成一个社区
        Map<Long, Integer> community = new HashMap<>();
        for (int i = 0; i < users.size(); i++) {
            community.put(users.get(i), i);
        }

        if (resonances.isEmpty()) {
            return normalizeCommunityIds(community);
        }

        // 构建加权邻接表
        Map<Long, Map<Long, Double>> adjacency = new HashMap<>();
        for (SentimentResonance r : resonances) {
            double score = r.getResonanceScore().doubleValue();
            adjacency.computeIfAbsent(r.getUserAId(), k -> new HashMap<>())
                    .merge(r.getUserBId(), score, Double::sum);
            adjacency.computeIfAbsent(r.getUserBId(), k -> new HashMap<>())
                    .merge(r.getUserAId(), score, Double::sum);
        }

        // 贪心迭代：将节点移入邻居权重最大的社区
        boolean changed = true;
        int iterations = 0;
        while (changed && iterations < MAX_LOUVAIN_ITERATIONS) {
            changed = false;
            iterations++;

            for (Long uid : users) {
                int currentComm = community.get(uid);

                // 统计当前节点到每个相邻社区的总权重
                Map<Integer, Double> communityWeight = new HashMap<>();
                Map<Long, Double> neighbors = adjacency.getOrDefault(uid, Collections.emptyMap());
                for (Map.Entry<Long, Double> entry : neighbors.entrySet()) {
                    int neighborComm = community.get(entry.getKey());
                    communityWeight.merge(neighborComm, entry.getValue(), Double::sum);
                }

                if (communityWeight.isEmpty()) continue;

                // 找到总权重最高的社区
                int bestComm = currentComm;
                double bestWeight = communityWeight.getOrDefault(currentComm, 0.0);
                for (Map.Entry<Integer, Double> e : communityWeight.entrySet()) {
                    if (e.getValue() > bestWeight) {
                        bestWeight = e.getValue();
                        bestComm = e.getKey();
                    }
                }

                if (bestComm != currentComm) {
                    community.put(uid, bestComm);
                    changed = true;
                }
            }
        }

        log.debug("Louvain 算法完成，迭代 {} 次，初始社区数 {} → 最终 {}",
                iterations, users.size(), new HashSet<>(community.values()).size());

        return normalizeCommunityIds(community);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将社区 ID 重新映射为 1 开始的连续整数
     */
    private Map<Long, Integer> normalizeCommunityIds(Map<Long, Integer> community) {
        Map<Integer, Integer> idMap = new HashMap<>();
        int nextId = 1;
        Map<Long, Integer> normalized = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : community.entrySet()) {
            int old = entry.getValue();
            if (!idMap.containsKey(old)) {
                idMap.put(old, nextId++);
            }
            normalized.put(entry.getKey(), idMap.get(old));
        }
        return normalized;
    }

    /**
     * 查询最新的共鸣计算日期
     */
    private LocalDate getLatestCalculationDate() {
        LambdaQueryWrapper<SentimentResonance> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SentimentResonance::getCalculationDate).last("LIMIT 1");
        SentimentResonance latest = resonanceMapper.selectOne(wrapper);
        return latest != null ? latest.getCalculationDate() : null;
    }

    /**
     * 根据用户 ID 集合批量构建网络节点 VO
     */
    private List<SentimentNetworkVO.NetworkNodeVO> buildNodeList(
            Set<Long> userIds, Long centerUserId, LocalDate date) {

        if (userIds.isEmpty()) return Collections.emptyList();

        // 批量查询用户基础信息
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds)
                .stream().collect(Collectors.toMap(User::getId, u -> u));

        // 逐一查询情感特征（可批量优化）
        Map<Long, Map<String, Object>> featureMap = new HashMap<>();
        for (Long uid : userIds) {
            Map<String, Object> f = statsMapper.selectUserEmotionFeature(uid);
            if (f != null) featureMap.put(uid, f);
        }

        // 查询每个用户的所属社区
        Map<Long, Integer> communityMap = new HashMap<>();
        for (Long uid : userIds) {
            Integer cid = resonanceMapper.selectCommunityIdByUser(uid, date);
            if (cid != null) communityMap.put(uid, cid);
        }

        return userIds.stream().map(uid -> {
            SentimentNetworkVO.NetworkNodeVO node = new SentimentNetworkVO.NetworkNodeVO();
            node.setUserId(uid);
            node.setIsCenterUser(uid.equals(centerUserId));

            User user = userMap.get(uid);
            if (user != null) {
                node.setNickname(user.getNickname());
                node.setAvatar(user.getAvatar());
            }

            Map<String, Object> feature = featureMap.get(uid);
            if (feature != null) {
                double avgScore = toDouble(feature.get("avg_score"));
                Integer totalPosts = toInt(feature.get("total_posts"));
                node.setAvgEmotionScore(round4(avgScore));
                node.setDominantEmotion(scoreToDominantEmotion(avgScore));
                node.setPostCount(totalPosts != null ? totalPosts : 0);
                node.setNodeSize(calcNodeSize(totalPosts != null ? totalPosts : 0));
            } else {
                node.setPostCount(0);
                node.setNodeSize(1.0);
                node.setDominantEmotion("NEUTRAL");
            }

            Integer cid = communityMap.get(uid);
            node.setCommunityId(cid);
            if (cid != null) {
                node.setCommunityType(determineCommunityType(
                        node.getAvgEmotionScore() != null ? node.getAvgEmotionScore() : 0.0,
                        node.getDominantEmotion()));
            }

            return node;
        }).collect(Collectors.toList());
    }

    /**
     * 构建成员详情列表
     */
    private List<SentimentCommunityVO.CommunityMemberVO> buildMemberList(Set<Long> memberIds) {
        if (memberIds.isEmpty()) return Collections.emptyList();

        Map<Long, User> userMap = userMapper.selectBatchIds(memberIds)
                .stream().collect(Collectors.toMap(User::getId, u -> u));

        return memberIds.stream().map(uid -> {
            SentimentCommunityVO.CommunityMemberVO member = new SentimentCommunityVO.CommunityMemberVO();
            member.setUserId(uid);

            User user = userMap.get(uid);
            if (user != null) {
                member.setNickname(user.getNickname());
                member.setAvatar(user.getAvatar());
            }

            Map<String, Object> feature = statsMapper.selectUserEmotionFeature(uid);
            if (feature != null) {
                double avgScore = toDouble(feature.get("avg_score"));
                Integer totalPosts = toInt(feature.get("total_posts"));
                member.setAvgEmotionScore(round4(avgScore));
                member.setDominantEmotion(scoreToDominantEmotion(avgScore));
                member.setPostCount(totalPosts != null ? totalPosts : 0);
            } else {
                member.setPostCount(0);
                member.setDominantEmotion("NEUTRAL");
            }
            return member;
        }).collect(Collectors.toList());
    }

    /**
     * 将 SentimentResonance 记录转为边 VO
     */
    private SentimentNetworkVO.NetworkEdgeVO buildEdgeVO(SentimentResonance r) {
        SentimentNetworkVO.NetworkEdgeVO edge = new SentimentNetworkVO.NetworkEdgeVO();
        edge.setSourceUserId(r.getUserAId());
        edge.setTargetUserId(r.getUserBId());
        edge.setResonanceScore(r.getResonanceScore() != null ? r.getResonanceScore().doubleValue() : 0.0);
        edge.setSentimentSimilarity(r.getSentimentSimilarity() != null
                ? r.getSentimentSimilarity().doubleValue() : 0.0);
        edge.setInteractionCount(r.getInteractionCount() != null ? r.getInteractionCount() : 0);
        edge.setCommonEmotionLabel(r.getCommonEmotionLabel());
        // 边权重 = 共鸣分数（可视化时控制边的粗细）
        edge.setEdgeWeight(edge.getResonanceScore());
        return edge;
    }

    /**
     * 构建网络统计信息
     */
    private SentimentNetworkVO.NetworkStatsVO buildNetworkStats(
            List<SentimentNetworkVO.NetworkNodeVO> nodes,
            List<SentimentNetworkVO.NetworkEdgeVO> edges,
            Long centerUserId) {

        SentimentNetworkVO.NetworkStatsVO stats = new SentimentNetworkVO.NetworkStatsVO();
        stats.setTotalNodes(nodes.size());
        stats.setTotalEdges(edges.size());

        double avgScore = edges.stream()
                .mapToDouble(SentimentNetworkVO.NetworkEdgeVO::getResonanceScore)
                .average().orElse(0.0);
        stats.setAvgResonanceScore(round4(avgScore));

        long communityCount = nodes.stream()
                .map(SentimentNetworkVO.NetworkNodeVO::getCommunityId)
                .filter(Objects::nonNull)
                .distinct().count();
        stats.setCommunityCount((int) communityCount);

        nodes.stream()
                .filter(n -> n.getUserId().equals(centerUserId))
                .findFirst()
                .ifPresent(n -> stats.setCenterUserDominantEmotion(n.getDominantEmotion()));

        return stats;
    }

    // ==================== 工具方法 ====================

    /** 根据平均情感分数判断主导情感 */
    String scoreToDominantEmotion(double score) {
        if (score > 0.2) return "POSITIVE";
        if (score < -0.2) return "NEGATIVE";
        return "NEUTRAL";
    }

    /** 根据社区平均情感分数和主导情感标签确定社区类型 */
    String determineCommunityType(double avgScore, String dominantEmotion) {
        if ("POSITIVE".equals(dominantEmotion) || avgScore > 0.2) return "OPTIMISTIC";
        if ("NEGATIVE".equals(dominantEmotion) || avgScore < -0.2) return "PESSIMISTIC";
        return "RATIONAL";
    }

    /** 社区类型 → 中文标签 */
    private String getCommunityLabel(String communityType) {
        return switch (communityType) {
            case "OPTIMISTIC" -> "乐观派";
            case "PESSIMISTIC" -> "悲观派";
            case "RATIONAL" -> "理性派";
            default -> "混合派";
        };
    }

    /** 根据两用户的平均情感分数确定共同情感标签 */
    private String resolveCommonEmotionLabel(double scoreA, double scoreB) {
        double avgScore = (scoreA + scoreB) / 2.0;
        if (avgScore > 0.2) return "POSITIVE";
        if (avgScore < -0.2) return "NEGATIVE";
        return "NEUTRAL";
    }

    /** 节点大小归一化（帖子数量 → 可视化大小）：范围 [1, 10] */
    private double calcNodeSize(int postCount) {
        if (postCount <= 0) return 1.0;
        // log 缩放，防止大节点过大
        return Math.min(10.0, 1.0 + Math.log1p(postCount) * 2);
    }

    private BigDecimal toBD(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);
    }

    private double toDouble(Object obj) {
        if (obj == null) return 0.0;
        return ((Number) obj).doubleValue();
    }

    private Integer toInt(Object obj) {
        if (obj == null) return null;
        return ((Number) obj).intValue();
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        return ((Number) obj).longValue();
    }

    private double round4(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }
}
