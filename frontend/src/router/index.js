import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomePage.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginPage.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterPage.vue')
  },
  {
    path: '/properties',
    name: 'PropertyList',
    component: () => import('../views/PropertyListPage.vue')
  },
  {
    path: '/properties/:id',
    name: 'PropertyDetail',
    component: () => import('../views/PropertyDetailPage.vue')
  },
  {
    path: '/recommendations',
    name: 'Recommendations',
    component: () => import('../views/RecommendationPage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/my-properties',
    name: 'MyProperties',
    component: () => import('../views/MyPropertiesPage.vue'),
    meta: { requiresAuth: true, requiresLandlord: true }
  },
  {
    path: '/property-occupancy',
    name: 'PropertyOccupancy',
    component: () => import('../views/PropertyOccupancyPage.vue'),
    meta: { requiresAuth: true, requiresLandlord: true }
  },
  {
    path: '/my-orders',
    name: 'MyOrders',
    component: () => import('../views/MyOrdersPage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/ProfilePage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/create-property',
    redirect: '/my-properties'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 如果目标路由需要认证但用户未登录，重定向到登录页
  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    next({ name: 'Login' })
  } else if (to.meta.requiresLandlord && !userStore.isLandlord) {
    // 非房东访问房东专属页面时，返回首页
    next({ name: 'Home' })
  } else if (to.name === 'Login' && userStore.isAuthenticated) {
    // 如果用户已登录且尝试访问登录页，重定向到首页
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
