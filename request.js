import axios from 'axios';

const service = axios.create({
  baseURL: '/api',
  timeout: 2000 // 请求超时 [cite: 115]
});

// 响应拦截器 
service.interceptors.response.use(
  response => {
    const res = response.data;
    // 逻辑成功处理 [cite: 122]
    return res;
  },
  async error => {
    const config = error.config;
    // 实现自动重试 3 次，间隔 2s [cite: 115]
    if (!config || !config.retry) config.retry = 3;
    
    if (config.retryCount >= config.retry) {
      // 提示：服务器开小差了 [cite: 115]
      console.error('服务器错误: ' + error.message);
      return Promise.reject(error);
    }
    
    config.retryCount = config.retryCount || 0;
    config.retryCount += 1;
    
    await new Promise(resolve => setTimeout(resolve, 2000));
    return service(config);
  }
);

export default service;
