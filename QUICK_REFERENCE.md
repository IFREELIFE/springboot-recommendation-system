# 快速参考指南

## 📋 一句话总结各模块

### 核心模块速查表

| 模块 | 作用 | 核心类 | 关键方法 |
|------|------|--------|----------|
| **认证模块** | 用户注册、登录、JWT令牌管理 | `AuthService` | `registerUser()`, `loginUser()` |
| **房源模块** | 房源的增删改查、搜索、上传图片 | `PropertyService` | `createProperty()`, `searchProperties()` |
| **订单模块** | 订单创建、状态管理、退订审核 | `OrderService` | `createOrder()`, `updateOrderStatus()` |
| **推荐模块** | 智能推荐算法 | `RecommendationService` | `getRecommendations()` ⭐ |

---

## 🎯 推荐算法三剑客

### 1. 混合推荐（主推）
- **权重**: 协同过滤60% + 内容推荐40%
- **适用**: 所有用户
- **接口**: `GET /api/recommendations`

### 2. 协同过滤
- **原理**: 找相似用户，推荐他们喜欢的房源
- **公式**: 杰卡德相似度 = 交集 / 并集
- **接口**: `GET /api/recommendations/collaborative`

### 3. 内容推荐
- **原理**: 分析房源特征，推荐相似房源
- **特征**: 城市(30%) + 类型(20%) + 价格(25%) + 卧室(15%) + 评分(10%)
- **接口**: `GET /api/recommendations/content-based`

---

## 🔑 核心代码片段

### JWT认证使用

```java
// 1. 登录获取token
POST /api/auth/login
{
  "username": "test",
  "password": "123456"
}

// 2. 使用token访问接口
GET /api/recommendations
Headers: Authorization: Bearer <token>
```

### MyBatis-Plus常用操作

```java
// 查询单条
Property property = propertyMapper.selectById(id);

// 条件查询
QueryWrapper<Property> wrapper = new QueryWrapper<>();
wrapper.eq("city", "厦门").ge("price", 200);
List<Property> list = propertyMapper.selectList(wrapper);

// 分页查询
Page<Property> page = new Page<>(1, 10);
IPage<Property> result = propertyMapper.selectPage(page, wrapper);
```

### 推荐算法调用

```java
// 混合推荐
List<Property> recommendations = 
    recommendationService.getRecommendations(userId, 10);

// 协同过滤
List<Property> collaborative = 
    recommendationService.getCollaborativeFilteringRecommendations(userId, 10);

// 内容推荐
List<Property> contentBased = 
    recommendationService.getContentBasedRecommendations(userId, 10);
```

---

## 📊 数据库表关系

```
users (用户表)
  ├─ 1:N → properties (房源表) [landlord_id]
  ├─ 1:N → orders (订单表) [user_id]
  └─ 1:N → user_property_interaction (交互表) [user_id]

properties (房源表)
  ├─ 1:N → orders (订单表) [property_id]
  └─ 1:N → user_property_interaction (交互表) [property_id]

orders (订单表)
  ├─ N:1 → users (用户表) [user_id]
  └─ N:1 → properties (房源表) [property_id]
```

---

## 🚀 常用接口速查

### 认证接口
```bash
# 注册
POST /api/auth/register

# 登录
POST /api/auth/login
```

### 房源接口
```bash
# 创建房源（需认证）
POST /api/properties

# 搜索房源（公开）
GET /api/properties/search?city=厦门&minPrice=100&maxPrice=500

# 热门房源（公开）
GET /api/properties/popular

# 高评分房源（公开）
GET /api/properties/top-rated
```

### 订单接口
```bash
# 创建订单（需认证）
POST /api/orders

# 我的订单（需认证）
GET /api/orders/my-orders

# 更新订单状态（需认证）
PUT /api/orders/{id}/status?status=CONFIRMED
```

### 推荐接口（需认证）
```bash
# 混合推荐
GET /api/recommendations?limit=10

# 协同过滤推荐
GET /api/recommendations/collaborative?limit=10

# 内容推荐
GET /api/recommendations/content-based?limit=10
```

---

## 🔧 配置要点

### application.properties 关键配置
```properties
# 数据库
spring.datasource.url=jdbc:mysql://localhost:3306/homestay_recommendation
spring.datasource.username=root
spring.datasource.password=your_password

# JWT
jwt.secret=your-secret-key-at-least-256-bits
jwt.expiration=86400000

# 文件上传
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB

# MyBatis-Plus
mybatis-plus.mapper-locations=classpath*:/mapper/**/*.xml
mybatis-plus.global-config.db-config.logic-delete-value=1
```

---

## 🐛 调试技巧

### 1. 查看SQL日志
```properties
# application.properties
mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl
```

### 2. 启用详细日志
```properties
logging.level.com.recommendation.homestay=DEBUG
logging.level.org.springframework.security=DEBUG
```

### 3. 测试推荐算法
```java
@Test
public void testRecommendation() {
    Long userId = 1L;
    List<Property> recommendations = 
        recommendationService.getRecommendations(userId, 10);
    
    System.out.println("推荐数量: " + recommendations.size());
    recommendations.forEach(p -> 
        System.out.println("房源: " + p.getTitle() + ", 价格: " + p.getPrice())
    );
}
```

---

## 💡 性能优化建议

### 1. 使用Redis缓存推荐结果
```java
@Cacheable(value = "recommendations", key = "#userId")
public List<Property> getRecommendations(Long userId, int limit) {
    // 推荐算法逻辑
}
```

### 2. 数据库索引
```sql
-- 用户交互表索引
CREATE INDEX idx_user_id ON user_property_interaction(user_id);
CREATE INDEX idx_property_id ON user_property_interaction(property_id);

-- 房源表索引
CREATE INDEX idx_city_price ON properties(city, price);
CREATE INDEX idx_available ON properties(available);
```

### 3. 分页查询
```java
// 避免一次性查询所有数据
Page<Property> page = new Page<>(pageNum, pageSize);
IPage<Property> result = propertyMapper.selectPage(page, wrapper);
```

---

## 📈 扩展建议

### 功能扩展
1. **实时推荐**: 使用Kafka处理用户行为流
2. **A/B测试**: 对比不同推荐算法效果
3. **深度学习**: 使用神经网络提升推荐精度
4. **图片识别**: 使用AI分析房源图片特征

### 架构优化
1. **微服务化**: 拆分推荐服务为独立微服务
2. **容器化**: 使用Docker部署
3. **负载均衡**: 使用Nginx或Gateway
4. **监控告警**: 集成Prometheus + Grafana

---

## 🎓 学习路径

### 初级开发者
1. 掌握Java基础
2. 学习Spring Boot基础
3. 了解RESTful API设计
4. 理解数据库基本操作

### 中级开发者
1. 深入Spring Security
2. 掌握MyBatis-Plus高级特性
3. 学习推荐算法原理
4. 了解缓存和性能优化

### 高级开发者
1. 分布式系统设计
2. 机器学习在推荐系统中的应用
3. 大数据处理
4. 系统架构设计

---

## 🔗 相关资源

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [推荐系统实践](https://book.douban.com/subject/10769749/)
- [Swagger UI](http://localhost:8080/swagger-ui/index.html)

---

**💪 持续学习，不断进步！**
