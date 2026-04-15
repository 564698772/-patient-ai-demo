<template>
  <section class="hospital-section">
    <div class="section-header">
      <h2 class="section-title">
        <svg class="title-icon" viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path d="M10 2a6 6 0 100 12A6 6 0 0010 2zM2 10a8 8 0 1116 0 8 8 0 01-16 0z" fill="#2563eb"/>
          <path d="M10 6v4l3 3" stroke="#2563eb" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        附近医院推荐
      </h2>
      <span class="hospital-count">{{ hospitals.length }} 家</span>
    </div>

    <!-- Empty state -->
    <div v-if="hospitals.length === 0" class="empty-state">
      <div class="empty-icon" aria-hidden="true">🏥</div>
      <p class="empty-title">未找到附近医院</p>
      <p class="empty-desc">可能是位置未授权或搜索范围内暂无匹配医院，建议扩大范围或手动搜索</p>
    </div>

    <!-- Hospital list -->
    <div v-else class="hospital-list" role="list">
      <HospitalCard
        v-for="(hospital, index) in hospitals"
        :key="hospital.name + index"
        :hospital="hospital"
        :rank="index + 1"
        role="listitem"
      />
    </div>

    <p class="disclaimer">
      医院信息由高德地图提供，仅供参考。就医前请致电医院确认科室开诊情况。
    </p>
  </section>
</template>

<script setup lang="ts">
import type { HospitalDto } from '../types'
import HospitalCard from './HospitalCard.vue'

defineProps<{ hospitals: HospitalDto[] }>()
</script>

<style scoped>
.hospital-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--color-text);
}

.title-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.hospital-count {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-primary);
  background: var(--color-primary-50);
  padding: var(--space-1) var(--space-3);
  border-radius: 20px;
  border: 1px solid var(--color-primary-light);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-10) var(--space-4);
  text-align: center;
}

.empty-icon {
  font-size: 40px;
}

.empty-title {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--color-text);
}

.empty-desc {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  max-width: 300px;
  line-height: 1.6;
}

.hospital-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.disclaimer {
  font-size: 12px;
  color: var(--color-text-muted);
  text-align: center;
  line-height: 1.5;
  padding: 0 var(--space-2);
}
</style>
