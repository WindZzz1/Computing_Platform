<template>
  <div class="page">
    <h1 class="page-title">报表与导出</h1>
    <p class="page-desc">当前页展示真实报表准备情况。导出入口保留，但只有在后端已有相应导出接口时才适合继续接通。</p>

    <section class="page-grid">
      <div class="panel span-5">
        <div class="toolbar">
          <h3 class="panel-title">指标点支撑情况</h3>
          <el-select v-model="selectedMajorId" style="width: 260px" @change="reload">
            <el-option v-for="major in majors" :key="major.id" :label="major.majorName" :value="major.id" />
          </el-select>
        </div>
        <el-table v-loading="loading" :data="indicatorRows" border>
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

      <div class="panel span-7">
        <h3 class="panel-title">可导出报表</h3>
        <el-table :data="reports" border>
          <el-table-column prop="name" label="报表名称" min-width="220" />
          <el-table-column prop="role" label="使用角色" width="210" />
          <el-table-column prop="basis" label="当前依据" min-width="240" />
          <el-table-column prop="backend" label="后端状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.backendReady ? 'success' : 'warning'">{{ row.backendReady ? '可继续接' : '后端缺失' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button type="primary" link @click="showExportTip(row, 'Excel')">导出 Excel</el-button>
              <el-button type="primary" link @click="showExportTip(row, 'PDF')">导出 PDF</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { pageGraduationRequirements, pageIndicators } from '@/api/indicator'
import { listMajors } from '@/api/major'
import { checkMatrixConfig, getMatrixConfig } from '@/api/matrix'
import type {
  GraduationRequirementVO,
  IndicatorPointVO,
  MatrixConfigVO,
  MatrixWeightCheckVO,
  SysDictMajorSimpleVO
} from '@/api/backend'

type IndicatorSupportRow = {
  code: string
  requirement: string
  courseCount: number
  weightSum: string
  ready: boolean
}

type ReportRow = {
  name: string
  role: string
  basis: string
  backendReady: boolean
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

const loading = ref(false)
const majors = ref<SysDictMajorSimpleVO[]>([])
const selectedMajorId = ref<number>()
const requirements = ref<GraduationRequirementVO[]>([])
const indicators = ref<IndicatorPointVO[]>([])
const matrixConfig = ref<MatrixConfigVO>()
const matrixCheck = ref<MatrixWeightCheckVO>()

const selectedMajor = computed(() => majors.value.find((item) => item.id === selectedMajorId.value))

const reports = computed<ReportRow[]>(() => [
  {
    name: '课程目标达成情况评价表',
    role: '课程教师',
    basis: '课程目标、考核点已接通；结果表接口仍缺失',
    backendReady: false,
    tip: '后端还没有课程目标结果导出接口，目前只能保留入口。'
  },
  {
    name: '专业毕业要求达成度报告',
    role: '专业负责人、教务管理员',
    basis: `毕业要求 ${requirements.value.length} 条，指标点 ${indicators.value.length} 条，矩阵配置已接入`,
    backendReady: false,
    tip: '后端还没有专业级结果表和导出接口，目前先展示准备状态。'
  },
  {
    name: '宏观支撑矩阵台账',
    role: '专业负责人',
    basis: matrixConfig.value?.matrixData?.length ? `已有 ${matrixConfig.value.matrixData.length} 条矩阵配置` : '暂无矩阵配置',
    backendReady: true,
    tip: '这类报表可以继续做前端导出，现有数据已经够用。'
  },
  {
    name: '学生考核点原始成绩明细',
    role: '课程教师、教务管理员',
    basis: '学生导入和教学班绑定已接通，但成绩结果查询接口仍缺失',
    backendReady: false,
    tip: '后端还没有成绩明细查询/导出接口。'
  }
])

const indicatorRows = computed<IndicatorSupportRow[]>(() => {
  const config = matrixConfig.value
  const matrixData = config?.matrixData ?? []
  const columnSums = matrixCheck.value?.columnSums ?? {}

  return indicators.value
    .filter((indicator) => !selectedMajorId.value || !indicator.requirementId || requirements.value.some((req) => req.id === indicator.requirementId))
    .map((indicator) => {
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

const reload = async () => {
  if (!selectedMajorId.value) return

  loading.value = true
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
    const message = error instanceof Error ? error.message : '报表页数据加载失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
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

const showExportTip = (row: ReportRow, type: string) => {
  if (row.backendReady && row.name === '宏观支撑矩阵台账') {
    if (type === 'Excel') {
      exportMatrixLedgerExcel()
      return
    }
    if (type === 'PDF') {
      exportMatrixLedgerPdf()
      return
    }
  }

  if (row.backendReady) {
    ElMessage.success(`${row.name} 的 ${type} 导出前端入口可以继续做，当前基础数据已具备。`)
    return
  }
  ElMessage.info(`${row.name} 暂时不能接 ${type} 导出。原因：${row.tip}`)
}

onMounted(async () => {
  try {
    majors.value = await listMajors()
    selectedMajorId.value = majors.value[0]?.id
    await reload()
  } catch (error) {
    const message = error instanceof Error ? error.message : '专业列表加载失败'
    ElMessage.error(message)
  }
})
</script>
