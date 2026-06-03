<template>
  <div class="page">
    <h1 class="page-title">系统总览</h1>
    <p class="page-desc">
      首页只展示当前后端已经提供的数据能力，方便我们先把已具备的课程、指标点、教学班和矩阵配置全部接起来。
    </p>

    <section class="metric-grid">
      <div v-for="metric in metrics" :key="metric.label" class="metric-card">
        <div class="metric-icon" :class="metric.tone">
          <el-icon :size="24"><component :is="metric.icon" /></el-icon>
        </div>
        <div>
          <div class="metric-label">{{ metric.label }}</div>
          <div class="metric-value">{{ metric.value }}</div>
          <div class="metric-sub">{{ metric.sub }}</div>
        </div>
      </div>
    </section>

    <section class="page-grid">
      <div class="panel span-8">
        <div class="toolbar">
          <h3 class="panel-title">专业数据准备概览</h3>
          <el-select
            v-model="selectedMajorId"
            style="width: 260px"
            clearable
            placeholder="选择专业查看详情"
            @change="reloadMajorBoard"
          >
            <el-option v-for="major in majors" :key="major.id" :label="major.majorName" :value="major.id" />
          </el-select>
        </div>

        <el-table v-loading="majorLoading" :data="majorOverviewRows" border>
          <el-table-column prop="label" label="项目" min-width="150" />
          <el-table-column prop="value" label="当前值" min-width="140" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.statusType">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="hint" label="说明" min-width="240" />
        </el-table>

        <el-alert
          v-if="selectedMajorName"
          :title="`当前专业：${selectedMajorName}`"
          type="info"
          show-icon
          style="margin-top: 12px"
        />
        <el-alert
          v-if="matrixCheckMessage"
          :title="matrixCheckMessage"
          :type="matrixCheckValid ? 'success' : 'warning'"
          show-icon
          style="margin-top: 12px"
        />
      </div>

      <div class="panel span-4">
        <h3 class="panel-title">数据分布</h3>
        <div ref="pieEl" class="chart-box"></div>
        <div class="chart-legend">
          <span>已配置矩阵课程：{{ configuredCourseCount }}</span>
          <span>未配置矩阵课程：{{ unconfiguredCourseCount }}</span>
          <span>教学班覆盖课程：{{ classCoveredCourseCount }}</span>
        </div>
      </div>

      <div class="panel span-5">
        <h3 class="panel-title">最近数据变更</h3>
        <div v-if="recentRecords.length" class="record-list">
          <div v-for="record in recentRecords" :key="record.key" class="record-item">
            <b>{{ record.time }}</b>
            <span>{{ record.title }}</span>
            <span>{{ record.detail }}</span>
            <el-tag :type="record.tagType" effect="light">{{ record.tag }}</el-tag>
          </div>
        </div>
        <el-empty v-else description="暂时没有可展示的最近记录" />
      </div>

      <div class="panel span-7">
        <h3 class="panel-title">联调提示</h3>
        <div class="notice-list">
          <div v-for="notice in notices" :key="notice.title" class="notice-item">
            <div>
              <div class="notice-title">{{ notice.title }}</div>
              <div class="muted">{{ notice.desc }}</div>
            </div>
            <el-tag :type="notice.type" effect="light">{{ notice.tag }}</el-tag>
          </div>
        </div>
      </div>

      <div class="panel span-12">
        <h3 class="panel-title">
          快捷入口
          <el-button link type="primary" @click="openDoc">查看接口文档</el-button>
        </h3>
        <div class="quick-grid">
          <button v-for="entry in quickEntries" :key="entry.label" class="quick-button" @click="$router.push(entry.path)">
            <el-icon :size="24"><component :is="entry.icon" /></el-icon>
            <span>{{ entry.label }}</span>
            <small>{{ entry.desc }}</small>
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import {
  Aim,
  DataAnalysis,
  Document,
  Files,
  Grid,
  Memo,
  Notebook,
  Reading,
  School,
  User
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { listCourses, pageCourses } from '@/api/course'
import { pageGraduationRequirements, pageIndicators } from '@/api/indicator'
import { listMajors } from '@/api/major'
import { getMatrixConfig, checkMatrixConfig } from '@/api/matrix'
import { apiDocUrl } from '@/api/request'
import { pageTeachingClasses } from '@/api/teaching-class'
import type {
  CourseSimpleVO,
  CourseVO,
  GraduationRequirementVO,
  IndicatorPointVO,
  MatrixConfigVO,
  MatrixWeightCheckVO,
  SysDictMajorSimpleVO,
  TeachingClassVO
} from '@/api/backend'

type OverviewRow = {
  label: string
  value: string
  status: string
  statusType: 'success' | 'warning' | 'info'
  hint: string
}

type RecentRecord = {
  key: string
  time: string
  title: string
  detail: string
  tag: string
  tagType: 'primary' | 'success' | 'warning'
}

const user = useUserStore()

const pieEl = ref<HTMLDivElement>()
let pieChart: echarts.ECharts | undefined

const pageLoading = ref(false)
const majorLoading = ref(false)

const majors = ref<SysDictMajorSimpleVO[]>([])
const selectedMajorId = ref<number>()

const courseList = ref<CourseSimpleVO[]>([])
const allCourseRows = ref<CourseVO[]>([])
const allTeachingClasses = ref<TeachingClassVO[]>([])
const allIndicators = ref<IndicatorPointVO[]>([])
const allRequirements = ref<GraduationRequirementVO[]>([])

const majorCourseRows = ref<CourseVO[]>([])
const majorRequirements = ref<GraduationRequirementVO[]>([])
const majorIndicators = ref<IndicatorPointVO[]>([])
const majorTeachingClasses = ref<TeachingClassVO[]>([])
const majorMatrixConfig = ref<MatrixConfigVO>()
const majorMatrixCheck = ref<MatrixWeightCheckVO>()

const selectedMajorName = computed(
  () => majors.value.find((item) => item.id === selectedMajorId.value)?.majorName || ''
)

const matrixCheckValid = computed(() => majorMatrixCheck.value?.valid ?? false)
const matrixCheckMessage = computed(() => majorMatrixCheck.value?.message || '')

const configuredCourseIds = computed(
  () => new Set((majorMatrixConfig.value?.matrixData ?? []).map((item) => item.courseId).filter(Boolean))
)

const classCoveredCourseIds = computed(
  () => new Set(majorTeachingClasses.value.map((item) => item.courseId).filter((id): id is number => Number.isFinite(id)))
)

const configuredCourseCount = computed(() => configuredCourseIds.value.size)
const classCoveredCourseCount = computed(() => classCoveredCourseIds.value.size)
const unconfiguredCourseCount = computed(() =>
  Math.max(majorCourseRows.value.length - configuredCourseCount.value, 0)
)

const readyIndicatorCount = computed(() => {
  const matrixData = majorMatrixConfig.value?.matrixData ?? []
  const columnSums = majorMatrixCheck.value?.columnSums ?? {}

  return majorIndicators.value.filter((indicator) => {
    const linkedItems = matrixData.filter((item) => item.indicatorId === indicator.id)
    const rawSum = columnSums[indicator.id]
    const numericSum = rawSum === undefined || rawSum === null || rawSum === '' ? undefined : Number(rawSum)
    return linkedItems.length > 0 && numericSum !== undefined && Math.abs(numericSum - 1) <= 0.001
  }).length
})

const metrics = computed(() => [
  {
    label: '课程总数',
    value: String(courseList.value.length),
    sub: '来自课程列表接口',
    icon: Files,
    tone: ''
  },
  {
    label: '毕业要求',
    value: String(allRequirements.value.length),
    sub: `指标点 ${allIndicators.value.length} 条`,
    icon: Reading,
    tone: 'green'
  },
  {
    label: '教学班',
    value: String(allTeachingClasses.value.length),
    sub: `覆盖课程 ${new Set(allTeachingClasses.value.map((item) => item.courseId).filter(Boolean)).size} 门`,
    icon: School,
    tone: 'yellow'
  },
  {
    label: '专业数',
    value: String(majors.value.length),
    sub: selectedMajorName.value ? `当前查看 ${selectedMajorName.value}` : '可按专业切换查看',
    icon: Grid,
    tone: 'blue'
  },
  {
    label: '当前账号',
    value: user.roleName,
    sub: user.collegeName || user.name,
    icon: User,
    tone: 'lock'
  }
])

const majorOverviewRows = computed<OverviewRow[]>(() => {
  if (!selectedMajorId.value) {
    return [
      {
        label: '专业选择',
        value: '未选择',
        status: '待选择',
        statusType: 'info',
        hint: '先选择一个专业，再看该专业的课程、指标点和矩阵配置情况'
      }
    ]
  }

  return [
    {
      label: '课程数量',
      value: `${majorCourseRows.value.length} 门`,
      status: majorCourseRows.value.length ? '正常' : '缺少',
      statusType: majorCourseRows.value.length ? 'success' : 'warning',
      hint: majorCourseRows.value.length ? '该专业已存在课程数据' : '这个专业下还没有查到课程'
    },
    {
      label: '毕业要求 / 指标点',
      value: `${majorRequirements.value.length} / ${majorIndicators.value.length}`,
      status: majorIndicators.value.length ? '正常' : '缺少',
      statusType: majorIndicators.value.length ? 'success' : 'warning',
      hint: majorIndicators.value.length ? '毕业要求与指标点接口可正常返回' : '还没有查到该专业的指标点'
    },
    {
      label: '矩阵配置课程',
      value: `${configuredCourseCount.value} 门`,
      status: configuredCourseCount.value ? '已配置' : '未配置',
      statusType: configuredCourseCount.value ? 'success' : 'warning',
      hint: configuredCourseCount.value
        ? '这些课程已经在课程-指标点矩阵中配置关系'
        : '还没有课程进入矩阵配置'
    },
    {
      label: '教学班覆盖',
      value: `${majorTeachingClasses.value.length} 个班`,
      status: majorTeachingClasses.value.length ? '已开课' : '未开课',
      statusType: majorTeachingClasses.value.length ? 'success' : 'warning',
      hint: majorTeachingClasses.value.length ? '该专业已有教学班与学生规模数据' : '暂时没有查到教学班'
    },
    {
      label: '矩阵校验',
      value: matrixCheckValid.value ? '通过' : '未通过',
      status: matrixCheckValid.value ? '就绪' : '待完善',
      statusType: matrixCheckValid.value ? 'success' : 'warning',
      hint: matrixCheckMessage.value || '已按矩阵校验接口检查权重合计'
    },
    {
      label: '指标点就绪数',
      value: `${readyIndicatorCount.value} / ${majorIndicators.value.length}`,
      status: readyIndicatorCount.value === majorIndicators.value.length && majorIndicators.value.length ? '齐全' : '待补充',
      statusType:
        readyIndicatorCount.value === majorIndicators.value.length && majorIndicators.value.length ? 'success' : 'warning',
      hint: '只有权重列和约等于 1 的指标点，才算真正可用于后续联调'
    }
  ]
})

const recentRecords = computed<RecentRecord[]>(() => {
  const courseRecords = allCourseRows.value.slice(0, 4).map((course) => ({
    key: `course-${course.id}`,
    time: course.updateTime || course.createTime || '-',
    title: course.courseName,
    detail: `${course.courseCode} · ${course.majorName || '未绑定专业'}`,
    tag: '课程',
    tagType: 'primary' as const
  }))

  const classRecords = allTeachingClasses.value.slice(0, 4).map((item) => ({
    key: `class-${item.id}`,
    time: item.updateTime || item.createTime || '-',
    title: item.className,
    detail: `${item.courseName || '-'} · ${item.teacherName || '未分配教师'}`,
    tag: '教学班',
    tagType: 'success' as const
  }))

  return [...courseRecords, ...classRecords]
    .sort((a, b) => String(b.time).localeCompare(String(a.time)))
    .slice(0, 6)
})

const notices = computed(() => [
  {
    title: '首页已切换为真实数据看板',
    desc: '不再使用演示用达成度和假通知，全部来自当前已经接通的后端接口。',
    tag: '已完成',
    type: 'success' as const
  },
  {
    title: '专业级计算结果接口仍未提供',
    desc: '因此首页只展示“准备状态”，不伪造专业达成度结果和最终报表结果。',
    tag: '后端待补',
    type: 'warning' as const
  },
  {
    title: '前端联调优先级建议',
    desc: '继续优先接课程、矩阵、教学班、成绩导入这些后端已经存在的链路。',
    tag: '建议',
    type: 'info' as const
  }
])

const quickEntries = [
  { label: '基础数据', path: '/basic-data', icon: Document, desc: '课程、毕业要求、指标点' },
  { label: '支撑矩阵', path: '/matrix', icon: Grid, desc: '课程与指标点权重矩阵' },
  { label: '课程大纲', path: '/syllabus', icon: Notebook, desc: '课程目标与考核点' },
  { label: '成绩导入', path: '/score', icon: DataAnalysis, desc: '学生与教学班数据' },
  { label: '计算准备', path: '/calculation', icon: Aim, desc: '专业级计算前置检查' },
  { label: '报表准备', path: '/report', icon: Memo, desc: '报表导出能力现状' }
]

const openDoc = () => {
  window.open(apiDocUrl, '_blank')
}

const renderPieChart = () => {
  if (!pieEl.value) return

  if (!pieChart) {
    pieChart = echarts.init(pieEl.value)
  }

  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    color: ['#1776f2', '#f7c243', '#4ec66b'],
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '42%'],
        label: {
          formatter: '{b}\n{c}'
        },
        data: [
          { name: '已配置矩阵课程', value: configuredCourseCount.value },
          { name: '未配置矩阵课程', value: unconfiguredCourseCount.value },
          { name: '教学班覆盖课程', value: classCoveredCourseCount.value }
        ]
      }
    ]
  })
}

const resizeCharts = () => {
  pieChart?.resize()
}

const buildMatrixItems = () =>
  (majorMatrixConfig.value?.matrixData ?? []).map((item) => ({
    courseId: item.courseId,
    indicatorId: item.indicatorId,
    totalWeight: Number(item.totalWeight ?? 0)
  }))

const reloadMajorBoard = async () => {
  if (!selectedMajorId.value) {
    majorCourseRows.value = []
    majorRequirements.value = []
    majorIndicators.value = []
    majorTeachingClasses.value = []
    majorMatrixConfig.value = undefined
    majorMatrixCheck.value = undefined
    await nextTick()
    renderPieChart()
    return
  }

  majorLoading.value = true
  try {
    const [coursePage, requirementPage, indicatorPage, classPage, matrix] = await Promise.all([
      pageCourses({ current: 1, pageSize: 300, majorId: selectedMajorId.value }),
      pageGraduationRequirements({ current: 1, pageSize: 200, majorId: selectedMajorId.value }),
      pageIndicators({ current: 1, pageSize: 300 }),
      pageTeachingClasses({ current: 1, pageSize: 500 }),
      getMatrixConfig(selectedMajorId.value)
    ])

    majorCourseRows.value = coursePage.records
    majorRequirements.value = requirementPage.records
    majorIndicators.value = indicatorPage.records.filter((item) =>
      item.requirementId ? requirementPage.records.some((req) => req.id === item.requirementId) : false
    )
    majorTeachingClasses.value = classPage.records.filter(
      (item) => item.courseId && coursePage.records.some((course) => course.id === item.courseId)
    )
    majorMatrixConfig.value = matrix

    const matrixItems = buildMatrixItems()
    majorMatrixCheck.value = matrixItems.length
      ? await checkMatrixConfig(selectedMajorId.value, matrixItems)
      : {
          valid: false,
          message: '当前专业还没有矩阵配置数据',
          columnSums: {}
        }

    await nextTick()
    renderPieChart()
  } catch (error) {
    const message = error instanceof Error ? error.message : '首页专业概览加载失败'
    ElMessage.error(message)
  } finally {
    majorLoading.value = false
  }
}

const loadPageData = async () => {
  pageLoading.value = true
  try {
    const [majorResult, courseListResult, coursePageResult, classPageResult, requirementPageResult, indicatorPageResult] =
      await Promise.all([
        listMajors(),
        listCourses(),
        pageCourses({ current: 1, pageSize: 300 }),
        pageTeachingClasses({ current: 1, pageSize: 500 }),
        pageGraduationRequirements({ current: 1, pageSize: 300 }),
        pageIndicators({ current: 1, pageSize: 500 })
      ])

    majors.value = majorResult
    courseList.value = courseListResult
    allCourseRows.value = coursePageResult.records
    allTeachingClasses.value = classPageResult.records
    allRequirements.value = requirementPageResult.records
    allIndicators.value = indicatorPageResult.records

    if (!selectedMajorId.value && majors.value.length) {
      selectedMajorId.value = majors.value[0].id
    }

    await reloadMajorBoard()
  } catch (error) {
    const message = error instanceof Error ? error.message : '首页数据加载失败'
    ElMessage.error(message)
  } finally {
    pageLoading.value = false
  }
}

onMounted(async () => {
  await loadPageData()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  pieChart?.dispose()
})
</script>

<style scoped>
.chart-legend {
  display: grid;
  gap: 8px;
  margin-top: 14px;
  color: #52637a;
  font-size: 13px;
}

.notice-title {
  font-weight: 600;
  color: #20324d;
  margin-bottom: 4px;
}

.quick-button small {
  color: #6c7b90;
  font-size: 12px;
}
</style>
