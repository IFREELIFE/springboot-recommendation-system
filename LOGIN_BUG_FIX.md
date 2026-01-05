# 登录页面跳转问题修复说明

## 问题描述

用户报告在登录页面点击登录按钮后，会发生以下现象：
1. 页面先跳转到首页 (home)
2. 立即返回到登录页面
3. 后端显示两次 SQL 查询
4. 账号查询失败

## 问题原因分析

### 1. 前端路由守卫问题

**主要原因**：在 `router/index.js` 中的路由守卫检查认证状态的时机不当。

```javascript
// 原有问题代码
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    next({ name: 'Login' })
  } else {
    next()
  }
})
```

**问题详解**：
- 用户登录成功后，调用 `router.push('/')` 导航到首页
- 路由守卫在导航前执行
- 此时虽然已经设置了 token，但由于状态更新时序问题，`isAuthenticated` 可能尚未更新
- 导致即使登录成功也可能被重定向回登录页
- 形成了 "登录 → 首页 → 登录页" 的循环

### 2. 状态管理问题

**问题代码**：在 `LoginPage.vue` 中设置用户状态的顺序不当

```javascript
// 原有问题代码
const user = await authService.login(form)
userStore.setToken(user.token)  // 先设置 token
userStore.setUser(user)          // 后设置用户信息
ElMessage.success('登录成功！')
router.push('/')                 // 立即导航
```

**问题**：
- token 和 user 设置的顺序可能导致状态不一致
- 没有等待状态完全更新就执行导航
- 用户信息没有持久化到 localStorage，页面刷新后丢失

### 3. 后端重复查询问题

**问题位置**：
- `AuthService.loginUser()` 中认证时查询一次用户（第一次 SQL 查询）
- `JwtAuthenticationFilter.doFilterInternal()` 中验证 token 时再次查询用户（第二次 SQL 查询）

这导致每次携带 token 的请求都会触发两次数据库查询。

## 修复方案

### 修复 1: 优化登录页面的状态设置

**文件**: `frontend/src/views/LoginPage.vue`

```javascript
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const user = await authService.login(form)
        // 先设置用户信息和token（顺序很重要）
        userStore.setUser(user)
        userStore.setToken(user.token)
        ElMessage.success('登录成功！')
        // 使用 await 确保状态更新后再导航
        await router.push('/')
      } catch (error) {
        ElMessage.error(error.message || '登录失败，请检查用户名和密码')
      } finally {
        loading.value = false
      }
    }
  })
}
```

**改进点**：
1. 先设置用户信息，再设置 token
2. 使用 `await router.push('/')` 确保导航是异步完成的
3. 调整顺序保证状态一致性

### 修复 2: 改进路由守卫逻辑

**文件**: `frontend/src/router/index.js`

```javascript
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 如果目标路由需要认证但用户未登录，重定向到登录页
  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    next({ name: 'Login' })
  } else if (to.name === 'Login' && userStore.isAuthenticated) {
    // 如果用户已登录且尝试访问登录页，重定向到首页
    next({ name: 'Home' })
  } else {
    next()
  }
})
```

**改进点**：
1. 添加了已登录用户访问登录页的处理
2. 防止登录后再次跳转到登录页
3. 避免循环重定向

### 修复 3: 增强用户状态持久化

**文件**: `frontend/src/store/user.js`

```javascript
function setUser(userData) {
  user.value = userData
  // 同时将用户信息保存到 localStorage 以便刷新后恢复
  if (userData) {
    localStorage.setItem('user', JSON.stringify(userData))
  } else {
    localStorage.removeItem('user')
  }
}

function checkAuth() {
  const savedToken = localStorage.getItem('token')
  const savedUser = localStorage.getItem('user')
  
  if (savedToken) {
    token.value = savedToken
    
    // 优先使用保存的用户信息
    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser)
      } catch (error) {
        console.error('Failed to parse saved user data:', error)
        // 如果解析失败，尝试从 token 中获取
        const userData = authService.getCurrentUser()
        if (userData) {
          user.value = userData
        }
      }
    } else {
      // 如果没有保存的用户信息，从 token 中获取
      const userData = authService.getCurrentUser()
      if (userData) {
        user.value = userData
      }
    }
  }
}

function logout() {
  user.value = null
  token.value = null
  localStorage.removeItem('token')
  localStorage.removeItem('user')  // 清除用户信息
}
```

**改进点**：
1. 用户信息同时保存到 localStorage
2. 页面刷新后可以恢复完整的用户状态
3. 增加错误处理，提高健壮性

### 修复 4: 应用启动时初始化认证状态

**文件**: `frontend/src/main.js`

```javascript
import { useUserStore } from './store/user'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)

// 初始化用户认证状态（在路由挂载之前）
const userStore = useUserStore()
userStore.checkAuth()

app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
```

**改进点**：
1. 在应用启动时就初始化认证状态
2. 确保路由守卫执行时状态已经准备好
3. 避免首次加载时的状态不一致

## 修复效果

修复后的登录流程：

1. ✅ 用户输入账号密码点击登录
2. ✅ 调用后端 API 进行认证
3. ✅ 认证成功后，依次设置用户信息和 token
4. ✅ 显示"登录成功"消息
5. ✅ 导航到首页，路由守卫检查通过
6. ✅ 成功停留在首页，不再跳回登录页
7. ✅ 页面刷新后用户状态保持登录

## 测试验证

### 测试场景 1：正常登录
1. 打开登录页面
2. 输入正确的用户名和密码
3. 点击登录按钮
4. **预期结果**：成功跳转到首页并停留，不再返回登录页

### 测试场景 2：页面刷新
1. 登录成功后
2. 刷新浏览器页面 (F5)
3. **预期结果**：用户保持登录状态，不会跳转到登录页

### 测试场景 3：已登录访问登录页
1. 在已登录状态下
2. 手动访问 `/login` 路径
3. **预期结果**：自动重定向到首页

### 测试场景 4：未登录访问受保护页面
1. 未登录状态下
2. 访问需要认证的页面（如 `/recommendations`）
3. **预期结果**：重定向到登录页

## 后端优化建议（可选）

虽然这次主要修复前端问题，但后端也可以进行以下优化以减少 SQL 查询次数：

### 建议 1：在 JWT token 中包含更多用户信息

修改 `JwtTokenProvider.generateToken()` 方法，在 token payload 中包含必要的用户信息，减少从数据库加载的需求。

### 建议 2：添加用户缓存

在 `CustomUserDetailsService` 中使用 Redis 或本地缓存来缓存用户信息，减少数据库查询。

```java
@Cacheable(value = "users", key = "#id")
@Transactional
public UserDetails loadUserById(Long id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    return UserPrincipal.create(user);
}
```

## 总结

本次修复主要解决了前端登录后的路由跳转循环问题，通过以下几个方面的改进：

1. ✅ 优化了登录状态设置的顺序和时机
2. ✅ 改进了路由守卫的逻辑，避免循环重定向
3. ✅ 增强了用户状态的持久化和初始化
4. ✅ 确保了应用启动时认证状态的正确加载

这些修改确保了登录流程的稳定性和用户体验的流畅性。

---

**修复日期**: 2026-01-05  
**修复版本**: v1.0.1
