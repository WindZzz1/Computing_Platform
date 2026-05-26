import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import AdminLayout from '@/layouts/AdminLayout.vue'

const routes: RouteRecordRaw[] = [
  { path: '/login', component: () => import('@/views/login/LoginView.vue') },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', meta: { title: '首页' }, component: () => import('@/views/dashboard/DashboardView.vue') },
      { path: 'basic-data', meta: { title: '基础数据管理' }, component: () => import('@/views/basic-data/BasicDataView.vue') },
      { path: 'matrix', meta: { title: '宏观支撑矩阵' }, component: () => import('@/views/matrix/MatrixView.vue') },
      { path: 'syllabus', meta: { title: '课程大纲管理' }, component: () => import('@/views/syllabus/SyllabusView.vue') },
      { path: 'score', meta: { title: '成绩管理与计算' }, component: () => import('@/views/score/ScoreView.vue') },
      { path: 'calculation', meta: { title: '专业级计算' }, component: () => import('@/views/calculation/CalculationView.vue') },
      { path: 'report', meta: { title: '报表与导出' }, component: () => import('@/views/report/ReportView.vue') }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  const user = useUserStore()
  if (to.path !== '/login' && !user.token) return '/login'
})

export default router
