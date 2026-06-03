import axios from 'axios'
import { ElMessage } from 'element-plus'

export interface BaseResponse<T> {
  code: number
  data: T
  message: string
}

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000
})

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const result = response.data as BaseResponse<unknown>
    if (!result || typeof result.code !== 'number') {
      return response.data
    }
    if (result.code === 0) {
      return result.data
    }
    if (result.code === 40100) {
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('name')
      ElMessage.error(result.message || '登录已失效，请重新登录')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    } else {
      ElMessage.error(result.message || '请求失败')
    }
    return Promise.reject(result)
  },
  (error) => {
    ElMessage.error(error?.message || '网络异常，请检查后端服务')
    return Promise.reject(error)
  }
)

export default request
