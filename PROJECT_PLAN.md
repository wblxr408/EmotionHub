# 情感社交媒体分析平台 - 项目规划文档

## 项目概述

**项目名称**: EmotionHub - 情感社交媒体分析平台
**项目类型**: 全栈Web应用（练手项目）
**技术栈**: Java后端 + Vue 3前端 + LLM集成
**核心功能**: 用户发表动态/帖子，系统分析情感倾向，提供情绪统计和趋势分析

---

## 技术架构

### 后端技术栈（Java）

#### 核心框架
- **Spring Boot 3.2+** - 主框架
- **Spring Security 6** - 安全认证（JWT）
- **Spring Data JPA** - 数据持久化
- **MyBatis-Plus 3.5+** - 增强型ORM（可选，与JPA二选一）
- **Spring WebSocket** - 实时通知

#### 数据库
- **MySQL 8.0+** - 主数据库
- **Redis 7.0+** - 缓存 + Session存储

#### 工具库
- **Lombok** - 简化代码
- **Hutool** - Java工具类库
- **Knife4j/Swagger** - API文档
- **Caffeine** - 本地缓存

#### LLM集成
- **OkHttp/RestTemplate** - HTTP客户端
- **Jackson** - JSON处理
- 支持多个LLM API:
  - OpenAI GPT-4/3.5
  - 通义千问（阿里云）
  - 文心一言（百度）
  - 智谱AI（GLM-4）

### 前端技术栈（Vue 3）

- **Vue 3.4+** - 核心框架
- **TypeScript 5+** - 类型支持
- **Vite 5+** - 构建工具
- **Pinia** - 状态管理
- **Vue Router 4** - 路由管理
- **Element Plus** - UI组件库
- **ECharts 5** - 数据可视化
- **Axios** - HTTP客户端
- **Socket.io-client** - WebSocket客户端

---

## 数据库设计

### 核心表结构

#### 1. 用户表 (user)
```sql
- id (主键)
- username (用户名，唯一)
- password (加密密码)
- email (邮箱)
- nickname (昵称)
- avatar (头像URL)
- role (角色: USER/ADMIN)
- status (状态: ACTIVE/BANNED)
- created_at, updated_at
```

#### 2. 帖子表 (post)
```sql
- id (主键)
- user_id (外键)
- content (内容)
- images (图片URLs，JSON)
- emotion_score (情感分数: -1到1)
- emotion_label (情感标签: POSITIVE/NEUTRAL/NEGATIVE)
- llm_analysis (LLM分析结果，JSON)
- view_count (浏览数)
- like_count (点赞数)
- comment_count (评论数)
- status (状态: PUBLISHED/DELETED)
- created_at, updated_at
```

#### 3. 评论表 (comment)
```sql
- id (主键)
- post_id (外键)
- user_id (外键)
- parent_id (父评论ID，支持嵌套)
- content (内容)
- emotion_score (情感分数)
- emotion_label (情感标签)
- like_count (点赞数)
- created_at, updated_at
```

#### 4. 点赞表 (like_record)
```sql
- id (主键)
- user_id (外键)
- target_id (目标ID：帖子或评论)
- target_type (类型: POST/COMMENT)
- created_at
- 唯一索引: (user_id, target_id, target_type)
```

#### 5. 情感分析记录表 (emotion_analysis)
```sql
- id (主键)
- user_id (外键)
- content_type (类型: POST/COMMENT)
- content_id (内容ID)
- llm_provider (LLM提供商)
- request_data (请求数据，JSON)
- response_data (响应数据，JSON)
- emotion_score (情感分数)
- emotion_label (情感标签)
- keywords (关键词，JSON)
- analysis_time (分析耗时ms)
- created_at
```

#### 6. 用户情感统计表 (user_emotion_stats)
```sql
- id (主键)
- user_id (外键)
- date (统计日期)
- positive_count (积极情绪数)
- neutral_count (中性情绪数)
- negative_count (消极情绪数)
- avg_emotion_score (平均情感分数)
- total_posts (总帖子数)
- created_at, updated_at
- 唯一索引: (user_id, date)
```

#### 7. 通知表 (notification)
```sql
- id (主键)
- user_id (接收者ID)
- type (类型: LIKE/COMMENT/SYSTEM)
- title (标题)
- content (内容)
- related_id (关联ID)
- is_read (是否已读)
- created_at
```

---

## 系统架构设计

### 后端模块划分

```
emotion-hub-backend/
├── emotion-hub-common/          # 公共模块
│   ├── constants/               # 常量定义
│   ├── enums/                   # 枚举类
│   ├── exception/               # 自定义异常
│   ├── utils/                   # 工具类
│   └── vo/                      # 通用VO
│
├── emotion-hub-model/           # 数据模型模块
│   ├── entity/                  # 实体类
│   ├── dto/                     # 数据传输对象
│   └── vo/                      # 视图对象
│
├── emotion-hub-service/         # 业务服务模块
│   ├── user/                    # 用户服务
│   ├── post/                    # 帖子服务
│   ├── comment/                 # 评论服务
│   ├── emotion/                 # 情感分析服务
│   ├── llm/                     # LLM集成服务
│   ├── notification/            # 通知服务
│   └── statistics/              # 统计服务
│
├── emotion-hub-web/             # Web接口模块
│   ├── controller/              # 控制器
│   ├── interceptor/             # 拦截器
│   ├── filter/                  # 过滤器
│   ├── config/                  # 配置类
│   └── websocket/               # WebSocket处理
│
└── emotion-hub-admin/           # 管理后台模块（可选）
    └── controller/              # 管理接口
```

### 核心业务流程

#### 1. 用户发帖流程
```
用户提交帖子
  → 参数校验
  → 保存帖子（状态：待分析）
  → 异步调用LLM分析
  → 更新帖子情感数据
  → 更新用户统计
  → WebSocket推送结果
```

#### 2. LLM情感分析流程
```
接收文本内容
  → 选择LLM提供商（负载均衡/降级策略）
  → 构建Prompt（情感分析专用）
  → 调用LLM API
  → 解析响应（情感分数、标签、关键词）
  → 记录分析日志
  → 返回结果
```

#### 3. 情感统计流程
```
定时任务（每日凌晨）
  → 统计用户昨日情感数据
  → 计算情感分布
  → 生成趋势报告
  → 存入统计表
```

---

## API接口设计

### 用户模块 (/api/user)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /register | 用户注册 | 公开 |
| POST | /login | 用户登录 | 公开 |
| POST | /logout | 用户登出 | 认证 |
| GET | /profile | 获取个人信息 | 认证 |
| PUT | /profile | 更新个人信息 | 认证 |
| GET | /stats | 获取情感统计 | 认证 |

### 帖子模块 (/api/post)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | / | 创建帖子 | 认证 |
| GET | /{id} | 获取帖子详情 | 公开 |
| PUT | /{id} | 更新帖子 | 认证+所有者 |
| DELETE | /{id} | 删除帖子 | 认证+所有者 |
| GET | /list | 分页查询帖子 | 公开 |
| GET | /my | 我的帖子列表 | 认证 |
| POST | /{id}/like | 点赞/取消点赞 | 认证 |

### 评论模块 (/api/comment)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | / | 创建评论 | 认证 |
| GET | /post/{postId} | 获取帖子评论 | 公开 |
| DELETE | /{id} | 删除评论 | 认证+所有者 |
| POST | /{id}/like | 点赞评论 | 认证 |

### 情感分析模块 (/api/emotion)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /analyze | 实时情感分析 | 认证 |
| GET | /stats/daily | 每日情感统计 | 认证 |
| GET | /stats/trend | 情感趋势分析 | 认证 |
| GET | /wordcloud | 情感关键词云 | 认证 |

### 通知模块 (/api/notification)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /list | 通知列表 | 认证 |
| PUT | /{id}/read | 标记已读 | 认证 |
| PUT | /read-all | 全部标记已读 | 认证 |
| GET | /unread-count | 未读数量 | 认证 |

### 管理模块 (/api/admin)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /users | 用户列表 | 管理员 |
| PUT | /user/{id}/status | 修改用户状态 | 管理员 |
| GET | /posts | 帖子管理 | 管理员 |
| DELETE | /post/{id} | 删除帖子 | 管理员 |
| GET | /statistics | 系统统计 | 管理员 |

---

## LLM集成方案

### 多LLM提供商适配器模式

```java
// LLM服务接口
public interface LLMService {
    EmotionAnalysisResult analyze(String content);
    String getProviderName();
    boolean isAvailable();
}

// 实现类
- OpenAIService
- QianWenService (通义千问)
- WenXinService (文心一言)
- ZhiPuService (智谱AI)
```

### LLM调用策略

1. **负载均衡**: 轮询/随机选择可用的LLM服务
2. **降级策略**: 主服务失败时自动切换备用服务
3. **限流控制**: 使用Guava RateLimiter限制调用频率
4. **缓存机制**: 相同内容24小时内返回缓存结果
5. **异步处理**: 使用线程池异步调用，不阻塞主流程

### Prompt设计

```
系统角色：你是一个专业的情感分析专家。

任务：分析以下文本的情感倾向，并返回JSON格式结果。

文本：{用户输入内容}

要求：
1. 情感分数：-1（非常消极）到 1（非常积极）
2. 情感标签：POSITIVE（积极）、NEUTRAL（中性）、NEGATIVE（消极）
3. 关键词：提取3-5个情感关键词
4. 简要分析：50字以内的情感分析说明

返回格式：
{
  "score": 0.8,
  "label": "POSITIVE",
  "keywords": ["开心", "满足", "期待"],
  "analysis": "文本表达了积极乐观的情绪..."
}
```

---

## 前端架构设计

### 目录结构

```
emotion-hub-frontend/
├── src/
│   ├── api/                    # API接口
│   │   ├── user.ts
│   │   ├── post.ts
│   │   ├── comment.ts
│   │   └── emotion.ts
│   │
│   ├── assets/                 # 静态资源
│   │   ├── images/
│   │   └── styles/
│   │
│   ├── components/             # 公共组件
│   │   ├── EmotionChart/       # 情感图表
│   │   ├── PostCard/           # 帖子卡片
│   │   ├── CommentList/        # 评论列表
│   │   └── NotificationBell/   # 通知铃铛
│   │
│   ├── views/                  # 页面视图
│   │   ├── Home/               # 首页（帖子流）
│   │   ├── PostDetail/         # 帖子详情
│   │   ├── Profile/            # 个人中心
│   │   ├── Statistics/         # 情感统计
│   │   ├── Login/              # 登录注册
│   │   └── Admin/              # 管理后台
│   │
│   ├── stores/                 # Pinia状态管理
│   │   ├── user.ts             # 用户状态
│   │   ├── post.ts             # 帖子状态
│   │   └── notification.ts     # 通知状态
│   │
│   ├── router/                 # 路由配置
│   │   └── index.ts
│   │
│   ├── utils/                  # 工具函数
│   │   ├── request.ts          # Axios封装
│   │   ├── websocket.ts        # WebSocket封装
│   │   └── emotion.ts          # 情感工具
│   │
│   ├── types/                  # TypeScript类型
│   │   └── index.ts
│   │
│   ├── App.vue
│   └── main.ts
│
├── public/
├── index.html
├── vite.config.ts
├── tsconfig.json
└── package.json
```

### 核心页面设计

#### 1. 首页（帖子流）
- 顶部导航栏（Logo、搜索、通知、用户头像）
- 发帖输入框（支持文本+图片）
- 帖子列表（瀑布流/时间流）
  - 用户信息
  - 帖子内容
  - 情感标签（带颜色）
  - 点赞/评论/分享按钮
- 右侧边栏：热门话题、情感趋势

#### 2. 帖子详情页
- 帖子完整内容
- LLM情感分析结果展示
  - 情感分数可视化
  - 关键词标签云
  - 分析说明
- 评论区（支持嵌套）

#### 3. 个人中心
- 用户信息编辑
- 我的帖子列表
- 情感统计仪表盘
  - 情感分布饼图
  - 情感趋势折线图
  - 情感日历热力图
  - 关键词词云

#### 4. 管理后台
- 用户管理（列表、状态修改）
- 内容管理（帖子审核、删除）
- 系统统计（用户数、帖子数、情感分布）

---

## 开发计划

### 第一阶段：基础框架搭建（1-2周）

**后端**
- [ ] 创建Spring Boot多模块项目
- [ ] 配置MySQL + Redis
- [ ] 集成MyBatis-Plus/JPA
- [ ] 配置Swagger文档
- [ ] 实现JWT认证

**前端**
- [ ] 创建Vue 3 + Vite项目
- [ ] 配置TypeScript + ESLint
- [ ] 集成Element Plus
- [ ] 配置路由和状态管理
- [ ] 封装Axios请求

### 第二阶段：用户模块（1周）

- [ ] 用户注册/登录接口
- [ ] 用户信息CRUD
- [ ] 角色权限控制
- [ ] 前端登录页面
- [ ] 前端个人中心页面

### 第三阶段：帖子模块（1-2周）

- [ ] 帖子CRUD接口
- [ ] 点赞功能
- [ ] 评论功能（含嵌套）
- [ ] 分页查询
- [ ] 前端帖子流页面
- [ ] 前端帖子详情页
- [ ] 前端发帖组件

### 第四阶段：LLM集成（1-2周）

- [ ] LLM服务接口设计
- [ ] 实现多个LLM适配器
- [ ] 负载均衡和降级策略
- [ ] 异步分析任务
- [ ] 缓存机制
- [ ] 前端情感分析结果展示

### 第五阶段：情感统计（1周）

- [ ] 情感数据统计接口
- [ ] 定时任务统计
- [ ] 趋势分析算法
- [ ] 前端ECharts图表集成
- [ ] 情感仪表盘页面

### 第六阶段：实时通知（1周）

- [ ] WebSocket服务端
- [ ] 通知CRUD接口
- [ ] 前端WebSocket客户端
- [ ] 实时通知组件
- [ ] 消息推送

### 第七阶段：管理后台（1周）

- [ ] 管理员权限控制
- [ ] 用户管理接口
- [ ] 内容管理接口
- [ ] 前端管理页面

### 第八阶段：优化和部署（1周）

- [ ] 性能优化
- [ ] 安全加固
- [ ] 单元测试
- [ ] 接口文档完善
- [ ] Docker部署配置
- [ ] 项目文档

---

## 技术亮点

1. **多LLM集成**: 支持多个大模型API，实现负载均衡和降级
2. **实时通信**: WebSocket实现实时通知推送
3. **数据可视化**: ECharts展示情感趋势和统计
4. **微服务架构**: 模块化设计，易于扩展
5. **异步处理**: 情感分析异步化，提升用户体验
6. **缓存优化**: 多级缓存（Redis + Caffeine）
7. **权限控制**: 基于角色的访问控制（RBAC）
8. **API文档**: Swagger自动生成接口文档

---

## 部署方案

### 开发环境
- 本地MySQL + Redis
- 后端: IDEA运行Spring Boot
- 前端: npm run dev

### 生产环境（Docker）
```yaml
services:
  mysql:
    image: mysql:8.0
  redis:
    image: redis:7.0
  backend:
    build: ./backend
    ports: 8080:8080
  frontend:
    build: ./frontend
    ports: 80:80
  nginx:
    image: nginx
    # 反向代理配置
```

---

## 注意事项

1. **LLM API成本**: 建议使用免费额度或低成本API进行开发测试
2. **数据安全**: 用户密码加密存储，敏感信息脱敏
3. **并发控制**: LLM调用需要限流，避免超额
4. **错误处理**: 完善的异常处理和用户提示
5. **代码规范**: 遵循阿里巴巴Java开发手册
6. **Git管理**: 使用Git Flow工作流

---

## 扩展功能（可选）

- [ ] 情感日记导出（PDF/Excel）
- [ ] 好友系统和私信
- [ ] 话题标签系统
- [ ] 内容推荐算法
- [ ] 移动端适配
- [ ] 多语言支持
- [ ] 第三方登录（微信/QQ）
- [ ] 图片情感识别（OCR + 情感分析）

---

## 学习资源

### Java后端
- Spring Boot官方文档
- MyBatis-Plus官方文档
- Redis实战

### Vue前端
- Vue 3官方文档
- Element Plus组件库
- ECharts可视化

### LLM集成
- OpenAI API文档
- 通义千问API文档
- Prompt工程指南

---

**项目预计总开发时间**: 8-12周（根据个人时间安排）
**难度等级**: 中等偏上
**适合人群**: 有一定Java和前端基础的大学生

祝开发顺利！🚀
