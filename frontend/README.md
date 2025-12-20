# 民宿推荐系统前端 (Frontend)

基于 Vue 3 + Vite + Element Plus 的民宿推荐系统前端应用。

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vite** - 下一代前端构建工具
- **Vue Router 4** - 官方路由管理器
- **Pinia** - Vue 状态管理库
- **Element Plus** - 基于 Vue 3 的组件库
- **Axios** - HTTP 客户端
- **Day.js** - 轻量级日期处理库

## 功能特性

### 用户功能
- ✅ 用户注册与登录（JWT认证）
- ✅ 浏览房源列表（分页）
- ✅ 搜索和筛选房源（城市、价格、卧室数）
- ✅ 查看房源详情（图片轮播、设施展示）
- ✅ 在线预订房源
- ✅ 查看我的订单
- ✅ 取消待确认订单

### 房东功能
- ✅ 发布新房源
- ✅ 管理我的房源
- ✅ 查看房源统计（预订数、浏览量、评分）
- ✅ 删除房源

### 推荐功能
- ✅ 智能混合推荐（协同过滤 + 基于内容）
- ✅ 协同过滤推荐
- ✅ 基于内容推荐

## 快速开始

### 1. 安装依赖

```bash
cd frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

应用将在 http://localhost:3000 启动

### 3. 构建生产版本

```bash
npm run build
```

构建文件将生成在 `dist/` 目录

### 4. 预览生产构建

```bash
npm run preview
```

## 项目结构

```
frontend/
├── index.html                 # HTML 入口文件
├── package.json              # 项目依赖配置
├── vite.config.js            # Vite 配置文件
└── src/
    ├── main.js               # 应用入口
    ├── App.vue               # 根组件
    ├── router/               # 路由配置
    │   └── index.js
    ├── store/                # 状态管理
    │   └── user.js          # 用户状态
    ├── components/           # 可复用组件
    │   ├── AppHeader.vue    # 顶部导航栏
    │   ├── AppFooter.vue    # 页脚
    │   └── PropertyCard.vue # 房源卡片
    ├── views/               # 页面组件
    │   ├── HomePage.vue            # 首页
    │   ├── LoginPage.vue           # 登录页
    │   ├── RegisterPage.vue        # 注册页
    │   ├── PropertyListPage.vue    # 房源列表
    │   ├── PropertyDetailPage.vue  # 房源详情
    │   ├── RecommendationPage.vue  # 推荐页面
    │   ├── MyPropertiesPage.vue    # 我的房源
    │   ├── MyOrdersPage.vue        # 我的订单
    │   └── CreatePropertyPage.vue  # 发布房源
    ├── services/            # API 服务
    │   ├── api.js                    # Axios 实例配置
    │   ├── authService.js            # 认证服务
    │   ├── propertyService.js        # 房源服务
    │   ├── orderService.js           # 订单服务
    │   └── recommendationService.js  # 推荐服务
    └── assets/              # 静态资源
        └── styles/
            └── main.css     # 全局样式
```

## API 代理配置

开发环境下，Vite 会将 `/api` 请求代理到后端服务器 `http://localhost:8080`。

如需修改后端地址，编辑 `vite.config.js`：

```javascript
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://your-backend-url:8080',
        changeOrigin: true
      }
    }
  }
})
```

## 主要页面说明

### 1. 首页 (HomePage)
- 展示热门房源（按预订量）
- 展示高评分房源
- 提供快速导航

### 2. 房源列表 (PropertyListPage)
- 分页展示所有房源
- 支持按城市、价格范围、卧室数筛选
- 搜索功能

### 3. 房源详情 (PropertyDetailPage)
- 图片轮播展示
- 详细房源信息
- 在线预订功能（需登录）
- 设施服务展示

### 4. 推荐页面 (RecommendationPage)
- **智能混合推荐**：60% 协同过滤 + 40% 基于内容
- **协同过滤推荐**：基于相似用户的推荐
- **基于内容推荐**：基于房源特征的推荐

### 5. 我的订单 (MyOrdersPage)
- 查看所有订单记录
- 订单状态展示（待确认、已确认、已取消、已完成）
- 取消待确认订单功能

### 6. 我的房源 (MyPropertiesPage)
- 房东查看和管理房源
- 显示预订数、浏览量、评分等统计
- 删除房源功能

### 7. 发布房源 (CreatePropertyPage)
- 房东发布新房源
- 完整的表单验证
- 图片和设施配置

## 用户角色

系统支持三种用户角色：

1. **普通用户 (USER)**
   - 浏览和搜索房源
   - 预订房源
   - 查看订单

2. **房东 (LANDLORD)**
   - 普通用户所有功能
   - 发布房源
   - 管理房源

3. **管理员 (ADMIN)**
   - 所有功能权限

## 特性亮点

### 1. 响应式设计
- 使用 Element Plus 的栅格系统
- 支持移动端、平板、桌面端

### 2. 状态管理
- 使用 Pinia 进行全局状态管理
- 用户认证状态持久化

### 3. 路由守卫
- 自动检查登录状态
- 未登录用户访问受保护页面自动跳转登录

### 4. API 拦截器
- 自动添加 JWT Token
- 统一错误处理
- 401 自动跳转登录

### 5. 用户体验
- Loading 状态提示
- 友好的错误消息
- 表单验证
- 操作确认对话框

## 开发建议

### 代码规范
- 使用 Vue 3 Composition API
- 遵循 Vue 3 官方风格指南
- 组件使用 `<script setup>` 语法

### 状态管理
- 使用 Pinia 管理全局状态
- 页面级状态使用 `ref` 和 `reactive`

### 样式
- 优先使用 Element Plus 组件样式
- 自定义样式使用 scoped CSS
- 全局样式放在 `assets/styles/main.css`

## 常见问题

### Q: 如何修改主题颜色？
A: 在 `main.js` 中配置 Element Plus 主题：
```javascript
app.use(ElementPlus, {
  locale: zhCn,
  // 添加主题配置
})
```

### Q: 登录后刷新页面需要重新登录？
A: Token 已存储在 localStorage，检查浏览器是否禁用了 localStorage。

### Q: 图片无法显示？
A: 确保图片 URL 可访问，或使用占位图片。

### Q: API 请求失败？
A: 检查后端服务是否启动（http://localhost:8080）。

## 浏览器兼容性

- Chrome >= 87
- Firefox >= 78
- Safari >= 14
- Edge >= 88

## 许可证

MIT License
