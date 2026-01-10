<template>
  <el-header class="app-header">
    <div class="header-content">
      <div class="left-section">
        <div class="logo" @click="router.push('/')">
          <el-icon><House /></el-icon>
          <span>民宿推荐</span>
        </div>
        <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          @select="handleMenuSelect"
          class="header-menu"
        >
          <template v-if="userStore.isAdmin">
            <el-menu-item index="/admin">
              <el-icon><Grid /></el-icon>
              <span>管理员</span>
            </el-menu-item>
          </template>
          <template v-else-if="!userStore.isLandlord">
            <el-menu-item index="/">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/properties">
              <el-icon><Grid /></el-icon>
              <span>浏览房源</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.isAuthenticated" index="/recommendations">
              <el-icon><StarFilled /></el-icon>
              <span>为你推荐</span>
            </el-menu-item>
          </template>
          <template v-else>
            <el-menu-item index="/my-properties">
              <el-icon><HomeFilled /></el-icon>
              <span>我的房源</span>
            </el-menu-item>
            <el-menu-item index="/property-occupancy">
              <el-icon><Grid /></el-icon>
              <span>入住与剩余房间</span>
            </el-menu-item>
            <el-menu-item index="/landlord/orders">
              <el-icon><StarFilled /></el-icon>
              <span>退订审核</span>
            </el-menu-item>
          </template>
        </el-menu>
      </div>
      
      <div class="right-section">
        <template v-if="userStore.isAuthenticated">
          <el-dropdown @command="handleCommand">
            <el-avatar :icon="UserFilled" />
            <template #dropdown>
              <el-dropdown-menu>
                <template v-if="userStore.isAdmin">
                  <el-dropdown-item command="admin">管理员控制台</el-dropdown-item>
                </template>
                <template v-else>
                  <el-dropdown-item command="my-orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <template v-if="userStore.isLandlord">
                    <el-dropdown-item command="my-properties">
                      我的房源
                    </el-dropdown-item>
                    <el-dropdown-item command="create-property">
                      发布房源
                    </el-dropdown-item>
                    <el-dropdown-item command="property-occupancy">
                      入住与剩余房间
                    </el-dropdown-item>
                    <el-dropdown-item command="landlord-orders">
                      退订审核
                    </el-dropdown-item>
                  </template>
                </template>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button @click="router.push('/login')">登录</el-button>
          <el-button type="primary" @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import { House, HomeFilled, Grid, StarFilled, UserFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleMenuSelect = (index) => {
  router.push(index)
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'my-orders') {
    router.push('/my-orders')
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'my-properties') {
    router.push('/my-properties')
  } else if (command === 'property-occupancy') {
    router.push('/property-occupancy')
  } else if (command === 'landlord-orders') {
    router.push('/landlord/orders')
  } else if (command === 'create-property') {
    router.push('/landlord/create-property')
  } else if (command === 'admin') {
    router.push('/admin')
  }
}
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  height: 60px;
  padding: 0 50px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.left-section {
  display: flex;
  align-items: center;
  gap: 50px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
}

.header-menu {
  border: none;
  background: transparent;
}

.right-section {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
