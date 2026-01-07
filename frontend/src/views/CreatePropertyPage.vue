<template>
  <div class="create-property-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>发布新房源</span>
        </div>
      </template>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="房源标题" prop="title">
          <el-input v-model="form.title" placeholder="例如：市中心温馨公寓" />
        </el-form-item>

        <el-form-item label="房源描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="详细描述您的房源..."
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="城市" prop="city">
              <el-input v-model="form.city" placeholder="例如：北京" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="区域" prop="district">
              <el-input v-model="form.district" placeholder="例如：朝阳区" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" placeholder="街道门牌号" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="价格（/晚）" prop="price">
              <el-input-number
                v-model="form.price"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房源类型" prop="propertyType">
              <el-select v-model="form.propertyType" placeholder="选择房源类型" style="width: 100%">
                <el-option label="公寓" value="apartment" />
                <el-option label="独栋房屋" value="house" />
                <el-option label="别墅" value="villa" />
                <el-option label="单间" value="studio" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="卧室数量" prop="bedrooms">
              <el-input-number v-model="form.bedrooms" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="卫生间数量" prop="bathrooms">
              <el-input-number v-model="form.bathrooms" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最多可住" prop="maxGuests">
              <el-input-number v-model="form.maxGuests" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="设施服务">
          <el-input
            v-model="form.amenities"
            placeholder="多个设施请用逗号分隔，例如：WiFi,空调,洗衣机"
          />
        </el-form-item>

        <el-form-item label="房源图片">
          <el-upload
            ref="uploadRef"
            class="upload-area"
            drag
            multiple
            :auto-upload="false"
            :file-list="fileList"
            accept="image/*"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将图片拖到此处，或<em>点击上传</em>
            </div>
            <div class="el-upload__tip">支持一次选择多张图片</div>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleSubmit">
            发布房源
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import propertyService from '../services/propertyService'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const uploadRef = ref()
const fileList = ref([])

const form = reactive({
  title: '',
  description: '',
  city: '',
  district: '',
  address: '',
  price: 0,
  bedrooms: 1,
  bathrooms: 1,
  maxGuests: 1,
  propertyType: '',
  amenities: '',
  images: []
})

const rules = {
  title: [{ required: true, message: '请输入房源标题', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区域', trigger: 'blur' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  bedrooms: [{ required: true, message: '请输入卧室数量', trigger: 'blur' }],
  bathrooms: [{ required: true, message: '请输入卫生间数量', trigger: 'blur' }],
  maxGuests: [{ required: true, message: '请输入最多可住人数', trigger: 'blur' }]
}

const handleFileChange = (_, uploadFiles) => {
  fileList.value = uploadFiles
}

const handleFileRemove = (_, uploadFiles) => {
  fileList.value = uploadFiles
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        let currentImages = Array.isArray(form.images) ? form.images : []
        if (fileList.value.length > 0) {
          const formData = new FormData()
          let hasFile = false
          fileList.value.forEach((file) => {
            if (file.raw) {
              formData.append('files', file.raw)
              hasFile = true
            }
          })
          if (!hasFile) {
            throw new Error('请选择有效的图片文件')
          }

          const uploadResp = await propertyService.uploadImages(formData)
          if (!uploadResp.success) {
            throw new Error(uploadResp.message || '图片上传失败')
          }
          currentImages = uploadResp.data || []
          form.images = currentImages
        }

        const propertyData = {
          ...form,
          amenities: form.amenities
            ? JSON.stringify(form.amenities.split(',').map((a) => a.trim()))
            : '[]',
          images: JSON.stringify(currentImages)
        }

        const response = await propertyService.createProperty(propertyData)
        if (response.success) {
          ElMessage.success('房源发布成功！')
          router.push('/my-properties')
        }
      } catch (error) {
        ElMessage.error(error.message || '发布失败，请重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.create-property-page {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.upload-area {
  width: 100%;
}
</style>
