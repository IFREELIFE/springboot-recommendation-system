# Homestay Recommendation System (民宿房源推荐系统)

Spring Boot based backend management system for homestay property recommendations with hybrid recommendation algorithms.

> **关于代码生成**: 本项目使用 AI 辅助工具直接生成代码到仓库中。详细的代码生成流程和最佳实践请参考 [CODE_GENERATION_PROCESS.md](CODE_GENERATION_PROCESS.md)。
>
> **About Code Generation**: This project uses AI-assisted tools to generate code directly into the repository. For detailed code generation workflow and best practices, please refer to [CODE_GENERATION_PROCESS.md](CODE_GENERATION_PROCESS.md).

## 功能模块 (Features)

### 1. 用户管理模块 (User Management)
- 用户注册与登录 (User registration and login)
- 角色权限控制 (Role-based access control)
  - USER: 普通用户 (Regular users)
  - LANDLORD: 房东 (Property owners)
  - ADMIN: 管理员 (Administrators)

### 2. 房源管理模块 (Property Management)
- CRUD操作 (Create, Read, Update, Delete)
- 房东上传和管理房源信息 (Landlords can upload and manage property information)
- 房源搜索和筛选 (Property search and filtering)

### 3. 推荐模块 (Recommendation Module)
- **混合推荐算法** (Hybrid Recommendation Algorithm)
  - 协同过滤 (Collaborative Filtering): 基于用户相似度推荐
  - 基于内容推荐 (Content-Based Filtering): 基于房源特征推荐
  - 混合策略: 60% 协同过滤 + 40% 基于内容

### 4. 订单模块 (Order Module)
- 创建订单 (Create orders)
- 查看订单历史 (View order history)
- 订单状态管理 (Order status management)
  - PENDING: 待确认
  - CONFIRMED: 已确认
  - CANCELLED: 已取消
  - COMPLETED: 已完成

### 5. 搜索模块 (Search Module)
- 集成Elasticsearch支持全文搜索 (Elasticsearch integration for full-text search)
- 按城市、价格、房间数筛选 (Filter by city, price, bedrooms)

### 6. 安全认证模块 (Security & Authentication)
- JWT Token认证 (JWT token authentication)
- Spring Security保护接口 (Spring Security for API protection)
- BCrypt密码加密 (BCrypt password encryption)

## 技术栈 (Tech Stack)

- **框架**: Spring Boot 2.7.18
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **搜索引擎**: Elasticsearch
- **安全**: Spring Security + JWT
- **ORM**: MyBatis-Plus 3.5.3.1
- **构建工具**: Maven
- **Java版本**: 11

## 项目结构 (Project Structure)

```
src/main/java/com/recommendation/homestay/
├── HomestayRecommendationApplication.java  # Main application
├── controller/                              # REST API Controllers
│   ├── AuthController.java                 # Authentication endpoints
│   ├── PropertyController.java             # Property management endpoints
│   ├── OrderController.java                # Order management endpoints
│   └── RecommendationController.java       # Recommendation endpoints
├── service/                                 # Business logic
│   ├── AuthService.java                    # Authentication service
│   ├── PropertyService.java                # Property management
│   ├── OrderService.java                   # Order management
│   └── RecommendationService.java          # Recommendation algorithms
├── mapper/                                  # MyBatis-Plus Mapper interfaces
│   ├── UserMapper.java
│   ├── PropertyMapper.java
│   ├── OrderMapper.java
│   └── UserPropertyInteractionMapper.java
├── entity/                                  # MyBatis-Plus entities
│   ├── User.java
│   ├── Property.java
│   ├── Order.java
│   └── UserPropertyInteraction.java
├── dto/                                     # Data Transfer Objects
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── PropertyRequest.java
│   ├── OrderRequest.java
│   └── ApiResponse.java
└── security/                                # Security configuration
    ├── SecurityConfig.java
    ├── JwtTokenProvider.java
    ├── JwtAuthenticationFilter.java
    ├── UserPrincipal.java
    └── CustomUserDetailsService.java

sql/
├── schema.sql                               # Database schema
└── sample_data.sql                          # Sample data for testing
```

## 环境要求 (Prerequisites)

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+ (optional)
- Elasticsearch 7.x+ (optional)

## 快速开始 (Quick Start)

### 1. 克隆项目 (Clone Repository)
```bash
git clone https://github.com/IFREELIFE/springboot-recommendation-system.git
cd springboot-recommendation-system
```

### 2. 配置数据库 (Configure Database)

创建MySQL数据库并执行SQL脚本:
```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p < sql/sample_data.sql
```

### 3. 配置应用 (Configure Application)

编辑 `src/main/resources/application.properties`:

```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/homestay_recommendation
spring.datasource.username=root
spring.datasource.password=your_password

# Redis Configuration (optional)
spring.redis.host=localhost
spring.redis.port=6379

# Elasticsearch Configuration (optional)
spring.elasticsearch.rest.uris=http://localhost:9200
```

### 4. 编译项目 (Build Project)
```bash
mvn clean install
```

### 5. 运行应用 (Run Application)
```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动

## API接口文档 (API Documentation)

### 认证接口 (Authentication APIs)

#### 注册 (Register)
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com",
  "role": "USER"
}
```

#### 登录 (Login)
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}
```

### 房源接口 (Property APIs)

#### 获取所有房源 (Get All Properties)
```http
GET /api/properties?page=0&size=10
```

#### 获取房源详情 (Get Property Details)
```http
GET /api/properties/{id}
```

#### 创建房源 (Create Property) - 需要LANDLORD角色
```http
POST /api/properties
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "title": "Cozy Apartment",
  "description": "A beautiful apartment",
  "city": "Beijing",
  "district": "Chaoyang",
  "address": "123 Main St",
  "price": 500.00,
  "bedrooms": 2,
  "bathrooms": 1,
  "maxGuests": 4,
  "propertyType": "apartment"
}
```

#### 搜索房源 (Search Properties)
```http
GET /api/properties/search?city=Beijing&minPrice=300&maxPrice=800&bedrooms=2
```

### 订单接口 (Order APIs)

#### 创建订单 (Create Order)
```http
POST /api/orders
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "propertyId": 1,
  "checkInDate": "2024-12-25",
  "checkOutDate": "2024-12-28",
  "guestCount": 2
}
```

#### 获取我的订单 (Get My Orders)
```http
GET /api/orders/my-orders?page=0&size=10
Authorization: Bearer {jwt_token}
```

### 推荐接口 (Recommendation APIs)

#### 获取混合推荐 (Get Hybrid Recommendations)
```http
GET /api/recommendations?limit=10
Authorization: Bearer {jwt_token}
```

#### 获取协同过滤推荐 (Get Collaborative Filtering Recommendations)
```http
GET /api/recommendations/collaborative?limit=10
Authorization: Bearer {jwt_token}
```

#### 获取基于内容推荐 (Get Content-Based Recommendations)
```http
GET /api/recommendations/content-based?limit=10
Authorization: Bearer {jwt_token}
```

## 推荐算法说明 (Recommendation Algorithm)

### 协同过滤 (Collaborative Filtering)
- 基于用户-房源交互矩阵
- 使用Jaccard相似度计算用户相似性
- 推荐相似用户喜欢的房源

### 基于内容推荐 (Content-Based Filtering)
- 分析用户历史偏好
- 考虑因素:
  - 城市偏好 (30%)
  - 房源类型 (20%)
  - 价格相似度 (25%)
  - 房间数相似度 (15%)
  - 评分加成 (10%)

### 混合策略 (Hybrid Approach)
- 协同过滤权重: 60%
- 基于内容权重: 40%
- 冷启动问题: 返回热门或高评分房源

## 缓存策略 (Caching Strategy)

使用Redis缓存:
- 房源列表缓存
- 推荐结果缓存
- 热门房源缓存
- 高评分房源缓存

## MyBatis-Plus ORM 实现 (MyBatis-Plus Implementation)

本项目完全采用 **MyBatis-Plus** 作为 ORM 框架，没有使用 Spring Data JPA。

### 核心特性
- **BaseMapper**: 所有 Mapper 接口继承 BaseMapper，获得标准 CRUD 方法
- **QueryWrapper**: 使用 QueryWrapper 构建灵活的查询条件
- **自动填充**: 使用 MyMetaObjectHandler 自动填充 createdAt 和 updatedAt 字段
- **分页插件**: 集成 MyBatis-Plus 分页插件，支持自动分页

### 示例代码

#### 实体类 (Entity)
```java
@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

#### Mapper 接口
```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 继承 BaseMapper 自动获得 CRUD 方法
}
```

#### Service 层查询
```java
// 使用 QueryWrapper 查询
QueryWrapper<User> query = new QueryWrapper<>();
query.eq("username", username);
User user = userMapper.selectOne(query);

// 分页查询
Page<Property> page = new Page<>(pageNum, pageSize);
IPage<Property> result = propertyMapper.selectPage(page, queryWrapper);
```

### 详细文档
完整的 MyBatis-Plus 实现说明请参考: [MYBATIS_PLUS_IMPLEMENTATION.md](MYBATIS_PLUS_IMPLEMENTATION.md)

## 数据库设计 (Database Schema)

### 核心表
1. **users** - 用户表
2. **properties** - 房源表
3. **orders** - 订单表
4. **user_property_interactions** - 用户交互表 (用于推荐算法)

详见: `sql/schema.sql`

## 测试 (Testing)

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=AuthServiceTest
```

## 部署 (Deployment)

### 打包应用
```bash
mvn clean package
```

### 运行JAR文件
```bash
java -jar target/homestay-recommendation-1.0.0.jar
```

## 文档 (Documentation)

本项目提供完整的文档支持：

- **[README.md](README.md)**: 项目概述和快速开始指南
- **[CODE_GENERATION_PROCESS.md](CODE_GENERATION_PROCESS.md)**: 代码生成流程和工作方式说明
- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)**: 完整的 API 接口文档
- **[ARCHITECTURE.md](ARCHITECTURE.md)**: 系统架构设计文档
- **[DEPLOYMENT.md](DEPLOYMENT.md)**: 详细的部署指南
- **[PROJECT_EVALUATION.md](PROJECT_EVALUATION.md)**: 项目整体评价与改进建议

## 许可证 (License)

MIT License

## 作者 (Author)

IFREELIFE

## 贡献 (Contributing)

欢迎提交Issue和Pull Request!
