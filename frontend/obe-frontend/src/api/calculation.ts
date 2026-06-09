import {
  type AchievementCalculationDetailVO,
  apiPost,
  type AchievementCalculationRequest,
  type AchievementCalculationResultVO,
  type AchievementCalculationStatusVO,
  type MajorCalculationDashboardVO,
  type MajorCalculationRequest,
  type MajorCalculationResultVO,
  type MajorDashboardQueryRequest
} from './backend'

export function calculateCourseAchievement(payload: AchievementCalculationRequest) {
  return apiPost<AchievementCalculationResultVO>('/achievement-calculation/calculate', payload)
}

export function getCourseAchievementCalculationStatus(classId: number) {
  return apiPost<AchievementCalculationStatusVO>('/achievement-calculation/status', { classId })
}

export function getCourseAchievementCalculationDetail(classId: number) {
  return apiPost<AchievementCalculationDetailVO>('/achievement-calculation/detail', { classId })
}

export function getMajorCalculationDashboard(payload: MajorDashboardQueryRequest) {
  return apiPost<MajorCalculationDashboardVO>('/major-calculation/dashboard', payload)
}

export function calculateMajorAchievement(payload: MajorCalculationRequest) {
  return apiPost<MajorCalculationResultVO>('/major-calculation/calculate', payload)
}

export function getMajorCalculationResult(payload: MajorCalculationRequest) {
  return apiPost<MajorCalculationResultVO>('/major-calculation/result', payload)
}

export function deleteMajorCalculationResult(payload: MajorCalculationRequest) {
  return apiPost<boolean>('/major-calculation/delete', payload)
}
