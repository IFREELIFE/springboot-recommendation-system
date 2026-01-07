<template>
  <div class="my-properties-page">
    <div class="header">
      <h2>我的房源</h2>
      <el-button type="primary" @click="$router.push('/create-property')">
        发布新房源
      </el-button>
    </div>

    <el-table :data="properties" v-loading="loading" stripe>
      <el-table-column label="房源名称">
        <template #default="scope">
          <a @click="$router.push(`/properties/${scope.row.id}`)" style="color: #409eff; cursor: pointer">
            {{ scope.row.title }}
          </a>
        </template>
      </el-table-column>
      <el-table-column prop="city" label="城市" width="100" />
      <el-table-column label="价格" width="120">
        <template #default="scope">
          ¥{{ scope.row.price }}/晚
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.available ? 'success' : 'danger'">
            {{ scope.row.available ? '可预订' : '不可用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="bookingCount" label="预订数" width="100" />
      <el-table-column prop="viewCount" label="浏览量" width="100" />
      <el-table-column label="评分" width="100">
        <template #default="scope">
          {{ scope.row.rating }}/5.0
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row.id)">
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="pagination.total > 0"
      v-model:current-page="pagination.page"
      :page-size="pagination.size"
      :total="pagination.total"
      layout="total, prev, pager, next"
      @current-change="fetchProperties"
      style="margin-top: 24px; text-align: center"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import propertyService from '../services/propertyService'

const router = useRouter()
const properties = ref([])
const loading = ref(false)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

onMounted(() => {
  fetchProperties()
})

const fetchProperties = async () => {
  loading.value = true
  try {
    const response = await propertyService.getMyProperties({
      page: pagination.page - 1,
      size: pagination.size
    })
    if (response.success) {
      properties.value = response.data.content
      pagination.total = response.data.totalElements
    }
  } catch (error) {
    ElMessage.error('获取房源列表失败')
  } finally {
    loading.value = false
  }
}

const handleEdit = (id) => {
  router.push({ name: 'CreateProperty', query: { id } })
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除这个房源吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await propertyService.deleteProperty(id)
    if (response.success) {
      ElMessage.success('删除成功')
      fetchProperties()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>
