package com.seu.emotionhub.service.impl;

import com.seu.emotionhub.dao.mapper.SentimentResonanceMapper;
import com.seu.emotionhub.dao.mapper.UserEmotionStatsMapper;
import com.seu.emotionhub.dao.mapper.UserMapper;
import com.seu.emotionhub.model.dto.response.SentimentCommunityVO;
import com.seu.emotionhub.model.dto.response.SentimentNetworkVO;
import com.seu.emotionhub.model.entity.SentimentResonance;
import com.seu.emotionhub.model.entity.User;
import com.seu.emotionhub.service.impl.SentimentResonanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SentimentResonanceService 单元测试
 *
 * 覆盖范围：
 * 1. 余弦相似度计算（正常值、零向量、完全相同、完全相反）
 * 2. Louvain 社区发现算法（空图、单节点、完整图）
 * 3. 情感特征提取
 * 4. 社区类型判断
 * 5. 服务层方法（getResonanceNetwork、getSentimentCommunities）
 *
 * @author EmotionHub Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SentimentResonanceService 单元测试")
class SentimentResonanceServiceTest {

    @Mock
    private SentimentResonanceMapper resonanceMapper;

    @Mock
    private UserEmotionStatsMapper statsMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private SentimentResonanceServiceImpl service;

    // ==================== 余弦相似度测试 ====================

    @Nested
    @DisplayName("余弦相似度计算")
    class CosineSimilarityTest {

        @Test
        @DisplayName("相同向量 → 相似度应为 1.0")
        void sameVectors_shouldReturn1() {
            double[] v = {0.5, 0.4, 0.3, 0.3};
            double sim = service.calculateCosineSimilarity(v, v);
            assertThat(sim).isCloseTo(1.0, within(1e-6));
        }

        @Test
        @DisplayName("正交向量 → 相似度应为 0.0")
        void orthogonalVectors_shouldReturn0() {
            double[] v1 = {1.0, 0.0, 0.0, 0.0};
            double[] v2 = {0.0, 1.0, 0.0, 0.0};
            double sim = service.calculateCosineSimilarity(v1, v2);
            assertThat(sim).isCloseTo(0.0, within(1e-6));
        }

        @Test
        @DisplayName("反向向量 → 相似度应为 -1.0")
        void oppositeVectors_shouldReturnMinus1() {
            double[] v1 = {1.0, 0.0, 0.0, 0.0};
            double[] v2 = {-1.0, 0.0, 0.0, 0.0};
            double sim = service.calculateCosineSimilarity(v1, v2);
            assertThat(sim).isCloseTo(-1.0, within(1e-6));
        }

        @Test
        @DisplayName("零向量（v1） → 相似度应为 0.0")
        void zeroVector_v1_shouldReturn0() {
            double[] v1 = {0.0, 0.0, 0.0, 0.0};
            double[] v2 = {0.5, 0.3, 0.1, 0.1};
            double sim = service.calculateCosineSimilarity(v1, v2);
            assertThat(sim).isEqualTo(0.0);
        }

        @Test
        @DisplayName("两个零向量 → 相似度应为 0.0")
        void bothZeroVectors_shouldReturn0() {
            double[] v1 = {0.0, 0.0, 0.0, 0.0};
            double[] v2 = {0.0, 0.0, 0.0, 0.0};
            double sim = service.calculateCosineSimilarity(v1, v2);
            assertThat(sim).isEqualTo(0.0);
        }

        @Test
        @DisplayName("典型用户向量 → 相似度应在 [-1, 1] 范围内")
        void typicalVectors_shouldBeInRange() {
            double[] v1 = {0.3, 0.6, 0.3, 0.1};
            double[] v2 = {0.2, 0.5, 0.3, 0.2};
            double sim = service.calculateCosineSimilarity(v1, v2);
            assertThat(sim).isBetween(-1.0, 1.0);
            // 两个正向量应有正相似度
            assertThat(sim).isGreaterThan(0.0);
        }
    }

    // ==================== Louvain 社区发现测试 ====================

    @Nested
    @DisplayName("Louvain 社区发现算法")
    class LouvainCommunityDetectionTest {

        @Test
        @DisplayName("空边列表 → 每个节点各自成社区")
        void emptyEdges_shouldReturnSingletonCommunities() {
            List<Long> users = Arrays.asList(1L, 2L, 3L);
            Map<Long, Integer> result = service.detectCommunities(Collections.emptyList(), users);

            assertThat(result).hasSize(3);
            // 三个不同社区
            assertThat(new HashSet<>(result.values())).hasSize(3);
        }

        @Test
        @DisplayName("空用户列表 → 返回空映射")
        void emptyUsers_shouldReturnEmptyMap() {
            Map<Long, Integer> result = service.detectCommunities(Collections.emptyList(), Collections.emptyList());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("完全连通图（3节点）→ 所有节点应在同一社区")
        void fullyConnectedTriangle_shouldBeOneCommunity() {
            List<Long> users = Arrays.asList(1L, 2L, 3L);

            // 创建三条边: 1-2, 1-3, 2-3，共鸣分数都很高
            List<SentimentResonance> edges = new ArrayList<>();
            edges.add(buildResonance(1L, 2L, 0.9));
            edges.add(buildResonance(1L, 3L, 0.9));
            edges.add(buildResonance(2L, 3L, 0.9));

            Map<Long, Integer> result = service.detectCommunities(edges, users);

            assertThat(result).hasSize(3);
            // 所有节点应在同一社区
            assertThat(new HashSet<>(result.values())).hasSize(1);
        }

        @Test
        @DisplayName("两个独立子图 → 应形成两个不同社区")
        void twoDisconnectedSubgraphs_shouldFormTwoCommunities() {
            List<Long> users = Arrays.asList(1L, 2L, 3L, 4L);

            // 子图1: 1-2 强连接
            // 子图2: 3-4 强连接
            // 子图间无连接
            List<SentimentResonance> edges = new ArrayList<>();
            edges.add(buildResonance(1L, 2L, 0.95));
            edges.add(buildResonance(3L, 4L, 0.95));

            Map<Long, Integer> result = service.detectCommunities(edges, users);

            assertThat(result).hasSize(4);
            // 节点1和2应在同一社区
            assertThat(result.get(1L)).isEqualTo(result.get(2L));
            // 节点3和4应在同一社区
            assertThat(result.get(3L)).isEqualTo(result.get(4L));
            // 两个子图应在不同社区
            assertThat(result.get(1L)).isNotEqualTo(result.get(3L));
        }

        @Test
        @DisplayName("单个节点 → 应有效处理，返回包含该节点的映射")
        void singleUser_shouldHandleGracefully() {
            List<Long> users = Collections.singletonList(1L);
            Map<Long, Integer> result = service.detectCommunities(Collections.emptyList(), users);

            assertThat(result).hasSize(1);
            assertThat(result.get(1L)).isNotNull();
        }

        @Test
        @DisplayName("社区 ID 应从 1 开始连续递增")
        void communityIds_shouldBeNormalized() {
            List<Long> users = Arrays.asList(1L, 2L, 3L);
            List<SentimentResonance> edges = Collections.singletonList(buildResonance(1L, 2L, 0.9));

            Map<Long, Integer> result = service.detectCommunities(edges, users);

            // 所有社区 ID 应 >= 1
            result.values().forEach(id -> assertThat(id).isGreaterThanOrEqualTo(1));
        }
    }

    // ==================== 情感特征提取测试 ====================

    @Nested
    @DisplayName("用户情感特征向量提取")
    class FeatureVectorExtractionTest {

        @Test
        @DisplayName("用户无情感数据 → 返回 null")
        void noEmotionData_shouldReturnNull() {
            when(statsMapper.selectUserEmotionFeature(1L)).thenReturn(null);
            double[] vector = service.extractFeatureVector(1L);
            assertThat(vector).isNull();
        }

        @Test
        @DisplayName("用户无帖子 → 返回 null")
        void noPostsInStats_shouldReturnNull() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("total_posts", 0);
            when(statsMapper.selectUserEmotionFeature(1L)).thenReturn(stats);
            double[] vector = service.extractFeatureVector(1L);
            assertThat(vector).isNull();
        }

        @Test
        @DisplayName("有效数据 → 返回4维向量，所有值在合理范围内")
        void validData_shouldReturn4DVector() {
            Map<String, Object> stats = buildUserStats(0.5, 6, 2, 2, 10);
            when(statsMapper.selectUserEmotionFeature(1L)).thenReturn(stats);

            double[] vector = service.extractFeatureVector(1L);

            assertThat(vector).isNotNull().hasSize(4);
            // avgScore
            assertThat(vector[0]).isCloseTo(0.5, within(1e-6));
            // positiveRatio = 6/10 = 0.6
            assertThat(vector[1]).isCloseTo(0.6, within(1e-6));
            // neutralRatio = 2/10 = 0.2
            assertThat(vector[2]).isCloseTo(0.2, within(1e-6));
            // negativeRatio = 2/10 = 0.2
            assertThat(vector[3]).isCloseTo(0.2, within(1e-6));
        }

        @Test
        @DisplayName("全正向情感数据 → positiveRatio 应接近 1.0")
        void allPositiveEmotions_shouldHaveHighPositiveRatio() {
            Map<String, Object> stats = buildUserStats(0.8, 10, 0, 0, 10);
            when(statsMapper.selectUserEmotionFeature(1L)).thenReturn(stats);

            double[] vector = service.extractFeatureVector(1L);

            assertThat(vector).isNotNull();
            assertThat(vector[1]).isCloseTo(1.0, within(1e-6)); // positiveRatio
            assertThat(vector[2]).isCloseTo(0.0, within(1e-6)); // neutralRatio
            assertThat(vector[3]).isCloseTo(0.0, within(1e-6)); // negativeRatio
        }
    }

    // ==================== 社区类型判断测试 ====================

    @Nested
    @DisplayName("社区类型判断")
    class CommunityTypeTest {

        @Test
        @DisplayName("POSITIVE 主导情感 → OPTIMISTIC")
        void positiveDominant_shouldBeOptimistic() {
            assertThat(service.determineCommunityType(0.5, "POSITIVE")).isEqualTo("OPTIMISTIC");
        }

        @Test
        @DisplayName("NEGATIVE 主导情感 → PESSIMISTIC")
        void negativeDominant_shouldBePessimistic() {
            assertThat(service.determineCommunityType(-0.5, "NEGATIVE")).isEqualTo("PESSIMISTIC");
        }

        @Test
        @DisplayName("avgScore > 0.2（无论标签）→ OPTIMISTIC")
        void highAvgScore_shouldBeOptimistic() {
            assertThat(service.determineCommunityType(0.3, "NEUTRAL")).isEqualTo("OPTIMISTIC");
        }

        @Test
        @DisplayName("avgScore < -0.2（无论标签）→ PESSIMISTIC")
        void lowAvgScore_shouldBePessimistic() {
            assertThat(service.determineCommunityType(-0.3, "NEUTRAL")).isEqualTo("PESSIMISTIC");
        }

        @Test
        @DisplayName("avgScore 在 [-0.2, 0.2] → RATIONAL")
        void neutralAvgScore_shouldBeRational() {
            assertThat(service.determineCommunityType(0.1, "NEUTRAL")).isEqualTo("RATIONAL");
            assertThat(service.determineCommunityType(-0.1, "NEUTRAL")).isEqualTo("RATIONAL");
            assertThat(service.determineCommunityType(0.0, null)).isEqualTo("RATIONAL");
        }
    }

    // ==================== 主导情感映射测试 ====================

    @Nested
    @DisplayName("情感分数 → 主导情感标签")
    class ScoreToDominantEmotionTest {

        @Test
        @DisplayName("分数 > 0.2 → POSITIVE")
        void highScore_shouldBePositive() {
            assertThat(service.scoreToDominantEmotion(0.5)).isEqualTo("POSITIVE");
            assertThat(service.scoreToDominantEmotion(0.21)).isEqualTo("POSITIVE");
            assertThat(service.scoreToDominantEmotion(1.0)).isEqualTo("POSITIVE");
        }

        @Test
        @DisplayName("分数 < -0.2 → NEGATIVE")
        void lowScore_shouldBeNegative() {
            assertThat(service.scoreToDominantEmotion(-0.5)).isEqualTo("NEGATIVE");
            assertThat(service.scoreToDominantEmotion(-0.21)).isEqualTo("NEGATIVE");
            assertThat(service.scoreToDominantEmotion(-1.0)).isEqualTo("NEGATIVE");
        }

        @Test
        @DisplayName("分数在 [-0.2, 0.2] → NEUTRAL")
        void midScore_shouldBeNeutral() {
            assertThat(service.scoreToDominantEmotion(0.0)).isEqualTo("NEUTRAL");
            assertThat(service.scoreToDominantEmotion(0.2)).isEqualTo("NEUTRAL");
            assertThat(service.scoreToDominantEmotion(-0.2)).isEqualTo("NEUTRAL");
        }
    }

    // ==================== 服务方法集成测试（Mock） ====================

    @Nested
    @DisplayName("getSentimentCommunities - 服务方法")
    class GetSentimentCommunitiesTest {

        @Test
        @DisplayName("无共鸣数据 → 返回空列表")
        void noResonanceData_shouldReturnEmptyList() {
            when(resonanceMapper.selectOne(any())).thenReturn(null);
            List<SentimentCommunityVO> result = service.getSentimentCommunities();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("有共鸣数据但无社区 → 返回空列表")
        void hasDataButNoCommunities_shouldReturnEmptyList() {
            SentimentResonance latestResonance = buildResonance(1L, 2L, 0.8);
            latestResonance.setCalculationDate(LocalDate.now());
            when(resonanceMapper.selectOne(any())).thenReturn(latestResonance);
            when(resonanceMapper.selectCommunityStats(any())).thenReturn(Collections.emptyList());

            List<SentimentCommunityVO> result = service.getSentimentCommunities();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("有一个社区 → 正确返回社区信息")
        void oneCommunity_shouldReturnCommunityInfo() {
            SentimentResonance latestResonance = buildResonance(1L, 2L, 0.8);
            latestResonance.setCalculationDate(LocalDate.now());
            when(resonanceMapper.selectOne(any())).thenReturn(latestResonance);

            // 社区统计数据
            Map<String, Object> commStat = new HashMap<>();
            commStat.put("community_id", 1);
            commStat.put("member_count", 2);
            commStat.put("avg_resonance", 0.8);
            commStat.put("common_emotion_label", "POSITIVE");
            when(resonanceMapper.selectCommunityStats(any())).thenReturn(List.of(commStat));

            // 社区关系
            List<SentimentResonance> relations = List.of(buildResonance(1L, 2L, 0.8));
            when(resonanceMapper.selectByCommunityId(eq(1), any())).thenReturn(relations);

            // 用户信息
            User user1 = buildUser(1L, "Alice");
            User user2 = buildUser(2L, "Bob");
            when(userMapper.selectBatchIds(anyCollection())).thenReturn(List.of(user1, user2));

            // 情感统计
            Map<String, Object> stats1 = buildUserStats(0.6, 6, 2, 2, 10);
            Map<String, Object> stats2 = buildUserStats(0.4, 5, 3, 2, 10);
            when(statsMapper.selectUserEmotionFeature(1L)).thenReturn(stats1);
            when(statsMapper.selectUserEmotionFeature(2L)).thenReturn(stats2);

            List<SentimentCommunityVO> result = service.getSentimentCommunities();

            assertThat(result).hasSize(1);
            SentimentCommunityVO community = result.get(0);
            assertThat(community.getCommunityId()).isEqualTo(1);
            assertThat(community.getCommunityType()).isEqualTo("OPTIMISTIC");
            assertThat(community.getCommunityLabel()).isEqualTo("乐观派");
            assertThat(community.getMemberCount()).isEqualTo(2);
            assertThat(community.getAvgResonanceScore()).isCloseTo(0.8, within(1e-6));
            assertThat(community.getMembers()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getResonanceNetwork - 服务方法")
    class GetResonanceNetworkTest {

        @Test
        @DisplayName("无共鸣数据 → 自动触发计算，返回含中心节点的网络")
        void noData_shouldTriggerRecalculationAndReturnNetwork() {
            // 首次查询无数据
            when(resonanceMapper.selectOne(any())).thenReturn(null);
            // recalculate 中查询活跃用户为空（不实际计算）
            when(statsMapper.selectActiveUserIds()).thenReturn(Collections.emptyList());
            // 二次查询仍无数据（recalculate 没写入任何数据）
            when(resonanceMapper.selectResonanceNetwork(anyLong(), any(), anyDouble(), anyInt()))
                    .thenReturn(Collections.emptyList());
            // 用户信息
            User user = buildUser(1L, "TestUser");
            when(userMapper.selectBatchIds(anyCollection())).thenReturn(List.of(user));
            when(statsMapper.selectUserEmotionFeature(1L)).thenReturn(null);
            when(resonanceMapper.selectCommunityIdByUser(anyLong(), any())).thenReturn(null);

            SentimentNetworkVO result = service.getResonanceNetwork(1L, 2);

            assertThat(result).isNotNull();
            assertThat(result.getCenterUserId()).isEqualTo(1L);
            assertThat(result.getDepth()).isEqualTo(2);
            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getEdges()).isEmpty();

            // 中心节点标记正确
            SentimentNetworkVO.NetworkNodeVO centerNode = result.getNodes().get(0);
            assertThat(centerNode.getIsCenterUser()).isTrue();
            assertThat(centerNode.getNickname()).isEqualTo("TestUser");
        }

        @Test
        @DisplayName("深度超出 MAX_DEPTH(3) → 自动截断为 3")
        void depthExceedsMax_shouldBeCappedAt3() {
            SentimentResonance latestResonance = buildResonance(1L, 2L, 0.8);
            latestResonance.setCalculationDate(LocalDate.now());
            when(resonanceMapper.selectOne(any())).thenReturn(latestResonance);
            when(resonanceMapper.selectResonanceNetwork(anyLong(), any(), anyDouble(), anyInt()))
                    .thenReturn(Collections.emptyList());
            User user = buildUser(1L, "TestUser");
            when(userMapper.selectBatchIds(anyCollection())).thenReturn(List.of(user));
            when(statsMapper.selectUserEmotionFeature(anyLong())).thenReturn(null);
            when(resonanceMapper.selectCommunityIdByUser(anyLong(), any())).thenReturn(null);

            SentimentNetworkVO result = service.getResonanceNetwork(1L, 10);

            assertThat(result.getDepth()).isEqualTo(3);
        }

        @Test
        @DisplayName("depth=null → 默认使用深度 2")
        void nullDepth_shouldDefaultTo2() {
            SentimentResonance latestResonance = buildResonance(1L, 2L, 0.8);
            latestResonance.setCalculationDate(LocalDate.now());
            when(resonanceMapper.selectOne(any())).thenReturn(latestResonance);
            when(resonanceMapper.selectResonanceNetwork(anyLong(), any(), anyDouble(), anyInt()))
                    .thenReturn(Collections.emptyList());
            User user = buildUser(1L, "TestUser");
            when(userMapper.selectBatchIds(anyCollection())).thenReturn(List.of(user));
            when(statsMapper.selectUserEmotionFeature(anyLong())).thenReturn(null);
            when(resonanceMapper.selectCommunityIdByUser(anyLong(), any())).thenReturn(null);

            SentimentNetworkVO result = service.getResonanceNetwork(1L, null);

            assertThat(result.getDepth()).isEqualTo(2);
        }

        @Test
        @DisplayName("有邻居节点 → 正确构建节点和边")
        void withNeighbors_shouldBuildNodesAndEdges() {
            SentimentResonance latestResonance = buildResonance(1L, 2L, 0.8);
            latestResonance.setCalculationDate(LocalDate.now());
            when(resonanceMapper.selectOne(any())).thenReturn(latestResonance);

            // 用户1的邻居：用户2
            SentimentResonance edge12 = buildResonance(1L, 2L, 0.75);
            when(resonanceMapper.selectResonanceNetwork(eq(1L), any(), anyDouble(), anyInt()))
                    .thenReturn(List.of(edge12));
            // 用户2无邻居（深度限制）
            when(resonanceMapper.selectResonanceNetwork(eq(2L), any(), anyDouble(), anyInt()))
                    .thenReturn(Collections.emptyList());

            User user1 = buildUser(1L, "Alice");
            User user2 = buildUser(2L, "Bob");
            when(userMapper.selectBatchIds(anyCollection())).thenReturn(List.of(user1, user2));
            when(statsMapper.selectUserEmotionFeature(anyLong())).thenReturn(null);
            when(resonanceMapper.selectCommunityIdByUser(anyLong(), any())).thenReturn(null);

            SentimentNetworkVO result = service.getResonanceNetwork(1L, 2);

            assertThat(result.getNodes()).hasSize(2);
            assertThat(result.getEdges()).hasSize(1);

            // 统计数据正确
            assertThat(result.getStats().getTotalNodes()).isEqualTo(2);
            assertThat(result.getStats().getTotalEdges()).isEqualTo(1);

            // 边数据正确
            SentimentNetworkVO.NetworkEdgeVO edge = result.getEdges().get(0);
            assertThat(edge.getSourceUserId()).isEqualTo(1L);
            assertThat(edge.getTargetUserId()).isEqualTo(2L);
            assertThat(edge.getResonanceScore()).isCloseTo(0.75, within(1e-4));
        }
    }

    @Nested
    @DisplayName("recalculateResonance - 全量计算")
    class RecalculateResonanceTest {

        @Test
        @DisplayName("活跃用户 < 2 → 跳过计算，返回 0")
        void fewActiveUsers_shouldSkipAndReturn0() {
            when(statsMapper.selectActiveUserIds()).thenReturn(Collections.singletonList(1L));
            int result = service.recalculateResonance();
            assertThat(result).isEqualTo(0);
            verify(resonanceMapper, never()).insert(any());
        }

        @Test
        @DisplayName("无有效特征向量用户 → 返回 0")
        void noValidFeatureVectors_shouldReturn0() {
            when(statsMapper.selectActiveUserIds()).thenReturn(Arrays.asList(1L, 2L));
            when(statsMapper.selectUserEmotionFeature(anyLong())).thenReturn(null);
            int result = service.recalculateResonance();
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("两个高度相似用户 → 应创建共鸣对")
        void twoSimilarUsers_shouldCreateResonancePair() {
            when(statsMapper.selectActiveUserIds()).thenReturn(Arrays.asList(1L, 2L));

            // 两个特征很相似的用户
            Map<String, Object> stats = buildUserStats(0.6, 7, 2, 1, 10);
            when(statsMapper.selectUserEmotionFeature(anyLong())).thenReturn(stats);

            // mock delete 和 insert
            when(resonanceMapper.delete(any())).thenReturn(0);
            when(resonanceMapper.insert(any())).thenReturn(1);

            int result = service.recalculateResonance();

            assertThat(result).isEqualTo(1);
            verify(resonanceMapper, times(1)).insert(any(SentimentResonance.class));
        }

        @Test
        @DisplayName("两个完全不同情感的用户 → 共鸣分数低，可能不创建共鸣对")
        void twoOppositeUsers_resonanceScoreMayBeBelowThreshold() {
            when(statsMapper.selectActiveUserIds()).thenReturn(Arrays.asList(1L, 2L));

            // 用户1: 高度正面
            Map<String, Object> stats1 = buildUserStats(0.9, 10, 0, 0, 10);
            when(statsMapper.selectUserEmotionFeature(1L)).thenReturn(stats1);
            // 用户2: 高度负面
            Map<String, Object> stats2 = buildUserStats(-0.9, 0, 0, 10, 10);
            when(statsMapper.selectUserEmotionFeature(2L)).thenReturn(stats2);

            when(resonanceMapper.delete(any())).thenReturn(0);

            int result = service.recalculateResonance();

            // 高度相反的用户余弦相似度应接近 -1，归一化后约 0
            // 低于 RESONANCE_THRESHOLD(0.3)，不应创建共鸣对
            assertThat(result).isEqualTo(0);
        }
    }

    // ==================== 辅助方法 ====================

    private SentimentResonance buildResonance(Long userA, Long userB, double score) {
        SentimentResonance r = new SentimentResonance();
        r.setUserAId(userA);
        r.setUserBId(userB);
        r.setResonanceScore(BigDecimal.valueOf(score));
        r.setSentimentSimilarity(BigDecimal.valueOf(score));
        r.setInteractionCount(0);
        r.setCommonEmotionLabel("POSITIVE");
        return r;
    }

    private User buildUser(Long id, String nickname) {
        User user = new User();
        user.setId(id);
        user.setNickname(nickname);
        user.setAvatar("https://example.com/avatar/" + id + ".jpg");
        return user;
    }

    /**
     * 构建 user_emotion_stats 聚合查询结果 Map
     */
    private Map<String, Object> buildUserStats(double avgScore, int positive,
                                                int neutral, int negative, int totalPosts) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("avg_score", avgScore);
        stats.put("total_positive", positive);
        stats.put("total_neutral", neutral);
        stats.put("total_negative", negative);
        stats.put("total_posts", totalPosts);
        return stats;
    }
}
