<template>
  <el-card 
    class="property-card" 
    :body-style="{ padding: '0px' }"
    @click="goToDetail"
  >
    <el-image
      :src="coverImage"
      fit="cover"
      style="width: 100%; height: 200px;"
    >
      <template #error>
        <div class="image-slot">
          <el-icon><Picture /></el-icon>
        </div>
      </template>
    </el-image>
    <div style="padding: 14px;">
      <h3 class="title">{{ property.title }}</h3>
      <div class="location">
        <el-icon><Location /></el-icon>
        {{ property.city }} - {{ property.district }}
      </div>
      <div class="price-row">
        <div class="price">
          <span class="amount">¥{{ property.price }}</span>
          <span class="unit">/晚</span>
        </div>
        <el-rate
          v-model="rating"
          disabled
          show-score
          :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
          score-template="{value}"
        />
      </div>
      <div class="tags">
        <el-tag size="small">
          <el-icon><User /></el-icon>
          {{ property.maxGuests }}人
        </el-tag>
        <el-tag size="small">
          {{ property.bedrooms }}室{{ property.bathrooms }}卫
        </el-tag>
        <el-tag v-if="property.propertyType" type="info" size="small">
          {{ property.propertyType }}
        </el-tag>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Picture, Location, User } from '@element-plus/icons-vue'

const props = defineProps({
  property: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const coverImage = computed(() => {
  if (props.property.images) {
    try {
      const images = JSON.parse(props.property.images)
      return images[0] || 'https://via.placeholder.com/300x200'
    } catch {
      return 'https://via.placeholder.com/300x200'
    }
  }
  return 'https://via.placeholder.com/300x200'
})

const rating = computed(() => Number(props.property.rating) || 0)

const goToDetail = () => {
  router.push(`/properties/${props.property.id}`)
}
</script>

<style scoped>
.property-card {
  cursor: pointer;
  transition: all 0.3s;
}

.property-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.title {
  margin: 0 0 8px 0;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.location {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.price {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.amount {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}

.unit {
  color: #999;
  font-size: 14px;
}

.tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  font-size: 50px;
  color: #ccc;
}
</style>
