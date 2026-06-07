import request from './request'
import {
  apiPost,
  type ApiResponse,
  type GradeEntryImportRequest,
  type GradeEntryQueryRequest,
  type GradeImportResultVO,
  type PageResponse,
  type StudentScoreUpdateRequest,
  type StudentScoreVO
} from './backend'

export function queryGrades(payload: GradeEntryQueryRequest) {
  return apiPost<PageResponse<StudentScoreVO>>('/grade-entry/query', payload)
}

export function importGrades(payload: GradeEntryImportRequest) {
  return apiPost<GradeImportResultVO>('/grade-entry/import', payload)
}

export function updateGrades(payload: StudentScoreUpdateRequest) {
  return apiPost<boolean>('/grade-entry/update', payload)
}

export function deleteClassGrades(id: number) {
  return apiPost<boolean>('/grade-entry/delete', { id })
}

export async function downloadGradeTemplate(classId: number) {
  const response = await request.post<ApiResponse<Blob>>(
    '/grade-entry/template/download',
    { classId },
    {
      responseType: 'blob'
    }
  )

  return response.data as unknown as Blob
}
