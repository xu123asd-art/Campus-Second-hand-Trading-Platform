<template>
  <div id="app" style="padding: 20px;">
    <el-form :inline="true" :model="searchForm">
      <el-form-item label="关键字">
        <el-input v-model="searchForm.keyword" placeholder="请输入搜索内容" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </el-form-item>
    </el-form>

    <!-- 搜索历史 -->
    <div v-if="searchHistory.length > 0" style="margin-bottom: 16px;">
      <span style="font-weight: bold; margin-right: 8px;">搜索历史：</span>
      <el-tag
        v-for="(item, index) in searchHistory"
        :key="index"
        closable
        @close="removeHistory(index)"
        @click="selectHistory(item)"
        style="margin: 4px; cursor: pointer;"
      >
        {{ item }}
      </el-tag>
      <el-button size="small" @click="clearHistory" type="danger" plain>清除历史</el-button>
    </div>

    <!-- 加载状态 -->
    <Skeleton v-if="loading" :rows="3" />

    <!-- 结果表格 -->
    <el-table v-else-if="tableData.length > 0" :data="tableData" border style="width: 100%">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="desc" label="描述" />
      <el-table-column prop="date" label="日期" />
    </el-table>

    <!-- 空结果 -->
    <div v-else-if="hasSearched && !loading" style="color: #999;">未找到相关商品</div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import Skeleton from './components/Skeleton.vue'

const searchForm = reactive({
  keyword: ''
})
const tableData = ref([])
const loading = ref(false)
const hasSearched = ref(false)
const searchHistory = ref([])

onMounted(() => {
  const saved = localStorage.getItem('searchHistory')
  if (saved) {
    try {
      searchHistory.value = JSON.parse(saved)
    } catch (e) {}
  }
})

// 从 searchHistory 中移除已存在的 keyword（如果有），然后插入到数组头部，最多保留 10 条，保存到 localStorage
function saveHistory(keyword) {
  // 移除已存在的 keyword
  searchHistory.value = searchHistory.value.filter(item => item !== keyword)
  // 插入到数组头部
  searchHistory.value.unshift(keyword)
  // 最多保留 10 条
  if (searchHistory.value.length > 10) {
    searchHistory.value = searchHistory.value.slice(0, 10)
  }
  // 保存到 localStorage
  localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
}

function removeHistory(index) {
  searchHistory.value.splice(index, 1)
  localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
}

function clearHistory() {
  searchHistory.value = []
  localStorage.removeItem('searchHistory')
}

function selectHistory(keyword) {
  searchForm.keyword = keyword
  handleSearch()
}

function handleSearch() {
  const kw = searchForm.keyword.trim()
  if (!kw) return
  saveHistory(kw)

  loading.value = true
  hasSearched.value = false
  setTimeout(() => {
    const mockData = [
      { name: '红楼梦', desc: '古典文学', date: '2025-01-01' },
      { name: '西游记', desc: '神话小说', date: '2025-01-02' }
    ]
    tableData.value = mockData.filter(item => item.name.includes(kw))
    loading.value = false
    hasSearched.value = true
  }, 500)
}
</script>