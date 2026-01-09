<template>
  <div class="my-orders-page">
    <el-row :gutter="20">
      <el-col :md="24" :xs="24">
        <div class="orders-header">
          <h2>我的订单</h2>
        </div>
        <el-table :data="orders" v-loading="loading" stripe>
          <el-table-column prop="orderNumber" label="订单号" width="150" />
          <el-table-column label="房源名称">
            <template #default="scope">
              {{ scope.row.property?.title || '已删除' }}
            </template>
          </el-table-column>
          <el-table-column label="入住日期" width="120">
            <template #default="scope">
              {{ formatDate(scope.row.checkInDate) }}
            </template>
          </el-table-column>
          <el-table-column label="退房日期" width="120">
            <template #default="scope">
              {{ formatDate(scope.row.checkOutDate) }}
            </template>
          </el-table-column>
          <el-table-column label="类型" width="100">
            <template #default="scope">
              <el-tag type="info">{{ getOrderType(scope.row) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="人数" width="80">
            <template #default="scope">
              {{ scope.row.guestCount }}人
            </template>
          </el-table-column>
          <el-table-column label="总价" width="100">
            <template #default="scope">
              ¥{{ scope.row.totalPrice }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="下单时间" width="160">
            <template #default="scope">
              {{ formatDateTime(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button
                v-if="canCancel(scope.row)"
                type="danger"
                size="small"
                @click="handleCancel(scope.row.id)"
              >
                取消订单
              </el-button>
              <span v-else style="color: #999">
                {{ scope.row.status === 'CANCEL_REQUESTED' ? '等待审核' : '无法取消' }}
              </span>
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
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
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

onMounted(() => {
  fetchOrders()
})

const fetchOrders = async () => {
  loading.value = true
  try {
    const response = await orderService.getMyOrders({
      page: pagination.page - 1,
      size: pagination.size
    })
    if (response.success) {
      orders.value = response.data?.content ?? []
      pagination.total = response.data?.totalElements ?? 0
    }
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确定取消这个订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await orderService.cancelOrder(id)
    if (response.success) {
      ElMessage.success(response.message || '操作成功')
      fetchOrders()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消失败')
    }
  }
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const formatDateTime = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
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

const getOrderType = (order) => {
  if (!order?.checkInDate) return '预定'
  return dayjs(order.checkInDate).isAfter(dayjs(), 'day') ? '预定' : '已使用'
}

const canCancel = (order) => {
  const blocked = ['CANCELLED', 'COMPLETED', 'CANCEL_REQUESTED']
  return !blocked.includes(order.status)
}
</script>

<style scoped>
.orders-header {
  display: flex;
  align-items: flex-end;
  margin-bottom: 16px;
}
</style>
