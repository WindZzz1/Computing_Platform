import axios from 'axios'

const request = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL || '/api', timeout: 10000 })
request.interceptors.request.use(config => { config.headers.Authorization = localStorage.getItem('token') || ''; return config })
export default request
