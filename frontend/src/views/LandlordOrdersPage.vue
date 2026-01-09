<template>
  <div class="landlord-orders-page">
    <div class="header">
      <h2>房东订单</h2>
      <p class="sub">查看并审核来自其他用户的订单与退订请求</p>
    </div>

    <el-table :data="orders" v-loading="loading" stripe>
      <el-table-column prop="orderNumber" label="订单号" width="150" />
      <el-table-column label="房源">
        <template #default="scope">
          {{ scope.row.property?.title || '已删除' }}
        </template>
      </el-table-column>
      <el-table-column label="用户" width="140">
        <template #default="scope">
          {{ scope.row.user?.username || '未知用户' }}
        </template>
      </el-table-column>
      <el-table-column label="联系方式" width="180">
        <template #default="scope">
          <div class="contact">
            <span v-if="scope.row.user?.phone">电话：{{ scope.row.user.phone }}</span>
            <span v-if="scope.row.user?.email">邮箱：{{ scope.row.user.email }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="入住" width="120">
        <template #default="scope">
          {{ formatDate(scope.row.checkInDate) }}
        </template>
      </el-table-column>
      <el-table-column label="退房" width="120">
        <template #default="scope">
          {{ formatDate(scope.row.checkOutDate) }}
        </template>
      </el-table-column>
      <el-table-column label="类型" width="100">
        <template #default="scope">
          <el-tag type="info">{{ getOrderType(scope.row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="140">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="总价" width="100">
        <template #default="scope">
          ¥{{ scope.row.totalPrice }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <template v-if="scope.row.status === 'CANCEL_REQUESTED'">
            <el-button type="primary" size="small" @click="handleReview(scope.row.id, true)">通过退订</el-button>
            <el-button type="danger" size="small" @click="handleReview(scope.row.id, false)">拒绝退订</el-button>
          </template>
          <span v-else style="color: #999">无操作</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="pagination.total > 0"
      v-model:current-page="pagination.page"
      :page-size="pagination.size"
      :total="pagination.total"
      layout="total, prev, pager, next"
      @current-change="fetchOrders"
      style="margin-top: 24px; text-align: center"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import orderService from '../services/orderService'

const orders = ref([])
const loading = ref(false)
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const fetchOrders = async () => {
  loading.value = true
  try {
    const response = await orderService.getLandlordOrders({
      page: pagination.page - 1,
      size: pagination.size
    })
    if (response.success) {
      orders.value = response.data?.content ?? []
      pagination.total = response.data?.totalElements ?? 0
    }
  } catch (error) {
    ElMessage.error(error.message || '加载订单失败')
  } finally {
    loading.value = false
  }
}

const handleReview = async (id, approve) => {
  try {
    await ElMessageBox.confirm(approve ? '确认同意退订？' : '确认拒绝退订？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await orderService.reviewCancellation(id, approve)
    if (response.success) {
      ElMessage.success(approve ? '已同意退订' : '已拒绝退订')
      fetchOrders()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD')

const getOrderType = (order) => {
  if (!order?.checkInDate) return '预定'
  return dayjs(order.checkInDate).isAfter(dayjs(), 'day') ? '预定' : '已使用'
}

const getStatusType = (status) => {
  const map = {
    PENDING: 'warning',
    CONFIRMED: '',
    CANCELLED: 'danger',
    COMPLETED: 'success',
    CANCEL_REQUESTED: 'warning',
    CANCEL_REJECTED: 'info'
  }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = {
    PENDING: '待确认',
    CONFIRMED: '已确认',
    CANCELLED: '已取消',
    COMPLETED: '已完成',
    CANCEL_REQUESTED: '退订审核中',
    CANCEL_REJECTED: '退订被拒'
  }
  return map[status] || status
}

onMounted(fetchOrders)
</script>

<style scoped>
.header {
  margin-bottom: 16px;
}

.sub {
  color: #666;
  margin-top: 4px;
}

.contact {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
</style>
