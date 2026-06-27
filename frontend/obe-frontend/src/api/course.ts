import request from './request'
import {
  apiPost,
  type ApiResponse,
  type CourseCreateRequest,
  type CourseImportResult,
  type CoursePageQuery,
  type CourseSimpleVO,
  type CourseUpdateRequest,
  type CourseVO,
  type PageResponse
} from './backend'

export function pageCourses(payload: CoursePageQuery) {
  return apiPost<PageResponse<CourseVO>>('/course/page', payload)
}

export function getCourse(id: number) {
  return apiPost<CourseVO>('/course/get', { id })
}

export function listCourses() {
  return apiPost<CourseSimpleVO[]>('/course/list')
}

export function listMyCourses() {
  return apiPost<CourseSimpleVO[]>('/course/my')
}

export function createCourse(payload: CourseCreateRequest) {
  return apiPost<number>('/course/add', payload)
}

export function updateCourse(payload: CourseUpdateRequest) {
  return apiPost<boolean>('/course/update', payload)
}

export function deleteCourse(id: number) {
  return apiPost<boolean>('/course/delete', { id })
}

export async function importCoursesFromExcel(file: File) {
  const formData = new FormData()
  formData.append('file', file)

  const response = await request.post<ApiResponse<CourseImportResult>>('/course/import/excel', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })

  if (response.data.code !== 0) {
    throw new Error(response.data.message || '课程导入失败')
  }

  return response.data.data
}

export async function downloadCourseTemplate() {
  const response = await request.get('/course/template', {
    responseType: 'blob'
  })
  return response.data as Blob
}
