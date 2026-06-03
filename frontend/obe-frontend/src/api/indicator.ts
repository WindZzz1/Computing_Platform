import {
  apiPost,
  type GraduationRequirementCreateRequest,
  type GraduationRequirementPageQuery,
  type GraduationRequirementUpdateRequest,
  type GraduationRequirementVO,
  type IndicatorPageQuery,
  type IndicatorPointCreateRequest,
  type IndicatorPointUpdateRequest,
  type IndicatorPointVO,
  type PageResponse
} from './backend'

export function pageIndicators(payload: IndicatorPageQuery) {
  return apiPost<PageResponse<IndicatorPointVO>>('/requirement/indicator/page', payload)
}

export function createIndicator(payload: IndicatorPointCreateRequest) {
  return apiPost<number>('/requirement/indicator/add', payload)
}

export function updateIndicator(payload: IndicatorPointUpdateRequest) {
  return apiPost<boolean>('/requirement/indicator/update', payload)
}

export function deleteIndicator(id: number) {
  return apiPost<boolean>('/requirement/indicator/delete', { id })
}

export function pageGraduationRequirements(payload: GraduationRequirementPageQuery) {
  return apiPost<PageResponse<GraduationRequirementVO>>('/requirement/graduation/page', payload)
}

export function createGraduationRequirement(payload: GraduationRequirementCreateRequest) {
  return apiPost<number>('/requirement/graduation/add', payload)
}

export function updateGraduationRequirement(payload: GraduationRequirementUpdateRequest) {
  return apiPost<boolean>('/requirement/graduation/update', payload)
}

export function deleteGraduationRequirement(id: number) {
  return apiPost<boolean>('/requirement/graduation/delete', { id })
}
