import axios from 'axios'
import request from './request'
import { apiPost, type ApiResponse } from './backend'
import type {
  CourseAchievementReportRequest,
  CourseAchievementReportVO,
  MajorAchievementRadarVO,
  MajorReportRequest,
  PenetrationAccountVO
} from './backend'

function unwrapBlobErrorMessage(text: string) {
  try {
    const parsed = JSON.parse(text) as { message?: string }
    return parsed.message || text
  } catch {
    return text
  }
}

async function normalizeBlobRequestError(error: unknown, fallback: string) {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data
    if (data instanceof Blob) {
      const text = await data.text()
      throw new Error(unwrapBlobErrorMessage(text) || fallback)
    }
    if (typeof data === 'string' && data) {
      throw new Error(unwrapBlobErrorMessage(data) || fallback)
    }
    throw new Error(error.message || fallback)
  }

  if (error instanceof Error) {
    throw error
  }

  throw new Error(fallback)
}

export function getCourseAchievementReportData(payload: CourseAchievementReportRequest) {
  return apiPost<CourseAchievementReportVO>('/course-achievement-report/data', payload)
}

export function getMajorReportRadarData(payload: MajorReportRequest) {
  return apiPost<MajorAchievementRadarVO>('/major-report/radar-data', {
    ...payload,
    reportType: 'RADAR'
  })
}

export function getMajorPenetrationAccount(payload: MajorReportRequest) {
  return apiPost<PenetrationAccountVO>('/major-report/penetration-account', {
    ...payload,
    reportType: 'ACCOUNT'
  })
}

export async function downloadCourseAchievementTemplate() {
  try {
    const response = await request.get('/course-achievement-report/template', {
      responseType: 'blob'
    })
    return response.data as Blob
  } catch (error) {
    await normalizeBlobRequestError(error, '课程报表模板下载失败')
    throw error
  }
}

export async function exportCourseAchievementReportExcel(payload: CourseAchievementReportRequest) {
  try {
    const response = await request.post('/course-achievement-report/export/excel', payload, {
      responseType: 'blob'
    })
    return response.data as Blob
  } catch (error) {
    await normalizeBlobRequestError(error, '课程报表 Excel 导出失败')
    throw error
  }
}

export async function exportCourseAchievementReportPdf(payload: CourseAchievementReportRequest) {
  try {
    const response = await request.post('/course-achievement-report/export/pdf', payload, {
      responseType: 'blob'
    })
    return response.data as Blob
  } catch (error) {
    await normalizeBlobRequestError(error, '课程报表 PDF 导出失败')
    throw error
  }
}

export async function exportMajorPenetrationAccountExcel(payload: MajorReportRequest) {
  try {
    const response = await request.post('/major-report/export/account-excel', payload, {
      responseType: 'blob'
    })
    return response.data as Blob
  } catch (error) {
    await normalizeBlobRequestError(error, '专业穿透式台账导出失败')
    throw error
  }
}

export async function exportMajorIndicatorAchievementExcel(payload: MajorReportRequest) {
  try {
    const response = await request.post('/major-report/export/indicator-excel', payload, {
      responseType: 'blob'
    })
    return response.data as Blob
  } catch (error) {
    await normalizeBlobRequestError(error, '专业指标点达成度导出失败')
    throw error
  }
}

export async function exportMajorIndicatorAchievementPdf(payload: MajorReportRequest) {
  try {
    const response = await request.post('/major-report/export/indicator-pdf', payload, {
      responseType: 'blob'
    })
    return response.data as Blob
  } catch (error) {
    await normalizeBlobRequestError(error, '专业指标点达成度导出失败')
    throw error
  }
}
