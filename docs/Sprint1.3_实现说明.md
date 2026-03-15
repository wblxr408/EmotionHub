# Sprint 1.3 用户情感影响力评分 - 实现说明

## 📋 实现概述

Sprint 1.3 已完整实现，包含以下核心功能：
- ✅ PageRank 变体算法计算综合影响力
- ✅ 正面/负面影响力分析
- ✅ 争议性评分计算
- ✅ 互动深度统计
- ✅ 情感改变率分析
- ✅ 影响力排行榜
- ✅ 影响力历史趋势
- ✅ 自动定时计算（每日凌晨3点）

---

## 📁 新增文件清单

### 1. Service 层
- **UserInfluenceService.java**
  - 路径: `backend/emotion-hub-service/src/main/java/com/seu/emotionhub/service/`
  - 功能: 用户影响力服务接口定义

- **UserInfluenceServiceImpl.java**
  - 路径: `backend/emotion-hub-service/src/main/java/com/seu/emotionhub/service/impl/`
  - 功能: 影响力计算核心算法实现（750+ 行代码）
  - 核心算法:
    - PageRank 变体（阻尼系数 0.85，最大迭代 20 次）
    - 正面/负面影响力统计
    - 争议性评分（情感分数标准差）
    - 互动深度（评论树深度）
    - 情感改变率

### 2. Controller 层
- **UserInfluenceController.java**
  - 路径: `backend/emotion-hub-web/src/main/java/com/seu/emotionhub/web/controller/`
  - 功能: 提供 REST API 接口

### 3. 数据模型层（更新）
- **UserInfluenceVO.java**
  - 路径: `backend/emotion-hub-model/src/main/java/com/seu/emotionhub/model/dto/response/`
  - 更新: 补充完整字段（影响力等级、各项指标）

### 4. 数据库层
- **database/init.sql**
  - 新增表:
    - `user_influence_score` - 用户情感影响力评分表
    - `sentiment_resonance` - 情感共鸣关系表（为 Sprint 1.4 准备）

---

## 🔌 API 接口列表

### 1. 查询接口

#### 1.1 获取用户最新影响力评分
```http
GET /api/influence/user/{userId}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "userId": 1,
    "nickname": "张三",
    "avatar": "https://...",
    "influenceScore": 75.5000,
    "positiveImpact": 68.3000,
    "negativeImpact": 15.2000,
    "controversialScore": 42.0000,
    "postCount": 25,
    "commentCount": 120,
    "avgEngagementDepth": 2.5000,
    "sentimentChangeRate": 0.3500,
    "calculationDate": "2024-03-15",
    "influenceLevel": "EXPERT",
    "influenceLevelDesc": "专家影响者"
  }
}
```

#### 1.2 获取用户指定日期的影响力评分
```http
GET /api/influence/user/{userId}/history?date=2024-03-15
```

#### 1.3 获取用户影响力历史趋势
```http
GET /api/influence/user/{userId}/trend?startDate=2024-03-01&endDate=2024-03-31
```

**返回:** 影响力历史记录数组，可用于绘制趋势图

---

### 2. 排行榜接口

#### 2.1 综合影响力排行榜
```http
GET /api/influence/ranking/overall?limit=20
```

**说明:** 基于 PageRank 算法计算的综合影响力 Top N

#### 2.2 正能量影响力排行榜
```http
GET /api/influence/ranking/positive?limit=20
```

**说明:** 引发正面评论最多的用户 Top N

#### 2.3 话题制造者排行榜
```http
GET /api/influence/ranking/controversial?limit=20
```

**说明:** 引发评论区情感分化最大的用户 Top N（高争议性）

---

### 3. 管理接口

#### 3.1 手动触发全量影响力计算
```http
POST /api/influence/recalculate
```

**说明:** 重新计算所有活跃用户的影响力，耗时较长

#### 3.2 计算单个用户的影响力
```http
POST /api/influence/calculate/{userId}
```

**说明:** 重新计算指定用户的影响力评分

---

## 🧮 核心算法说明

### 1. PageRank 变体算法

**目的:** 计算用户的综合影响力

**原理:**
- 构建用户互动图：用户 A 评论用户 B 的帖子 → A 向 B 投票
- 迭代计算 PageRank 值
- 参数:
  - 阻尼系数: 0.85
  - 最大迭代次数: 20
  - 收敛阈值: 0.0001

**公式:**
```
PR(u) = (1-d)/N + d * Σ(PR(v) * w(v→u) / OutDegree(v))
```

### 2. 正面/负面影响力

**计算方法:**
- 统计用户帖子引发的所有评论
- 正面影响力 = 正面评论数 / 总评论数
- 负面影响力 = 负面评论数 / 总评论数

### 3. 争议性评分

**计算方法:**
- 计算帖子评论区情感分数的标准差
- 标准差越大，说明评论区情感分化越严重
- 归一化到 [0, 1] 区间

**公式:**
```
Controversial = stdDev(emotion_scores)
```

### 4. 平均互动深度

**计算方法:**
- 递归计算每条评论在评论树中的深度
- 求所有评论深度的平均值
- 深度越大，说明讨论越激烈

### 5. 情感改变率

**计算方法:**
- 统计评论情感与帖子情感不一致的比例
- 比例越高，说明用户越能引发不同的情感反应

**公式:**
```
ChangeRate = (不一致评论数 / 总评论数)
```

---

## ⏰ 定时任务配置

### 自动计算任务

**触发时间:** 每日凌晨 3:00
**Cron 表达式:** `0 0 3 * * ?`
**执行内容:** 计算所有活跃用户（最近30天有发帖）的影响力评分

**代码位置:**
```java
// UserInfluenceServiceImpl.java
@Scheduled(cron = "0 0 3 * * ?")
public void scheduledInfluenceCalculation() {
    // 自动计算逻辑
}
```

---

## 💾 数据库表结构

### user_influence_score 表

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户ID（外键） |
| influence_score | DECIMAL(6,4) | 综合影响力分数（0-100） |
| positive_impact | DECIMAL(6,4) | 正面影响力（0-100） |
| negative_impact | DECIMAL(6,4) | 负面影响力（0-100） |
| controversial_score | DECIMAL(6,4) | 争议性分数（0-100） |
| post_count | INT | 统计期内帖子数 |
| comment_count | INT | 统计期内获得的评论数 |
| avg_engagement_depth | DECIMAL(6,4) | 平均互动深度 |
| sentiment_change_rate | DECIMAL(6,4) | 情感改变率（0-1） |
| calculation_date | DATE | 计算日期 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

**索引:**
- 主键: `id`
- 唯一索引: `(user_id, calculation_date)`
- 普通索引: `influence_score`, `positive_impact`, `controversial_score`, `calculation_date`

---

## 🧪 测试步骤

### 1. 数据库初始化

```bash
# 进入数据库目录
cd database

# 执行初始化脚本
mysql -u root -p < init.sql
```

### 2. 启动后端服务

```bash
cd backend/emotion-hub-web
mvn spring-boot:run
```

### 3. 测试 API 接口

#### 方式一：通过 Swagger UI
1. 打开浏览器访问: http://localhost:8080/doc.html
2. 找到 "用户情感影响力" 分组
3. 测试各个接口

#### 方式二：使用 curl 命令

**手动触发计算（首次使用必须执行）:**
```bash
curl -X POST http://localhost:8080/api/influence/recalculate
```

**查询用户影响力:**
```bash
curl http://localhost:8080/api/influence/user/1
```

**查询综合影响力排行榜:**
```bash
curl http://localhost:8080/api/influence/ranking/overall?limit=10
```

**查询正能量排行榜:**
```bash
curl http://localhost:8080/api/influence/ranking/positive?limit=10
```

**查询话题制造者排行榜:**
```bash
curl http://localhost:8080/api/influence/ranking/controversial?limit=10
```

**查询影响力趋势:**
```bash
curl "http://localhost:8080/api/influence/user/1/trend?startDate=2024-03-01&endDate=2024-03-31"
```

### 4. 验证定时任务

**方式一：修改 Cron 表达式测试**
```java
// 临时修改为每分钟执行一次
@Scheduled(cron = "0 * * * * ?")
```

**方式二：查看日志**
```
2024-03-15 03:00:00 INFO  [scheduling-1] - 定时任务：开始每日用户影响力计算...
2024-03-15 03:00:05 INFO  [scheduling-1] - 定时任务：用户影响力计算完成，成功计算 50 个用户
```

---

## 📊 影响力等级划分

| 分数区间 | 等级代码 | 等级名称 | 描述 |
|---------|---------|---------|------|
| 80-100 | LEGENDARY | 传奇影响者 | 社区顶级影响力 |
| 60-79 | EXPERT | 专家影响者 | 高影响力用户 |
| 40-59 | ADVANCED | 进阶影响者 | 中等影响力 |
| 20-39 | INTERMEDIATE | 中级影响者 | 正在成长 |
| 0-19 | NOVICE | 新手影响者 | 初级影响力 |

---

## 🔧 配置参数

### 可调整的算法参数

在 `UserInfluenceServiceImpl.java` 中：

```java
/** PageRank 阻尼系数 */
private static final double DAMPING_FACTOR = 0.85;

/** PageRank 最大迭代次数 */
private static final int MAX_ITERATIONS = 20;

/** PageRank 收敛阈值 */
private static final double CONVERGENCE_THRESHOLD = 0.0001;

/** 统计天数（最近30天的数据） */
private static final int STATS_DAYS = 30;
```

---

## ⚠️ 注意事项

1. **首次使用**
   - 必须先手动触发一次计算: `POST /api/influence/recalculate`
   - 首次计算可能耗时较长（取决于用户数量）

2. **性能考虑**
   - PageRank 算法的时间复杂度为 O(n²)，用户数量过多时会较慢
   - 建议只计算最近 30 天活跃的用户
   - 定时任务安排在凌晨低峰期执行

3. **数据要求**
   - 用户至少需要发过 1 条帖子才会被计算影响力
   - 影响力评分基于最近 30 天的数据

4. **定时任务**
   - 定时任务在应用启动时自动启用（`@EnableScheduling`）
   - Cron 表达式可在 Service 类中修改

---

## 📈 后续优化建议

1. **性能优化**
   - 对大量用户使用批量计算 + 异步任务
   - 缓存计算结果，减少数据库查询

2. **算法优化**
   - 引入时间衰减因子（越久的互动权重越低）
   - 考虑互动类型权重（点赞 < 评论 < 深度讨论）

3. **可视化**
   - 前端实现影响力趋势图
   - 影响力雷达图（多维度展示）

4. **扩展功能**
   - 影响力徽章系统
   - 影响力排名变化通知
   - 影响力分析报告

---

## ✅ 实现完成度

- [x] Service 层接口定义
- [x] Service 层核心算法实现
- [x] Controller 层 REST API
- [x] 数据库表结构
- [x] 定时任务配置
- [x] VO 对象完善
- [x] API 文档注解
- [x] 实现说明文档

**总代码量:** 约 1000+ 行
**实现日期:** 2024-03-15
**开发团队:** EmotionHub Team
