# API 接口文档 (API Documentation)

## 基础信息 (Base Information)

- **Base URL**: `http://localhost:8080`
- **认证方式**: JWT Bearer Token
- **Content-Type**: `application/json`

---

## 认证接口 (Authentication APIs)

### 1. 用户注册 (User Registration)

**Endpoint**: `POST /api/auth/register`

**Request Body**:
```json
{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com",
  "phone": "13800138000",
  "role": "USER"
}
```

**Response**:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": 1
}
```

**角色选项** (Role Options):
- `USER`: 普通用户
- `LANDLORD`: 房东
- `ADMIN`: 管理员

---

### 2. 用户登录 (User Login)

**Endpoint**: `POST /api/auth/login`

**Request Body**:
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "role": "ROLE_USER"
  }
}
```

---

## 房源接口 (Property APIs)

### 3. 获取所有房源 (Get All Properties)

**Endpoint**: `GET /api/properties`

**Query Parameters**:
- `page` (optional): 页码，默认 0
- `size` (optional): 每页数量，默认 10
- `sortBy` (optional): 排序字段，默认 `createdAt`
- `sortDir` (optional): 排序方向，`ASC` 或 `DESC`，默认 `DESC`

**Response**:
```json
{
  "success": true,
  "message": "Properties retrieved successfully",
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0
  }
}
```

---

### 4. 获取房源详情 (Get Property Details)

**Endpoint**: `GET /api/properties/{id}`

**Response**:
```json
{
  "success": true,
  "message": "Property retrieved successfully",
  "data": {
    "id": 1,
    "title": "Cozy Downtown Apartment",
    "description": "Modern apartment...",
    "city": "Beijing",
    "district": "Chaoyang",
    "address": "123 Chaoyang Road",
    "price": 500.00,
    "bedrooms": 2,
    "bathrooms": 1,
    "maxGuests": 4,
    "propertyType": "apartment",
    "amenities": "[\"WiFi\",\"Air Conditioning\"]",
    "images": "[\"image1.jpg\"]",
    "imagesBase64": ["<base64 string>", "<base64 string>"],
    "available": true,
    "rating": 4.5,
    "reviewCount": 20,
    "viewCount": 150,
    "bookingCount": 35
  }
}
```

---

### 5. 创建房源 (Create Property) - 需要 LANDLORD 角色

**Endpoint**: `POST /api/properties`

**Headers**:
```
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "title": "Cozy Apartment",
  "description": "A beautiful apartment in downtown",
  "city": "Beijing",
  "district": "Chaoyang",
  "address": "123 Main Street",
  "price": 500.00,
  "bedrooms": 2,
  "bathrooms": 1,
  "maxGuests": 4,
  "propertyType": "apartment",
  "amenities": "[\"WiFi\",\"Kitchen\",\"Air Conditioning\"]",
  "images": "[\"image1.jpg\",\"image2.jpg\"]"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Property created successfully",
  "data": {
    "id": 1,
    ...
  }
}
```

---

### 5.1 上传房源图片 (Upload Property Images) - 需要 LANDLORD 角色

**Endpoint**: `POST /api/properties/upload`

**Headers**:
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Request**:
- `files`: 多张图片文件，使用相同的字段名 `files` 传递
- `propertyId` (可选): 若传入，将在上传成功后把图片地址追加保存到该房源的 `images` 字段

**Response**:
```json
{
  "success": true,
  "message": "Images uploaded successfully",
  "data": [
    "/api/uploads/8d08c2a1f0d84e39bd1f98f0a524bba1.jpg",
    "/api/uploads/9e9f22c541cf4b338c05a62012d613ab.png"
  ]
}
```

---

### 6. 更新房源 (Update Property) - 需要 LANDLORD 角色

**Endpoint**: `PUT /api/properties/{id}`

**Headers**:
```
Authorization: Bearer {token}
```

**Request Body**: 同创建房源

---

### 7. 删除房源 (Delete Property) - 需要 LANDLORD 角色

**Endpoint**: `DELETE /api/properties/{id}`

**Headers**:
```
Authorization: Bearer {token}
```

---

### 8. 获取我的房源 (Get My Properties) - 需要 LANDLORD 角色

**Endpoint**: `GET /api/properties/landlord/my-properties`

**Headers**:
```
Authorization: Bearer {token}
```

**Query Parameters**:
- `page` (optional): 页码
- `size` (optional): 每页数量

---

### 9. 搜索房源 (Search Properties)

**Endpoint**: `GET /api/properties/search`

**Query Parameters**:
- `city` (optional): 城市
- `minPrice` (optional): 最低价格
- `maxPrice` (optional): 最高价格
- `bedrooms` (optional): 卧室数量
- `page` (optional): 页码
- `size` (optional): 每页数量

**Example**:
```
GET /api/properties/search?city=Beijing&minPrice=300&maxPrice=800&bedrooms=2&page=0&size=10
```

---

### 10. 获取热门房源 (Get Popular Properties)

**Endpoint**: `GET /api/properties/popular`

**Response**: 返回预订量最高的前10个房源

---

### 11. 获取高评分房源 (Get Top Rated Properties)

**Endpoint**: `GET /api/properties/top-rated`

**Response**: 返回评分最高的前10个房源

---

## 订单接口 (Order APIs)

### 12. 创建订单 (Create Order) - 需要认证

**Endpoint**: `POST /api/orders`

**Headers**:
```
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "propertyId": 1,
  "checkInDate": "2024-12-25",
  "checkOutDate": "2024-12-28",
  "guestCount": 2,
  "remarks": "Late check-in please"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": 1,
    "orderNumber": "ORD-12345678",
    "checkInDate": "2024-12-25",
    "checkOutDate": "2024-12-28",
    "guestCount": 2,
    "totalPrice": 1500.00,
    "status": "PENDING",
    "remarks": "Late check-in please"
  }
}
```

---

### 13. 获取订单详情 (Get Order Details) - 需要认证

**Endpoint**: `GET /api/orders/{id}`

**Headers**:
```
Authorization: Bearer {token}
```

---

### 14. 根据订单号获取订单 (Get Order by Number) - 需要认证

**Endpoint**: `GET /api/orders/number/{orderNumber}`

**Headers**:
```
Authorization: Bearer {token}
```

**Example**:
```
GET /api/orders/number/ORD-12345678
```

---

### 15. 获取我的订单 (Get My Orders) - 需要认证

**Endpoint**: `GET /api/orders/my-orders`

**Headers**:
```
Authorization: Bearer {token}
```

**Query Parameters**:
- `page` (optional): 页码
- `size` (optional): 每页数量

---

### 16. 更新订单状态 (Update Order Status) - 需要认证

**Endpoint**: `PUT /api/orders/{id}/status`

**Headers**:
```
Authorization: Bearer {token}
```

**Query Parameters**:
- `status`: 订单状态 (`PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`)

**Example**:
```
PUT /api/orders/1/status?status=CONFIRMED
```

---

### 17. 取消订单 (Cancel Order) - 需要认证

**Endpoint**: `DELETE /api/orders/{id}`

**Headers**:
```
Authorization: Bearer {token}
```

---

## 推荐接口 (Recommendation APIs)

### 18. 获取混合推荐 (Get Hybrid Recommendations) - 需要认证

**Endpoint**: `GET /api/recommendations`

**Headers**:
```
Authorization: Bearer {token}
```

**Query Parameters**:
- `limit` (optional): 推荐数量，默认 10

**Response**:
```json
{
  "success": true,
  "message": "Recommendations retrieved successfully",
  "data": [
    {
      "id": 5,
      "title": "Mountain Retreat",
      ...
    },
    ...
  ]
}
```

**算法说明**:
- 60% 协同过滤 (基于用户相似度)
- 40% 基于内容推荐 (基于房源特征)

---

### 19. 获取协同过滤推荐 (Get Collaborative Filtering) - 需要认证

**Endpoint**: `GET /api/recommendations/collaborative`

**Headers**:
```
Authorization: Bearer {token}
```

**Query Parameters**:
- `limit` (optional): 推荐数量，默认 10

**算法说明**:
- 基于用户-房源交互矩阵
- 使用 Jaccard 相似度计算用户相似性
- 推荐相似用户喜欢的房源

---

### 20. 获取基于内容推荐 (Get Content-Based Recommendations) - 需要认证

**Endpoint**: `GET /api/recommendations/content-based`

**Headers**:
```
Authorization: Bearer {token}
```

**Query Parameters**:
- `limit` (optional): 推荐数量，默认 10

**算法说明**:
- 分析用户历史偏好
- 考虑因素:
  - 城市偏好 (30%)
  - 房源类型 (20%)
  - 价格相似度 (25%)
  - 房间数相似度 (15%)
  - 评分加成 (10%)

---

## 状态码 (HTTP Status Codes)

- `200 OK`: 请求成功
- `201 Created`: 资源创建成功
- `400 Bad Request`: 请求参数错误
- `401 Unauthorized`: 未授权或token无效
- `403 Forbidden`: 没有权限
- `404 Not Found`: 资源不存在
- `500 Internal Server Error`: 服务器错误

---

## 错误响应格式 (Error Response Format)

```json
{
  "success": false,
  "message": "Error message description",
  "data": null
}
```

---

## 测试流程 (Testing Workflow)

1. **注册用户**:
   ```bash
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username":"testuser","password":"password123","email":"test@example.com","role":"USER"}'
   ```

2. **登录获取Token**:
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"testuser","password":"password123"}'
   ```

3. **使用Token访问受保护的接口**:
   ```bash
   curl -X GET http://localhost:8080/api/recommendations \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

---

## 注意事项 (Notes)

1. JWT Token 默认有效期为 24 小时
2. 所有需要认证的接口必须在请求头中携带 Bearer Token
3. 房源的创建、更新、删除需要 LANDLORD 或 ADMIN 角色
4. 推荐算法会根据用户的历史交互数据提供个性化推荐
5. 如果用户没有历史数据（冷启动），系统会推荐热门或高评分房源
