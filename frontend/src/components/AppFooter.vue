<template>
  <el-footer class="app-footer">
    <p>民宿推荐系统 ©{{ currentYear }} Created by IFREELIFE</p>
  </el-footer>
</template>

<script setup>
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.app-footer {
  text-align: center;
  padding: 20px;
  background: #f5f5f5;
  color: #666;
}
</style>
