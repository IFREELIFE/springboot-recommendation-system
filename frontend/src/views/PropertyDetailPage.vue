<template>
  <div class="property-detail-page" v-loading="loading">
    <el-card v-if="property">
      <el-row :gutter="24">
        <el-col :xs="24" :md="12">
          <el-carousel height="400px">
            <el-carousel-item v-for="(img, index) in images" :key="index">
              <el-image :src="img" fit="cover" style="width: 100%; height: 100%" />
            </el-carousel-item>
          </el-carousel>
        </el-col>
        <el-col :xs="24" :md="12">
          <h1>{{ property.title }}</h1>
          <el-rate v-model="rating" disabled show-score />
          <div class="price">
            <span class="amount">¥{{ property.price }}</span>
            <span class="unit">/晚</span>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="位置">
              {{ property.city }} - {{ property.district }}
            </el-descriptions-item>
            <el-descriptions-item label="详细地址">
              {{ property.address }}
            </el-descriptions-item>
            <el-descriptions-item label="房型">
              {{ property.propertyType || '未指定' }}
            </el-descriptions-item>
            <el-descriptions-item label="可住人数">
              最多 {{ property.maxGuests }} 人
            </el-descriptions-item>
            <el-descriptions-item label="房间配置">
              {{ property.bedrooms }} 卧室 · {{ property.bathrooms }} 卫生间
            </el-descriptions-item>
          </el-descriptions>
          <el-button
            type="primary"
            size="large"
            style="width: 100%; margin-top: 20px"
            @click="showBookingDialog"
            :disabled="!property.available"
          >
            {{ property.available ? '立即预订' : '暂不可订' }}
          </el-button>
        </el-col>
      </el-row>

      <el-card style="margin-top: 24px">
        <template #header>房源描述</template>
        <p>{{ property.description || '暂无描述' }}</p>
      </el-card>

      <el-card v-if="amenities.length > 0" style="margin-top: 24px">
        <template #header>设施与服务</template>
        <el-tag v-for="amenity in amenities" :key="amenity" style="margin: 4px">
          {{ amenity }}
        </el-tag>
      </el-card>
    </el-card>

    <el-dialog v-model="bookingDialogVisible" title="预订房源" width="500px">
      <el-form :model="bookingForm" label-width="80px">
        <el-form-item label="入住日期">
          <el-date-picker
            v-model="bookingForm.dates"
            type="daterange"
            range-separator="至"
            start-placeholder="入住日期"
            end-placeholder="退房日期"
            :disabled-date="disabledDate"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="入住人数">
          <el-input-number
            v-model="bookingForm.guestCount"
            :min="1"
            :max="property?.maxGuests || 10"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="bookingForm.remarks"
            type="textarea"
            :rows="3"
            placeholder="特殊要求或备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBooking">确认预订</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import propertyService from '../services/propertyService'
import orderService from '../services/orderService'
import { useUserStore } from '../store/user'
import { storeToRefs } from 'pinia'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isAuthenticated } = storeToRefs(userStore)

const property = ref(null)
const loading = ref(true)
const bookingDialogVisible = ref(false)

const bookingForm = reactive({
  dates: [],
  guestCount: 1,
  remarks: ''
})

const images = computed(() => {
  if (property.value?.images) {
    try {
      return JSON.parse(property.value.images)
    } catch {
      return ['https://via.placeholder.com/600x400']
    }
  }
  return ['https://via.placeholder.com/600x400']
})

const amenities = computed(() => {
  if (property.value?.amenities) {
    try {
      return JSON.parse(property.value.amenities)
    } catch {
      return []
    }
  }
  return []
})

const rating = computed(() => Number(property.value?.rating) || 0)

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

onMounted(async () => {
  try {
    const response = await propertyService.getPropertyById(route.params.id)
    if (response.success) {
      property.value = response.data
    }
  } catch (error) {
    ElMessage.error('获取房源信息失败')
  } finally {
    loading.value = false
  }
})

const showBookingDialog = () => {
  if (!isAuthenticated.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  bookingDialogVisible.value = true
}

const handleBooking = async () => {
  if (!bookingForm.dates || bookingForm.dates.length !== 2) {
    ElMessage.warning('请选择入住日期')
    return
  }

  try {
    const orderData = {
      propertyId: property.value.id,
      checkInDate: dayjs(bookingForm.dates[0]).format('YYYY-MM-DD'),
      checkOutDate: dayjs(bookingForm.dates[1]).format('YYYY-MM-DD'),
      guestCount: bookingForm.guestCount,
      remarks: bookingForm.remarks
    }

    const response = await orderService.createOrder(orderData)
    if (response.success) {
      ElMessage.success('预订成功！')
      bookingDialogVisible.value = false
      router.push('/my-orders')
    }
  } catch (error) {
    ElMessage.error(error.message || '预订失败')
  }
}
</script>

<style scoped>
.price {
  margin: 20px 0;
}

.amount {
  font-size: 32px;
  font-weight: bold;
  color: #f56c6c;
}

.unit {
  font-size: 18px;
  color: #999;
}
</style>
