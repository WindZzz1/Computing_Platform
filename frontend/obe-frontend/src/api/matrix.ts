import request from './request'
import { apiGet, apiPost, type ApiResponse, type MatrixConfigVO, type MatrixWeightCheckVO } from './backend'

export function getMatrixConfig(majorId: number) {
  return apiGet<MatrixConfigVO>(`/matrix/config/${majorId}`)
}

export function saveMatrixConfig(
  majorId: number,
  items: Array<{ courseId: number; indicatorId: number; totalWeight: number }>
) {
  return apiPost<boolean>('/matrix/save', {
    majorId,
    matrixItems: items
  })
}

export function checkMatrixConfig(
  majorId: number,
  items: Array<{ courseId: number; indicatorId: number; totalWeight: number }>
) {
  return apiPost<MatrixWeightCheckVO>('/matrix/check', {
    majorId,
    matrixItems: items
  })
}

export async function importMatrixFromExcel(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await request.post<ApiResponse<Record<string, unknown>>>(
    '/matrix/import/excel', formData,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  )
  if (response.data.code !== 0) throw new Error(response.data.message || '宏观支撑矩阵导入失败')
  return response.data.data
}

export async function downloadMatrixTemplate() {
  const response = await request.get('/matrix/template', { responseType: 'blob' })
  return response.data as Blob
}
