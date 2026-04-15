<template>
  <!-- Fallback state -->
  <div v-if="analysis.fallback" class="result-card result-card--fallback" role="status">
    <div class="card-icon" aria-hidden="true">⚠️</div>
    <div class="card-body">
      <h2 class="card-title">AI 分析暂时不可用</h2>
      <p class="card-text">{{ analysis.fallbackMessage || 'AI分析暂时不可用，请直接拨打120或前往最近医院' }}</p>
    </div>
  </div>

  <!-- Normal state -->
  <div v-else class="result-card result-card--success" role="status" aria-live="polite">
    <div class="card-header">
      <div class="card-icon" aria-hidden="true">🩺</div>
      <h2 class="card-title">AI 分析结果</h2>
    </div>
    <div class="card-body">
      <div class="department-row">
        <span class="department-label">建议就诊科室</span>
        <span class="department-value">{{ analysis.department || '综合内科' }}</span>
      </div>
      <div v-if="analysis.reason" class="reason-row">
        <span class="reason-label">分析原因</span>
        <p class="reason-text">{{ analysis.reason }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { AnalysisResponse } from '../types'
defineProps<{ analysis: AnalysisResponse }>()
</script>

<style scoped>
.result-card {
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  border: 1px solid transparent;
  box-shadow: var(--shadow-sm);
}

.result-card--fallback {
  background: var(--color-warning-bg);
  border-color: #fde68a;
  display: flex;
  gap: var(--space-3);
  align-items: flex-start;
}

.result-card--success {
  background: var(--color-card);
  border-color: var(--color-border);
}

.card-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.card-icon {
  font-size: 22px;
  line-height: 1;
  flex-shrink: 0;
}

.card-title {
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--color-text);
}

.result-card--fallback .card-title {
  font-size: var(--text-base);
  color: #92400e;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.result-card--fallback .card-body {
  flex: 1;
}

.card-text {
  font-size: var(--text-sm);
  color: #92400e;
  line-height: 1.5;
}

.department-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--color-primary-50);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-primary-light);
}

.department-label {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  white-space: nowrap;
}

.department-value {
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--color-primary);
}

.reason-row {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.reason-label {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-secondary);
}

.reason-text {
  font-size: var(--text-sm);
  color: var(--color-text);
  line-height: 1.65;
  padding: var(--space-3) var(--space-4);
  background: #f9fafb;
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--color-primary-light);
}
</style>
