<template>
  <div class="page">
    <h1 class="page-title">报表与导出</h1>
    <p class="page-desc">本页按当前登录角色展示真实可调用的报表接口，并对后端尚未完整实现的能力给出明确提示。</p>

    <el-alert
      :title="roleSummary.title"
      :type="roleSummary.type"
      :description="roleSummary.description"
      show-icon
      :closable="false"
      class="summary-alert"
    />

    <section class="page-grid top-grid">
      <div class="panel span-5">
        <div class="toolbar">
          <h3 class="panel-title">接口状态概览</h3>
          <el-tag type="info">{{ user.roleName }}</el-tag>
        </div>
        <el-table :data="reportCatalog" border>
          <el-table-column prop="name" label="能力" min-width="180" />
          <el-table-column prop="targetRole" label="适用角色" width="180" />
          <el-table-column label="调用状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.tagType">{{ row.statusText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="tip" label="说明" min-width="220" />
        </el-table>
      </div>

      <div v-if="canUseMajorPrep" class="panel span-7">
        <div class="toolbar">
          <h3 class="panel-title">指标点支撑情况</h3>
          <el-select v-model="selectedMajorId" style="width: 260px" placeholder="选择专业" @change="reloadMajorSupportData">
            <el-option v-for="major in majors" :key="major.id" :label="major.majorName" :value="major.id" />
          </el-select>
        </div>
        <el-table v-loading="supportLoading" :data="indicatorRows" border empty-text="当前专业暂无指标点或矩阵配置">
          <el-table-column prop="code" label="指标点" width="100" />
          <el-table-column prop="requirement" label="所属毕业要求" min-width="140" />
          <el-table-column prop="courseCount" label="支撑课程数" width="110" />
          <el-table-column prop="weightSum" label="矩阵列合计" width="120" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="row.ready ? 'success' : 'warning'">{{ row.ready ? '已就绪' : '待完善' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>

    <section v-if="canUseCourseReport" class="panel action-panel">
      <div class="toolbar">
        <h3 class="panel-title">课程报表联调</h3>
        <el-tag type="success">教师接口已开放</el-tag>
      </div>
      <div class="form-grid">
        <el-select v-model="selectedClassId" placeholder="选择教学班" style="width: 320px" :disabled="!courseClasses.length">
          <el-option v-for="item in courseClasses" :key="item.id" :label="buildClassLabel(item)" :value="item.id" />
        </el-select>
        <el-button :loading="courseActionLoading" :disabled="!courseClasses.length" @click="handleDownloadCourseTemplate">下载报表模板</el-button>
        <el-button type="primary" :loading="courseActionLoading" :disabled="!selectedClassId" @click="handleLoadCourseReport">查询报表数据</el-button>
        <el-button :loading="courseActionLoading" :disabled="!selectedClassId" @click="handleExportCourseReport('EXCEL')">导出 Excel</el-button>
        <el-button :loading="courseActionLoading" :disabled="!selectedClassId" @click="handleExportCourseReport('PDF')">导出 PDF</el-button>
      </div>
      <el-alert
        v-if="!courseClasses.length"
        title="当前账号下还没有可联调的教学班"
        type="info"
        description="请先在成绩管理页创建教学班并完成课程、教师、学期绑定，再回来联调课程报表接口。"
        show-icon
        :closable="false"
        class="section-alert"
      />
      <el-alert
        v-if="courseStatus"
        :title="courseStatus.title"
        :type="courseStatus.type"
        :description="courseStatus.description"
        show-icon
        :closable="false"
        class="section-alert"
      />

      <div v-if="courseReportData" class="result-grid">
        <el-card shadow="never" class="result-card">
          <template #header>课程报表返回摘要</template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="教学班">{{ courseReportData.className || courseReportData.classId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="课程">{{ courseReportData.courseName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="教师">{{ courseReportData.teacherName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="学生数">{{ courseReportData.studentCount ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="课程目标数">{{ courseReportData.objectiveSummaries?.length ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="指标点结果数">{{ courseReportData.indicatorAchievements?.length ?? 0 }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </div>
    </section>

    <section v-if="canUseMajorReport" class="panel action-panel">
      <div class="toolbar">
        <h3 class="panel-title">专业报表联调</h3>
        <el-tag type="warning">接口已开放，服务层待完整实现</el-tag>
      </div>
      <div class="form-grid">
        <el-select v-model="selectedMajorId" placeholder="选择专业" style="width: 220px" :disabled="!majors.length" @change="reloadMajorSupportData">
          <el-option v-for="major in majors" :key="major.id" :label="major.majorName" :value="major.id" />
        </el-select>
        <el-select v-model="selectedTermId" placeholder="选择学期" style="width: 220px" :disabled="!schoolYears.length">
          <el-option
            v-for="term in schoolYears"
            :key="term.id"
            :label="`${term.yearName} ${term.semesterName}`"
            :value="term.id"
          />
        </el-select>
        <el-input v-model.trim="selectedGrade" placeholder="输入年级，例如 2021" style="width: 200px" />
        <el-button type="primary" :loading="majorActionLoading" :disabled="!canSubmitMajorRequest" @click="handleLoadMajorRadar">查询雷达图数据</el-button>
        <el-button :loading="majorActionLoading" :disabled="!canSubmitMajorRequest" @click="handleLoadPenetrationAccount">查询穿透式台账</el-button>
        <el-button :loading="majorActionLoading" :disabled="!canSubmitMajorRequest" @click="handleExportMajorAccount">导出台账 Excel</el-button>
      </div>
      <el-alert
        v-if="!majors.length || !schoolYears.length"
        title="当前还缺少专业或学期基础数据"
        type="info"
        description="请先在基础数据管理中补齐专业、毕业要求、学期信息，再进行专业报表联调。"
        show-icon
        :closable="false"
        class="section-alert"
      />
      <el-alert
        v-if="majorStatus"
        :title="majorStatus.title"
        :type="majorStatus.type"
        :description="majorStatus.description"
        show-icon
        :closable="false"
        class="section-alert"
      />

      <div v-if="majorRadarData || majorPenetrationData" class="result-grid">
        <el-card v-if="majorRadarData" shadow="never" class="result-card">
          <template #header>雷达图接口返回摘要</template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="专业">{{ majorRadarData.majorName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="年级">{{ majorRadarData.grade || selectedGrade || '-' }}</el-descriptions-item>
            <el-descriptions-item label="指标点数">{{ majorRadarData.indicatorPoints?.length ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="生成时间">{{ majorRadarData.generatedTime || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card v-if="majorPenetrationData" shadow="never" class="result-card">
          <template #header>穿透式台账接口返回摘要</template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="专业">{{ majorPenetrationData.majorInfo?.majorName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="年级">{{ majorPenetrationData.majorInfo?.grade || selectedGrade || '-' }}</el-descriptions-item>
            <el-descriptions-item label="课程数">{{ majorPenetrationData.majorInfo?.totalCourses ?? majorPenetrationData.courses?.length ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="学生数">{{ majorPenetrationData.majorInfo?.totalStudents ?? 0 }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </div>
    </section>

    <section v-if="canUseMatrixLedger" class="panel action-panel">
      <div class="toolbar">
        <h3 class="panel-title">宏观支撑矩阵台账</h3>
        <el-tag :type="matrixLedgerRows.length ? 'success' : 'warning'">{{ matrixLedgerRows.length ? '基础数据已具备' : '等待矩阵配置' }}</el-tag>
      </div>
      <div class="form-grid">
        <el-select v-model="selectedMajorId" placeholder="选择专业" style="width: 260px" @change="reloadMajorSupportData">
          <el-option v-for="major in majors" :key="major.id" :label="major.majorName" :value="major.id" />
        </el-select>
        <el-button :disabled="!matrixLedgerRows.length" @click="exportMatrixLedgerExcel">导出 Excel</el-button>
        <el-button :disabled="!matrixLedgerRows.length" @click="exportMatrixLedgerPdf">导出 PDF</el-button>
      </div>
      <el-table :data="matrixLedgerRows.slice(0, 6)" border empty-text="当前专业还没有矩阵台账数据">
        <el-table-column prop="courseCode" label="课程代码" width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="140" />
        <el-table-column prop="indicatorCode" label="指标点" width="110" />
        <el-table-column prop="requirement" label="毕业要求" min-width="160" />
        <el-table-column prop="totalWeight" label="权重" width="100" />
      </el-table>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { pageGraduationRequirements, pageIndicators } from '@/api/indicator'
import { listMajors } from '@/api/major'
import { checkMatrixConfig, getMatrixConfig } from '@/api/matrix'
import {
  downloadCourseAchievementTemplate,
  exportCourseAchievementReportExcel,
  exportCourseAchievementReportPdf,
  exportMajorPenetrationAccountExcel,
  getCourseAchievementReportData,
  getMajorPenetrationAccount,
  getMajorReportRadarData
} from '@/api/report'
import { listSchoolYears } from '@/api/schoolyear'
import { pageTeachingClasses } from '@/api/teaching-class'
import { useUserStore } from '@/stores/user'
import type {
  CourseAchievementReportVO,
  GraduationRequirementVO,
  IndicatorPointVO,
  MajorAchievementRadarVO,
  MatrixConfigVO,
  MatrixWeightCheckVO,
  PenetrationAccountVO,
  SysDictMajorSimpleVO,
  SysDictSchoolYearVO,
  TeachingClassVO
} from '@/api/backend'

type IndicatorSupportRow = {
  code: string
  requirement: string
  courseCount: number
  weightSum: string
  ready: boolean
}

type ReportCatalogRow = {
  name: string
  targetRole: string
  statusText: string
  tagType: 'success' | 'warning' | 'info'
  tip: string
}

type MatrixLedgerRow = {
  majorName: string
  courseCode: string
  courseName: string
  indicatorCode: string
  indicatorName: string
  requirement: string
  totalWeight: string
}

type StatusState = {
  title: string
  description: string
  type: 'success' | 'warning' | 'info' | 'error'
}

const user = useUserStore()

const canUseCourseReport = computed(() => user.role === 'teacher')
const canUseMajorReport = computed(() => user.role === 'leader' || user.role === 'edu')
const canUseMajorPrep = computed(() => user.role === 'admin' || user.role === 'leader' || user.role === 'edu')
const canUseMatrixLedger = computed(() => user.role === 'admin' || user.role === 'leader' || user.role === 'edu')

const supportLoading = ref(false)
const courseActionLoading = ref(false)
const majorActionLoading = ref(false)

const majors = ref<SysDictMajorSimpleVO[]>([])
const schoolYears = ref<SysDictSchoolYearVO[]>([])
const courseClasses = ref<TeachingClassVO[]>([])

const selectedMajorId = ref<number>()
const selectedTermId = ref<number>()
const selectedGrade = ref('')
const selectedClassId = ref<number>()

const requirements = ref<GraduationRequirementVO[]>([])
const indicators = ref<IndicatorPointVO[]>([])
const matrixConfig = ref<MatrixConfigVO>()
const matrixCheck = ref<MatrixWeightCheckVO>()

const courseReportData = ref<CourseAchievementReportVO>()
const majorRadarData = ref<MajorAchievementRadarVO>()
const majorPenetrationData = ref<PenetrationAccountVO>()

const courseStatus = ref<StatusState>()
const majorStatus = ref<StatusState>()

const selectedMajor = computed(() => majors.value.find((item) => item.id === selectedMajorId.value))
const canSubmitMajorRequest = computed(() => Boolean(selectedMajorId.value && selectedTermId.value && selectedGrade.value))

const roleSummary = computed<StatusState>(() => {
  if (canUseCourseReport.value) {
    return {
      title: '当前角色可直接联调课程报表接口',
      description: '课程报表模板下载接口可直接使用，报表数据和导出接口已开放，但后端服务层目前大概率仍会返回“待完整实现”。',
      type: 'success'
    }
  }

  if (canUseMajorReport.value) {
    return {
      title: '当前角色可直接联调专业报表接口',
      description: '专业雷达图、穿透式台账、台账 Excel 导出接口都已开放，但服务层仍处于未完整实现状态，页面会保留真实报错提示。',
      type: 'warning'
    }
  }

  return {
    title: '当前角色以报表准备信息为主',
    description: '管理员当前更适合查看矩阵台账准备情况和基础数据就绪度，报表业务接口本身并不直接开放给管理员角色。',
    type: 'info'
  }
})

const reportCatalog = computed<ReportCatalogRow[]>(() => [
  {
    name: '课程目标达成情况评价表',
    targetRole: '课程教师',
    statusText: canUseCourseReport.value ? '可调用' : '当前角色不可调',
    tagType: canUseCourseReport.value ? 'success' : 'info',
    tip: canUseCourseReport.value
      ? '模板下载可直接使用，数据/导出接口已开放，服务层待完整实现。'
      : '仅课程教师可直接联调这组接口。'
  },
  {
    name: '专业毕业要求达成度报告',
    targetRole: '专业负责人、教务',
    statusText: canUseMajorReport.value ? '可调用' : '当前角色不可调',
    tagType: canUseMajorReport.value ? 'warning' : 'info',
    tip: canUseMajorReport.value
      ? '雷达图、穿透式台账、Excel 导出入口都已开放，但后端服务层当前仍可能返回未完成提示。'
      : '当前角色可先看准备状态，不建议直接调用该接口。'
  },
  {
    name: '宏观支撑矩阵台账',
    targetRole: '管理员、专业负责人、教务',
    statusText: canUseMatrixLedger.value ? '前端可导出' : '当前角色不可调',
    tagType: canUseMatrixLedger.value ? 'success' : 'info',
    tip: '该能力使用当前矩阵配置数据做前端导出，不依赖额外后端报表服务。'
  },
  {
    name: '学生考核点原始成绩明细',
    targetRole: '课程教师、教务',
    statusText: '后端缺失',
    tagType: 'warning',
    tip: '当前仓库还没有这组明细查询/导出接口，页面只保留状态说明。'
  }
])

const indicatorRows = computed<IndicatorSupportRow[]>(() => {
  const config = matrixConfig.value
  const matrixData = config?.matrixData ?? []
  const columnSums = matrixCheck.value?.columnSums ?? {}

  return indicators.value.map((indicator) => {
    const linkedItems = matrixData.filter((item) => item.indicatorId === indicator.id)
    const rawSum = columnSums[indicator.id]
    const numericSum = rawSum === undefined || rawSum === null || rawSum === '' ? undefined : Number(rawSum)
    const ready = linkedItems.length > 0 && numericSum !== undefined && Math.abs(numericSum - 1) <= 0.001

    return {
      code: indicator.indicatorCode,
      requirement: indicator.requirementCode || indicator.requirementName || '-',
      courseCount: linkedItems.length,
      weightSum: numericSum === undefined || Number.isNaN(numericSum) ? '-' : numericSum.toFixed(3),
      ready
    }
  })
})

const matrixLedgerRows = computed<MatrixLedgerRow[]>(() => {
  const config = matrixConfig.value
  if (!config) {
    return []
  }

  const courseMap = new Map(
    (config.courses ?? []).map((course) => [
      course.id,
      {
        courseCode: course.courseCode,
        courseName: course.courseName
      }
    ])
  )

  const indicatorMap = new Map(
    (config.indicators ?? []).map((indicator) => [
      indicator.id,
      {
        indicatorCode: indicator.indicatorCode,
        indicatorName: indicator.indicatorName,
        requirementId: indicator.requirementId
      }
    ])
  )

  const requirementMap = new Map(
    requirements.value.map((item) => [
      item.id,
      item.requirementCode ? `${item.requirementCode} ${item.requirementName}` : item.requirementName
    ])
  )

  return (config.matrixData ?? []).map((item) => {
    const course = courseMap.get(item.courseId)
    const indicator = indicatorMap.get(item.indicatorId)

    return {
      majorName: config.majorName || selectedMajor.value?.majorName || '-',
      courseCode: course?.courseCode || '-',
      courseName: course?.courseName || '-',
      indicatorCode: indicator?.indicatorCode || '-',
      indicatorName: indicator?.indicatorName || '-',
      requirement: indicator?.requirementId ? requirementMap.get(indicator.requirementId) || '-' : '-',
      totalWeight: Number(item.totalWeight ?? 0).toFixed(3)
    }
  })
})

const buildClassLabel = (item: TeachingClassVO) => {
  const course = item.courseName || item.courseCode || '未命名课程'
  const className = item.className || `教学班 ${item.id}`
  return `${course} / ${className}`
}

const normalizeBackendMessage = (message: string, scene: 'course' | 'major') => {
  if (message.includes('待完整实现')) {
    return scene === 'course'
      ? '后端已开放课程报表接口，但当前服务层仍返回“待完整实现”，这说明联调入口已接通，正式报表结果还要继续补后端。'
      : '后端已开放专业报表接口，但当前服务层仍返回“待完整实现”，这说明页面已经接上真实接口，后续重点是补后端服务能力。'
  }

  if (message.includes('无权') || message.includes('403')) {
    return scene === 'course'
      ? '当前账号没有课程报表调用权限，请确认是否使用课程教师账号登录。'
      : '当前账号没有专业报表调用权限，请确认是否使用教务或专业负责人账号登录。'
  }

  return message
}

const setCourseStatus = (title: string, description: string, type: StatusState['type']) => {
  courseStatus.value = { title, description, type }
}

const setMajorStatus = (title: string, description: string, type: StatusState['type']) => {
  majorStatus.value = { title, description, type }
}

const ensureCourseClassSelected = () => {
  if (!selectedClassId.value) {
    ElMessage.warning('请先选择教学班')
    return false
  }
  return true
}

const ensureMajorRequestReady = () => {
  if (!selectedMajorId.value) {
    ElMessage.warning('请先选择专业')
    return false
  }
  if (!selectedTermId.value) {
    ElMessage.warning('请先选择学期')
    return false
  }
  if (!selectedGrade.value) {
    ElMessage.warning('请先输入年级')
    return false
  }
  return true
}

const buildMajorRequest = () => ({
  majorId: Number(selectedMajorId.value),
  termId: Number(selectedTermId.value),
  grade: selectedGrade.value
})

const reloadMajorSupportData = async () => {
  if (!selectedMajorId.value || !canUseMajorPrep.value) {
    return
  }

  supportLoading.value = true
  try {
    const [requirementPage, indicatorPage, matrix] = await Promise.all([
      pageGraduationRequirements({ current: 1, pageSize: 200, majorId: selectedMajorId.value }),
      pageIndicators({ current: 1, pageSize: 200 }),
      getMatrixConfig(selectedMajorId.value)
    ])

    requirements.value = requirementPage.records
    indicators.value = indicatorPage.records
    matrixConfig.value = matrix

    const matrixItems = (matrix.matrixData ?? []).map((item) => ({
      courseId: item.courseId,
      indicatorId: item.indicatorId,
      totalWeight: Number(item.totalWeight ?? 0)
    }))

    matrixCheck.value = matrixItems.length
      ? await checkMatrixConfig(selectedMajorId.value, matrixItems)
      : {
          valid: false,
          message: '当前专业还没有矩阵配置',
          columnSums: {}
        }
  } catch (error) {
    const message = error instanceof Error ? error.message : '报表准备数据加载失败'
    ElMessage.error(message)
  } finally {
    supportLoading.value = false
  }
}

const downloadBlob = (blob: Blob, fileName: string) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.click()
  window.URL.revokeObjectURL(url)
}

const handleDownloadCourseTemplate = async () => {
  courseActionLoading.value = true
  try {
    const blob = await downloadCourseAchievementTemplate()
    downloadBlob(blob, '课程目标达成情况评价报表模板.xlsx')
    setCourseStatus('模板下载成功', '课程报表模板接口可正常访问，说明教师报表入口已经接通。', 'success')
  } catch (error) {
    const message = error instanceof Error ? error.message : '课程报表模板下载失败'
    const normalized = normalizeBackendMessage(message, 'course')
    setCourseStatus('模板下载失败', normalized, 'error')
    ElMessage.error(normalized)
  } finally {
    courseActionLoading.value = false
  }
}

const handleLoadCourseReport = async () => {
  if (!ensureCourseClassSelected()) {
    return
  }

  courseActionLoading.value = true
  courseReportData.value = undefined
  try {
    const data = await getCourseAchievementReportData({
      classId: Number(selectedClassId.value),
      exportFormat: 'EXCEL',
      includeStudentDetails: true,
      includeIndicatorAchievement: true
    })

    courseReportData.value = data
    setCourseStatus('课程报表数据查询成功', '后端已返回课程报表结果，可以继续补前端展示细节。', 'success')
  } catch (error) {
    const message = error instanceof Error ? error.message : '课程报表数据查询失败'
    const normalized = normalizeBackendMessage(message, 'course')
    setCourseStatus('课程报表数据查询失败', normalized, 'warning')
    ElMessage.warning(normalized)
  } finally {
    courseActionLoading.value = false
  }
}

const handleExportCourseReport = async (format: 'EXCEL' | 'PDF') => {
  if (!ensureCourseClassSelected()) {
    return
  }

  courseActionLoading.value = true
  try {
    const payload = {
      classId: Number(selectedClassId.value),
      exportFormat: format,
      includeStudentDetails: true,
      includeIndicatorAchievement: true
    }

    const blob =
      format === 'EXCEL'
        ? await exportCourseAchievementReportExcel(payload)
        : await exportCourseAchievementReportPdf(payload)

    const suffix = format === 'EXCEL' ? 'xlsx' : 'pdf'
    const selectedClass = courseClasses.value.find((item) => item.id === selectedClassId.value)
    const fileName = `${selectedClass?.className || '课程报表'}.${suffix}`
    downloadBlob(blob, fileName)
    setCourseStatus(`${format} 导出成功`, '当前导出接口已返回文件流，可以继续完善文件命名与导出入口。', 'success')
  } catch (error) {
    const message = error instanceof Error ? error.message : `${format} 导出失败`
    const normalized = normalizeBackendMessage(message, 'course')
    setCourseStatus(`${format} 导出失败`, normalized, 'warning')
    ElMessage.warning(normalized)
  } finally {
    courseActionLoading.value = false
  }
}

const handleLoadMajorRadar = async () => {
  if (!ensureMajorRequestReady()) {
    return
  }

  majorActionLoading.value = true
  majorRadarData.value = undefined
  try {
    const data = await getMajorReportRadarData(buildMajorRequest())
    majorRadarData.value = data
    setMajorStatus('雷达图接口查询成功', '后端已返回雷达图数据，可以继续补图表渲染。', 'success')
  } catch (error) {
    const message = error instanceof Error ? error.message : '雷达图接口查询失败'
    const normalized = normalizeBackendMessage(message, 'major')
    setMajorStatus('雷达图接口查询失败', normalized, 'warning')
    ElMessage.warning(normalized)
  } finally {
    majorActionLoading.value = false
  }
}

const handleLoadPenetrationAccount = async () => {
  if (!ensureMajorRequestReady()) {
    return
  }

  majorActionLoading.value = true
  majorPenetrationData.value = undefined
  try {
    const data = await getMajorPenetrationAccount(buildMajorRequest())
    majorPenetrationData.value = data
    setMajorStatus('穿透式台账查询成功', '后端已返回台账数据，可以继续补详情展示。', 'success')
  } catch (error) {
    const message = error instanceof Error ? error.message : '穿透式台账查询失败'
    const normalized = normalizeBackendMessage(message, 'major')
    setMajorStatus('穿透式台账查询失败', normalized, 'warning')
    ElMessage.warning(normalized)
  } finally {
    majorActionLoading.value = false
  }
}

const handleExportMajorAccount = async () => {
  if (!ensureMajorRequestReady()) {
    return
  }

  majorActionLoading.value = true
  try {
    const blob = await exportMajorPenetrationAccountExcel(buildMajorRequest())
    const fileName = `${selectedMajor.value?.majorName || '专业'}_${selectedGrade.value}_穿透式台账.xlsx`
    downloadBlob(blob, fileName)
    setMajorStatus('专业台账导出成功', '后端已返回 Excel 文件流，可以继续补导出入口和文件命名优化。', 'success')
  } catch (error) {
    const message = error instanceof Error ? error.message : '专业台账导出失败'
    const normalized = normalizeBackendMessage(message, 'major')
    setMajorStatus('专业台账导出失败', normalized, 'warning')
    ElMessage.warning(normalized)
  } finally {
    majorActionLoading.value = false
  }
}

const exportMatrixLedgerExcel = () => {
  if (!matrixLedgerRows.value.length) {
    ElMessage.warning('当前专业还没有矩阵台账数据，暂时无法导出')
    return
  }

  const headers = ['专业', '课程代码', '课程名称', '指标点编号', '指标点名称', '所属毕业要求', '权重']
  const rows = matrixLedgerRows.value.map((item) => [
    item.majorName,
    item.courseCode,
    item.courseName,
    item.indicatorCode,
    item.indicatorName,
    item.requirement,
    item.totalWeight
  ])

  const csvContent = [headers, ...rows]
    .map((row) => row.map((cell) => `"${String(cell ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\r\n')

  const fileName = `${selectedMajor.value?.majorName || '专业'}-宏观支撑矩阵台账.csv`
  downloadBlob(new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' }), fileName)
  ElMessage.success('矩阵台账 Excel 已开始下载')
}

const exportMatrixLedgerPdf = () => {
  if (!matrixLedgerRows.value.length) {
    ElMessage.warning('当前专业还没有矩阵台账数据，暂时无法导出')
    return
  }

  const tableRows = matrixLedgerRows.value
    .map(
      (item) => `
        <tr>
          <td>${item.majorName}</td>
          <td>${item.courseCode}</td>
          <td>${item.courseName}</td>
          <td>${item.indicatorCode}</td>
          <td>${item.indicatorName}</td>
          <td>${item.requirement}</td>
          <td>${item.totalWeight}</td>
        </tr>
      `
    )
    .join('')

  const printWindow = window.open('', '_blank', 'width=1200,height=800')
  if (!printWindow) {
    ElMessage.warning('浏览器拦截了打印窗口，请允许弹窗后重试')
    return
  }

  const title = `${selectedMajor.value?.majorName || '专业'} - 宏观支撑矩阵台账`
  printWindow.document.write(`
    <!doctype html>
    <html lang="zh-CN">
      <head>
        <meta charset="UTF-8" />
        <title>${title}</title>
        <style>
          body {
            font-family: "Microsoft YaHei", sans-serif;
            margin: 24px;
            color: #1f2937;
          }
          h1 {
            font-size: 20px;
            margin-bottom: 8px;
          }
          p {
            margin: 0 0 16px;
            color: #4b5563;
          }
          table {
            width: 100%;
            border-collapse: collapse;
            font-size: 12px;
          }
          th, td {
            border: 1px solid #d1d5db;
            padding: 8px;
            text-align: left;
            vertical-align: top;
          }
          th {
            background: #f3f4f6;
          }
        </style>
      </head>
      <body>
        <h1>${title}</h1>
        <p>共 ${matrixLedgerRows.value.length} 条矩阵台账记录，可在打印窗口中直接另存为 PDF。</p>
        <table>
          <thead>
            <tr>
              <th>专业</th>
              <th>课程代码</th>
              <th>课程名称</th>
              <th>指标点编号</th>
              <th>指标点名称</th>
              <th>所属毕业要求</th>
              <th>权重</th>
            </tr>
          </thead>
          <tbody>${tableRows}</tbody>
        </table>
      </body>
    </html>
  `)
  printWindow.document.close()
  printWindow.focus()
  printWindow.print()
}

onMounted(async () => {
  try {
    const loaders: Array<Promise<unknown>> = []

    if (canUseCourseReport.value) {
      loaders.push(
        pageTeachingClasses({ current: 1, pageSize: 500 }).then((page) => {
          courseClasses.value = page.records
          selectedClassId.value = page.records[0]?.id
          if (!page.records.length) {
            setCourseStatus('当前暂无可用教学班', '请先在成绩管理页创建教学班并绑定课程、教师、学期，再继续联调课程报表。', 'info')
          }
        })
      )
    }

    if (canUseMajorPrep.value || canUseMajorReport.value) {
      loaders.push(
        Promise.all([listMajors(), listSchoolYears()]).then(([majorList, termList]) => {
          majors.value = majorList
          schoolYears.value = termList
          selectedMajorId.value = majorList[0]?.id
          selectedTermId.value = termList[0]?.id
          if (!majorList.length || !termList.length) {
            setMajorStatus('专业报表联调条件未就绪', '当前还缺少专业或学期基础数据，请先补齐基础数据后再联调专业报表。', 'info')
          }
        })
      )
    }

    await Promise.all(loaders)

    if (selectedMajorId.value && canUseMajorPrep.value) {
      await reloadMajorSupportData()
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '报表页初始化失败'
    ElMessage.error(message)
  }
})
</script>

<style scoped>
.top-grid {
  margin-bottom: 20px;
}

.summary-alert {
  margin-bottom: 20px;
}

.action-panel {
  margin-top: 20px;
}

.form-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.section-alert {
  margin-bottom: 16px;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 16px;
}

.result-card {
  border: 1px solid var(--line);
}
</style>
