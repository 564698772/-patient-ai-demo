<template>
  <div v-if="show" class="location-banner" :class="bannerClass" role="status">
    <div class="banner-icon" aria-hidden="true">{{ icon }}</div>
    <div class="banner-content">
      <p class="banner-text">{{ text }}</p>
      <button v-if="showRetry" class="retry-btn" @click="$emit('retry')">重新授权</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { LocationState } from '../types'

const props = defineProps<{ location: LocationState }>()
defineEmits<{ retry: [] }>()

const show = computed(() => props.location.status !== 'idle')

const bannerClass = computed(() => ({
  'banner--requesting': props.location.status === 'requesting',
  'banner--granted': props.location.status === 'granted',
  'banner--denied': props.location.status === 'denied',
}))

const icon = computed(() => {
  switch (props.location.status) {
    case 'requesting': return '📍'
    case 'granted': return '✅'
    case 'denied': return '⚠️'
    default: return ''
  }
})

const text = computed(() => {
  switch (props.location.status) {
    case 'requesting': return '正在获取您的位置...'
    case 'granted': return '位置已获取，将为您推荐附近医院'
    case 'denied': return props.location.status === 'denied' ? (props.location as { status: 'denied'; reason: string }).reason : ''
    default: return ''
  }
})

const showRetry = computed(() => props.location.status === 'denied')
</script>

<style scoped>
.location-banner {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  border: 1px solid transparent;
}

.banner--requesting {
  background: var(--color-primary-50);
  border-color: var(--color-primary-light);
  color: var(--color-primary-dark);
}

.banner--granted {
  background: var(--color-success-bg);
  border-color: #bbf7d0;
  color: #15803d;
}

.banner--denied {
  background: var(--color-warning-bg);
  border-color: #fde68a;
  color: #92400e;
}

.banner-icon {
  font-size: 16px;
  line-height: 1.4;
  flex-shrink: 0;
}

.banner-content {
  flex: 1;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.banner-text {
  flex: 1;
  min-width: 0;
}

.retry-btn {
  background: none;
  color: #92400e;
  font-size: var(--text-sm);
  font-weight: 600;
  text-decoration: underline;
  padding: 0;
  white-space: nowrap;
}
</style>
