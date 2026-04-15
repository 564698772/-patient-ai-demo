<template>
  <div class="hospital-card">
    <div class="card-left">
      <div class="rank-badge" aria-hidden="true">{{ rank }}</div>
    </div>
    <div class="card-main">
      <div class="hospital-name">{{ hospital.name }}</div>
      <div class="hospital-address">
        <svg class="addr-icon" viewBox="0 0 16 16" fill="none" aria-hidden="true">
          <path d="M8 1.5a4.5 4.5 0 100 9 4.5 4.5 0 000-9zM8 8a1.5 1.5 0 110-3 1.5 1.5 0 010 3z" fill="#9ca3af"/>
          <path d="M8 10.5c-2.21 0-4 .672-4 1.5s1.79 1.5 4 1.5 4-.672 4-1.5-1.79-1.5-4-1.5z" fill="#9ca3af" opacity=".4"/>
        </svg>
        {{ hospital.address || '地址暂无' }}
      </div>
      <div v-if="hospital.departments?.length" class="departments">
        <span
          v-for="dep in hospital.departments.slice(0, 3)"
          :key="dep"
          class="dept-tag"
        >{{ dep }}</span>
      </div>
    </div>
    <div class="card-right">
      <div class="distance">
        <span class="distance-value">{{ formattedDistance }}</span>
        <span class="distance-unit">{{ distanceUnit }}</span>
      </div>
      <a
        :href="navUrl"
        target="_blank"
        rel="noopener noreferrer"
        class="nav-btn"
        :aria-label="`导航到${hospital.name}`"
      >导航</a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { HospitalDto } from '../types'

const props = defineProps<{
  hospital: HospitalDto
  rank: number
}>()

const formattedDistance = computed(() => {
  const d = props.hospital.distance
  if (d == null) return '—'
  return d >= 1000 ? (d / 1000).toFixed(1) : String(d)
})

const distanceUnit = computed(() => {
  const d = props.hospital.distance
  if (d == null) return ''
  return d >= 1000 ? 'km' : 'm'
})

const navUrl = computed(() => {
  const name = encodeURIComponent(props.hospital.name)
  return `https://uri.amap.com/search?keyword=${name}&src=patient-ai&callnative=1`
})
</script>

<style scoped>
.hospital-card {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: box-shadow 0.15s, transform 0.15s;
}

.hospital-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.card-left {
  flex-shrink: 0;
  display: flex;
  align-items: flex-start;
  padding-top: 2px;
}

.rank-badge {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--color-primary-50);
  color: var(--color-primary);
  font-size: var(--text-sm);
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.hospital-card:first-child .rank-badge {
  background: var(--color-primary);
  color: white;
}

.card-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.hospital-name {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--color-text);
  line-height: 1.3;
}

.hospital-address {
  display: flex;
  align-items: flex-start;
  gap: var(--space-1);
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  line-height: 1.4;
}

.addr-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
  margin-top: 1px;
}

.departments {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-1);
}

.dept-tag {
  padding: 2px var(--space-2);
  background: var(--color-success-bg);
  color: var(--color-success);
  border: 1px solid #bbf7d0;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}

.card-right {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-2);
}

.distance {
  text-align: right;
}

.distance-value {
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--color-primary);
}

.distance-unit {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  margin-left: 2px;
}

.nav-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-2) var(--space-3);
  background: var(--color-primary-50);
  color: var(--color-primary);
  border: 1px solid var(--color-primary-light);
  border-radius: var(--radius-sm);
  font-size: var(--text-sm);
  font-weight: 600;
  text-decoration: none;
  min-height: 36px;
  min-width: 52px;
  transition: background 0.12s;
}

.nav-btn:hover {
  background: var(--color-primary-light);
}
</style>
