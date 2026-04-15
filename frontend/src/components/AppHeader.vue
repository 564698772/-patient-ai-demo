<template>
  <header class="header">
    <div class="header-inner">
      <div class="logo">
        <svg class="logo-icon" viewBox="0 0 32 32" fill="none" aria-hidden="true">
          <rect width="32" height="32" rx="8" fill="#2563eb"/>
          <path d="M16 8v16M8 16h16" stroke="white" stroke-width="3.5" stroke-linecap="round"/>
        </svg>
        <span class="logo-text">就医助手</span>
      </div>
      <p class="tagline">描述症状，找到附近适合的医院</p>
      <a href="tel:120" class="sos-btn" aria-label="紧急呼叫120急救">
        <svg class="sos-icon" viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path d="M2 3.5A1.5 1.5 0 013.5 2h1.148a1.5 1.5 0 011.465 1.175l.716 3.223a1.5 1.5 0 01-1.052 1.767l-.933.267c-.41.117-.643.555-.48.95a11.542 11.542 0 006.254 6.254c.395.163.833-.07.95-.48l.267-.933a1.5 1.5 0 011.767-1.052l3.223.716A1.5 1.5 0 0118 15.352V16.5a1.5 1.5 0 01-1.5 1.5H15c-1.149 0-2.263-.15-3.326-.43A13.022 13.022 0 012.43 8.326 13.019 13.019 0 012 5V3.5z" fill="currentColor"/>
        </svg>
        <span>120</span>
      </a>
    </div>
  </header>
</template>

<style scoped>
.header {
  background: var(--color-card);
  border-bottom: 1px solid var(--color-border);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: var(--shadow-sm);
}

.header-inner {
  max-width: 680px;
  margin: 0 auto;
  padding: var(--space-4) var(--space-4);
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-shrink: 0;
}

.logo-icon {
  width: 32px;
  height: 32px;
}

.logo-text {
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--color-primary);
  letter-spacing: -0.3px;
}

.tagline {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  flex: 1;
  display: none;
}

@media (min-width: 480px) {
  .tagline {
    display: block;
  }
}

.sos-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  background: #dc2626;
  color: white;
  border-radius: 20px;
  font-size: 15px;
  font-weight: 700;
  text-decoration: none;
  flex-shrink: 0;
  min-height: 36px;
  transition: background 0.15s, transform 0.1s;
  box-shadow: 0 2px 6px rgba(220,38,38,0.35);
  animation: sos-pulse 2.5s ease-in-out infinite;
}

.sos-btn:hover {
  background: #b91c1c;
  transform: scale(1.04);
}

.sos-btn:active {
  transform: scale(0.96);
}

.sos-icon {
  width: 15px;
  height: 15px;
  flex-shrink: 0;
}

@keyframes sos-pulse {
  0%, 100% { box-shadow: 0 2px 6px rgba(220,38,38,0.35); }
  50% { box-shadow: 0 2px 14px rgba(220,38,38,0.6); }
}
</style>
