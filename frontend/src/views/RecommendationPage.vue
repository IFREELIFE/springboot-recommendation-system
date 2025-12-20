<template>
  <div class="recommendation-page">
    <h2>为你推荐</h2>
    <p style="color: #999; margin-bottom: 24px">
      基于你的浏览历史和偏好，我们为你精选了这些房源
    </p>

    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="智能推荐" name="hybrid">
        <el-row :gutter="16" v-loading="loading">
          <el-col
            v-for="property in hybridRecommendations"
            :key="property.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <PropertyCard :property="property" />
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="协同过滤推荐" name="collaborative">
        <el-row :gutter="16" v-loading="loading">
          <el-col
            v-for="property in collaborativeRecommendations"
            :key="property.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <PropertyCard :property="property" />
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="基于内容推荐" name="content-based">
        <el-row :gutter="16" v-loading="loading">
          <el-col
            v-for="property in contentBasedRecommendations"
            :key="property.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <PropertyCard :property="property" />
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PropertyCard from '../components/PropertyCard.vue'
import recommendationService from '../services/recommendationService'

const activeTab = ref('hybrid')
const loading = ref(true)
const hybridRecommendations = ref([])
const collaborativeRecommendations = ref([])
const contentBasedRecommendations = ref([])

onMounted(async () => {
  await fetchRecommendations()
})

const fetchRecommendations = async () => {
  loading.value = true
  try {
    const [hybrid, collaborative, contentBased] = await Promise.all([
      recommendationService.getRecommendations(12),
      recommendationService.getCollaborativeRecommendations(12),
      recommendationService.getContentBasedRecommendations(12)
    ])

    if (hybrid.success) hybridRecommendations.value = hybrid.data
    if (collaborative.success) collaborativeRecommendations.value = collaborative.data
    if (contentBased.success) contentBasedRecommendations.value = contentBased.data
  } catch (error) {
    console.error('Failed to fetch recommendations:', error)
  } finally {
    loading.value = false
  }
}

const handleTabClick = () => {
  // Optional: could fetch data on tab change
}
</script>
