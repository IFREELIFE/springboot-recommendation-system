<template>
  <div class="property-list-page">
    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="城市">
          <el-input v-model="searchForm.city" placeholder="请输入城市" clearable />
        </el-form-item>
        <el-form-item label="最低价格">
          <el-input-number v-model="searchForm.minPrice" :min="0" placeholder="最低价格" />
        </el-form-item>
        <el-form-item label="最高价格">
          <el-input-number v-model="searchForm.maxPrice" :min="0" placeholder="最高价格" />
        </el-form-item>
        <el-form-item label="卧室数">
          <el-input-number v-model="searchForm.bedrooms" :min="1" placeholder="卧室数" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" v-loading="loading">
      <el-col
        v-for="property in properties"
        :key="property.id"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
      >
        <PropertyCard :property="property" />
      </el-col>
    </el-row>

    <el-pagination
      v-if="properties.length > 0"
      v-model:current-page="pagination.page"
      :page-size="pagination.size"
      :total="pagination.total"
      layout="total, prev, pager, next"
      @current-change="handlePageChange"
      style="margin-top: 24px; text-align: center;"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import PropertyCard from '../components/PropertyCard.vue'
import propertyService from '../services/propertyService'

const properties = ref([])
const loading = ref(false)

const searchForm = reactive({
  city: '',
  minPrice: null,
  maxPrice: null,
  bedrooms: null
})

const pagination = reactive({
  page: 1,
  size: 12,
  total: 0
})

onMounted(() => {
  fetchProperties()
})

const fetchProperties = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...searchForm
    }
    
    const hasSearchParams = Object.values(searchForm).some(v => v !== null && v !== '')
    const response = hasSearchParams
      ? await propertyService.searchProperties(params)
      : await propertyService.getProperties(params)
    
    if (response.success) {
      properties.value = response.data.content
      pagination.total = response.data.totalElements
    }
  } catch (error) {
    console.error('Failed to fetch properties:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchProperties()
}

const handlePageChange = () => {
  fetchProperties()
}
</script>

<style scoped>
.search-card {
  margin-bottom: 24px;
}
</style>
