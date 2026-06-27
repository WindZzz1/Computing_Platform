import type { Role } from '@/types'

export type FeatureKey =
  | 'dashboard'
  | 'basicData'
  | 'courseLibrary'
  | 'indicatorLibrary'
  | 'matrix'
  | 'syllabus'
  | 'score'
  | 'calculation'
  | 'report'

const featureRoles: Record<FeatureKey, Role[]> = {
  dashboard: ['leader'],
  basicData: ['admin', 'edu', 'leader'],
  courseLibrary: ['admin', 'edu'],
  indicatorLibrary: ['admin', 'leader'],
  matrix: ['leader'],
  syllabus: ['teacher'],
  score: ['edu', 'teacher'],
  calculation: ['edu', 'leader', 'teacher'],
  report: ['edu', 'leader', 'teacher']
}

export function canAccessFeature(role: Role, feature: FeatureKey) {
  return featureRoles[feature].includes(role)
}

export function getAccessibleFeatures(role: Role) {
  return Object.keys(featureRoles).filter((feature) => canAccessFeature(role, feature as FeatureKey)) as FeatureKey[]
}

const roleHomeRoute: Record<Role, string> = {
  admin: '/basic-data',
  edu: '/basic-data',
  leader: '/dashboard',
  teacher: '/syllabus'
}

export function getDefaultRoute(role: Role) {
  return roleHomeRoute[role] || '/login'
}
