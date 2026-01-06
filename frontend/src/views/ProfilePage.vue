<template>
  <div class="profile-page">
    <el-card class="profile-card">
      <template #header>个人信息</template>
      <el-form :model="profileForm" label-width="90px">
        <el-form-item label="账号">
          <el-input v-model="profileForm.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="profileForm.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="profileForm.phone" />
        </el-form-item>
        <el-form-item label="头像链接">
          <el-input v-model="profileForm.avatar" placeholder="http://..." />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="profileForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="savingProfile" @click="saveProfile">
            保存信息
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import userService from '../services/userService'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const savingProfile = ref(false)

const profileForm = reactive({
  username: '',
  email: '',
  phone: '',
  avatar: '',
  password: ''
})

onMounted(() => {
  fetchProfile()
})

const fetchProfile = async () => {
  try {
    const response = await userService.getProfile()
    if (response.success) {
      Object.assign(profileForm, response.data, { password: '' })
      userStore.setUser({
        ...(userStore.user || {}),
        ...response.data,
        role: userStore.user?.role
      })
    }
  } catch (error) {
    ElMessage.error('获取个人信息失败')
  }
}

const saveProfile = async () => {
  savingProfile.value = true
  try {
    const payload = {
      email: profileForm.email,
      phone: profileForm.phone,
      avatar: profileForm.avatar
    }
    if (profileForm.password) {
      payload.password = profileForm.password
    }
    const response = await userService.updateProfile(payload)
    if (response.success) {
      ElMessage.success('个人信息已更新')
      Object.assign(profileForm, response.data, { password: '' })
      userStore.setUser({
        ...(userStore.user || {}),
        ...response.data,
        role: userStore.user?.role
      })
    }
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    savingProfile.value = false
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 700px;
  margin: 0 auto;
}

.profile-card {
  margin-top: 16px;
}
</style>
