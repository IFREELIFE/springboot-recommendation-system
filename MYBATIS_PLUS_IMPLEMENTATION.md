# MyBatis-Plus 实现说明

## 概述

本项目完全使用 **MyBatis-Plus** 作为 ORM 框架，所有数据库操作均通过 MyBatis-Plus 完成。项目中不包含任何 Spring Data JPA 代码。

## 技术栈

- **MyBatis-Plus**: 3.5.3.1 - 主要 ORM 框架
- **MySQL**: 8.0.33 - 数据库
- **Spring Boot**: 2.7.18 - 应用框架

## MyBatis-Plus 核心配置

### 1. Maven 依赖 (pom.xml)

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.3.1</version>
</dependency>
```

### 2. 数据源配置 (application.properties)

```properties
# MySQL 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/homestay_recommendation?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=nyf
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# MyBatis-Plus 配置
mybatis-plus.mapper-locations=classpath*:/mapper/**/*.xml
mybatis-plus.type-aliases-package=com.recommendation.homestay.entity
mybatis-plus.configuration.map-underscore-to-camel-case=true
mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl
mybatis-plus.global-config.db-config.id-type=auto
mybatis-plus.global-config.db-config.logic-delete-field=deleted
mybatis-plus.global-config.db-config.logic-delete-value=1
mybatis-plus.global-config.db-config.logic-not-delete-value=0
```

### 3. Java 配置类

#### MyBatisPlusConfig.java
- 配置分页拦截器，支持 MySQL 分页
- 配置 Mapper 扫描路径

#### MyMetaObjectHandler.java
- 自动填充 `createdAt` 和 `updatedAt` 时间戳字段
- 在插入时填充两个字段
- 在更新时只填充 `updatedAt` 字段

## 实体类 (Entity) 设计

所有实体类使用 MyBatis-Plus 注解：

### 核心注解说明

- `@TableName("table_name")`: 指定数据库表名
- `@TableId(type = IdType.AUTO)`: 主键自增
- `@TableField(fill = FieldFill.INSERT)`: 插入时自动填充
- `@TableField(fill = FieldFill.INSERT_UPDATE)`: 插入和更新时自动填充
- `@TableField(exist = false)`: 标记非数据库字段（用于关联对象）

### 实体类列表

1. **User** (用户)
   - 表名: `users`
   - 支持角色: USER, LANDLORD, ADMIN
   
2. **Property** (房源)
   - 表名: `properties`
   - 包含房源详情、价格、评分等信息
   
3. **Order** (订单)
   - 表名: `orders`
   - 订单状态: PENDING, CONFIRMED, CANCELLED, COMPLETED
   
4. **UserPropertyInteraction** (用户房源交互)
   - 表名: `user_property_interactions`
   - 交互类型: VIEW, FAVORITE, BOOK, REVIEW

## Mapper 接口设计

所有 Mapper 接口继承 `BaseMapper<T>`，获得基础 CRUD 功能：

### BaseMapper 提供的方法

```java
// 插入
int insert(T entity);

// 删除
int deleteById(Serializable id);
int delete(Wrapper<T> wrapper);

// 更新
int updateById(T entity);
int update(T entity, Wrapper<T> wrapper);

// 查询
T selectById(Serializable id);
List<T> selectList(Wrapper<T> wrapper);
IPage<T> selectPage(IPage<T> page, Wrapper<T> wrapper);
Long selectCount(Wrapper<T> wrapper);
```

### 自定义查询示例

#### UserMapper
完全使用 BaseMapper 提供的方法，无需自定义查询。

#### PropertyMapper
```java
@Select("SELECT * FROM properties WHERE available = 1 ORDER BY booking_count DESC LIMIT 10")
List<Property> findTop10ByAvailableTrueOrderByBookingCountDesc();

@Update("UPDATE properties SET view_count = view_count + 1 WHERE id = #{id}")
int incrementViewCount(Long id);
```

#### OrderMapper
```java
@Select("SELECT * FROM orders WHERE order_number = #{orderNumber}")
Order findByOrderNumber(String orderNumber);
```

## Service 层实现

### 使用 QueryWrapper 进行查询

QueryWrapper 是 MyBatis-Plus 推荐的查询方式，提供类型安全和灵活的查询构建：

```java
// 按用户名查询
QueryWrapper<User> queryWrapper = new QueryWrapper<>();
queryWrapper.eq("username", username);
User user = userMapper.selectOne(queryWrapper);

// 统计数量
QueryWrapper<User> countQuery = new QueryWrapper<>();
countQuery.eq("email", email);
long count = userMapper.selectCount(countQuery);

// 复杂查询
QueryWrapper<Property> query = new QueryWrapper<>();
query.eq("available", true)
     .eq("city", city)
     .ge("price", minPrice)
     .le("price", maxPrice)
     .orderByDesc("created_at");
List<Property> properties = propertyMapper.selectList(query);
```

### 分页查询

```java
// 创建分页对象
Page<Property> pageParam = new Page<>(page + 1, size);

// 构建查询条件
QueryWrapper<Property> queryWrapper = new QueryWrapper<>();
queryWrapper.eq("available", true);

// 执行分页查询
IPage<Property> result = propertyMapper.selectPage(pageParam, queryWrapper);
```

### 事务管理

使用 Spring 的 `@Transactional` 注解：

```java
@Transactional
public User registerUser(RegisterRequest request) {
    // 验证用户名唯一性
    QueryWrapper<User> query = new QueryWrapper<>();
    query.eq("username", request.getUsername());
    if (userMapper.selectCount(query) > 0) {
        throw new RuntimeException("Username already exists");
    }
    
    // 创建用户
    User user = new User();
    user.setUsername(request.getUsername());
    userMapper.insert(user);
    return user;
}
```

## 主要服务类说明

### 1. AuthService
- 用户注册：使用 `QueryWrapper` 验证唯一性，使用 `insert()` 创建用户
- 用户登录：整合 Spring Security，生成 JWT token

### 2. PropertyService
- CRUD 操作：使用 MyBatis-Plus 的 `insert()`, `updateById()`, `deleteById()`, `selectById()`
- 分页查询：使用 `Page` 和 `selectPage()`
- 搜索功能：使用 `QueryWrapper` 构建动态查询条件

### 3. OrderService
- 订单创建：事务性插入订单并更新房源预订计数
- 订单查询：使用分页和条件查询

### 4. RecommendationService
- 混合推荐算法：结合协同过滤和基于内容的推荐
- 使用 MyBatis-Plus 查询用户交互数据和房源信息

## 最佳实践

### 1. 使用 QueryWrapper 而非自定义 SQL
✅ **推荐**:
```java
QueryWrapper<User> query = new QueryWrapper<>();
query.eq("username", username);
User user = userMapper.selectOne(query);
```

❌ **避免**:
```java
@Select("SELECT * FROM users WHERE username = #{username}")
User findByUsername(String username);
```

### 2. 利用自动填充功能
实体类中使用 `@TableField(fill = FieldFill.INSERT)` 注解，配合 `MyMetaObjectHandler` 自动填充时间戳。

### 3. 使用 BaseMapper 提供的方法
优先使用 `insert()`, `updateById()`, `selectById()` 等基础方法，保持代码简洁。

### 4. 分页查询使用 IPage
使用 MyBatis-Plus 的分页插件，避免手动分页计算。

### 5. 事务边界清晰
在 Service 层方法上添加 `@Transactional` 注解，确保数据一致性。

## 与 Spring Data JPA 的对比

| 特性 | MyBatis-Plus | Spring Data JPA |
|------|-------------|-----------------|
| SQL 控制 | 完全控制，可编写原生 SQL | ORM 自动生成，较少控制 |
| 性能优化 | 易于优化 | 需要了解 Hibernate 细节 |
| 学习曲线 | 较平缓 | 较陡峭 |
| 灵活性 | 高 | 中等 |
| 代码量 | 少（继承 BaseMapper） | 少（继承 JpaRepository） |
| 适用场景 | 需要 SQL 控制的项目 | 快速开发标准 CRUD |

## 项目特点总结

1. **纯 MyBatis-Plus 实现**：项目中没有任何 JPA 相关代码
2. **最佳实践**：优先使用 QueryWrapper，避免冗余的自定义 SQL
3. **自动化**：时间戳自动填充，主键自动生成
4. **高性能**：分页插件优化，SQL 可控
5. **易维护**：代码结构清晰，注释完善

## 参考资源

- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [MyBatis-Plus GitHub](https://github.com/baomidou/mybatis-plus)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
