<template>
  <div class="skeleton-container">
    <div v-for="i in rows" :key="i" class="skeleton-row">
      <div class="skeleton-cell skeleton-name"></div>
      <div class="skeleton-cell skeleton-desc"></div>
      <div class="skeleton-cell skeleton-date"></div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

defineProps({
  rows: {
    type: Number,
    default: 3
  }
})
</script>

<style scoped>
.skeleton-container {
  width: 100%;
}

.skeleton-row {
  display: flex;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.skeleton-cell {
  background: linear-gradient(
    90deg,
    #f0f0f0 25%,
    #e0e0e0 50%,
    #f0f0f0 75%
  );
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 4px;
}

.skeleton-name {
  flex: 0 0 15%;
  height: 20px;
}

.skeleton-desc {
  flex: 0 0 45%;
  height: 20px;
}

.skeleton-date {
  flex: 0 0 15%;
  height: 20px;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
