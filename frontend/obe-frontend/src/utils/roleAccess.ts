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
  dashboard: ['admin', 'edu', 'leader', 'teacher'],
  basicData: ['admin', 'edu', 'leader'],
  courseLibrary: ['admin', 'edu'],
  indicatorLibrary: ['admin', 'leader'],
  matrix: ['admin', 'edu'],
  syllabus: ['admin', 'teacher'],
  score: ['admin', 'edu'],
  calculation: ['admin', 'edu'],
  report: ['admin', 'leader']
}

export function canAccessFeature(role: Role, feature: FeatureKey) {
  if (role === 'admin') {
    return true
  }
  return featureRoles[feature].includes(role)
}

export function getAccessibleFeatures(role: Role) {
  return Object.keys(featureRoles).filter((feature) => canAccessFeature(role, feature as FeatureKey)) as FeatureKey[]
}

export function getDefaultRoute(role: Role) {
  if (canAccessFeature(role, 'dashboard')) return '/dashboard'
  return '/login'
}
