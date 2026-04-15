<template>
  <div class="loading-wrapper" role="status" aria-live="polite">
    <div class="steps">
      <div
        v-for="(step, i) in steps"
        :key="step.label"
        class="step"
        :class="{
          'step--done': i < currentStep,
          'step--active': i === currentStep,
          'step--pending': i > currentStep
        }"
      >
        <div class="step-dot">
          <svg v-if="i < currentStep" class="step-check" viewBox="0 0 16 16" aria-hidden="true">
            <path d="M3 8l4 4 6-6" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
          </svg>
          <span v-else-if="i === currentStep" class="step-spinner" aria-hidden="true"></span>
        </div>
        <span class="step-label">{{ step.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const steps = [
  { label: 'AI 分析症状中' },
  { label: '匹配推荐科室' },
  { label: '搜索附近医院' },
]

const currentStep = ref(0)
let timer: ReturnType<typeof setInterval>

onMounted(() => {
  timer = setInterval(() => {
    if (currentStep.value < steps.length - 1) {
      currentStep.value++
    }
  }, 1800)
})

onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.loading-wrapper {
  padding: var(--space-8) var(--space-4);
  display: flex;
  justify-content: center;
}

.steps {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  width: 100%;
  max-width: 260px;
}

.step {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  transition: opacity 0.3s;
}

.step--pending {
  opacity: 0.35;
}

.step-dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.3s;
}

.step--done .step-dot {
  background: var(--color-success);
}

.step--active .step-dot {
  background: var(--color-primary);
}

.step--pending .step-dot {
  background: var(--color-border);
}

.step-check {
  width: 16px;
  height: 16px;
}

.step-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.4);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  display: block;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.step-label {
  font-size: var(--text-base);
  font-weight: 500;
  color: var(--color-text);
}

.step--active .step-label {
  font-weight: 600;
  color: var(--color-primary);
}
</style>
