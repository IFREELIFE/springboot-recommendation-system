<template>
  <div class="home-page">
    <el-card class="hero-section">
      <div class="hero-content">
        <h1>欢迎来到民宿推荐系统</h1>
        <p>发现你的完美住宿体验</p>
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
import { ref, onMounted } from 'vue'
import { TrendCharts, StarFilled } from '@element-plus/icons-vue'
import PropertyCard from '../components/PropertyCard.vue'
import propertyService from '../services/propertyService'

const popularProperties = ref([])
const topRatedProperties = ref([])
const loading = ref(true)

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
