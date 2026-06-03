<template>
  <div class="page">
    <h1 class="page-title">专业级计算看板</h1>
    <p class="page-desc">当前页基于真实课程、教学班、矩阵配置和矩阵校验结果，展示专业级计算前的准备状态。</p>

    <section class="page-grid">
      <div class="panel span-8">
        <div class="toolbar">
          <h3 class="panel-title">课程准备状态</h3>
          <el-select v-model="selectedMajorId" style="width: 260px" @change="reloadBoard">
            <el-option v-for="major in majors" :key="major.id" :label="major.name" :value="major.id" />
          </el-select>
        </div>
        <el-table v-loading="loading" :data="courseRows" border>
          <el-table-column prop="name" label="课程名称" min-width="180" />
          <el-table-column prop="teacher" label="任课教师" width="130" />
          <el-table-column prop="classCount" label="教学班" width="90" />
          <el-table-column prop="studentCount" label="学生数" width="90" />
          <el-table-column prop="status" label="准备状态" width="120">
            <template #default="{ row }"><StatusTag :status="row.status" /></template>
          </el-table-column>
          <el-table-column prop="actionHint" label="建议" min-width="160" />
        </el-table>
        <div class="run-bar">
          <el-button @click="reloadBoard">刷新状态</el-button>
          <el-button type="primary" :disabled="!canRun" @click="showRunTip">检查计算条件</el-button>
        </div>
        <el-alert
          v-if="matrixCheckMessage"
          :title="matrixCheckMessage"
          :type="matrixCheckValid ? 'success' : 'warning'"
          show-icon
        />
        <el-alert
          v-if="!canRun"
          title="当前只能确认数据准备情况。后端还没有专业级计算执行与结果写入接口，所以这里先不触发正式计算。"
          type="info"
          show-icon
          style="margin-top: 12px"
        />
      </div>

      <div class="panel span-4">
        <h3 class="panel-title">三级计算链路</h3>
        <el-steps direction="vertical" :active="3" finish-status="success">
          <el-step title="一级" description="学生考核点成绩 -> 学生课程目标达成度" />
          <el-step title="二级" description="课程目标达成度 + wjk -> 课程指标点 Ek" />
          <el-step title="三级" description="课程指标点 Ek + Wc -> 专业指标点 Gk" />
        </el-steps>
      </div>

      <div class="panel span-12">
        <h3 class="panel-title">专业指标点准备结果</h3>
        <el-table v-loading="loading" :data="indicatorRows" border>
          <el-table-column prop="code" label="毕业要求指标点" width="150" />
          <el-table-column prop="name" label="指标点名称" min-width="220" />
          <el-table-column prop="weightSum" label="矩阵列权重合计" width="160" />
          <el-table-column prop="courseCount" label="关联课程数" width="120" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.status === '已就绪' ? 'success' : 'warning'">{{ row.status }}</el-tag>
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
import StatusTag from '@/components/StatusTag/StatusTag.vue'
import { pageCourses } from '@/api/course'
import { checkMatrixConfig, getMatrixConfig } from '@/api/matrix'
import { pageTeachingClasses } from '@/api/teaching-class'
import type { CourseVO, MatrixConfigVO, MatrixWeightCheckVO, TeachingClassVO } from '@/api/backend'

type CourseRow = {
  id: number
  name: string
  teacher: string
  classCount: number
  studentCount: number
  status: string
  actionHint: string
}

type IndicatorRow = {
  code: string
  name: string
  weightSum: string
  courseCount: number
  status: string
}

const loading = ref(false)
const majors = ref<Array<{ id: number; name: string }>>([])
const selectedMajorId = ref<number>()
const courses = ref<CourseVO[]>([])
const teachingClasses = ref<TeachingClassVO[]>([])
const matrixConfig = ref<MatrixConfigVO>()
const matrixCheck = ref<MatrixWeightCheckVO>()

const matrixCheckValid = computed(() => matrixCheck.value?.valid ?? false)
const matrixCheckMessage = computed(() => matrixCheck.value?.message || '')

const buildMatrixItems = () =>
  (matrixConfig.value?.matrixData ?? []).map((item) => ({
    courseId: item.courseId,
    indicatorId: item.indicatorId,
    totalWeight: Number(item.totalWeight ?? 0)
  }))

const courseRows = computed<CourseRow[]>(() => {
  const matrixData = matrixConfig.value?.matrixData ?? []

  return courses.value.map((course) => {
    const classList = teachingClasses.value.filter((item) => item.courseId === course.id)
    const linkedItems = matrixData.filter((item) => item.courseId === course.id)
    const teacher = Array.from(new Set(classList.map((item) => item.teacherName).filter(Boolean))).join(' / ') || '-'
    const studentCount = classList.reduce((sum, item) => sum + Number(item.studentCount ?? 0), 0)

    if (!linkedItems.length) {
      return {
        id: course.id,
        name: course.courseName,
        teacher,
        classCount: classList.length,
        studentCount,
        status: '未配置',
        actionHint: '先到矩阵页配置课程-指标点关系'
      }
    }

    if (!classList.length) {
      return {
        id: course.id,
        name: course.courseName,
        teacher,
        classCount: 0,
        studentCount: 0,
        status: '未开课',
        actionHint: '当前课程还没有教学班数据'
      }
    }

    return {
      id: course.id,
      name: course.courseName,
      teacher,
      classCount: classList.length,
      studentCount,
      status: '已就绪',
      actionHint: '矩阵已关联，且已有教学班数据'
    }
  })
})

const indicatorRows = computed<IndicatorRow[]>(() => {
  const config = matrixConfig.value
  const matrixData = config?.matrixData ?? []
  const indicators = config?.indicators ?? []
  const columnSums = matrixCheck.value?.columnSums ?? {}

  return indicators.map((indicator) => {
    const linkedItems = matrixData.filter((item) => item.indicatorId === indicator.id)
    const rawSum = columnSums[indicator.id]
    const numericSum =
      rawSum === undefined || rawSum === null || rawSum === '' ? undefined : Number(rawSum)
    const weightSum = numericSum === undefined || Number.isNaN(numericSum) ? '-' : numericSum.toFixed(3)
    const isReady = linkedItems.length > 0 && numericSum !== undefined && Math.abs(numericSum - 1) <= 0.001

    return {
      code: indicator.indicatorCode,
      name: indicator.indicatorName,
      weightSum,
      courseCount: linkedItems.length,
      status: isReady ? '已就绪' : '待完善'
    }
  })
})

const canRun = computed(() => {
  if (!courseRows.value.length || !indicatorRows.value.length) {
    return false
  }
  return courseRows.value.every((course) => course.status === '已就绪') && indicatorRows.value.every((item) => item.status === '已就绪')
})

const showRunTip = () => {
  if (canRun.value) {
    ElMessage.success('当前数据准备条件已满足，但后端还没有专业级计算执行接口。')
    return
  }
  ElMessage.warning('当前还有课程或指标点未准备完成，请先根据看板提示补齐数据。')
}

const reloadBoard = async () => {
  if (!selectedMajorId.value) return

  loading.value = true
  try {
    const [coursePage, classPage, matrix] = await Promise.all([
      pageCourses({ current: 1, pageSize: 200, majorId: selectedMajorId.value }),
      pageTeachingClasses({ current: 1, pageSize: 500 }),
      getMatrixConfig(selectedMajorId.value)
    ])

    courses.value = coursePage.records
    teachingClasses.value = classPage.records.filter(
      (item) => item.courseId && coursePage.records.some((course) => course.id === item.courseId)
    )
    matrixConfig.value = matrix

    const matrixItems = buildMatrixItems()
    matrixCheck.value = matrixItems.length
      ? await checkMatrixConfig(selectedMajorId.value, matrixItems)
      : {
          valid: false,
          message: '当前专业还没有矩阵配置数据',
          columnSums: {}
        }
  } catch (error) {
    const message = error instanceof Error ? error.message : '专业级看板加载失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const coursePage = await pageCourses({ current: 1, pageSize: 200 })
    const majorMap = new Map<number, string>()

    for (const course of coursePage.records) {
      if (course.majorId && course.majorName) {
        majorMap.set(course.majorId, course.majorName)
      }
    }

    majors.value = Array.from(majorMap.entries()).map(([id, name]) => ({ id, name }))
    selectedMajorId.value = majors.value[0]?.id
    await reloadBoard()
  } catch (error) {
    const message = error instanceof Error ? error.message : '专业列表加载失败'
    ElMessage.error(message)
  }
})
</script>

<style scoped>
.run-bar {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin: 14px 0;
}
</style>
