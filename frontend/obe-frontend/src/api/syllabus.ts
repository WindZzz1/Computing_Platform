import request from './request'
import {
  apiPost,
  type ApiResponse,
  type AssessmentPointCreateRequest,
  type AssessmentPointUpdateRequest,
  type AssessmentPointVO,
  type CourseObjectiveCreateRequest,
  type CourseObjectiveUpdateRequest,
  type CourseObjectiveVO,
  type IndicatorPointVO,
  type PageResponse,
  type WeightCheckVO,
  type WeightObjectiveIndicatorVO
} from './backend'

export function listCourseObjectives(courseId: number) {
  return apiPost<PageResponse<CourseObjectiveVO>>('/course/objective/list', { courseId })
}

export function getCourseObjective(id: number) {
  return apiPost<CourseObjectiveVO>('/course/objective/get', { id })
}

export function createCourseObjective(payload: CourseObjectiveCreateRequest) {
  return apiPost<number>('/course/objective/add', payload)
}

export function updateCourseObjective(payload: CourseObjectiveUpdateRequest) {
  return apiPost<boolean>('/course/objective/update', payload)
}

export function deleteCourseObjective(id: number) {
  return apiPost<boolean>('/course/objective/delete', { id })
}

export function listAssessmentPoints(courseId: number) {
  return apiPost<PageResponse<AssessmentPointVO>>('/assessment/point/list', { courseId })
}

export function getAssessmentPoint(id: number) {
  return apiPost<AssessmentPointVO>('/assessment/point/get', { id })
}

export function createAssessmentPoint(payload: AssessmentPointCreateRequest) {
  return apiPost<number>('/assessment/point/add', payload)
}

export function updateAssessmentPoint(payload: AssessmentPointUpdateRequest) {
  return apiPost<boolean>('/assessment/point/update', payload)
}

export function deleteAssessmentPoint(id: number) {
  return apiPost<boolean>('/assessment/point/delete', { id })
}

export function listAvailableIndicators(courseId: number) {
  return apiPost<IndicatorPointVO[]>('/weight/objective-indicator/available', { courseId })
}

export function listObjectiveIndicatorWeights(courseId: number) {
  return apiPost<WeightObjectiveIndicatorVO[]>('/weight/objective-indicator/list', { courseId })
}

export function saveObjectiveIndicatorWeights(
  courseId: number,
  items: Array<{ objectiveId: number; indicatorId: number; innerWeight: number }>
) {
  return apiPost<boolean>('/weight/objective-indicator/save', {
    courseId,
    weightList: items
  })
}

export function checkObjectiveIndicatorWeights(
  courseId: number,
  items: Array<{ objectiveId: number; indicatorId: number; innerWeight: number }>
) {
  return apiPost<WeightCheckVO>('/weight/objective-indicator/check', {
    courseId,
    weightList: items
  })
}

// 课程目标 Excel 批量导入
export async function importCourseObjectivesFromExcel(file: File) {
  const formData = new FormData()
  formData.append('file', file)

  const response = await request.post<ApiResponse<Record<string, unknown>>>(
    '/course/objective/import/excel',
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  )

  if (response.data.code !== 0) {
    throw new Error(response.data.message || '课程目标导入失败')
  }

  return response.data.data
}

// 下载课程目标导入模板
export async function downloadCourseObjectiveTemplate() {
  const response = await request.get('/course/objective/template', {
    responseType: 'blob'
  })
  return response.data as Blob
}

// 考核点 Excel 批量导入（含支撑权重）
export async function importAssessmentPointsFromExcel(file: File) {
  const formData = new FormData()
  formData.append('file', file)

  const response = await request.post<ApiResponse<Record<string, unknown>>>(
    '/assessment/point/import/excel',
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  )

  if (response.data.code !== 0) {
    throw new Error(response.data.message || '考核点导入失败')
  }

  return response.data.data
}

// 下载考核点导入模板
export async function downloadAssessmentPointTemplate() {
  const response = await request.get('/assessment/point/template', {
    responseType: 'blob'
  })
  return response.data as Blob
}

// 内部贡献权重 Excel 批量导入
export async function importObjectiveIndicatorWeightsFromExcel(file: File) {
  const formData = new FormData()
  formData.append('file', file)

  const response = await request.post<ApiResponse<Record<string, unknown>>>(
    '/weight/objective-indicator/import/excel',
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  )

  if (response.data.code !== 0) {
    throw new Error(response.data.message || '内部贡献权重导入失败')
  }

  return response.data.data
}

// 下载内部贡献权重导入模板
export async function downloadObjectiveWeightTemplate() {
  const response = await request.get('/weight/objective-indicator/template', {
    responseType: 'blob'
  })
  return response.data as Blob
}
