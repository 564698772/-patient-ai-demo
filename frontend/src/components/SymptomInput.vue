<template>
  <section class="input-section">
    <label for="symptoms" class="input-label">
      描述您的症状
      <span class="input-hint">越详细越准确</span>
    </label>
    <div class="textarea-wrapper" :class="{ 'textarea-wrapper--focused': focused }">
      <textarea
        id="symptoms"
        ref="textareaRef"
        v-model="localValue"
        class="symptoms-textarea"
        placeholder="例如：头痛发烧两天，体温 38.5°C，伴有咽喉疼痛和轻微咳嗽"
        rows="4"
        maxlength="500"
        :disabled="loading"
        @focus="focused = true"
        @blur="focused = false"
        @keydown.ctrl.enter="handleSubmit"
        @keydown.meta.enter="handleSubmit"
        aria-describedby="char-count symptoms-tip"
      />
      <div class="textarea-footer">
        <span id="symptoms-tip" class="tip">Ctrl+Enter 快速提交</span>
        <span id="char-count" class="char-count" :class="{ 'char-count--warn': localValue.length > 450 }">
          {{ localValue.length }}/500
        </span>
      </div>
    </div>

    <!-- Quick symptom tags -->
    <div class="quick-tags" aria-label="常见症状快速选择">
      <span class="quick-tags-label">常见症状：</span>
      <button
        v-for="tag in quickTags"
        :key="tag"
        class="quick-tag"
        type="button"
        :disabled="loading"
        @click="appendTag(tag)"
      >{{ tag }}</button>
    </div>

    <button
      class="submit-btn"
      :class="{ 'submit-btn--loading': loading }"
      :disabled="!localValue.trim() || loading"
      type="button"
      @click="handleSubmit"
      :aria-busy="loading"
    >
      <span v-if="loading" class="btn-spinner" aria-hidden="true"></span>
      <span v-if="loading">AI 分析中...</span>
      <span v-else>
        <svg class="btn-icon" viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path d="M10 2L10 18M2 10L18 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" v-if="false"/>
          <path d="M9 3H5a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M16 3l1 1-7 7M12 3h5v5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        智能分析并推荐医院
      </span>
    </button>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  modelValue: string
  loading: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  submit: []
}>()

const focused = ref(false)
const textareaRef = ref<HTMLTextAreaElement>()

const localValue = ref(props.modelValue)

import { watch } from 'vue'
watch(() => props.modelValue, v => { localValue.value = v })
watch(localValue, v => emit('update:modelValue', v))

const quickTags = ['发烧', '头痛', '腹痛', '咳嗽', '胸闷', '腰痛', '皮肤过敏', '眩晕']

function appendTag(tag: string) {
  const sep = localValue.value && !localValue.value.endsWith('、') && !localValue.value.endsWith('，') ? '、' : ''
  localValue.value = localValue.value ? localValue.value + sep + tag : tag
  textareaRef.value?.focus()
}

function handleSubmit() {
  if (localValue.value.trim() && !props.loading) {
    emit('submit')
  }
}
</script>

<style scoped>
.input-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.input-label {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--color-text);
}

.input-hint {
  font-size: var(--text-sm);
  font-weight: 400;
  color: var(--color-text-muted);
}

.textarea-wrapper {
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-card);
  transition: border-color 0.15s, box-shadow 0.15s;
  overflow: hidden;
}

.textarea-wrapper--focused {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.symptoms-textarea {
  width: 100%;
  border: none;
  outline: none;
  resize: none;
  padding: var(--space-4);
  font-size: var(--text-base);
  line-height: 1.65;
  color: var(--color-text);
  background: transparent;
  min-height: 100px;
}

.symptoms-textarea::placeholder {
  color: var(--color-text-muted);
}

.symptoms-textarea:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.textarea-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-2) var(--space-4);
  border-top: 1px solid var(--color-border);
  background: #fafafa;
}

.tip {
  font-size: 12px;
  color: var(--color-text-muted);
}

.char-count {
  font-size: 12px;
  color: var(--color-text-muted);
}

.char-count--warn {
  color: var(--color-warning);
  font-weight: 600;
}

.quick-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
}

.quick-tags-label {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  white-space: nowrap;
}

.quick-tag {
  padding: var(--space-1) var(--space-3);
  background: var(--color-primary-50);
  color: var(--color-primary);
  border: 1px solid var(--color-primary-light);
  border-radius: 20px;
  font-size: var(--text-sm);
  font-weight: 500;
  transition: background 0.12s, color 0.12s;
  min-height: 32px;
}

.quick-tag:hover:not(:disabled) {
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
}

.quick-tag:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.submit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  width: 100%;
  padding: var(--space-4) var(--space-6);
  background: var(--color-primary);
  color: white;
  border-radius: var(--radius-md);
  font-size: var(--text-base);
  font-weight: 600;
  min-height: 52px;
  transition: background 0.15s, transform 0.1s, opacity 0.15s;
  letter-spacing: 0.2px;
}

.submit-btn:hover:not(:disabled) {
  background: var(--color-primary-dark);
}

.submit-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.submit-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.submit-btn--loading {
  background: var(--color-primary-dark);
}

.btn-icon {
  width: 18px;
  height: 18px;
}

.btn-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.35);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  flex-shrink: 0;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
