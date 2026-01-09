<template>
  <div class="property-occupancy-page">
    <div class="header">
      <h2>房源入住与剩余房间</h2>
    </div>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column label="房源名称">
        <template #default="scope">
          <a
            @click="$router.push(`/properties/${scope.row.id}`)"
            style="color: #409eff; cursor: pointer"
          >
            {{ scope.row.title }}
          </a>
        </template>
      </el-table-column>
      <el-table-column prop="city" label="城市" width="100" />
      <el-table-column prop="rooms" label="房间总数" width="110" />
      <el-table-column prop="bookedRooms" label="已预订房间" width="120" />
      <el-table-column prop="remainingRooms" label="剩余房间" width="110">
        <template #default="scope">
          <el-tag :type="scope.row.remainingRooms > 0 ? 'success' : 'danger'">
            {{ scope.row.remainingRooms }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="入住率" width="110">
        <template #default="scope">
          {{ scope.row.occupancyRate }}
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="pagination.total > 0"
      v-model:current-page="pagination.page"
      :page-size="pagination.size"
      :total="pagination.total"
      layout="total, prev, pager, next"
      @current-change="fetchData"
      style="margin-top: 24px; text-align: center"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import propertyService from '../services/propertyService'

const loading = ref(false)
const properties = ref([])
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const rows = computed(() =>
  properties.value.map((p) => {
    const rooms = Number(p.bedrooms || 0)
    // 假设 bookingCount 表示每间房的预订次数，如数据模型变化需调整
    const bookedRooms = Number(p.bookingCount || 0)
    const remainingRooms = Math.max(rooms - bookedRooms, 0)
    const occupancyRate = rooms > 0 ? `${((bookedRooms / rooms) * 100).toFixed(0)}%` : '0%'
    return {
      ...p,
      rooms,
      bookedRooms,
      remainingRooms,
      occupancyRate
    }
  })
)

const fetchData = async () => {
  loading.value = true
  try {
    const response = await propertyService.getMyProperties({
      page: pagination.page - 1,
      size: pagination.size
    })
    if (response.success) {
      properties.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('Failed to load occupancy data', error)
    ElMessage.error('入住与剩余房间数据加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.header {
  margin-bottom: 16px;
}
</style>
