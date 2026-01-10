<template>
  <el-config-provider :locale="locale">
    <div id="app">
      <AppHeader v-if="!isAuthPage" />
      <el-main class="main-content">
        <router-view />
      </el-main>
      <AppFooter v-if="!isAuthPage" />
    </div>
  </el-config-provider>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import AppHeader from './components/AppHeader.vue'
import AppFooter from './components/AppFooter.vue'
import { useUserStore } from './store/user'

const route = useRoute()
const userStore = useUserStore()
const locale = zhCn

const isAuthPage = computed(() => {
  return (
    route.path === '/login' ||
    route.path === '/register' ||
    route.path.startsWith('/admin')
  )
})

onMounted(() => {
  userStore.checkAuth()
})
</script>

<style>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 20px;
  margin-top: 60px;
}
</style>
