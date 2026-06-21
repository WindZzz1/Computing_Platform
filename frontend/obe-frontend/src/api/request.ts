import axios from 'axios'

export const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8101/api'
export const apiDocUrl = `${apiBaseUrl.replace(/\/api\/?$/, '')}/api/doc.html#/home`

const request = axios.create({
  baseURL: apiBaseUrl,
  timeout: 30000,
  withCredentials: true
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一处理业务码 40100（未登录）→ 清登录态并跳登录页，避免每个调用方各自处理。
// 不直接依赖 user store / router 以免循环依赖；store 在登录页重新加载时按 localStorage 初始化。
request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body === 'object' && body.code === 40100) {
      localStorage.removeItem('token')
      localStorage.removeItem('name')
      localStorage.removeItem('role')
      localStorage.removeItem('collegeName')
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return response
  },
  (error) => Promise.reject(error)
)

export default request
