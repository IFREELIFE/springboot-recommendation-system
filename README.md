# 民宿推荐系统 - 完整技术解析

## 📖 目录
- [系统简介](#系统简介)
- [核心功能](#核心功能)
- [技术架构](#技术架构)
- [关键接口详解](#关键接口详解)
- [推荐算法核心逻辑](#推荐算法核心逻辑)
- [数据流程](#数据流程)
- [快速开始](#快速开始)
- [零基础入门指南](#零基础入门指南)

---

## 系统简介

这是一个基于 **Spring Boot** 开发的民宿推荐系统，为用户提供个性化的房源推荐服务。系统通过分析用户的浏览、收藏、预订等行为，结合房源的特征信息，使用智能推荐算法为用户推荐最合适的民宿。

### 🎯 系统目标
- **用户端**：帮助用户快速找到符合需求的民宿
- **房东端**：帮助房东提高房源的曝光率和预订率
- **管理员**：提供完善的后台管理功能

---

## 核心功能

### 1. 用户管理
- 用户注册和登录（支持多角色：普通用户、房东、管理员）
- 个人信息管理
- 基于 JWT 的安全认证

### 2. 房源管理
- 房源发布、编辑、删除（房东功能）
- 房源图片上传
- 房源搜索和筛选
- 房源详情查看

### 3. 订单管理
- 创建订单
- 订单状态管理（待支付、已确认、已取消、已完成）
- 退订申请和审核

### 4. **智能推荐系统**（核心功能）
- 混合推荐（协同过滤 + 内容相似）
- 协同过滤推荐
- 基于内容的推荐

---

## 技术架构

### 🛠️ 核心技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| **Spring Boot** | 2.7.18 | 后端框架，提供快速开发能力 |
| **MyBatis-Plus** | 3.5.3.1 | 数据持久层框架，简化数据库操作 |
| **Spring Security** | - | 安全框架，负责认证和授权 |
| **JWT** | 0.11.5 | 无状态身份认证令牌 |
| **MySQL** | 8.0.33 | 关系型数据库，存储业务数据 |
| **Redis** | - | 缓存数据库，提高推荐性能 |
| **Elasticsearch** | - | 搜索引擎，支持全文检索 |
| **Lombok** | - | 简化 Java 代码编写 |

### 📦 项目结构

```
src/main/java/com/recommendation/homestay/
├── config/              # 配置类（安全、Redis、跨域等）
├── controller/          # 控制器层，处理 HTTP 请求
├── service/             # 业务逻辑层
├── mapper/              # 数据访问层（MyBatis-Plus）
├── entity/              # 实体类（对应数据库表）
├── dto/                 # 数据传输对象
└── security/            # 安全相关（JWT、用户认证）
```

### 🔄 系统架构图

```
┌─────────────┐
│  前端应用    │
│  (Vue/React) │
└──────┬──────┘
       │ HTTP/HTTPS
       ▼
┌─────────────────────────────────────┐
│         Spring Boot 后端             │
├─────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐│
│  │ Controller   │  │  Security    ││
│  │ (接口层)      │  │  (JWT认证)   ││
│  └──────┬───────┘  └──────────────┘│
│         │                            │
│  ┌──────▼───────┐                   │
│  │   Service    │                   │
│  │ (业务逻辑层)  │                   │
│  └──────┬───────┘                   │
│         │                            │
│  ┌──────▼───────┐                   │
│  │   Mapper     │                   │
│  │ (数据访问层)  │                   │
│  └──────┬───────┘                   │
└─────────┼─────────────────────────┘
          │
    ┌─────┴─────┬──────────┬─────────┐
    ▼           ▼          ▼         ▼
┌────────┐  ┌──────┐  ┌────────┐  ┌────────┐
│ MySQL  │  │Redis │  │Elastic │  │ 文件系统│
│(数据库) │  │(缓存) │  │Search  │  │(图片)  │
└────────┘  └──────┘  └────────┘  └────────┘
```

---

## 关键接口详解

### 📌 1. 认证接口 (AuthController)

#### 🔐 用户注册
**接口地址**: `POST /api/auth/register`

**作用**: 创建新用户账号

**请求参数**:
```json
{
  "username": "testuser",      // 用户名（必填）
  "email": "test@example.com", // 邮箱（必填）
  "password": "123456",        // 密码（必填）
  "role": "USER"              // 角色（可选：USER/LANDLORD/ADMIN）
}
```

**核心代码逻辑**:
```java
// 1. 检查用户名是否已存在
QueryWrapper<User> usernameQuery = new QueryWrapper<>();
usernameQuery.eq("username", request.getUsername());
if (userMapper.selectCount(usernameQuery) > 0) {
    throw new RuntimeException("用户名已存在");
}

// 2. 检查邮箱是否已存在
QueryWrapper<User> emailQuery = new QueryWrapper<>();
emailQuery.eq("email", request.getEmail());
if (userMapper.selectCount(emailQuery) > 0) {
    throw new RuntimeException("邮箱已存在");
}

// 3. 加密密码（使用 BCrypt 算法）
user.setPassword(passwordEncoder.encode(request.getPassword()));

// 4. 保存到数据库
userMapper.insert(user);
```

**返回结果**:
```json
{
  "success": true,
  "message": "用户注册成功",
  "data": 1  // 新创建的用户ID
}
```

---

#### 🔑 用户登录
**接口地址**: `POST /api/auth/login`

**作用**: 验证用户凭证并返回 JWT 令牌

**请求参数**:
```json
{
  "username": "testuser",
  "password": "123456"
}
```

**核心代码逻辑**:
```java
// 1. 使用 Spring Security 验证用户名和密码
Authentication authentication = authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(
        request.getUsername(),
        request.getPassword()
    )
);

// 2. 验证成功后，设置安全上下文
SecurityContextHolder.getContext().setAuthentication(authentication);

// 3. 生成 JWT 令牌
String jwt = tokenProvider.generateToken(authentication);

// 4. 返回令牌和用户信息
return new JwtResponse(jwt, userId, username, email, role);
```

**返回结果**:
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",  // JWT令牌
    "tokenType": "Bearer",
    "userId": 1,
    "username": "testuser",
    "email": "test@example.com",
    "role": "ROLE_USER"
  }
}
```

**🔍 JWT 令牌说明**:
- JWT (JSON Web Token) 是一种无状态的身份认证方式
- 后续所有需要认证的接口都需要在请求头中携带此令牌
- 格式: `Authorization: Bearer <token>`

---

### 📌 2. 房源接口 (PropertyController)

#### 🏠 创建房源
**接口地址**: `POST /api/properties`

**权限要求**: 房东或管理员

**作用**: 发布新的房源信息

**请求参数**:
```json
{
  "title": "温馨海景民宿",
  "description": "面朝大海，春暖花开",
  "city": "厦门",
  "address": "厦门市思明区",
  "price": 299.00,
  "bedrooms": 2,
  "bathrooms": 1,
  "propertyType": "公寓",
  "images": "[\"url1\", \"url2\"]"
}
```

**核心代码逻辑**:
```java
// 1. 创建房源对象
Property property = new Property();
property.setLandlordId(currentUser.getId());  // 设置房东ID
property.setTitle(request.getTitle());
property.setPrice(request.getPrice());
// ... 设置其他字段

// 2. 初始化统计数据
property.setViewCount(0);      // 浏览量
property.setBookingCount(0);   // 预订次数
property.setRating(5.0);       // 初始评分
property.setAvailable(true);   // 设置为可用

// 3. 保存到数据库
propertyMapper.insert(property);
```

---

#### 🔍 搜索房源
**接口地址**: `GET /api/properties/search`

**作用**: 根据条件筛选房源

**请求参数**:
- `city`: 城市（可选）
- `minPrice`: 最低价格（可选）
- `maxPrice`: 最高价格（可选）
- `bedrooms`: 卧室数量（可选）
- `page`: 页码（默认0）
- `size`: 每页数量（默认10）

**核心代码逻辑**:
```java
// 1. 构建查询条件
QueryWrapper<Property> queryWrapper = new QueryWrapper<>();
queryWrapper.eq("available", true);  // 只查询可用房源

// 2. 动态添加查询条件
if (city != null && !city.isEmpty()) {
    queryWrapper.like("city", city);  // 模糊匹配城市
}
if (minPrice != null) {
    queryWrapper.ge("price", minPrice);  // 价格大于等于最低价
}
if (maxPrice != null) {
    queryWrapper.le("price", maxPrice);  // 价格小于等于最高价
}
if (bedrooms != null) {
    queryWrapper.eq("bedrooms", bedrooms);  // 精确匹配卧室数
}

// 3. 分页查询
Page<Property> page = new Page<>(pageNum, pageSize);
IPage<Property> result = propertyMapper.selectPage(page, queryWrapper);
```

---

### 📌 3. 订单接口 (OrderController)

#### 📝 创建订单
**接口地址**: `POST /api/orders`

**作用**: 用户预订房源

**请求参数**:
```json
{
  "propertyId": 1,
  "checkInDate": "2024-01-01",
  "checkOutDate": "2024-01-05",
  "guests": 2
}
```

**核心代码逻辑**:
```java
// 1. 验证房源是否可用
Property property = propertyMapper.selectById(request.getPropertyId());
if (property == null || !property.getAvailable()) {
    throw new RuntimeException("房源不可用");
}

// 2. 检查日期冲突
boolean hasConflict = checkDateConflict(
    propertyId, checkInDate, checkOutDate
);
if (hasConflict) {
    throw new RuntimeException("该时间段已被预订");
}

// 3. 计算总价
int nights = calculateNights(checkInDate, checkOutDate);
BigDecimal totalAmount = property.getPrice().multiply(new BigDecimal(nights));

// 4. 创建订单
Order order = new Order();
order.setOrderNumber(generateOrderNumber());  // 生成订单号
order.setUserId(currentUser.getId());
order.setPropertyId(propertyId);
order.setTotalAmount(totalAmount);
order.setStatus(OrderStatus.PENDING);  // 初始状态：待支付

// 5. 保存订单
orderMapper.insert(order);

// 6. 更新房源预订次数
property.setBookingCount(property.getBookingCount() + 1);
propertyMapper.updateById(property);
```

---

### 📌 4. **推荐接口 (RecommendationController)** ⭐ 核心功能

#### 🎯 获取综合推荐
**接口地址**: `GET /api/recommendations`

**作用**: 为用户提供个性化的房源推荐（混合算法）

**请求参数**:
- `limit`: 返回推荐数量（默认10）

**返回结果**:
```json
{
  "success": true,
  "message": "推荐列表获取成功",
  "data": [
    {
      "id": 1,
      "title": "温馨海景民宿",
      "city": "厦门",
      "price": 299.00,
      "rating": 4.8
    }
    // ... 更多推荐房源
  ]
}
```

---

#### 🤝 协同过滤推荐
**接口地址**: `GET /api/recommendations/collaborative`

**作用**: 基于相似用户的行为推荐房源

**推荐逻辑**: "喜欢 A 房源的用户也喜欢 B 房源"

---

#### 📊 内容相似推荐
**接口地址**: `GET /api/recommendations/content-based`

**作用**: 基于房源特征的相似度推荐

**推荐逻辑**: "你喜欢的房源与这些房源很相似"

---

## 推荐算法核心逻辑

### 🧠 1. 混合推荐算法（核心）

**算法思路**: 结合协同过滤和内容相似两种算法，取长补短

**实现步骤**:

```java
public List<Property> getRecommendations(Long userId, int limit) {
    // 步骤1：获取两种算法的推荐结果（各取 limit*2 个候选）
    List<Property> collaborative = getCollaborativeFilteringRecommendations(userId, limit * 2);
    List<Property> contentBased = getContentBasedRecommendations(userId, limit * 2);
    
    // 步骤2：为每个房源计算综合得分
    Map<Long, Double> propertyScores = new HashMap<>();
    
    // 步骤3：协同过滤权重 60%
    for (int i = 0; i < collaborative.size(); i++) {
        Property property = collaborative.get(i);
        // 排名越靠前，得分越高
        double score = (collaborative.size() - i) * 0.6;
        propertyScores.merge(property.getId(), score, Double::sum);
    }
    
    // 步骤4：内容相似权重 40%
    for (int i = 0; i < contentBased.size(); i++) {
        Property property = contentBased.get(i);
        double score = (contentBased.size() - i) * 0.4;
        propertyScores.merge(property.getId(), score, Double::sum);
    }
    
    // 步骤5：按综合得分排序，返回前 N 个
    return propertyScores.entrySet().stream()
        .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
        .limit(limit)
        .map(entry -> propertyMapper.selectById(entry.getKey()))
        .collect(Collectors.toList());
}
```

**算法优势**:
- ✅ 结合了用户行为和房源特征
- ✅ 避免了单一算法的局限性
- ✅ 推荐结果更加准确和多样化

---

### 🤝 2. 协同过滤推荐算法

**核心思想**: 找到和你兴趣相似的用户，推荐他们喜欢的房源

**算法流程**:

```
用户A浏览了房源 [1, 2, 3]
用户B浏览了房源 [2, 3, 4, 5]
用户C浏览了房源 [1, 2, 6]

计算相似度：
- 用户A和B的相似度 = 交集{2,3} / 并集{1,2,3,4,5} = 2/5 = 0.4
- 用户A和C的相似度 = 交集{1,2} / 并集{1,2,3,6} = 2/4 = 0.5

因为C更相似，所以向A推荐房源6
```

**详细实现**:

```java
public List<Property> getCollaborativeFilteringRecommendations(Long userId, int limit) {
    // 步骤1：获取当前用户的交互记录
    List<UserPropertyInteraction> userInteractions = 
        interactionMapper.selectList(
            new QueryWrapper<UserPropertyInteraction>().eq("user_id", userId)
        );
    
    // 冷启动处理：如果用户没有交互记录，返回热门房源
    if (userInteractions.isEmpty()) {
        return propertyMapper.findTop10ByAvailableTrueOrderByBookingCountDesc();
    }
    
    // 步骤2：构建所有用户的房源交互集合
    // 数据结构：Map<用户ID, Set<房源ID>>
    Map<Long, Set<Long>> userPropertyMap = new HashMap<>();
    List<UserPropertyInteraction> allInteractions = interactionMapper.selectList(null);
    
    for (UserPropertyInteraction interaction : allInteractions) {
        userPropertyMap.computeIfAbsent(interaction.getUserId(), k -> new HashSet<>())
                       .add(interaction.getPropertyId());
    }
    
    // 步骤3：计算当前用户与其他用户的相似度（使用杰卡德相似度）
    Set<Long> currentUserProperties = userPropertyMap.get(userId);
    Map<Long, Double> similarityScores = new HashMap<>();
    
    for (Map.Entry<Long, Set<Long>> entry : userPropertyMap.entrySet()) {
        Long otherUserId = entry.getKey();
        if (otherUserId.equals(userId)) continue;  // 跳过自己
        
        Set<Long> otherUserProperties = entry.getValue();
        double similarity = calculateJaccardSimilarity(
            currentUserProperties, 
            otherUserProperties
        );
        
        if (similarity > 0) {
            similarityScores.put(otherUserId, similarity);
        }
    }
    
    // 步骤4：根据相似用户的交互汇总推荐得分
    Set<Long> interactedPropertyIds = currentUserProperties;
    Map<Long, Double> recommendationScores = new HashMap<>();
    
    for (Map.Entry<Long, Double> entry : similarityScores.entrySet()) {
        Long similarUserId = entry.getKey();
        Double similarity = entry.getValue();
        
        Set<Long> similarUserProperties = userPropertyMap.get(similarUserId);
        for (Long propertyId : similarUserProperties) {
            // 只推荐用户未交互过的房源
            if (!interactedPropertyIds.contains(propertyId)) {
                // 得分 = 相似用户的相似度累加
                recommendationScores.merge(propertyId, similarity, Double::sum);
            }
        }
    }
    
    // 步骤5：按得分排序，返回前N个
    return recommendationScores.entrySet().stream()
        .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
        .limit(limit)
        .map(entry -> propertyMapper.selectById(entry.getKey()))
        .filter(Objects::nonNull)
        .filter(Property::getAvailable)
        .collect(Collectors.toList());
}

// 杰卡德相似度计算
private double calculateJaccardSimilarity(Set<Long> set1, Set<Long> set2) {
    Set<Long> intersection = new HashSet<>(set1);
    intersection.retainAll(set2);  // 交集
    
    Set<Long> union = new HashSet<>(set1);
    union.addAll(set2);  // 并集
    
    return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
}
```

**杰卡德相似度 (Jaccard Similarity)**:
```
相似度 = |A ∩ B| / |A ∪ B|

例如：
用户A喜欢房源 {1, 2, 3}
用户B喜欢房源 {2, 3, 4}
交集 = {2, 3}，大小为2
并集 = {1, 2, 3, 4}，大小为4
相似度 = 2/4 = 0.5
```

---

### 📊 3. 基于内容的推荐算法

**核心思想**: 分析用户喜欢的房源特征，推荐具有相似特征的其他房源

**用户偏好特征**:
- 城市偏好（例如：用户经常浏览北京的房源）
- 房源类型偏好（例如：用户喜欢公寓型房源）
- 价格偏好（例如：用户喜欢200-300元的房源）
- 卧室数偏好（例如：用户喜欢2居室）

**详细实现**:

```java
public List<Property> getContentBasedRecommendations(Long userId, int limit) {
    // 步骤1：查询用户的交互记录
    List<UserPropertyInteraction> userInteractions = 
        interactionMapper.selectList(
            new QueryWrapper<UserPropertyInteraction>().eq("user_id", userId)
        );
    
    // 冷启动：无交互记录时返回高评分房源
    if (userInteractions.isEmpty()) {
        return propertyMapper.findTop10ByAvailableTrueOrderByRatingDesc();
    }
    
    // 步骤2：筛选用户"喜欢"的房源（正向交互）
    List<Property> likedProperties = new ArrayList<>();
    for (UserPropertyInteraction interaction : userInteractions) {
        boolean isPositive = 
            interaction.getType() == InteractionType.FAVORITE ||  // 收藏
            interaction.getType() == InteractionType.BOOK ||      // 预订
            (interaction.getRating() != null && interaction.getRating() >= 4);  // 高评分
        
        if (isPositive) {
            Property property = propertyMapper.selectById(interaction.getPropertyId());
            if (property != null) {
                likedProperties.add(property);
            }
        }
    }
    
    // 步骤3：提取用户偏好特征
    Map<String, Integer> cityPreferences = new HashMap<>();     // 城市偏好
    Map<String, Integer> typePreferences = new HashMap<>();     // 类型偏好
    double totalPrice = 0.0;
    int totalBedrooms = 0;
    
    for (Property property : likedProperties) {
        cityPreferences.merge(property.getCity(), 1, Integer::sum);
        typePreferences.merge(property.getPropertyType(), 1, Integer::sum);
        totalPrice += property.getPrice().doubleValue();
        totalBedrooms += property.getBedrooms();
    }
    
    // 计算平均偏好值
    double avgPrice = totalPrice / likedProperties.size();
    int avgBedrooms = totalBedrooms / likedProperties.size();
    
    // 步骤4：查询所有可用房源
    List<Property> allProperties = 
        propertyMapper.selectList(
            new QueryWrapper<Property>().eq("available", true)
        );
    
    // 过滤已交互的房源
    Set<Long> interactedIds = userInteractions.stream()
        .map(UserPropertyInteraction::getPropertyId)
        .collect(Collectors.toSet());
    
    // 步骤5：为每个候选房源打分
    Map<Long, Double> propertyScores = new HashMap<>();
    
    for (Property property : allProperties) {
        if (interactedIds.contains(property.getId())) {
            continue;  // 跳过已交互的
        }
        
        double score = 0.0;
        
        // 5.1 城市偏好得分（权重30%）
        if (cityPreferences.containsKey(property.getCity())) {
            score += cityPreferences.get(property.getCity()) * 0.3;
        }
        
        // 5.2 房源类型得分（权重20%）
        if (typePreferences.containsKey(property.getPropertyType())) {
            score += typePreferences.get(property.getPropertyType()) * 0.2;
        }
        
        // 5.3 价格相似度得分（权重25%）
        // 价格越接近用户偏好，得分越高
        double priceDiff = Math.abs(property.getPrice().doubleValue() - avgPrice);
        double priceSimilarity = 1.0 / (1.0 + priceDiff / avgPrice);
        score += priceSimilarity * 0.25;
        
        // 5.4 卧室数相似度得分（权重15%）
        int bedroomDiff = Math.abs(property.getBedrooms() - avgBedrooms);
        double bedroomSimilarity = 1.0 / (1.0 + bedroomDiff);
        score += bedroomSimilarity * 0.15;
        
        // 5.5 房源评分加分（权重10%）
        score += (property.getRating().doubleValue() / 5.0) * 0.1;
        
        propertyScores.put(property.getId(), score);
    }
    
    // 步骤6：按得分排序，返回前N个
    return propertyScores.entrySet().stream()
        .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
        .limit(limit)
        .map(entry -> propertyMapper.selectById(entry.getKey()))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
}
```

**打分权重说明**:
- 城市偏好：30%（位置是选择民宿的重要因素）
- 房源类型：20%（用户对公寓、别墅等有明确偏好）
- 价格相似度：25%（价格是关键决策因素）
- 卧室数相似度：15%（反映住宿人数需求）
- 房源评分：10%（保证推荐质量）

---

## 数据流程

### 📈 用户行为数据收集

```
用户操作 → 生成交互记录 → 存入数据库 → 影响推荐结果
```

**交互类型**:
- `VIEW`: 浏览房源（查看详情时自动记录）
- `FAVORITE`: 收藏房源
- `BOOK`: 预订房源
- `RATING`: 评价房源

**数据表结构** (`user_property_interaction`):
```sql
CREATE TABLE user_property_interaction (
    id BIGINT PRIMARY KEY,
    user_id BIGINT,          -- 用户ID
    property_id BIGINT,      -- 房源ID
    type VARCHAR(20),        -- 交互类型
    rating INT,              -- 评分（1-5）
    created_at TIMESTAMP     -- 交互时间
);
```

---

### 🔄 推荐流程完整示例

**场景**: 用户小明想查看推荐房源

```
步骤1：前端发起请求
GET /api/recommendations?limit=10
Headers: Authorization: Bearer <JWT令牌>

步骤2：后端验证JWT令牌
- 解析令牌获取用户ID（假设为101）
- 验证令牌有效性
- 检查用户权限

步骤3：调用推荐服务
recommendationService.getRecommendations(101, 10)

步骤4：执行推荐算法
4.1 调用协同过滤算法
    - 查询用户101的交互记录 [房源1, 房源2, 房源3]
    - 查询所有用户的交互记录
    - 找到相似用户：用户102（相似度0.6）、用户103（相似度0.5）
    - 汇总相似用户喜欢但用户101未交互的房源
    - 结果：[房源5(得分1.1), 房源7(得分0.6), 房源9(得分0.5)]

4.2 调用内容推荐算法
    - 分析用户101喜欢的房源特征
      城市：厦门（2次）、北京（1次）
      类型：公寓（2次）、别墅（1次）
      平均价格：280元
      平均卧室：2间
    - 对所有可用房源打分
    - 结果：[房源4(得分2.8), 房源5(得分2.5), 房源8(得分2.1)]

4.3 混合算法融合
    房源5: 1.1*0.6 + 2.5*0.4 = 1.66
    房源4: 0*0.6 + 2.8*0.4 = 1.12
    房源7: 0.6*0.6 + 0*0.4 = 0.36
    房源8: 0*0.6 + 2.1*0.4 = 0.84
    房源9: 0.5*0.6 + 0*0.4 = 0.30
    
    最终排序：[房源5, 房源4, 房源8, 房源7, 房源9]

步骤5：返回结果
{
  "success": true,
  "message": "推荐列表获取成功",
  "data": [
    {房源5的详细信息},
    {房源4的详细信息},
    ...
  ]
}
```

---

## 快速开始

### 📋 环境要求
- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Redis (可选，用于缓存)
- Elasticsearch (可选，用于搜索)

### 🚀 启动步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd springboot-recommendation-system
```

2. **配置数据库**
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE homestay_recommendation;

# 导入SQL脚本
mysql -u root -p homestay_recommendation < sql/schema.sql
```

3. **修改配置文件**
编辑 `src/main/resources/application.properties`:
```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/homestay_recommendation
spring.datasource.username=root
spring.datasource.password=your_password

# Redis配置（可选）
spring.redis.host=localhost
spring.redis.port=6379

# JWT密钥
jwt.secret=your-secret-key
jwt.expiration=86400000
```

4. **编译运行**
```bash
# 使用Maven编译
mvn clean package

# 运行应用
java -jar target/homestay-recommendation-1.0.0.jar

# 或使用Maven直接运行
mvn spring-boot:run
```

5. **访问接口**
- 后端API: `http://localhost:8080`
- Swagger文档: `http://localhost:8080/swagger-ui/index.html`

---

## 零基础入门指南

### 🎓 什么是 Spring Boot？

Spring Boot 是一个 Java 开发框架，它帮助开发者快速构建 Web 应用。就像搭积木一样，Spring Boot 提供了很多现成的"积木"（组件），我们只需要组装和配置就能快速开发。

### 🎓 什么是 REST API？

REST API 是一种网络接口设计风格。简单理解：
- **URL** 是地址：`/api/properties` 表示房源相关的功能
- **HTTP 方法** 是操作：
  - `GET`: 查询数据（例如：获取房源列表）
  - `POST`: 创建数据（例如：发布新房源）
  - `PUT`: 更新数据（例如：修改房源信息）
  - `DELETE`: 删除数据（例如：删除房源）

### 🎓 什么是 JWT？

JWT (JSON Web Token) 是一种身份认证方式。

**传统方式** (Session):
```
1. 用户登录 → 服务器生成Session并存储
2. 服务器返回SessionID
3. 后续请求携带SessionID
4. 服务器查找Session验证身份
```

**JWT方式**:
```
1. 用户登录 → 服务器生成JWT令牌（包含用户信息）
2. 服务器返回JWT令牌
3. 后续请求携带JWT令牌
4. 服务器直接解析JWT验证身份（无需查数据库）
```

**优势**: 服务器无需存储状态，更易扩展

### 🎓 什么是 MyBatis-Plus？

MyBatis-Plus 是一个数据库操作框架，简化了 SQL 编写。

**传统方式**:
```java
// 需要手写SQL
@Select("SELECT * FROM property WHERE id = #{id}")
Property getPropertyById(Long id);
```

**MyBatis-Plus 方式**:
```java
// 自动生成SQL，无需手写
Property property = propertyMapper.selectById(id);
```

### 🎓 推荐算法如何工作？

**通俗解释**:

1. **协同过滤** = "物以类聚，人以群分"
   ```
   你和小李都喜欢看科幻电影
   小李还喜欢看惊悚片
   那么系统会向你推荐惊悚片
   ```

2. **内容推荐** = "你喜欢这个，那你可能也喜欢那个"
   ```
   你喜欢的民宿都在海边
   系统会继续推荐海边的民宿
   ```

3. **混合推荐** = "综合考虑，给你最好的"
   ```
   60%考虑相似用户的选择
   40%考虑房源本身的特征
   ```

### 🎓 如何测试接口？

**方法1: 使用 Swagger UI**
1. 启动项目后访问：`http://localhost:8080/swagger-ui/index.html`
2. 选择接口
3. 点击 "Try it out"
4. 填写参数
5. 点击 "Execute"

**方法2: 使用 Postman**
1. 下载安装 Postman
2. 创建新请求
3. 设置请求方法和URL
4. 添加请求头（JWT认证）：
   ```
   Key: Authorization
   Value: Bearer <your-jwt-token>
   ```
5. 发送请求

**方法3: 使用 curl 命令**
```bash
# 注册用户
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"123456"}'

# 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'

# 获取推荐（需要替换<token>为登录返回的令牌）
curl -X GET http://localhost:8080/api/recommendations \
  -H "Authorization: Bearer <token>"
```

### 🎓 常见问题

**Q: 为什么需要 JWT 认证？**
A: 为了安全！只有登录的用户才能访问某些接口，防止未授权访问。

**Q: 推荐算法的数据从哪里来？**
A: 来自用户的浏览、收藏、预订等行为记录，存储在 `user_property_interaction` 表中。

**Q: 如果新用户没有历史数据怎么办？**
A: 这叫"冷启动问题"。系统会推荐热门房源或高评分房源作为初始推荐。

**Q: 推荐结果会实时更新吗？**
A: 会的！每次用户浏览或预订房源时，系统都会记录，影响下次的推荐结果。

---

## 📚 核心概念总结

### 分层架构
```
Controller  → 接收HTTP请求，调用Service
    ↓
Service     → 实现业务逻辑，调用Mapper
    ↓
Mapper      → 执行数据库操作
    ↓
Database    → 存储数据
```

### 数据流转
```
前端 → Controller → Service → Mapper → MySQL
                ↓
              Redis (缓存)
                ↓
         Elasticsearch (搜索)
```

### 安全机制
```
请求 → JWT过滤器 → 验证令牌 → 设置安全上下文 → 执行业务逻辑
```

---

## 📝 文档说明

- **RECOMMENDED_MODULE_INTERFACE.md**: 推荐模块接口文档
- **OTHER_API_DOC.md**: 其他接口文档
- **技术栈列表及其部署步骤.docx**: 部署文档

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

## 📄 许可证

本项目采用 MIT 许可证。

---

**祝您学习愉快！🎉**

如有疑问，欢迎提Issue或联系开发团队。
