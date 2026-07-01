import request from './request'
import {
  apiPost,
  type ApiResponse,
  type PageResponse,
  type StudentImportResult,
  type StudentVO,
  type TeachingClassCreateRequest,
  type TeachingClassPageQuery,
  type TeachingClassUpdateRequest,
  type TeachingClassVO
} from './backend'

export function pageTeachingClasses(payload: TeachingClassPageQuery) {
  return apiPost<PageResponse<TeachingClassVO>>('/teaching-class/page', payload)
}

export function listMyTeachingClasses() {
  return apiPost<TeachingClassVO[]>('/teaching-class/my')
}

export function createTeachingClass(payload: TeachingClassCreateRequest) {
  return apiPost<number>('/teaching-class/add', payload)
}

export function updateTeachingClass(payload: TeachingClassUpdateRequest) {
  return apiPost<boolean>('/teaching-class/update', payload)
}

export function deleteTeachingClass(id: number) {
  return apiPost<boolean>('/teaching-class/delete', { id })
}

export function getTeachingClass(id: number) {
  return apiPost<TeachingClassVO>('/teaching-class/get', { id })
}

export function getTeachingClassStudents(classId: number) {
  return apiPost<StudentVO[]>('/teaching-class/students', { id: classId })
}

export function bindStudentsToClass(classId: number, studentIds: number[]) {
  return apiPost<number>('/teaching-class/bind-students', {
    classId,
    studentIds
  })
}

export function unbindStudentFromClass(classId: number, studentId: number) {
  return apiPost<boolean>('/teaching-class/unbind-student', undefined, {
    params: {
      classId,
      studentId
    }
  })
}

export function importStudentsToClass(
  classId: number,
  students: Array<{ studentNo: string; studentName?: string }>
) {
  return apiPost<StudentImportResult>('/teaching-class/import-students', {
    classId,
    students
  })
}

export async function importStudentsFromExcel(file: File) {
  const formData = new FormData()
  formData.append('file', file)

  const response = await request.post<ApiResponse<StudentImportResult>>('/student/import/excel', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })

  if (response.data.code !== 0) {
    throw new Error(response.data.message || '学生导入失败')
  }

  return response.data.data
}

export async function downloadStudentTemplate() {
  const response = await request.get('/student/template', {
    responseType: 'blob'
  })
  return response.data as Blob
}

export async function importStudentsToClassFromExcel(classId: number, file: File) {
  const formData = new FormData()
  formData.append('classId', String(classId))
  formData.append('file', file)

  const response = await request.post<ApiResponse<StudentImportResult>>('/teaching-class/import-students/excel', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })

  if (response.data.code !== 0) {
    throw new Error(response.data.message || '教学班学生导入失败')
  }

  return response.data.data
}

export async function downloadClassStudentTemplate() {
  const response = await request.get('/teaching-class/import-students/template', {
    responseType: 'blob'
  })
  return response.data as Blob
}

export async function importTeachingClassesFromExcel(file: File) {
  const formData = new FormData()
  formData.append('file', file)

  const response = await request.post<ApiResponse<StudentImportResult>>('/teaching-class/import/excel', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })

  if (response.data.code !== 0) {
    throw new Error(response.data.message || '教学班导入失败')
  }

  return response.data.data
}

export async function downloadTeachingClassTemplate() {
  const response = await request.get('/teaching-class/template', {
    responseType: 'blob'
  })
  return response.data as Blob
}
