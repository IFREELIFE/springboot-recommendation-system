<template>
  <div class="admin-page">
    <h2>管理员控制台</h2>
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="用户账户" name="users">
          <el-table :data="userList" stripe>
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="phone" label="手机号" />
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'danger'">
                  {{ row.enabled ? '启用' : '已冻结' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button
                  size="small"
                  type="warning"
                  :disabled="row.role === 'ADMIN'"
                  @click="toggleFreeze(row, loadUsers)"
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

        <el-tab-pane label="房东账户" name="landlords">
          <el-table :data="landlordList" stripe>
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="phone" label="手机号" />
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'danger'">
                  {{ row.enabled ? '启用' : '已冻结' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button
                  size="small"
                  type="warning"
                  :disabled="row.role === 'ADMIN'"
                  @click="toggleFreeze(row, loadLandlords)"
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
              :total="landlordsPage.total"
              :page-size="landlordsPage.size"
              :current-page="landlordsPage.current + 1"
              @current-change="handleLandlordPageChange"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="房源信息" name="properties">
          <el-table :data="propertyList" stripe>
            <el-table-column prop="title" label="房源" />
            <el-table-column label="位置" min-width="200">
              <template #default="{ row }">
                {{ formatLocation(row) }}
              </template>
            </el-table-column>
            <el-table-column prop="remainingRooms" label="剩余房间" width="120">
              <template #default="{ row }">
                <el-tag :type="(row.remainingRooms ?? 0) > 0 ? 'success' : 'danger'">
                  {{ row.remainingRooms ?? 0 }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="maxGuests" label="可住人数" width="120" />
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="row.available ? 'success' : 'danger'">
                  {{ row.available ? '上架' : '已冻结' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" type="warning" @click="togglePropertyFreeze(row)">
                  {{ row.available ? '冻结房源' : '解冻房源' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="propertyPage.total"
              :page-size="propertyPage.size"
              :current-page="propertyPage.current + 1"
              @current-change="handlePropertyPageChange"
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

const activeTab = ref('users')

const userFilters = reactive({
  page: 0,
  size: 10
})

const landlordFilters = reactive({
  page: 0,
  size: 10
})

const propertyFilters = reactive({
  page: 0,
  size: 10
})

const usersPage = reactive({
  total: 0,
  size: 10,
  current: 0
})

const landlordsPage = reactive({
  total: 0,
  size: 10,
  current: 0
})

const propertyPage = reactive({
  total: 0,
  size: 10,
  current: 0
})

const userList = ref([])
const landlordList = ref([])
const propertyList = ref([])

const normalizePageData = (pageData) => {
  pageData = pageData?.data ?? pageData
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
      role: 'USER',
      page: userFilters.page,
      size: userFilters.size
    })
    const pageData = normalizePageData(res.data ?? res)
    userList.value = pageData.records
    usersPage.total = pageData.total
    usersPage.size = pageData.size
    usersPage.current = pageData.current
  } catch (error) {
    // handled globally
  }
}

const loadLandlords = async () => {
  try {
    const res = await adminService.fetchUsers({
      role: 'LANDLORD',
      page: landlordFilters.page,
      size: landlordFilters.size
    })
    const pageData = normalizePageData(res.data ?? res)
    landlordList.value = pageData.records
    landlordsPage.total = pageData.total
    landlordsPage.size = pageData.size
    landlordsPage.current = pageData.current
  } catch (error) {
    // handled globally
  }
}

const loadProperties = async () => {
  try {
    const res = await adminService.fetchOccupancy({
      page: propertyFilters.page,
      size: propertyFilters.size
    })
    const pageData = normalizePageData(res.data ?? res)
    propertyList.value = pageData.records
    propertyPage.total = pageData.total
    propertyPage.size = pageData.size
    propertyPage.current = pageData.current
  } catch (error) {
    // handled globally
  }
}

const handleUserPageChange = (page) => {
  userFilters.page = page - 1
  loadUsers()
}

const handleLandlordPageChange = (page) => {
  landlordFilters.page = page - 1
  loadLandlords()
}

const handlePropertyPageChange = (page) => {
  propertyFilters.page = page - 1
  loadProperties()
}

const toggleFreeze = async (row, reloadFn) => {
  const freeze = row.enabled
  try {
    await ElMessageBox.confirm(
      `确定要${freeze ? '冻结' : '解冻'}该账户吗？`,
      '提示',
      { type: 'warning' }
    )
    await adminService.freezeUser(row.id, freeze)
    ElMessage.success(freeze ? '已冻结' : '已解冻')
    if (reloadFn) {
      reloadFn()
    }
  } catch (error) {
    // cancel or handled by interceptor
  }
}

const togglePropertyFreeze = async (row) => {
  const freeze = row.available
  try {
    await ElMessageBox.confirm(
      `确定要${freeze ? '冻结' : '解冻'}该房源吗？`,
      '提示',
      { type: 'warning' }
    )
    await adminService.freezeProperty(row.id, freeze)
    ElMessage.success(freeze ? '房源已冻结' : '房源已解冻')
    loadProperties()
  } catch (error) {
    // cancel or handled by interceptor
  }
}

const LOCATION_UNKNOWN = '未提供'

const formatLocation = (row) => {
  const parts = [row.city, row.district, row.address].filter(Boolean)
  return parts.length ? parts.join(' - ') : LOCATION_UNKNOWN
}

onMounted(() => {
  loadUsers()
  loadLandlords()
  loadProperties()
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
