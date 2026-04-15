<template>
  <div class="app">
    <AppHeader />

    <main class="main">
      <div class="container">

        <!-- Step 1: Input -->
        <section class="card input-card">
          <LocationBanner :location="locationState" @retry="requestLocation" />
          <SymptomInput
            v-model="symptoms"
            :loading="queryState.phase === 'loading'"
            @submit="handleSubmit"
          />
        </section>

        <!-- Step 2: Loading -->
        <section v-if="queryState.phase === 'loading'" class="card">
          <LoadingState />
        </section>

        <!-- Step 3: Results -->
        <template v-if="queryState.phase === 'done'">
          <!-- Emergency alert (highest priority) -->
          <EmergencyAlert v-if="queryState.analysis.isEmergency" />

          <!-- AI Analysis result -->
          <section class="card">
            <AnalysisResult :analysis="queryState.analysis" />
          </section>

          <!-- Hospital list -->
          <section class="card">
            <HospitalList :hospitals="queryState.hospitals" />
          </section>

          <!-- Reset button -->
          <button class="reset-btn" type="button" @click="reset">
            <svg class="reset-icon" viewBox="0 0 20 20" fill="none" aria-hidden="true">
              <path d="M4 4v5h5M16 16v-5h-5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M4 9A7 7 0 0116 9" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
              <path d="M16 11A7 7 0 014 11" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            </svg>
            重新查询
          </button>
        </template>

        <!-- Error state -->
        <section v-if="queryState.phase === 'error'" class="card error-card" role="alert">
          <div class="error-icon" aria-hidden="true">❗</div>
          <div class="error-body">
            <p class="error-title">查询失败</p>
            <p class="error-msg">{{ queryState.message }}</p>
            <button class="retry-query-btn" type="button" @click="handleSubmit">重试</button>
          </div>
        </section>

      </div>
    </main>

    <footer class="footer">
      <p>本系统仅供参考，不构成医疗诊断。如有紧急情况请立即拨打 <strong>120</strong></p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppHeader from './components/AppHeader.vue'
import LocationBanner from './components/LocationBanner.vue'
import SymptomInput from './components/SymptomInput.vue'
import LoadingState from './components/LoadingState.vue'
import EmergencyAlert from './components/EmergencyAlert.vue'
import AnalysisResult from './components/AnalysisResult.vue'
import HospitalList from './components/HospitalList.vue'
import { analyzeSymptoms, findNearbyHospitals } from './api'
import type { LocationState, QueryState } from './types'

const symptoms = ref('')

const locationState = ref<LocationState>({ status: 'idle' })
const queryState = ref<QueryState>({ phase: 'idle' })

function requestLocation() {
  if (!navigator.geolocation) {
    locationState.value = { status: 'denied', reason: '您的浏览器不支持位置获取，医院推荐功能将不可用' }
    return
  }
  locationState.value = { status: 'requesting' }
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      locationState.value = {
        status: 'granted',
        lat: pos.coords.latitude,
        lon: pos.coords.longitude
      }
    },
    (err) => {
      const msg = err.code === 1
        ? '位置权限被拒绝，医院推荐功能将不可用。请在浏览器设置中允许位置访问。'
        : '位置获取失败，医院推荐功能将不可用'
      locationState.value = { status: 'denied', reason: msg }
    },
    { timeout: 10000, maximumAge: 300000 }
  )
}

onMounted(() => requestLocation())

async function handleSubmit() {
  if (!symptoms.value.trim()) return

  queryState.value = { phase: 'loading' }

  try {
    // 1. AI Analysis
    const analysis = await analyzeSymptoms({ symptoms: symptoms.value })

    // 2. Hospital search (if location available)
    let hospitals: import('./types').HospitalDto[] = []
    if (locationState.value.status === 'granted') {
      const loc = locationState.value
      hospitals = await findNearbyHospitals({
        latitude: loc.lat,
        longitude: loc.lon,
        department: analysis.department ?? null
      }).catch(() => [])
    }

    queryState.value = { phase: 'done', analysis, hospitals }

    // Scroll to results
    setTimeout(() => {
      document.querySelector('.card:nth-child(2)')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }, 100)
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : '网络异常，请稍后重试'
    queryState.value = { phase: 'error', message: msg }
  }
}

function reset() {
  queryState.value = { phase: 'idle' }
  symptoms.value = ''
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<style scoped>
.app {
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
}

.main {
  flex: 1;
  padding: var(--space-5) var(--space-4);
}

.container {
  max-width: 680px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.card {
  background: var(--color-card);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.input-card {
  border-color: var(--color-primary-light);
}

.error-card {
  display: flex;
  flex-direction: row;
  gap: var(--space-3);
  align-items: flex-start;
  background: var(--color-emergency-bg);
  border-color: var(--color-emergency-border);
}

.error-icon {
  font-size: 22px;
  flex-shrink: 0;
}

.error-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.error-title {
  font-size: var(--text-base);
  font-weight: 700;
  color: var(--color-emergency);
}

.error-msg {
  font-size: var(--text-sm);
  color: #7f1d1d;
}

.retry-query-btn {
  align-self: flex-start;
  padding: var(--space-2) var(--space-4);
  background: var(--color-emergency);
  color: white;
  border-radius: var(--radius-sm);
  font-size: var(--text-sm);
  font-weight: 600;
  min-height: 36px;
}

.reset-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  width: 100%;
  padding: var(--space-3) var(--space-4);
  background: transparent;
  color: var(--color-text-secondary);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: 500;
  min-height: 44px;
  transition: background 0.12s, color 0.12s;
}

.reset-btn:hover {
  background: #f3f4f6;
  color: var(--color-text);
}

.reset-icon {
  width: 16px;
  height: 16px;
}

.footer {
  padding: var(--space-4);
  text-align: center;
  font-size: 12px;
  color: var(--color-text-muted);
  border-top: 1px solid var(--color-border);
  line-height: 1.6;
}

@media (min-width: 640px) {
  .main {
    padding: var(--space-8) var(--space-6);
  }
  .card {
    padding: var(--space-6);
  }
}
</style>
