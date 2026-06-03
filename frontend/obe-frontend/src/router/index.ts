import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { canAccessFeature, getDefaultRoute, type FeatureKey } from '@/utils/roleAccess'

const routes: RouteRecordRaw[] = [
  { path: '/login', component: () => import('@/views/login/LoginView.vue') },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', meta: { title: '首页', feature: 'dashboard' }, component: () => import('@/views/dashboard/DashboardView.vue') },
      { path: 'basic-data', meta: { title: '基础数据管理', feature: 'basicData' }, component: () => import('@/views/basic-data/BasicDataView.vue') },
      { path: 'matrix', meta: { title: '矩阵配置', feature: 'matrix' }, component: () => import('@/views/matrix/MatrixView.vue') },
      { path: 'syllabus', meta: { title: '课程大纲管理', feature: 'syllabus' }, component: () => import('@/views/syllabus/SyllabusView.vue') },
      { path: 'score', meta: { title: '成绩管理与计算', feature: 'score' }, component: () => import('@/views/score/ScoreView.vue') },
      { path: 'calculation', meta: { title: '专业级计算', feature: 'calculation' }, component: () => import('@/views/calculation/CalculationView.vue') },
      { path: 'report', meta: { title: '报表与导出', feature: 'report' }, component: () => import('@/views/report/ReportView.vue') }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  const user = useUserStore()
  if (to.path !== '/login' && !user.token) return '/login'
  if (to.path === '/login' && user.token) return getDefaultRoute(user.role)

  const feature = to.meta.feature as FeatureKey | undefined
  if (feature && !canAccessFeature(user.role, feature)) {
    return getDefaultRoute(user.role)
  }
})

export default router
