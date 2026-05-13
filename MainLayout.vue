<template>
  <div class="app-wrapper">
    <header class="header">
      <nav class="nav-content">校园二手平台</nav>
    </header>

    <div class="main-container" :class="deviceType">
      <aside v-if="deviceType === 'desktop'" class="sidebar">
        <ul>
          <li>首页</li>
          <li>分类</li>
          <li>我的</li>
        </ul>
      </aside>

      <main class="content">
        <router-view />
      </main>
    </div>

    <footer v-if="deviceType === 'mobile'" class="mobile-footer">
      <div class="tab-item">首页</div>
      <div class="tab-item" @click="$router.push('/publish')">发布</div>
      <div class="tab-item">我的</div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

const deviceType = ref('desktop');

const handleResize = () => {
  const width = window.innerWidth;
  if (width < 768) {
    deviceType.value = 'mobile'; // 移动端 
  } else if (width < 1024) {
    deviceType.value = 'tablet'; // 平板端 
  } else {
    deviceType.value = 'desktop'; // 桌面端 
  }
};

onMounted(() => {
  handleResize();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped>
.main-container.desktop { display: flex; }
.main-container.tablet { display: block; }
.mobile-footer { position: fixed; bottom: 0; width: 100%; display: flex; }
</style>
