# 系统架构文档 (System Architecture)

## 系统概述 (System Overview)

民宿房源推荐系统是一个基于 Spring Boot 的后端管理系统，提供房源管理、用户管理、订单管理和智能推荐功能。系统采用微服务架构思想，模块化设计，易于扩展和维护。

---

## 技术架构 (Technical Architecture)

```
┌─────────────────────────────────────────────────────────────┐
│                      Client Layer                            │
│              (Web/Mobile/Third-party Apps)                   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway Layer                         │
│                   (CORS + JWT Security)                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Controller Layer                           │
│  ┌──────────┬──────────┬──────────┬──────────────────┐      │
│  │  Auth    │ Property │  Order   │ Recommendation   │      │
│  │Controller│Controller│Controller│   Controller     │      │
│  └──────────┴──────────┴──────────┴──────────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                             │
│  ┌──────────┬──────────┬──────────┬──────────────────┐      │
│  │  Auth    │ Property │  Order   │ Recommendation   │      │
│  │ Service  │ Service  │ Service  │    Service       │      │
│  └──────────┴──────────┴──────────┴──────────────────┘      │
│                                                               │
│  Business Logic:                                             │
│  - User Authentication & Authorization                       │
│  - Property CRUD & Search                                    │
│  - Order Processing & Status Management                      │
│  - Hybrid Recommendation Algorithm                           │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                 Repository Layer (DAO)                       │
│  ┌──────────┬──────────┬──────────┬──────────────────┐      │
│  │   User   │ Property │  Order   │  Interaction     │      │
│  │Repository│Repository│Repository│   Repository     │      │
│  └──────────┴──────────┴──────────┴──────────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  Data Layer                                  │
│  ┌──────────┬──────────┬──────────────────────────────┐     │
│  │  MySQL   │  Redis   │      Elasticsearch           │     │
│  │(Primary) │ (Cache)  │      (Search Engine)         │     │
│  └──────────┴──────────┴──────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

---

## 核心模块设计 (Core Module Design)

### 1. 认证与授权模块 (Authentication & Authorization)

**组件**:
- `JwtTokenProvider`: JWT Token 生成和验证
- `JwtAuthenticationFilter`: JWT 认证过滤器
- `SecurityConfig`: Spring Security 配置
- `UserPrincipal`: 用户主体对象
- `CustomUserDetailsService`: 用户详情服务

**工作流程**:
```
User Login Request
    ↓
AuthController.login()
    ↓
AuthenticationManager.authenticate()
    ↓
CustomUserDetailsService.loadUserByUsername()
    ↓
JwtTokenProvider.generateToken()
    ↓
Return JWT Token to Client
    ↓
Client includes token in subsequent requests
    ↓
JwtAuthenticationFilter.doFilterInternal()
    ↓
Token validation & user authentication
    ↓
Request proceeds to controller
```

**安全特性**:
- BCrypt 密码加密
- JWT Token 有效期控制（24小时）
- 基于角色的访问控制（RBAC）
- CORS 跨域配置

---

### 2. 房源管理模块 (Property Management)

**实体关系**:
```
User (Landlord)
    │
    │ 1:N
    ↓
Property
    │
    │ 1:N
    ↓
UserPropertyInteraction
```

**核心功能**:
- CRUD 操作（创建、读取、更新、删除）
- 房源搜索和筛选
- 分页和排序
- 访问量统计
- 房东房源管理

**缓存策略**:
- 使用 Redis 缓存房源列表
- 缓存热门房源（Top 10 by booking count）
- 缓存高评分房源（Top 10 by rating）
- TTL: 1小时

---

### 3. 订单管理模块 (Order Management)

**订单状态流转**:
```
PENDING (待确认)
    ↓
CONFIRMED (已确认)
    ↓
┌───────────┬───────────┐
│           │           │
↓           ↓           ↓
CANCELLED   COMPLETED   (订单进行中)
```

**业务规则**:
- 自动生成唯一订单号
- 入住日期验证（不能是过去的日期）
- 退房日期必须晚于入住日期
- 根据天数自动计算总价
- 更新房源预订计数

**数据一致性**:
- 使用 `@Transactional` 保证事务一致性
- 订单创建时同步更新房源统计

---

### 4. 推荐模块 (Recommendation Module)

#### 4.1 混合推荐算法 (Hybrid Algorithm)

**算法组成**:
```
Hybrid Recommendation = 
    60% × Collaborative Filtering + 
    40% × Content-Based Filtering
```

#### 4.2 协同过滤 (Collaborative Filtering)

**原理**: 基于"物以类聚、人以群分"的思想，找到相似用户，推荐他们喜欢的房源。

**步骤**:
1. 构建用户-房源交互矩阵
2. 计算用户间的 Jaccard 相似度
3. 找出最相似的用户
4. 推荐相似用户喜欢但当前用户未交互的房源

**公式**:
```
Jaccard(A, B) = |A ∩ B| / |A ∪ B|
```

**冷启动处理**:
- 新用户：推荐热门房源（按预订量排序）
- 新房源：暂不推荐，等待积累数据

#### 4.3 基于内容推荐 (Content-Based Filtering)

**原理**: 基于房源的内容特征和用户的历史偏好进行推荐。

**特征权重**:
- 城市偏好: 30%
- 房源类型: 20%
- 价格相似度: 25%
- 房间数相似度: 15%
- 评分加成: 10%

**评分计算**:
```python
score = 0.3 × city_match + 
        0.2 × type_match + 
        0.25 × price_similarity + 
        0.15 × bedroom_similarity + 
        0.1 × (rating/5)
```

**用户画像构建**:
- 分析用户收藏、预订、高评分的房源
- 提取用户偏好：城市、类型、价格区间、房间数

---

## 数据库设计 (Database Design)

### ER 图 (Entity-Relationship Diagram)

```
┌─────────────┐              ┌─────────────┐
│    User     │              │  Property   │
├─────────────┤              ├─────────────┤
│ id (PK)     │──────1:N────▶│ id (PK)     │
│ username    │              │ title       │
│ password    │              │ city        │
│ email       │              │ price       │
│ role        │              │ landlord_id │
└─────────────┘              │ (FK)        │
       │                     └─────────────┘
       │                            │
       │                            │
       │ 1:N                        │ 1:N
       │                            │
       ↓                            ↓
┌─────────────┐              ┌─────────────────────────┐
│    Order    │              │ UserPropertyInteraction │
├─────────────┤              ├─────────────────────────┤
│ id (PK)     │              │ id (PK)                 │
│ user_id(FK) │              │ user_id (FK)            │
│ property_id │              │ property_id (FK)        │
│ (FK)        │              │ type                    │
│ status      │              │ rating                  │
└─────────────┘              └─────────────────────────┘
```

### 表结构说明

#### users (用户表)
- 存储用户基本信息
- 支持三种角色：USER, LANDLORD, ADMIN
- BCrypt 加密密码

#### properties (房源表)
- 存储房源详细信息
- 包含统计字段：评分、预订数、浏览数
- 外键关联到 landlord (User)

#### orders (订单表)
- 记录预订信息
- 状态管理：PENDING, CONFIRMED, CANCELLED, COMPLETED
- 自动生成唯一订单号

#### user_property_interactions (用户交互表)
- 记录用户行为：VIEW, FAVORITE, BOOK, REVIEW
- 为推荐算法提供数据支持
- 时间序列数据，用于分析用户偏好变化

---

## 缓存架构 (Cache Architecture)

### Redis 缓存层次

```
Application
    ↓
┌───────────────────────────────────┐
│      Spring Cache Abstraction     │
├───────────────────────────────────┤
│  @Cacheable, @CacheEvict          │
└───────────────────────────────────┘
    ↓
┌───────────────────────────────────┐
│         Redis Cache               │
├───────────────────────────────────┤
│ - properties (房源缓存)           │
│ - recommendations (推荐缓存)      │
│ - popularProperties (热门房源)    │
│ - topRatedProperties (高评分)    │
└───────────────────────────────────┘
    ↓
MySQL (数据源)
```

### 缓存策略

**缓存键设计**:
- `properties::all-{page}-{size}`: 房源列表
- `properties::{propertyId}`: 单个房源
- `recommendations::{userId}`: 用户推荐
- `popularProperties`: 热门房源
- `topRatedProperties`: 高评分房源

**缓存更新策略**:
- 写入时更新（Write-Through）
- 创建/更新/删除房源时清除相关缓存
- TTL: 1小时自动过期

---

## API 设计原则 (API Design Principles)

### RESTful 风格

- **资源命名**: 使用复数名词（/api/properties）
- **HTTP 方法**:
  - GET: 查询
  - POST: 创建
  - PUT: 更新
  - DELETE: 删除

### 统一响应格式

```json
{
  "success": true/false,
  "message": "操作结果描述",
  "data": { }
}
```

### 分页规范

- 使用 Spring Data Pageable
- 参数：page（页码）、size（每页数量）、sort（排序字段）
- 返回：content（数据）、totalElements（总数）、totalPages（总页数）

---

## 性能优化 (Performance Optimization)

### 1. 数据库优化

**索引策略**:
```sql
-- 用户表
INDEX idx_username (username)
INDEX idx_email (email)

-- 房源表
INDEX idx_city (city)
INDEX idx_price (price)
INDEX idx_rating (rating)
INDEX idx_booking_count (booking_count)

-- 订单表
INDEX idx_order_number (order_number)
INDEX idx_user (user_id)
INDEX idx_created_at (created_at)

-- 交互表
INDEX idx_user (user_id)
INDEX idx_property (property_id)
```

**查询优化**:
- 使用 JPA 分页避免一次加载大量数据
- FetchType.LAZY 延迟加载关联对象
- 使用 @Query 自定义优化查询

### 2. 应用层优化

**缓存策略**:
- 热点数据缓存（Redis）
- 推荐结果缓存（避免重复计算）
- 页面级缓存（Nginx）

**异步处理**:
- 推荐算法计算可以异步化
- 统计数据更新异步化
- 日志记录异步化

### 3. 推荐算法优化

**计算优化**:
- 预计算用户相似度矩阵（定时任务）
- 增量更新而非全量计算
- 使用近似算法（ANN）加速相似度计算

**数据优化**:
- 仅考虑最近 N 天的交互数据
- 限制相似用户数量（Top-K）
- 使用采样减少计算量

---

## 扩展性设计 (Scalability Design)

### 水平扩展

**无状态设计**:
- JWT Token 存储在客户端
- Session 信息存储在 Redis
- 可以轻松部署多个应用实例

**负载均衡**:
```
          ┌─────────────┐
          │   Nginx LB  │
          └─────────────┘
                 │
        ┌────────┼────────┐
        │        │        │
        ↓        ↓        ↓
    ┌──────┐ ┌──────┐ ┌──────┐
    │ App1 │ │ App2 │ │ App3 │
    └──────┘ └──────┘ └──────┘
        │        │        │
        └────────┼────────┘
                 │
          ┌─────────────┐
          │   MySQL     │
          │   Master    │
          └─────────────┘
                 │
        ┌────────┼────────┐
        ↓        ↓        ↓
    ┌──────┐ ┌──────┐ ┌──────┐
    │Slave1│ │Slave2│ │Slave3│
    └──────┘ └──────┘ └──────┘
```

### 数据库扩展

**读写分离**:
- Master: 处理写操作
- Slaves: 处理读操作
- 使用 Spring 的 AbstractRoutingDataSource

**分库分表**:
- 按用户 ID 分片
- 按时间分表（订单、交互记录）

---

## 安全设计 (Security Design)

### 认证与授权

**多层防护**:
1. JWT Token 验证
2. 角色权限控制（@PreAuthorize）
3. 资源所有权验证（订单、房源）
4. SQL 注入防护（JPA）
5. XSS 防护（输入验证）

### 数据安全

**敏感数据保护**:
- 密码 BCrypt 加密（不可逆）
- JWT Secret 环境变量配置
- HTTPS 传输加密

---

## 监控与日志 (Monitoring & Logging)

### 日志级别

- ERROR: 系统错误
- WARN: 警告信息
- INFO: 一般信息
- DEBUG: 调试信息

### 关键指标

**应用指标**:
- QPS（每秒请求数）
- 响应时间
- 错误率
- JVM 内存使用

**业务指标**:
- 用户注册量
- 房源发布量
- 订单成交量
- 推荐点击率

---

## 未来扩展方向 (Future Enhancements)

1. **实时推荐**: 集成 Apache Flink 实现实时推荐
2. **消息队列**: 引入 RabbitMQ/Kafka 处理异步任务
3. **微服务拆分**: 拆分为用户服务、房源服务、推荐服务
4. **机器学习**: 使用深度学习模型优化推荐效果
5. **GraphQL**: 提供更灵活的 API 查询方式
6. **全文搜索**: 完善 Elasticsearch 集成
7. **支付集成**: 集成第三方支付服务
8. **消息通知**: 短信、邮件、推送通知

---

## 参考文档 (References)

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [JWT Introduction](https://jwt.io/introduction)
- [Collaborative Filtering](https://en.wikipedia.org/wiki/Collaborative_filtering)
- [Redis Best Practices](https://redis.io/docs/manual/patterns/)
