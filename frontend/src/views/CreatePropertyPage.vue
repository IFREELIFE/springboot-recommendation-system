<template>
  <div class="create-property-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑房源' : '发布新房源' }}</span>
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
            class="upload-block"
            drag
            multiple
            action="#"
            :auto-upload="false"
            :file-list="fileList"
            :on-change="handleFileChange"
            :on-remove="handleFileChange"
            accept="image/*"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">将图片拖到此处，或<em>点击上传</em></div>
            <div class="el-upload__tip">支持多张图片，上传后保存到本地服务器</div>
          </el-upload>
          <el-button
            type="primary"
            size="default"
            style="margin-top: 12px"
            :loading="uploading"
            @click="uploadImages"
          >
            上传图片
          </el-button>
          <div v-if="uploadedImages.length" class="uploaded-list">
            <el-tag
              v-for="url in uploadedImages"
              :key="url"
              type="success"
              class="upload-tag"
            >
              {{ url }}
            </el-tag>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '发布房源' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import propertyService from '../services/propertyService'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const uploading = ref(false)
const fileList = ref([])
const uploadedImages = ref([])
const propertyId = computed(() => (route.query.id ? String(route.query.id) : ''))
const isEdit = computed(() => Boolean(propertyId.value))

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
  images: ''
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

const handleFileChange = (file, files) => {
  fileList.value = files
}

// Accepts stringified JSON or array and returns comma-separated amenities
const parseAmenities = (value) => {
  if (!value) return ''
  if (Array.isArray(value)) return value.join(',')
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed.join(',') : ''
  } catch (e) {
    return ''
  }
}

const parseImages = (data) => {
  if (data?.imagesBase64?.length) {
    return data.imagesBase64.map((b64) => `data:image/*;base64,${b64}`)
  }
  if (data?.images) {
    try {
      const parsed = JSON.parse(data.images)
      return Array.isArray(parsed) ? parsed : []
    } catch (e) {
      return []
    }
  }
  return []
}

const loadProperty = async (id) => {
  loading.value = true
  try {
    const response = await propertyService.getPropertyById(id)
    if (response.success) {
      const data = response.data || {}
      form.title = data.title || ''
      form.description = data.description || ''
      form.city = data.city || ''
      form.district = data.district || ''
      form.address = data.address || ''
      form.price = data.price ?? 0
      form.bedrooms = data.bedrooms ?? 1
      form.bathrooms = data.bathrooms ?? 1
      form.maxGuests = data.maxGuests ?? 1
      form.propertyType = data.propertyType || ''
      form.amenities = parseAmenities(data.amenities)
      uploadedImages.value = parseImages(data)
    } else {
      ElMessage.error('房源信息获取失败')
    }
  } catch (error) {
    if (error.response?.status === 404) {
      ElMessage.error('房源不存在或已删除')
    } else if (error.response?.status === 403) {
      ElMessage.error('您无权编辑该房源')
    } else {
      ElMessage.error(error.message || '加载房源信息失败')
    }
  } finally {
    loading.value = false
  }
}

const uploadImages = async () => {
  if (!fileList.value.length) {
    ElMessage.warning('请先选择要上传的图片')
    return
  }
  uploading.value = true
  try {
    const files = fileList.value
      .map((item) => item.raw || item)
      .filter((f) => f instanceof File)
    const response = await propertyService.uploadImages(files)
    if (response.success) {
      uploadedImages.value = response.data
      ElMessage.success('图片上传成功')
    }
  } catch (error) {
    ElMessage.error(error.message || '上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const propertyData = {
          ...form,
          amenities: form.amenities
            ? JSON.stringify(form.amenities.split(',').map((a) => a.trim()))
            : '[]',
          images: uploadedImages.value.length
            ? JSON.stringify(uploadedImages.value)
            : '[]'
        }

        const response = isEdit.value
          ? await propertyService.updateProperty(propertyId.value, propertyData)
          : await propertyService.createProperty(propertyData)

        if (response.success) {
          ElMessage.success(isEdit.value ? '房源更新成功！' : '房源发布成功！')
          router.push('/my-properties')
        }
      } catch (error) {
        ElMessage.error(error.message || '保存失败，请重试')
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  if (isEdit.value) {
    if (!/^[0-9]+$/.test(propertyId.value)) {
      ElMessage.error('房源信息无效')
      router.push('/my-properties')
      return
    }
    loadProperty(propertyId.value)
  }
})
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

.uploaded-list {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.upload-tag {
  margin-right: 4px;
}
</style>
