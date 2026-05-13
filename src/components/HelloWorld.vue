<template>
  <el-card>
    <h1>{{ msg }}</h1>
    <el-button type="primary">搜索</el-button>
  </el-card>
</template>

<script setup>
defineProps(['msg'])
</script>
