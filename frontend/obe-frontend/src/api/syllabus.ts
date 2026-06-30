import {
  apiPost,
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
