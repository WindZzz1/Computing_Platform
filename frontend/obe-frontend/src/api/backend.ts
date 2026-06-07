import type { AxiosRequestConfig } from 'axios'
import request from './request'

export interface ApiResponse<T> {
  code: number
  data: T
  message: string
}

export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface PageQuery {
  current?: number
  pageSize?: number
}

export interface SysUserLoginRequest {
  username: string
  password: string
}

export interface SysUserLoginVO {
  id: number
  username: string
  roleCode: string
  collegeName?: string
  status?: number
  createTime?: string
  token: string
}

export interface CreateUserRequest {
  username: string
  password: string
  roleCode: string
  collegeId?: number | null
  status?: number | null
}

export interface SysUserVO {
  id: number
  username: string
  roleCode: string
  collegeName?: string
  status?: number
  createTime?: string
}

export interface CourseVO {
  id: number
  courseCode: string
  courseName: string
  courseNature?: string
  credit?: number
  majorId?: number
  majorName?: string
  collegeId?: number
  collegeName?: string
  createTime?: string
  updateTime?: string
}

export interface CourseSimpleVO {
  id: number
  courseCode: string
  courseName: string
}

export interface CourseObjectiveVO {
  id: number
  courseId: number
  objCode: string
  objName?: string
  objDesc?: string
  createTime?: string
  updateTime?: string
}

export interface AssessmentPointVO {
  id: number
  courseId: number
  pointCode: string
  pointName: string
  fullScore?: number
  objectiveId?: number
  objCode?: string
  objName?: string
  objectiveIds?: number[]
  objectives?: CourseObjectiveVO[]
  createTime?: string
  updateTime?: string
}

export interface CourseObjectiveCreateRequest {
  courseId: number
  objCode: string
  objName: string
  objDesc?: string
}

export interface CourseObjectiveUpdateRequest extends CourseObjectiveCreateRequest {
  id: number
}

export interface AssessmentPointCreateRequest {
  courseId: number
  pointCode: string
  pointName: string
  fullScore: number
  objectiveId?: number
  objectiveIds?: number[]
}

export interface AssessmentPointUpdateRequest extends AssessmentPointCreateRequest {
  id: number
}

export interface WeightObjectiveIndicatorVO {
  id: number
  courseId: number
  objectiveId: number
  objCode?: string
  objName?: string
  indicatorId: number
  indicatorCode?: string
  indicatorName?: string
  innerWeight?: number
  createTime?: string
  updateTime?: string
}

export interface WeightCheckVO {
  valid: boolean
  indicatorWeightSumMap?: Record<number, number | string>
}

export interface MatrixCourseIndicatorVO {
  id: number
  majorId: number
  majorName?: string
  courseId: number
  courseCode?: string
  courseName?: string
  indicatorId: number
  indicatorCode?: string
  indicatorName?: string
  totalWeight?: number
  createTime?: string
  updateTime?: string
}

export interface MatrixConfigVO {
  majorId: number
  majorName?: string
  courses: Array<{
    id: number
    courseCode: string
    courseName: string
    credit?: number
  }>
  indicators: Array<{
    id: number
    indicatorCode: string
    indicatorName: string
    requirementId?: number
  }>
  matrixData: MatrixCourseIndicatorVO[]
  columnSums?: Record<number, number>
}

export interface MatrixWeightCheckVO {
  valid: boolean
  message?: string
  columnSums?: Record<number, number | string>
}

export interface TeachingClassVO {
  id: number
  className: string
  courseId?: number
  courseCode?: string
  courseName?: string
  teacherId?: number
  teacherName?: string
  termId?: number
  yearName?: string
  semesterName?: string
  studentCount?: number
  createTime?: string
  updateTime?: string
}

export interface TeachingClassPageQuery extends PageQuery {
  className?: string
  courseId?: number | null
  teacherId?: number | null
  termId?: number | null
}

export interface TeachingClassCreateRequest {
  className: string
  courseId: number
  teacherId: number
  termId: number
}

export interface TeachingClassUpdateRequest extends TeachingClassCreateRequest {
  id: number
}

export interface StudentVO {
  id: number
  studentNo: string
  name: string
  collegeId?: number
  collegeName?: string
  majorId?: number
  majorName?: string
  className?: string
  createTime?: string
  updateTime?: string
}

export interface CoursePageQuery extends PageQuery {
  courseCode?: string
  courseName?: string
  courseNature?: string
  majorId?: number | null
  createTimeStart?: string
  createTimeEnd?: string
}

export interface CourseCreateRequest {
  courseCode: string
  courseName: string
  courseNature: string
  credit: number
  majorId?: number | null
}

export interface CourseUpdateRequest extends CourseCreateRequest {
  id: number
}

export interface CourseImportResult {
  total: number
  successCount: number
  failCount: number
  failDetails?: Array<Record<string, string>>
}

export interface StudentImportResult {
  total?: number
  totalCount?: number
  successCount: number
  failCount: number
  failDetails?: Array<Record<string, string>>
}

export interface GradeEntryTemplateRequest {
  classId: number
}

export interface GradeEntryImportRequest {
  classId: number
  excelFile: string
}

export interface GradeEntryQueryRequest extends PageQuery {
  classId?: number
  studentId?: number
  pointId?: number
}

export interface GradeImportResultVO {
  success?: boolean
  studentCount?: number
  scoreCount?: number
  errorMessages?: string[]
  warningMessages?: string[]
}

export interface StudentScoreVO {
  id: number
  studentId: number
  studentNo: string
  name: string
  pointId: number
  pointCode?: string
  pointName?: string
  score?: number
  fullScore?: number
}

export interface StudentScoreUpdateItem {
  id?: number
  studentId: number
  pointId: number
  score: number
}

export interface StudentScoreUpdateRequest {
  classId: number
  scores: StudentScoreUpdateItem[]
}

export interface IndicatorPointVO {
  id: number
  indicatorCode: string
  indicatorName: string
  description?: string
  requirementId?: number
  requirementCode?: string
  requirementName?: string
  createTime?: string
  updateTime?: string
}

export interface IndicatorPageQuery extends PageQuery {
  indicatorCode?: string
  indicatorName?: string
  requirementId?: number | null
  createTimeStart?: string
  createTimeEnd?: string
}

export interface GraduationRequirementVO {
  id: number
  requirementCode: string
  requirementName: string
  description?: string
  majorId?: number
  majorName?: string
  collegeId?: number
  collegeName?: string
  createTime?: string
  updateTime?: string
}

export interface GraduationRequirementPageQuery extends PageQuery {
  requirementCode?: string
  requirementName?: string
  majorId?: number | null
  createTimeStart?: string
  createTimeEnd?: string
}

export interface GraduationRequirementCreateRequest {
  requirementCode: string
  requirementName: string
  description?: string
  majorId?: number | null
}

export interface GraduationRequirementUpdateRequest extends GraduationRequirementCreateRequest {
  id: number
}

export interface IndicatorPointCreateRequest {
  indicatorCode: string
  indicatorName: string
  description?: string
  requirementId: number
}

export interface IndicatorPointUpdateRequest extends IndicatorPointCreateRequest {
  id: number
}

export interface DictSimpleItem {
  id: number
  name: string
}

export interface SysDictMajorSimpleVO {
  id: number
  majorName: string
}

export interface SysDictMajorVO {
  id: number
  majorCode: string
  majorName: string
  collegeId?: number
  collegeName?: string
}

export interface SysDictCollegeSimpleVO {
  id: number
  collegeName: string
}

export interface SysDictCollegeVO {
  id: number
  collegeName: string
}

export interface SysDictCollegeCreateRequest {
  collegeName: string
}

export interface SysDictCollegeUpdateRequest extends SysDictCollegeCreateRequest {
  id: number
}

export interface SysDictMajorCreateRequest {
  majorCode: string
  majorName: string
  collegeId: number
}

export interface SysDictMajorUpdateRequest extends SysDictMajorCreateRequest {
  id: number
}

export interface SysDictSchoolYearVO {
  id: number
  yearName: string
  semesterName: string
}

export interface SysDictSchoolYearCreateRequest {
  yearName: string
  semesterName: string
}

export interface SysDictSchoolYearUpdateRequest extends SysDictSchoolYearCreateRequest {
  id: number
}

function unwrap<T>(response: ApiResponse<T>): T {
  if (response.code !== 0) {
    throw new Error(response.message || '请求失败')
  }
  return response.data
}

export async function apiGet<T>(url: string, config?: AxiosRequestConfig) {
  const response = await request.get<ApiResponse<T>>(url, config)
  return unwrap(response.data)
}

export async function apiPost<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  const response = await request.post<ApiResponse<T>>(url, data, config)
  return unwrap(response.data)
}
