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
        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-avatar
              :src="profileForm.avatar"
              :icon="UserFilled"
              :size="64"
              class="avatar-preview"
            />
            <div class="upload-area">
              <el-upload
                class="upload-block"
                drag
                action="#"
                :auto-upload="false"
                :file-list="avatarFileList"
                :limit="1"
                accept="image/*"
                :on-change="handleAvatarFileChange"
                :on-remove="handleAvatarFileChange"
              >
                <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
                <div class="el-upload__text">拖拽或点击选择头像</div>
              </el-upload>
              <el-button
                type="primary"
                :loading="uploadingAvatar"
                style="margin-top: 12px"
                @click="uploadAvatar"
              >
                上传头像
              </el-button>
              <el-input
                v-model="profileForm.avatar"
                placeholder="上传后将自动填充头像链接"
                style="margin-top: 12px"
              />
              <div v-if="profileForm.avatar" class="avatar-url">当前头像链接：{{ profileForm.avatar }}</div>
            </div>
          </div>
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
import { UploadFilled, UserFilled } from '@element-plus/icons-vue'
import userService from '../services/userService'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const savingProfile = ref(false)
const uploadingAvatar = ref(false)
const avatarFileList = ref([])

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

const handleAvatarFileChange = (file, files) => {
  avatarFileList.value = files.slice(-1)
}

const uploadAvatar = async () => {
  if (!avatarFileList.value.length) {
    ElMessage.warning('请先选择要上传的头像')
    return
  }
  // el-upload 在拖拽/选择时会将原始文件放在 raw 字段中，手动构造的列表则直接使用文件对象
  const file = avatarFileList.value[0].raw || avatarFileList.value[0]
  if (!(file instanceof File)) {
    ElMessage.error('请选择有效的图片文件')
    return
  }
  uploadingAvatar.value = true
  try {
    const response = await userService.uploadAvatar(file)
    if (response.success) {
      profileForm.avatar = response.data
      ElMessage.success('头像上传成功，请保存信息')
    }
  } catch (error) {
    ElMessage.error(error.message || '头像上传失败')
  } finally {
    uploadingAvatar.value = false
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

.avatar-upload {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.avatar-preview {
  border: 1px solid #ebeef5;
}

.upload-area {
  max-width: 340px;
}

.avatar-url {
  margin-top: 8px;
  color: #666;
  word-break: break-all;
}
</style>
