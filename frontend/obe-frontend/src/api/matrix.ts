import { apiGet, apiPost, type MatrixConfigVO, type MatrixWeightCheckVO } from './backend'

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
