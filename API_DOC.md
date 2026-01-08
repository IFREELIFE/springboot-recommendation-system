# 接口文档（根目录生成）

> 说明：以下列举所有 `controller` 接口的访问地址、方法、参数、作用及返回结果；推荐模块已单独重点标注。默认服务地址为 `http://localhost:8080`，如有自定义请替换为实际 host/port。

- Swagger UI（推荐模块分组）：`http://localhost:8080/swagger-ui/index.html?urls.primaryName=recommendation`（springdoc 原生支持 `urls.primaryName` 选择分组）
- Swagger UI（其他接口分组）：`http://localhost:8080/swagger-ui/index.html?urls.primaryName=other-apis`
- OpenAPI JSON（推荐模块）：`http://localhost:8080/v3/api-docs/recommendation`
- OpenAPI JSON（其他接口）：`http://localhost:8080/v3/api-docs/other-apis`

## 通用返回格式

所有接口统一返回：

```json
{
  "success": true,
  "message": "描述",
  "data": { ... } // 业务数据或分页结构
}
```

需要鉴权的接口除登录/注册、房源公共查询和上传资源外，均要求携带 JWT。

---

## 推荐模块（重点标注）

路径前缀：`/api/recommendations`，需登录。

| 方法 | 地址 | 接收参数 | 作用 | 返回结果 |
| --- | --- | --- | --- | --- |
| GET | `/api/recommendations` | `limit`(int, 默认10) | 综合推荐（多种算法） | 推荐房源列表 `List<Property>` |
| GET | `/api/recommendations/collaborative` | `limit`(int, 默认10) | 协同过滤推荐（相似用户行为） | 推荐房源列表 |
| GET | `/api/recommendations/content-based` | `limit`(int, 默认10) | 内容相似推荐 | 推荐房源列表 |

---

## 认证模块

路径前缀：`/api/auth`

| 方法 | 地址 | 接收参数 | 作用 | 返回结果 |
| --- | --- | --- | --- | --- |
| POST | `/api/auth/register` | `RegisterRequest`：username(string, 必填)、email(string, 必填)、password(string, 必填)、role(string, 可选) | 注册用户 | `userId` |
| POST | `/api/auth/login` | `LoginRequest`：username(string, 必填)、password(string, 必填) | 用户登录获取 JWT | `JwtResponse{token,tokenType,user}` |

---

## 用户模块

路径前缀：`/api/users`，需登录。

| 方法 | 地址 | 接收参数 | 作用 | 返回结果 |
| --- | --- | --- | --- | --- |
| GET | `/api/users/me` | Header 携带 JWT | 获取当前用户信息 | `User`（password 置空） |
| PUT | `/api/users/me` | `UpdateUserRequest{email,phone,avatar,password}` | 更新当前用户资料/密码 | 更新后的 `User` |

---

## 房源模块

路径前缀：`/api/properties`。权限说明：除公开的 GET 查询外（详情、列表、搜索、热门、高评分），其余创建/上传/更新/删除/房东专属接口需房东或管理员角色。

| 方法 | 地址 | 接收参数 | 作用 | 返回结果 |
| --- | --- | --- | --- | --- |
| POST | `/api/properties` | JSON `PropertyRequest` | 创建房源（需房东/管理员） | `Property` |
| POST | `/api/properties/with-upload` | `request`(PropertyRequest)、`files[]` | 创建房源并上传图片（需房东/管理员） | `Property` |
| POST | `/api/properties/upload` | `files[]`，`propertyId`(可选) | 上传房源图片，附加到指定房源（需房东/管理员） | 图片 URL 列表 |
| PUT | `/api/properties/{id}` | `PropertyRequest` | 更新房源（需房东/管理员） | 更新后的 `Property` |
| DELETE | `/api/properties/{id}` | 路径参数 id | 删除房源（需房东/管理员） | 成功消息 |
| GET | `/api/properties/{id}` | 路径参数 id | 获取房源详情并增加浏览量 | `PropertyResponseDTO` |
| GET | `/api/properties` | `page` `size` | 分页获取房源列表 | `PageResponse<Property>` |
| GET | `/api/properties/landlord/my-properties` | `page` `size` | 获取当前房东的房源（需房东/管理员） | `PageResponse<Property>` |
| GET | `/api/properties/search` | `city` `minPrice` `maxPrice` `bedrooms` `page` `size` | 条件搜索房源 | `PageResponse<Property>` |
| GET | `/api/properties/popular` | - | 获取热门房源 | 房源列表 |
| GET | `/api/properties/top-rated` | - | 获取高评分房源 | 房源列表 |

---

## 订单模块

路径前缀：`/api/orders`，需登录。

| 方法 | 地址 | 接收参数 | 作用 | 返回结果 |
| --- | --- | --- | --- | --- |
| POST | `/api/orders` | `OrderRequest` | 创建订单 | `Order` |
| GET | `/api/orders/{id}` | 路径参数 id | 按 ID 查询订单 | `Order` |
| GET | `/api/orders/number/{orderNumber}` | 路径参数 orderNumber | 按订单号查询 | `Order` |
| GET | `/api/orders/my-orders` | `page` `size` | 获取当前用户订单 | `PageResponse<Order>` |
| PUT | `/api/orders/{id}/status` | 路径参数 id，`status`(枚举：PENDING/CONFIRMED/CANCELLED/COMPLETED) | 更新订单状态 | 更新后的 `Order` |
| DELETE | `/api/orders/{id}` | 路径参数 id | 取消订单 | 成功消息 |
