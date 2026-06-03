import request from './request'

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface PageParams {
  current?: number
  pageSize?: number
}

export interface CollegeVO {
  id: number
  collegeName: string
  collegeCode?: string
  createTime?: string
  updateTime?: string
}

export interface MajorVO {
  id: number
  majorCode: string
  majorName: string
  collegeId?: number
  collegeName?: string
  createTime?: string
  updateTime?: string
}

export interface SchoolYearVO {
  id: number
  yearName: string
  semesterName: string
  createTime?: string
  updateTime?: string
}

export const pageColleges = (params: PageParams & Partial<CollegeVO> = {}) => {
  return request.post<PageResult<CollegeVO>, PageResult<CollegeVO>>('/dict/college/page', {
    current: 1,
    pageSize: 10,
    ...params
  })
}

export const pageMajors = (params: PageParams & Partial<MajorVO> = {}) => {
  return request.post<PageResult<MajorVO>, PageResult<MajorVO>>('/dict/major/page', {
    current: 1,
    pageSize: 10,
    ...params
  })
}

export const pageSchoolYears = (params: PageParams & Partial<SchoolYearVO> = {}) => {
  return request.post<PageResult<SchoolYearVO>, PageResult<SchoolYearVO>>('/dict/schoolyear/page', {
    current: 1,
    pageSize: 10,
    ...params
  })
}
