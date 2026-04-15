<template>
  <div class="emergency-alert" role="alert" aria-live="assertive">
    <div class="alert-icon" aria-hidden="true">🚨</div>
    <div class="alert-body">
      <h2 class="alert-title">检测到急症症状</h2>
      <p class="alert-desc">您描述的症状可能需要紧急救治，请立即拨打急救电话。</p>
      <a href="tel:120" class="call-btn">
        <svg viewBox="0 0 20 20" fill="none" class="call-icon" aria-hidden="true">
          <path d="M2 3.5A1.5 1.5 0 013.5 2h1.148a1.5 1.5 0 011.465 1.175l.716 3.223a1.5 1.5 0 01-1.052 1.767l-.933.267c-.41.117-.643.555-.48.95a11.542 11.542 0 006.254 6.254c.395.163.833-.07.95-.48l.267-.933a1.5 1.5 0 011.767-1.052l3.223.716A1.5 1.5 0 0118 15.352V16.5a1.5 1.5 0 01-1.5 1.5H15c-1.149 0-2.263-.15-3.326-.43A13.022 13.022 0 012.43 8.326 13.019 13.019 0 012 5V3.5z" fill="currentColor"/>
        </svg>
        立即拨打 120
      </a>
    </div>
  </div>
</template>

<style scoped>
.emergency-alert {
  display: flex;
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--color-emergency-bg);
  border: 2px solid var(--color-emergency);
  border-radius: var(--radius-lg);
  animation: pulse-border 1.5s ease-in-out 2;
}

@keyframes pulse-border {
  0%, 100% { border-color: var(--color-emergency); }
  50% { border-color: #fca5a5; }
}

.alert-icon {
  font-size: 28px;
  line-height: 1;
  flex-shrink: 0;
}

.alert-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.alert-title {
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--color-emergency);
}

.alert-desc {
  font-size: var(--text-sm);
  color: #7f1d1d;
  line-height: 1.5;
}

.call-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5);
  background: var(--color-emergency);
  color: white;
  border-radius: var(--radius-md);
  font-size: var(--text-base);
  font-weight: 700;
  text-decoration: none;
  width: fit-content;
  margin-top: var(--space-1);
  transition: background 0.15s;
  min-height: 44px;
}

.call-btn:hover {
  background: #b91c1c;
}

.call-icon {
  width: 18px;
  height: 18px;
}
</style>
