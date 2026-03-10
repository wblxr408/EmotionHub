# EmotionHub 情感社交媒体分析平台 - 详细设计方案

## 项目概述

基于用户选择的需求：

- **核心功能**: 社交媒体情感分析（用户发帖，AI分析情感）
- **LLM集成**: 多个API集成（OpenAI、通义千问、文心一言、智谱AI）
- **前端技术**: Vue 3 + TypeScript
- **项目规模**: 多用户系统 + 角色权限管理 + 数据可视化 + 实时通知

## 阶段一：详细模块设计

### 模块1：用户认证与权限管理模块

**用户故事**:

- 作为新用户，我希望能够注册账号，以便使用系统功能
- 作为已注册用户，我希望能够登录系统s，访问我的个人数据
- 作为管理员，我希望能够管理用户状态，维护系统秩序

**核心功能**:

1. 用户注册（邮箱验证可选）
2. 用户登录（JWT Token认证）
3. 用户信息管理（头像、昵称、个人简介）
4. 角色权限控制（USER、ADMIN）
5. 密码加密存储（BCrypt）

**技术实现**:

- Spring Security 6 + JWT
- Redis存储Token（支持登出）
- 自定义注解实现权限控制

**关键文件**:

- `SecurityConfig.java` - 安全配置
- `JwtTokenProvider.java` - Token生成和验证
- `UserController.java` - 用户接口
- `UserService.java` - 用户业务逻辑

---

### 模块2：帖子管理模块（核心CRUD）

**用户故事**:

- 作为用户，我希望能够发布帖子，分享我的心情和想法
- 作为用户，我希望能够浏览其他人的帖子，了解大家的情感状态
- 作为用户，我希望能够编辑或删除我自己的帖子
- 作为用户，我希望能够给喜欢的帖子点赞

**核心功能**:

1. 创建帖子（文本 + 可选图片）
2. 查看帖子列表（分页、排序）
3. 查看帖子详情
4. 编辑帖子（仅作者）
5. 删除帖子（软删除）
6. 点赞/取消点赞
7. 帖子搜索（按关键词、情感标签）

**业务流程**:

```
用户发布帖子
  ↓
保存到数据库（状态：ANALYZING）
  ↓
触发异步情感分析任务
  ↓
分析完成后更新帖子状态（PUBLISHED）
  ↓
通过WebSocket推送分析结果给用户
```

**技术实现**:

- MyBatis-Plus实现CRUD
- 使用@Async异步处理情感分析
- 图片上传到本地/OSS
- 分页使用PageHelper

**关键文件**:

- `Post.java` - 帖子实体
- `PostController.java` - 帖子接口
- `PostService.java` - 帖子业务逻辑
- `PostMapper.java` - 数据访问层

---

### 模块3：评论系统模块

**用户故事**:

- 作为用户，我希望能够评论帖子，与作者互动
- 作为用户，我希望能够回复其他人的评论，进行深入讨论
- 作为用户，我希望能够看到评论的情感分析结果

**核心功能**:

1. 发表评论（支持嵌套回复）
2. 查看评论列表（树形结构）
3. 删除评论（仅作者或管理员）
4. 评论点赞
5. 评论情感分析（可选）

**技术实现**:

- 使用parent_id实现评论嵌套
- 递归查询构建评论树
- 评论也可触发情感分析

**关键文件**:

- `Comment.java` - 评论实体
- `CommentController.java` - 评论接口
- `CommentService.java` - 评论业务逻辑

---

### 模块4：LLM情感分析引擎

**用户故事**:

- 作为用户，我希望系统能自动分析我的帖子情感，了解我的情绪状态
- 作为用户，我希望看到详细的情感分析报告（分数、标签、关键词）
- 作为系统，我希望在某个LLM服务不可用时自动切换到备用服务

**核心功能**:

1. 多LLM提供商适配器

   - OpenAI GPT-4/3.5
   - 通义千问（阿里云）
   - 文心一言（百度）
   - 智谱AI GLM-4
2. 智能调度策略

   - 负载均衡（轮询/随机）
   - 自动降级（主服务失败切换备用）
   - 限流控制（防止API超额）
   - 结果缓存（相同内容24小时缓存）
3. 情感分析结果

   - 情感分数：-1（消极）到 1（积极）
   - 情感标签：POSITIVE/NEUTRAL/NEGATIVE
   - 关键词提取：3-5个情感关键词
   - 简要分析：50字情感说明

**技术实现**:

```java
// 策略模式 + 工厂模式
public interface LLMService {
    EmotionAnalysisResult analyze(String content);
    String getProviderName();
    boolean isAvailable();
}

// 实现类
@Service("openai")
public class OpenAIService implements LLMService { }

@Service("qianwen")
public class QianWenService implements LLMService { }

// 调度器
@Service
public class LLMScheduler {
    private List<LLMService> services;
    private LoadBalancer loadBalancer;

    public EmotionAnalysisResult analyze(String content) {
        // 1. 检查缓存
        // 2. 选择可用服务
        // 3. 调用分析
        // 4. 降级处理
        // 5. 缓存结果
    }
}
```

**Prompt工程**:

```
系统角色：你是一个专业的情感分析专家，擅长识别文本中的情感倾向。

任务：分析以下中文文本的情感，返回JSON格式结果。

文本：{用户输入}

要求：
1. 情感分数：-1.0（极度消极）到 1.0（极度积极），保留2位小数
2. 情感标签：POSITIVE（积极）、NEUTRAL（中性）、NEGATIVE（消极）
3. 关键词：提取3-5个最能体现情感的词语
4. 分析说明：用50字以内简要说明情感特征

返回JSON格式：
{
  "score": 0.75,
  "label": "POSITIVE",
  "keywords": ["开心", "满足", "期待"],
  "analysis": "文本整体表达积极乐观的情绪，充满对未来的期待"
}
```

**关键文件**:

- `LLMService.java` - LLM服务接口
- `OpenAIService.java` - OpenAI实现
- `QianWenService.java` - 通义千问实现
- `WenXinService.java` - 文心一言实现
- `ZhiPuService.java` - 智谱AI实现
- `LLMScheduler.java` - 调度器
- `EmotionAnalysisService.java` - 情感分析服务
- `EmotionAnalysisResult.java` - 分析结果VO

---

### 模块5：情感统计与可视化模块

**用户故事**:

- 作为用户，我希望看到我的情感趋势图，了解自己的情绪变化
- 作为用户，我希望看到情感分布饼图，知道自己的情绪构成
- 作为用户，我希望看到情感日历，回顾每天的情绪状态
- 作为用户，我希望看到我的情感关键词云，了解常用词汇

**核心功能**:

1. 每日情感统计

   - 积极/中性/消极帖子数量
   - 平均情感分数
   - 总帖子数
2. 情感趋势分析

   - 最近7天/30天趋势折线图
   - 情感波动分析
3. 情感分布

   - 饼图展示情感占比
   - 柱状图对比不同时期
4. 情感日历热力图

   - 类似GitHub贡献图
   - 颜色深浅表示情感强度
5. 关键词词云

   - 提取高频情感关键词
   - 词云可视化展示

**技术实现**:

- 定时任务（每日凌晨统计）
- Redis缓存统计结果
- 前端使用ECharts渲染图表

**关键文件**:

- `EmotionStatisticsService.java` - 统计服务
- `EmotionStatisticsTask.java` - 定时任务
- `StatisticsController.java` - 统计接口

---

### 模块6：实时通知系统

**用户故事**:

- 作为用户，我希望有人点赞我的帖子时收到实时通知
- 作为用户，我希望有人评论我的帖子时收到实时通知
- 作为用户，我希望情感分析完成时收到实时通知
- 作为用户，我希望能查看历史通知记录

**核心功能**:

1. 实时通知推送（WebSocket）

   - 点赞通知
   - 评论通知
   - 系统通知
   - 情感分析完成通知
2. 通知管理

   - 通知列表（分页）
   - 标记已读/未读
   - 批量标记已读
   - 未读数量统计

**技术实现**:

- Spring WebSocket
- STOMP协议
- Redis Pub/Sub（多实例支持）

**关键文件**:

- `WebSocketConfig.java` - WebSocket配置
- `NotificationWebSocketHandler.java` - 消息处理
- `NotificationService.java` - 通知服务
- `NotificationController.java` - 通知接口

---

### 模块7：管理后台模块

**用户故事**:

- 作为管理员，我希望能查看所有用户列表，管理用户状态
- 作为管理员，我希望能查看所有帖子，删除违规内容
- 作为管理员，我希望能看到系统统计数据，了解平台运营情况

**核心功能**:

1. 用户管理

   - 用户列表（搜索、分页）
   - 修改用户状态（激活/封禁）
   - 查看用户详情
2. 内容管理

   - 帖子列表（搜索、筛选）
   - 删除违规帖子
   - 查看举报记录
3. 系统统计

   - 用户总数、活跃用户
   - 帖子总数、今日新增
   - 情感分布统计
   - LLM调用统计

**关键文件**:

- `AdminController.java` - 管理接口
- `AdminService.java` - 管理业务逻辑

---

## 前端模块设计

### 页面1：登录注册页面

**路由**: `/login`, `/register`
**组件**: `Login.vue`, `Register.vue`
**功能**:

- 表单验证
- 登录/注册请求
- Token存储
- 自动跳转

### 页面2：首页（帖子流）

**路由**: `/home`
**组件**: `Home.vue`
**子组件**:

- `PostEditor.vue` - 发帖编辑器
- `PostCard.vue` - 帖子卡片
- `EmotionTag.vue` - 情感标签

**功能**:

- 发布新帖子
- 浏览帖子列表（无限滚动）
- 点赞/评论
- 查看情感标签

### 页面3：帖子详情页

**路由**: `/post/:id`
**组件**: `PostDetail.vue`
**子组件**:

- `EmotionAnalysis.vue` - 情感分析结果展示
- `CommentList.vue` - 评论列表
- `CommentItem.vue` - 评论项（支持嵌套）

**功能**:

- 查看完整帖子
- 查看详细情感分析
- 发表评论
- 嵌套回复

### 页面4：个人中心

**路由**: `/profile`
**组件**: `Profile.vue`
**子组件**:

- `UserInfo.vue` - 用户信息编辑
- `MyPosts.vue` - 我的帖子
- `EmotionDashboard.vue` - 情感仪表盘

**功能**:

- 编辑个人信息
- 查看我的帖子
- 查看情感统计图表

### 页面5：情感统计页

**路由**: `/statistics`
**组件**: `Statistics.vue`
**子组件**:

- `TrendChart.vue` - 趋势折线图
- `DistributionChart.vue` - 分布饼图
- `CalendarHeatmap.vue` - 日历热力图
- `WordCloud.vue` - 词云图

**功能**:

- ECharts图表展示
- 时间范围选择
- 数据导出

### 页面6：管理后台

**路由**: `/admin`
**组件**: `Admin.vue`
**子组件**:

- `UserManagement.vue` - 用户管理
- `PostManagement.vue` - 内容管理
- `SystemStats.vue` - 系统统计

**功能**:

- 管理员权限验证
- 数据表格展示
- 操作确认弹窗

---

## 数据库设计细节

### 表关系图

```
user (用户表)
  ├─ 1:N → post (帖子表)
  ├─ 1:N → comment (评论表)
  ├─ 1:N → like_record (点赞表)
  ├─ 1:N → notification (通知表)
  └─ 1:N → user_emotion_stats (情感统计表)

post (帖子表)
  ├─ 1:N → comment (评论表)
  ├─ 1:N → like_record (点赞表)
  └─ 1:1 → emotion_analysis (情感分析表)

comment (评论表)
  ├─ 1:N → comment (自关联，嵌套评论)
  └─ 1:N → like_record (点赞表)
```

---

## 关键技术实现

### 1. 异步情感分析

```java
@Async("emotionAnalysisExecutor")
public CompletableFuture<EmotionAnalysisResult> analyzeAsync(Long postId, String content) {
    // 1. 调用LLM分析
    EmotionAnalysisResult result = llmScheduler.analyze(content);

    // 2. 更新帖子情感数据
    postService.updateEmotion(postId, result);

    // 3. 更新用户统计
    statisticsService.updateUserStats(userId);

    // 4. 发送WebSocket通知
    notificationService.sendAnalysisComplete(userId, postId, result);

    return CompletableFuture.completedFuture(result);
}
```

### 2. LLM降级策略

```java
public EmotionAnalysisResult analyzeWithFallback(String content) {
    List<LLMService> availableServices = getAvailableServices();

    for (LLMService service : availableServices) {
        try {
            return service.analyze(content);
        } catch (Exception e) {
            log.warn("LLM service {} failed, trying next", service.getProviderName());
        }
    }

    // 所有服务都失败，返回默认结果
    return getDefaultResult(content);
}
```

### 3. WebSocket通知推送

```java
@MessageMapping("/notification")
public void handleNotification(NotificationMessage message) {
    simpMessagingTemplate.convertAndSendToUser(
        message.getUserId().toString(),
        "/queue/notifications",
        message
    );
}
```

---

## 验证计划

### 后端验证

1. 使用Swagger测试所有API接口
2. 单元测试覆盖核心业务逻辑
3. 集成测试验证LLM调用
4. 压力测试验证并发性能

### 前端验证

1. 手动测试所有页面功能
2. 测试WebSocket实时通知
3. 测试图表渲染效果
4. 测试响应式布局

### 端到端测试

1. 注册 → 登录 → 发帖 → 查看分析 → 评论 → 点赞
2. 查看统计图表
3. 管理员操作
4. 多用户并发测试

---

## 关键文件清单

### 后端核心文件

- 安全配置
- JWT工具
- 帖子服务
- LLM调度器
- 情感分析服务
- WebSocket配置
- 通知服务

### 前端核心文件

- 路由配置
- 用户状态
- 帖子API
- 帖子卡片
- 统计页面
- WebSocket封装

---

## 完整用户故事地图（User Story Mapping）

### Epic 1: 用户身份管理

- Story 1.1: 用户注册（验收标准：用户名唯一、邮箱验证、密码强度）
- Story 1.2: 用户登录（验收标准：JWT Token、7天有效期）
- Story 1.3: 个人信息管理（验收标准：修改昵称/头像/简介）

### Epic 2: 内容创作与互动

- Story 2.1: 发布帖子（验收标准：1-2000字、1-9张图片、异步分析）
- Story 2.2: 浏览帖子流（验收标准：时间倒序、无限滚动、情感标签）
- Story 2.3: 查看帖子详情（验收标准：完整内容、情感分析、评论列表）
- Story 2.4: 点赞帖子（验收标准：点赞/取消、实时更新、通知作者）
- Story 2.5: 评论帖子（验收标准：1-500字、嵌套3层、情感分析）

### Epic 3: 情感分析与洞察

- Story 3.1: 自动情感分析（验收标准：3-10秒完成、分数/标签/关键词）
- Story 3.2: 查看情感趋势（验收标准：7天/30天折线图、最高/最低点）
- Story 3.3: 查看情感分布（验收标准：饼图、点击查看对应帖子）
- Story 3.4: 情感日历（验收标准：GitHub风格热力图、悬停显示详情）
- Story 3.5: 关键词词云（验收标准：高频词、颜色表示情感、点击筛选）

### Epic 4: 实时通知与互动

- Story 4.1: 实时通知推送（验收标准：点赞/评论/分析完成/系统通知）
- Story 4.2: 通知列表（验收标准：时间倒序、已读/未读、分页）
- Story 4.3: 通知管理（验收标准：单个/批量标记已读）

### Epic 5: 管理与监控

- Story 5.1: 用户管理（验收标准：搜索/分页、封禁/解封）
- Story 5.2: 内容审核（验收标准：筛选/搜索、删除违规）
- Story 5.3: 系统统计（验收标准：用户数/帖子数/情感分布/LLM调用）

---

## 详细项目结构

### 后端项目结构（Maven多模块）

```
emotion-hub-backend/
├── pom.xml                          # 父POM（Spring Boot 3.2.x）
│
├── emotion-hub-common/              # 公共模块
│   ├── pom.xml
│   └── src/main/java/com/emotionhub/common/
│       ├── constants/               # 常量
│       │   ├── EmotionLabel.java    # POSITIVE/NEUTRAL/NEGATIVE
│       │   ├── UserRole.java        # USER/ADMIN
│       │   ├── PostStatus.java      # ANALYZING/PUBLISHED/DELETED
│       │   └── ApiConstants.java
│       ├── exception/               # 异常处理
│       │   ├── BusinessException.java
│       │   ├── UnauthorizedException.java
│       │   ├── ResourceNotFoundException.java
│       │   └── GlobalExceptionHandler.java
│       ├── utils/                   # 工具类
│       │   ├── JwtUtil.java
│       │   ├── RedisUtil.java
│       │   ├── DateUtil.java
│       │   └── PasswordUtil.java
│       └── response/                # 统一响应
│           ├── Result.java          # {code, message, data}
│           └── PageResult.java
│
├── emotion-hub-model/               # 数据模型
│   ├── pom.xml
│   └── src/main/java/com/emotionhub/model/
│       ├── entity/                  # 实体类（对应数据库表）
│       │   ├── User.java
│       │   ├── Post.java
│       │   ├── Comment.java
│       │   ├── LikeRecord.java
│       │   ├── EmotionAnalysis.java
│       │   ├── UserEmotionStats.java
│       │   └── Notification.java
│       ├── dto/                     # 数据传输对象（请求参数）
│       │   ├── UserRegisterDTO.java
│       │   ├── UserLoginDTO.java
│       │   ├── PostCreateDTO.java
│       │   ├── PostUpdateDTO.java
│       │   ├── CommentCreateDTO.java
│       │   └── EmotionQueryDTO.java
│       └── vo/                      # 视图对象（响应数据）
│           ├── UserVO.java
│           ├── PostVO.java
│           ├── CommentVO.java
│           ├── EmotionAnalysisVO.java
│           ├── EmotionStatsVO.java
│           └── NotificationVO.java
│
├── emotion-hub-dao/                 # 数据访问层
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/emotionhub/dao/
│       │   └── mapper/
│       │       ├── UserMapper.java
│       │       ├── PostMapper.java
│       │       ├── CommentMapper.java
│       │       ├── LikeRecordMapper.java
│       │       ├── EmotionAnalysisMapper.java
│       │       ├── UserEmotionStatsMapper.java
│       │       └── NotificationMapper.java
│       └── resources/mapper/        # MyBatis XML
│           ├── UserMapper.xml
│           ├── PostMapper.xml
│           └── ...
│
├── emotion-hub-service/             # 业务服务层
│   ├── pom.xml
│   └── src/main/java/com/emotionhub/service/
│       ├── user/
│       │   ├── UserService.java
│       │   └── impl/UserServiceImpl.java
│       ├── post/
│       │   ├── PostService.java
│       │   └── impl/PostServiceImpl.java
│       ├── comment/
│       │   ├── CommentService.java
│       │   └── impl/CommentServiceImpl.java
│       ├── emotion/
│       │   ├── EmotionAnalysisService.java
│       │   └── impl/EmotionAnalysisServiceImpl.java
│       ├── llm/                     # LLM集成（核心亮点）
│       │   ├── LLMService.java      # 接口
│       │   ├── LLMScheduler.java    # 调度器
│       │   ├── impl/
│       │   │   ├── OpenAIService.java
│       │   │   ├── QianWenService.java
│       │   │   ├── WenXinService.java
│       │   │   └── ZhiPuService.java
│       │   └── strategy/
│       │       ├── LoadBalancer.java      # 负载均衡
│       │       └── FallbackStrategy.java  # 降级策略
│       ├── notification/
│       │   ├── NotificationService.java
│       │   └── impl/NotificationServiceImpl.java
│       └── statistics/
│           ├── StatisticsService.java
│           └── impl/StatisticsServiceImpl.java
│
└── emotion-hub-web/                 # Web层（启动模块）
    ├── pom.xml
    └── src/main/
        ├── java/com/emotionhub/web/
        │   ├── EmotionHubApplication.java  # 启动类
        │   ├── controller/
        │   │   ├── UserController.java
        │   │   ├── PostController.java
        │   │   ├── CommentController.java
        │   │   ├── EmotionController.java
        │   │   ├── NotificationController.java
        │   │   └── AdminController.java
        │   ├── config/
        │   │   ├── SecurityConfig.java      # Spring Security
        │   │   ├── RedisConfig.java
        │   │   ├── WebSocketConfig.java
        │   │   ├── AsyncConfig.java         # 异步线程池
        │   │   ├── SwaggerConfig.java       # Knife4j
        │   │   └── CorsConfig.java
        │   ├── interceptor/
        │   │   ├── JwtInterceptor.java
        │   │   └── RateLimitInterceptor.java
        │   ├── filter/
        │   │   └── RequestLogFilter.java
        │   ├── websocket/
        │   │   └── NotificationWebSocketHandler.java
        │   └── task/
        │       └── EmotionStatisticsTask.java  # 定时任务
        └── resources/
            ├── application.yml
            ├── application-dev.yml
            ├── application-prod.yml
            └── logback-spring.xml
```

### 前端项目结构（Vue 3 + TypeScript）

```
emotion-hub-frontend/
├── public/
│   ├── favicon.ico
│   └── logo.png
│
├── src/
│   ├── api/                         # API接口封装
│   │   ├── request.ts               # Axios实例配置
│   │   ├── user.ts                  # 用户API
│   │   ├── post.ts                  # 帖子API
│   │   ├── comment.ts               # 评论API
│   │   ├── emotion.ts               # 情感分析API
│   │   ├── notification.ts          # 通知API
│   │   └── admin.ts                 # 管理API
│   │
│   ├── assets/                      # 静态资源
│   │   ├── images/
│   │   │   ├── logo.png
│   │   │   ├── default-avatar.png
│   │   │   └── emotion-icons/
│   │   │       ├── positive.svg
│   │   │       ├── neutral.svg
│   │   │       └── negative.svg
│   │   └── styles/
│   │       ├── variables.scss       # SCSS变量
│   │       ├── global.scss          # 全局样式
│   │       └── emotion-colors.scss  # 情感颜色主题
│   │
│   ├── components/                  # 公共组件
│   │   ├── layout/
│   │   │   ├── Header.vue           # 顶部导航
│   │   │   ├── Sidebar.vue          # 侧边栏
│   │   │   └── Footer.vue
│   │   ├── post/
│   │   │   ├── PostCard.vue         # 帖子卡片
│   │   │   ├── PostEditor.vue       # 发帖编辑器
│   │   │   └── PostList.vue         # 帖子列表
│   │   ├── comment/
│   │   │   ├── CommentList.vue      # 评论列表
│   │   │   ├── CommentItem.vue      # 评论项（递归）
│   │   │   └── CommentEditor.vue    # 评论编辑器
│   │   ├── emotion/
│   │   │   ├── EmotionTag.vue       # 情感标签
│   │   │   ├── EmotionScore.vue     # 情感分数进度条
│   │   │   ├── EmotionAnalysis.vue  # 情感分析详情
│   │   │   └── EmotionChart.vue     # 情感图表基础组件
│   │   ├── notification/
│   │   │   ├── NotificationBell.vue # 通知铃铛
│   │   │   └── NotificationItem.vue # 通知项
│   │   └── common/
│   │       ├── Loading.vue
│   │       ├── Empty.vue
│   │       └── ImageUpload.vue
│   │
│   ├── views/                       # 页面视图
│   │   ├── auth/
│   │   │   ├── Login.vue
│   │   │   └── Register.vue
│   │   ├── home/
│   │   │   └── Home.vue             # 首页（帖子流）
│   │   ├── post/
│   │   │   └── PostDetail.vue       # 帖子详情
│   │   ├── profile/
│   │   │   ├── Profile.vue          # 个人中心
│   │   │   ├── MyPosts.vue          # 我的帖子
│   │   │   └── Settings.vue         # 设置
│   │   ├── statistics/
│   │   │   ├── Statistics.vue       # 情感统计主页
│   │   │   ├── TrendChart.vue       # 趋势折线图
│   │   │   ├── DistributionChart.vue # 分布饼图
│   │   │   ├── CalendarHeatmap.vue  # 日历热力图
│   │   │   └── WordCloud.vue        # 词云图
│   │   ├── notification/
│   │   │   └── NotificationList.vue # 通知列表
│   │   └── admin/
│   │       ├── Admin.vue            # 管理后台
│   │       ├── UserManagement.vue   # 用户管理
│   │       ├── PostManagement.vue   # 内容管理
│   │       └── SystemStats.vue      # 系统统计
│   │
│   ├── stores/                      # Pinia状态管理
│   │   ├── user.ts                  # 用户状态（登录信息、Token）
│   │   ├── post.ts                  # 帖子状态
│   │   ├── notification.ts          # 通知状态（未读数量）
│   │   └── app.ts                   # 应用状态（主题、语言）
│   │
│   ├── router/                      # 路由配置
│   │   ├── index.ts                 # 路由主文件
│   │   └── guards.ts                # 路由守卫（认证、权限）
│   │
│   ├── utils/                       # 工具函数
│   │   ├── request.ts               # HTTP请求封装
│   │   ├── websocket.ts             # WebSocket封装
│   │   ├── emotion.ts               # 情感工具（颜色映射等）
│   │   ├── date.ts                  # 日期格式化
│   │   ├── storage.ts               # LocalStorage封装
│   │   └── validator.ts             # 表单验证规则
│   │
│   ├── types/                       # TypeScript类型定义
│   │   ├── user.ts
│   │   ├── post.ts
│   │   ├── comment.ts
│   │   ├── emotion.ts
│   │   ├── notification.ts
│   │   └── api.ts                   # API响应类型
│   │
│   ├── composables/                 # 组合式函数（Vue 3）
│   │   ├── useAuth.ts               # 认证逻辑
│   │   ├── usePost.ts               # 帖子逻辑
│   │   ├── useNotification.ts       # 通知逻辑
│   │   └── useWebSocket.ts          # WebSocket逻辑
│   │
│   ├── App.vue                      # 根组件
│   └── main.ts                      # 入口文件
│
├── .env.development                 # 开发环境变量
├── .env.production                  # 生产环境变量
├── .eslintrc.js                     # ESLint配置
├── .prettierrc.js                   # Prettier配置
├── tsconfig.json                    # TypeScript配置
├── vite.config.ts                   # Vite配置
└── package.json                     # 项目依赖
```

---

## 实施计划（10周开发周期。Maybe）

### 第1周：项目初始化

**目标**: 搭建基础框架

- [ ] 创建后端Maven多模块项目
- [ ] 创建前端Vue 3 + Vite项目
- [ ] 初始化MySQL数据库和表结构
- [ ] 配置Redis
- [ ] 配置开发环境

### 第2周：用户认证模块

**目标**: 实现用户注册登录

- [ ] 后端：用户注册/登录API
- [ ] 后端：JWT Token生成和验证
- [ ] 后端：Spring Security配置
- [ ] 前端：登录注册页面
- [ ] 前端：Token存储和路由守卫

### 第3-4周：帖子CRUD模块

**目标**: 实现帖子核心功能

- [ ] 后端：帖子CRUD API
- [ ] 后端：点赞功能
- [ ] 后端：评论功能（含嵌套）
- [ ] 后端：图片上传
- [ ] 前端：首页帖子流
- [ ] 前端：发帖编辑器
- [ ] 前端：帖子详情页
- [ ] 前端：评论组件

### 第5-6周：LLM情感分析（核心）

**目标**: 集成多个LLM服务

- [ ] 后端：LLM服务接口设计
- [ ] 后端：OpenAI集成
- [ ] 后端：通义千问集成
- [ ] 后端：文心一言集成
- [ ] 后端：智谱AI集成
- [ ] 后端：LLM调度器（负载均衡、降级）
- [ ] 后端：异步分析任务
- [ ] 后端：结果缓存
- [ ] 前端：情感分析结果展示

### 第7周：情感统计可视化

**目标**: 实现数据统计和图表

- [ ] 后端：情感统计API
- [ ] 后端：定时任务统计
- [ ] 前端：ECharts集成
- [ ] 前端：趋势折线图
- [ ] 前端：分布饼图
- [ ] 前端：日历热力图
- [ ] 前端：词云图

### 第8周：实时通知系统

**目标**: 实现WebSocket通知

- [ ] 后端：WebSocket服务端
- [ ] 后端：通知CRUD API
- [ ] 后端：Redis Pub/Sub
- [ ] 前端：WebSocket客户端
- [ ] 前端：通知铃铛组件
- [ ] 前端：通知列表页面

### 第9周：管理后台

**目标**: 实现管理功能

- [ ] 后端：管理员权限控制
- [ ] 后端：用户管理API
- [ ] 后端：内容管理API
- [ ] 后端：系统统计API
- [ ] 前端：管理后台页面
- [ ] 前端：用户管理
- [ ] 前端：内容管理
- [ ] 前端：系统统计

### 第10周：测试与部署

**目标**: 完善和上线

- [ ] 单元测试
- [ ] 集成测试
- [ ] 性能优化
- [ ] 安全加固
- [ ] Docker部署配置
- [ ] 文档完善
- [ ] 项目演示