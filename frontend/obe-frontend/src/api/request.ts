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

export default request
