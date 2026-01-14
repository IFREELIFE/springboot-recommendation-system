# 零基础实战教程 - 从注册到获取推荐

> 本教程将通过实际操作，带你完整体验民宿推荐系统的核心功能

---

## 📚 准备工作

### 工具准备
1. **安装Postman**: 用于测试API接口
   - 下载地址: https://www.postman.com/downloads/
   
2. **启动项目**: 确保系统已启动
   ```bash
   mvn spring-boot:run
   ```
   
3. **验证启动成功**: 浏览器访问
   ```
   http://localhost:8080/swagger-ui/index.html
   ```

---

## 🎯 实战步骤

### 步骤1: 注册新用户

**目标**: 创建一个新的用户账号

**操作步骤**:
1. 打开Postman
2. 创建新请求
   - 方法: `POST`
   - URL: `http://localhost:8080/api/auth/register`
3. 切换到 `Body` 标签
4. 选择 `raw` 和 `JSON`
5. 输入以下内容:
```json
{
  "username": "xiaoming",
  "email": "xiaoming@example.com",
  "password": "123456",
  "role": "USER"
}
```
6. 点击 `Send` 发送请求

**期望结果**:
```json
{
  "success": true,
  "message": "用户注册成功",
  "data": 1
}
```

**说明**:
- `data: 1` 表示新创建的用户ID为1
- 角色可选: USER(普通用户), LANDLORD(房东), ADMIN(管理员)

---

### 步骤2: 用户登录获取Token

**目标**: 登录并获取JWT令牌，用于后续认证

**操作步骤**:
1. 创建新请求
   - 方法: `POST`
   - URL: `http://localhost:8080/api/auth/login`
2. Body内容:
```json
{
  "username": "xiaoming",
  "password": "123456"
}
```
3. 点击 `Send`

**期望结果**:
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNjQwMDAwMDAwLCJleHAiOjE2NDAwODY0MDB9.xxxxx",
    "tokenType": "Bearer",
    "userId": 1,
    "username": "xiaoming",
    "email": "xiaoming@example.com",
    "role": "ROLE_USER"
  }
}
```

**重要**: 复制 `token` 的值，后续所有需要认证的接口都需要它！

---

### 步骤3: 查看房源列表

**目标**: 浏览所有可用的房源

**操作步骤**:
1. 创建新请求
   - 方法: `GET`
   - URL: `http://localhost:8080/api/properties?page=0&size=10`
2. 点击 `Send`

**期望结果**:
```json
{
  "success": true,
  "message": "成功",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "温馨海景民宿",
        "city": "厦门",
        "price": 299.00,
        "bedrooms": 2,
        "rating": 4.8
      },
      // ... 更多房源
    ],
    "totalElements": 50,
    "totalPages": 5,
    "currentPage": 0
  }
}
```

**说明**:
- 此接口无需认证，任何人都可以查看
- `page`: 页码（从0开始）
- `size`: 每页显示数量

---

### 步骤4: 搜索特定房源

**目标**: 按条件筛选房源

**操作步骤**:
1. 创建新请求
   - 方法: `GET`
   - URL: `http://localhost:8080/api/properties/search?city=厦门&minPrice=200&maxPrice=500&bedrooms=2`
2. 点击 `Send`

**期望结果**:
```json
{
  "success": true,
  "message": "成功",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "温馨海景民宿",
        "city": "厦门",
        "price": 299.00,
        "bedrooms": 2
      }
    ],
    "totalElements": 5
  }
}
```

**搜索参数说明**:
- `city`: 城市名（模糊匹配）
- `minPrice`: 最低价格
- `maxPrice`: 最高价格
- `bedrooms`: 卧室数量（精确匹配）

---

### 步骤5: 查看房源详情

**目标**: 查看某个房源的详细信息

**操作步骤**:
1. 创建新请求
   - 方法: `GET`
   - URL: `http://localhost:8080/api/properties/1`
2. 点击 `Send`

**期望结果**:
```json
{
  "success": true,
  "message": "房源获取成功",
  "data": {
    "id": 1,
    "title": "温馨海景民宿",
    "description": "面朝大海，春暖花开...",
    "city": "厦门",
    "address": "厦门市思明区",
    "price": 299.00,
    "bedrooms": 2,
    "bathrooms": 1,
    "rating": 4.8,
    "viewCount": 150,
    "bookingCount": 30,
    "images": ["/api/uploads/image1.jpg", "/api/uploads/image2.jpg"],
    "landlordName": "李房东"
  }
}
```

**注意**: 每次查看详情，浏览量(`viewCount`)会自动+1，这会影响推荐算法！

---

### 步骤6: 创建订单（需要认证）

**目标**: 预订一个房源

**操作步骤**:
1. 创建新请求
   - 方法: `POST`
   - URL: `http://localhost:8080/api/orders`
2. 添加认证头（重要！）
   - 切换到 `Headers` 标签
   - 添加: `Authorization: Bearer <你的token>`
3. Body内容:
```json
{
  "propertyId": 1,
  "checkInDate": "2024-02-01",
  "checkOutDate": "2024-02-05",
  "guests": 2
}
```
4. 点击 `Send`

**期望结果**:
```json
{
  "success": true,
  "message": "订单创建成功",
  "data": {
    "id": 1,
    "orderNumber": "ORD20240101123456",
    "propertyId": 1,
    "userId": 1,
    "checkInDate": "2024-02-01",
    "checkOutDate": "2024-02-05",
    "guests": 2,
    "totalAmount": 1196.00,
    "status": "PENDING"
  }
}
```

**订单状态说明**:
- `PENDING`: 待支付
- `CONFIRMED`: 已确认
- `CANCELLED`: 已取消
- `COMPLETED`: 已完成

**如何添加Token**:
```
在Postman的Headers标签中添加:
Key: Authorization
Value: Bearer eyJhbGciOiJIUzUxMiJ9.xxxxx（步骤2获取的token）
```

---

### 步骤7: 获取个性化推荐（核心功能）⭐

**目标**: 获取系统为你推荐的房源

**前提条件**: 
- 必须登录（有token）
- 最好有一些浏览或预订记录（影响推荐质量）

**操作步骤**:
1. 创建新请求
   - 方法: `GET`
   - URL: `http://localhost:8080/api/recommendations?limit=10`
2. 添加认证头
   - `Authorization: Bearer <你的token>`
3. 点击 `Send`

**期望结果**:
```json
{
  "success": true,
  "message": "推荐列表获取成功",
  "data": [
    {
      "id": 5,
      "title": "厦门鼓浪屿海景房",
      "city": "厦门",
      "price": 320.00,
      "bedrooms": 2,
      "rating": 4.9
    },
    {
      "id": 8,
      "title": "厦门曾厝垵民宿",
      "city": "厦门",
      "price": 280.00,
      "bedrooms": 2,
      "rating": 4.7
    }
    // ... 更多推荐
  ]
}
```

**推荐原理**:
1. 系统分析你浏览过的房源（步骤5）
2. 系统分析你预订过的房源（步骤6）
3. 找到和你兴趣相似的用户
4. 推荐他们喜欢但你还没看过的房源

---

### 步骤8: 对比不同推荐算法

**8.1 协同过滤推荐**
```
GET /api/recommendations/collaborative?limit=10
Headers: Authorization: Bearer <token>
```

**8.2 内容相似推荐**
```
GET /api/recommendations/content-based?limit=10
Headers: Authorization: Bearer <token>
```

**对比观察**:
- 协同过滤: 基于其他用户的行为，可能推荐不同类型的房源
- 内容推荐: 基于房源特征，推荐的房源会更相似
- 混合推荐: 综合两者优势，结果更准确

---

## 🧪 实验：观察推荐变化

### 实验目的
理解用户行为如何影响推荐结果

### 实验步骤

**阶段1: 初始状态**
1. 注册新用户 `testuser1`
2. 登录获取token
3. 获取推荐列表（记录结果）

**阶段2: 浏览特定类型房源**
1. 连续浏览5个厦门的房源（调用详情接口）
2. 再次获取推荐列表
3. 观察: 是否推荐了更多厦门的房源？

**阶段3: 预订房源**
1. 预订一个200-300元的房源
2. 再次获取推荐列表
3. 观察: 是否推荐了类似价格的房源？

**阶段4: 模拟相似用户**
1. 注册另一个用户 `testuser2`
2. 让 `testuser2` 浏览和 `testuser1` 相同的房源
3. 让 `testuser2` 浏览额外的房源（例如房源10）
4. 切换回 `testuser1`
5. 获取推荐列表
6. 观察: 是否推荐了房源10？

---

## 🎯 进阶操作

### 房东角色体验

**1. 注册房东账号**
```json
POST /api/auth/register
{
  "username": "landlord1",
  "email": "landlord1@example.com",
  "password": "123456",
  "role": "LANDLORD"
}
```

**2. 发布房源**
```json
POST /api/properties
Headers: Authorization: Bearer <landlord_token>
{
  "title": "我的温馨小屋",
  "description": "舒适便捷",
  "city": "北京",
  "address": "北京市朝阳区",
  "price": 350.00,
  "bedrooms": 1,
  "bathrooms": 1,
  "propertyType": "公寓"
}
```

**3. 查看我的房源**
```
GET /api/properties/landlord/my-properties
Headers: Authorization: Bearer <landlord_token>
```

---

## ❓ 常见问题解决

### 问题1: 401 Unauthorized
**原因**: Token过期或无效
**解决**: 重新登录获取新token

### 问题2: 推荐结果为空
**原因**: 
- 新用户没有历史行为
- 数据库中房源太少
**解决**: 
- 多浏览几个房源
- 确保数据库有足够的房源数据

### 问题3: Token在哪里使用？
**解决**: 在Postman的Headers中添加
```
Key: Authorization
Value: Bearer <你的token>
```
注意: "Bearer"和token之间有一个空格！

### 问题4: 如何查看SQL日志？
**解决**: 在 `application.properties` 添加:
```properties
mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl
```

---

## 🎓 学习小贴士

1. **先注册再登录**: 登录获取token是访问其他接口的前提
2. **保存好token**: 建议在Postman中设置环境变量存储token
3. **观察返回结果**: 每个接口的返回结果都包含重要信息
4. **理解状态码**: 200表示成功，401表示未认证，400表示请求错误
5. **多做实验**: 通过不同的操作组合，观察推荐结果的变化

---

## 📝 实践任务

### 任务1: 完整流程体验
- [ ] 注册用户
- [ ] 登录获取token
- [ ] 浏览至少3个房源
- [ ] 创建一个订单
- [ ] 获取推荐列表

### 任务2: 对比测试
- [ ] 获取混合推荐结果
- [ ] 获取协同过滤推荐结果
- [ ] 获取内容推荐结果
- [ ] 对比三种推荐的差异

### 任务3: 房东体验
- [ ] 注册房东账号
- [ ] 发布房源
- [ ] 查看自己的房源列表

---

## 🎉 恭喜！

如果你完成了以上所有步骤，说明你已经掌握了系统的核心功能！

**下一步**:
1. 阅读源码，理解实现原理
2. 尝试修改推荐算法的权重
3. 添加新的推荐特征
4. 优化推荐性能

**继续学习**:
- 深入研究推荐算法
- 学习Spring Boot高级特性
- 了解分布式系统设计

---

**💪 实践是最好的学习方式，加油！**
