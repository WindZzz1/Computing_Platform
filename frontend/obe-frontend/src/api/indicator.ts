import request from './request'
import {
  apiPost,
  type ApiResponse,
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

export function getIndicator(id: number) {
  return apiPost<IndicatorPointVO>('/requirement/indicator/get', { id })
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

export function getGraduationRequirement(id: number) {
  return apiPost<GraduationRequirementVO>('/requirement/graduation/get', { id })
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

export async function importGraduationRequirementsFromExcel(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await request.post<ApiResponse<Record<string, unknown>>>(
    '/requirement/graduation/import/excel', formData,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  )
  if (response.data.code !== 0) throw new Error(response.data.message || '毕业要求导入失败')
  return response.data.data
}

export async function downloadGraduationRequirementTemplate() {
  const response = await request.get('/requirement/graduation/template', { responseType: 'blob' })
  return response.data as Blob
}

export async function importIndicatorPointsFromExcel(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await request.post<ApiResponse<Record<string, unknown>>>(
    '/requirement/indicator/import/excel', formData,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  )
  if (response.data.code !== 0) throw new Error(response.data.message || '指标点导入失败')
  return response.data.data
}

export async function downloadIndicatorPointTemplate() {
  const response = await request.get('/requirement/indicator/template', { responseType: 'blob' })
  return response.data as Blob
}
