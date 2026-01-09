## 推荐模块（重点标注）

路径前缀：`/api/recommendations`，需登录。

| 方法 | 地址 | 接收参数 | 作用 | 返回结果 |
| --- | --- | --- | --- | --- |
| GET | `/api/recommendations` | `limit`(int, 默认10) | 混合推荐（协同过滤 60% + 基于内容 40% 加权融合） | 推荐房源列表 `List<Property>` |
| GET | `/api/recommendations/collaborative` | `limit`(int, 默认10) | 协同过滤推荐（相似用户行为） | 推荐房源列表 |
| GET | `/api/recommendations/content-based` | `limit`(int, 默认10) | 内容相似推荐 | 推荐房源列表 |
