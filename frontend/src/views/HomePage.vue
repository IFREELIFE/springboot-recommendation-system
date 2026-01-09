<template>
  <div class="home-page">
    <el-card class="hero-section">
      <div class="hero-content">
        <h1>{{ heroTitle }}</h1>
        <p>{{ heroSubtitle }}</p>
        <div class="hero-actions">
          <template v-if="userStore.isLandlord">
            <el-button type="primary" @click="goTo('/my-properties')">房源信息</el-button>
            <el-button @click="goTo('/property-occupancy')">入住与剩余房间</el-button>
          </template>
          <template v-else>
            <el-button type="primary" @click="goTo('/recommendations')" :disabled="!userStore.isAuthenticated">
              获取专属推荐
            </el-button>
            <el-button @click="goTo('/properties')">浏览精选房源</el-button>
          </template>
        </div>
      </div>
    </el-card>

    <div class="section">
      <h2>
        <el-icon color="#f56c6c"><TrendCharts /></el-icon>
        热门房源
      </h2>
      <el-row :gutter="16" v-loading="loading">
        <el-col
          v-for="property in popularProperties"
          :key="property.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <PropertyCard :property="property" />
        </el-col>
      </el-row>
    </div>

    <div class="section">
      <h2>
        <el-icon color="#f39c12"><StarFilled /></el-icon>
        高评分房源
      </h2>
      <el-row :gutter="16" v-loading="loading">
        <el-col
          v-for="property in topRatedProperties"
          :key="property.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <PropertyCard :property="property" />
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { TrendCharts, StarFilled } from '@element-plus/icons-vue'
import PropertyCard from '../components/PropertyCard.vue'
import propertyService from '../services/propertyService'
import { useUserStore } from '../store/user'

const popularProperties = ref([])
const topRatedProperties = ref([])
const loading = ref(true)
const userStore = useUserStore()
const router = useRouter()

const heroTitle = computed(() =>
  userStore.isLandlord ? '房东工作台' : '欢迎来到民宿推荐系统'
)

const heroSubtitle = computed(() =>
  userStore.isLandlord ? '查看房源信息、入住情况与剩余房间' : '发现你的完美住宿体验'
)

const goTo = (path) => {
  router.push(path)
}

onMounted(async () => {
  try {
    const [popular, topRated] = await Promise.all([
      propertyService.getPopularProperties(),
      propertyService.getTopRatedProperties()
    ])
    if (popular.success) popularProperties.value = popular.data
    if (topRated.success) topRatedProperties.value = topRated.data
  } catch (error) {
    console.error('Failed to fetch data:', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.hero-section {
  margin-bottom: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.hero-content {
  text-align: center;
  padding: 40px 0;
}

.hero-actions {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-content h1 {
  font-size: 36px;
  margin: 0 0 16px 0;
}

.hero-content p {
  font-size: 18px;
  margin: 0;
}

.section {
  margin-bottom: 48px;
}

.section h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
}
</style>
