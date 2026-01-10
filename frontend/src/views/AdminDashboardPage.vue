<template>
  <div class="admin-page">
    <h2>管理员控制台</h2>
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="账户管理" name="accounts">
          <div class="toolbar">
            <el-select v-model="filters.role" placeholder="选择角色" style="width: 180px" @change="loadUsers">
              <el-option label="全部" value="ALL" />
              <el-option label="用户" value="USER" />
              <el-option label="房东" value="LANDLORD" />
            </el-select>
          </div>
          <el-table :data="userList" stripe>
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="phone" label="手机号" />
            <el-table-column prop="role" label="角色">
              <template #default="{ row }">
                <el-tag type="info">{{ displayRole(row.role) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="enabled" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'danger'">
                  {{ row.enabled ? '启用' : '已冻结' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button
                  size="small"
                  type="warning"
                  :disabled="row.role === 'ADMIN'"
                  @click="toggleFreeze(row)"
                >
                  {{ row.enabled ? '冻结' : '解冻' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="usersPage.total"
              :page-size="usersPage.size"
              :current-page="usersPage.current + 1"
              @current-change="handleUserPageChange"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="房源入住" name="occupancy">
          <div class="toolbar">
            <el-input
              v-model="occupancyFilters.landlordId"
              placeholder="按房东ID过滤（可选）"
              style="width: 240px"
              clearable
              @clear="loadOccupancy"
              @keyup.enter.native="loadOccupancy"
            />
            <el-button type="primary" @click="loadOccupancy">查询</el-button>
          </div>
          <el-table :data="occupancyList" stripe>
            <el-table-column prop="title" label="房源" />
            <el-table-column prop="city" label="城市" />
            <el-table-column prop="price" label="价格" />
            <el-table-column prop="bookingCount" label="预订次数" />
            <el-table-column prop="occupiedRooms" label="已入住" />
            <el-table-column prop="remainingRooms" label="剩余房间" />
            <el-table-column prop="activeGuests" label="当前入住人数" />
          </el-table>
          <div class="pagination">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="occupancyPage.total"
              :page-size="occupancyPage.size"
              :current-page="occupancyPage.current + 1"
              @current-change="handleOccupancyPageChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import adminService from '../services/adminService'

const activeTab = ref('accounts')

const filters = reactive({
  role: 'ALL',
  page: 0,
  size: 10
})

const usersPage = reactive({
  total: 0,
  size: 10,
  current: 0
})

const userList = ref([])

const occupancyFilters = reactive({
  landlordId: '',
  page: 0,
  size: 10
})

const occupancyPage = reactive({
  total: 0,
  size: 10,
  current: 0
})

const occupancyList = ref([])

const displayRole = (role) => {
  if (role === 'ADMIN' || role === 'ROLE_ADMIN') return '管理员'
  if (role === 'LANDLORD' || role === 'ROLE_LANDLORD') return '房东'
  return '用户'
}

const normalizePageData = (pageData) => {
  return {
    records: pageData?.records || pageData?.content || [],
    total: pageData?.total ?? pageData?.totalElements ?? 0,
    size: pageData?.size ?? 10,
    current: (pageData?.current ?? 1) - 1
  }
}

const loadUsers = async () => {
  try {
    const res = await adminService.fetchUsers({
      role: filters.role,
      page: filters.page,
      size: filters.size
    })
    const pageData = normalizePageData(res.data)
    userList.value = pageData.records
    usersPage.total = pageData.total
    usersPage.size = pageData.size
    usersPage.current = pageData.current
  } catch (error) {
    // error handled globally
  }
}

const handleUserPageChange = (page) => {
  filters.page = page - 1
  loadUsers()
}

const toggleFreeze = async (row) => {
  const freeze = row.enabled
  try {
    await ElMessageBox.confirm(
      `确定要${freeze ? '冻结' : '解冻'}该账户吗？`,
      '提示',
      { type: 'warning' }
    )
    await adminService.freezeUser(row.id, freeze)
    ElMessage.success(freeze ? '已冻结' : '已解冻')
    loadUsers()
  } catch (error) {
    // cancel or handled by interceptor
  }
}

const loadOccupancy = async () => {
  try {
    const res = await adminService.fetchOccupancy({
      landlordId: occupancyFilters.landlordId,
      page: occupancyFilters.page,
      size: occupancyFilters.size
    })
    const pageData = normalizePageData(res.data)
    occupancyList.value = pageData.records
    occupancyPage.total = pageData.total
    occupancyPage.size = pageData.size
    occupancyPage.current = pageData.current
  } catch (error) {
    // handled globally
  }
}

const handleOccupancyPageChange = (page) => {
  occupancyFilters.page = page - 1
  loadOccupancy()
}

onMounted(() => {
  loadUsers()
  loadOccupancy()
})
</script>

<style scoped>
.admin-page {
  max-width: 1200px;
  margin: 0 auto;
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
