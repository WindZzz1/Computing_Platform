<template>
  <div class="page">
    <h1 class="page-title">达成度计算中心</h1>
    <p class="page-desc">当前页接通课程级与专业级真实计算接口，支持按角色查看准备情况、触发计算和读取结果。</p>

    <section class="page-grid">
      <div class="panel span-12">
        <div class="toolbar">
          <div>
            <h3 class="panel-title">课程级达成度计算</h3>
            <p class="panel-desc">课程教师可按教学班触发一级、二级达成度计算，并查看当前班级是否已有结果。</p>
          </div>
          <el-tag :type="canRunCourseCalculation ? 'success' : canViewCourseCalculation ? 'warning' : 'info'">
            {{ canRunCourseCalculation ? '当前角色可操作' : canViewCourseCalculation ? '当前角色仅可查看状态' : '当前角色只可查看说明' }}
          </el-tag>
        </div>

        <el-alert
          v-if="!canRunCourseCalculation"
          :title="
            canViewCourseCalculation
              ? '当前角色可以查看课程级计算状态，但不能直接触发课程级计算。'
              : '课程级达成度计算接口只对课程教师开放。你当前可以查看页面说明，但不能直接触发计算。'
          "
          type="info"
          show-icon
          class="block-alert"
        />

        <div class="filters">
          <el-select v-model="selectedCourseClassId" placeholder="请选择教学班" style="width: 320px" filterable>
            <el-option
              v-for="item in teacherTeachingClasses"
              :key="item.id"
              :label="buildClassLabel(item)"
              :value="item.id"
            />
          </el-select>
          <el-switch v-model="courseForceRecalculate" active-text="强制重算" />
          <el-button :disabled="!selectedCourseClassId" @click="loadCourseCalculationStatus">刷新状态</el-button>
          <el-button
            type="primary"
            :loading="courseCalculating"
            :disabled="!selectedCourseClassId || !canRunCourseCalculation"
            @click="handleCourseCalculation"
          >
            开始计算
          </el-button>
          <el-button
            :loading="courseExportingExcel"
            :disabled="!canExportCourseReport"
            @click="handleExportCourseReport('EXCEL')"
          >
            导出 Excel
          </el-button>
          <el-button
            :loading="courseExportingPdf"
            :disabled="!canExportCourseReport"
            @click="handleExportCourseReport('PDF')"
          >
            导出 PDF
          </el-button>
        </div>

        <el-empty
          v-if="!teacherTeachingClasses.length"
          description="当前没有可用于课程级计算的教学班。请先确认自己名下已有教学班并且完成成绩录入。"
        />

        <template v-else>
          <div class="summary-cards">
            <el-card shadow="never" class="summary-card">
              <div class="summary-label">当前教学班</div>
              <div class="summary-value">{{ selectedCourseClassName || '未选择' }}</div>
            </el-card>
            <el-card shadow="never" class="summary-card">
              <div class="summary-label">一级记录数</div>
              <div class="summary-value">{{ courseCalculationStatus?.levelOneRecordCount ?? 0 }}</div>
            </el-card>
            <el-card shadow="never" class="summary-card">
              <div class="summary-label">二级记录数</div>
              <div class="summary-value">{{ courseCalculationStatus?.levelTwoRecordCount ?? 0 }}</div>
            </el-card>
            <el-card shadow="never" class="summary-card">
              <div class="summary-label">结果状态</div>
              <div class="summary-value">
                {{ courseCalculationStatus?.hasCalculationResult ? '已有结果' : '尚未计算' }}
              </div>
            </el-card>
          </div>

          <el-alert
            v-if="courseGuideMessage"
            :title="courseGuideMessage"
            :type="courseGuideType"
            show-icon
            class="block-alert"
          />

          <el-alert
            v-if="courseStatusMessage"
            :title="courseStatusMessage"
            :type="courseStatusType"
            show-icon
            class="block-alert"
          />

          <el-alert
            v-if="courseCalculationError"
            :title="courseCalculationError"
            type="warning"
            show-icon
            class="block-alert"
          />

          <div v-if="courseCalculationResult" class="result-grid">
            <el-card shadow="never" class="result-card">
              <template #header>一级达成度统计</template>
              <ul class="data-list">
                <li>学生数：{{ courseCalculationResult.levelOneStats?.totalStudents ?? 0 }}</li>
                <li>课程目标数：{{ courseCalculationResult.levelOneStats?.totalObjectives ?? 0 }}</li>
                <li>记录数：{{ courseCalculationResult.levelOneStats?.totalRecords ?? 0 }}</li>
                <li>平均达成度：{{ formatNumber(courseCalculationResult.levelOneStats?.averageAchievement) }}</li>
              </ul>
            </el-card>
            <el-card shadow="never" class="result-card">
              <template #header>二级达成度统计</template>
              <ul class="data-list">
                <li>指标点数：{{ courseCalculationResult.levelTwoStats?.totalIndicators ?? 0 }}</li>
                <li>记录数：{{ courseCalculationResult.levelTwoStats?.totalRecords ?? 0 }}</li>
                <li>平均达成度：{{ formatNumber(courseCalculationResult.levelTwoStats?.averageAchievement) }}</li>
                <li>计算完成：{{ formatTime(courseCalculationResult.calcEndTime) }}</li>
              </ul>
            </el-card>
            <el-card shadow="never" class="result-card wide-card">
              <template #header>一级达成度明细</template>
              <el-table :data="courseLevelOneDetails" border size="small" empty-text="当前还没有一级达成度明细">
                <el-table-column prop="studentNo" label="学号" width="120" />
                <el-table-column prop="name" label="姓名" width="110" />
                <el-table-column prop="objectiveCode" label="课程目标编号" width="130" />
                <el-table-column prop="objectiveName" label="课程目标名称" min-width="180" />
                <el-table-column label="一级达成度" width="120">
                  <template #default="{ row }">{{ formatNumber(row.achievement) }}</template>
                </el-table-column>
                <el-table-column prop="calculateTime" label="计算时间" min-width="180">
                  <template #default="{ row }">{{ formatTime(row.calculateTime) }}</template>
                </el-table-column>
              </el-table>
            </el-card>
            <el-card shadow="never" class="result-card wide-card">
              <template #header>二级达成度明细</template>
              <el-table :data="courseLevelTwoDetails" border size="small" empty-text="当前还没有二级达成度明细">
                <el-table-column prop="indicatorCode" label="指标点编号" width="130" />
                <el-table-column prop="indicatorName" label="指标点名称" min-width="180" />
                <el-table-column label="二级达成度" width="120">
                  <template #default="{ row }">{{ formatNumber(row.achievement) }}</template>
                </el-table-column>
                <el-table-column prop="calculateTime" label="计算时间" min-width="180">
                  <template #default="{ row }">{{ formatTime(row.calculateTime) }}</template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
        </template>
      </div>

      <div class="panel span-12">
        <div class="toolbar">
          <div>
            <h3 class="panel-title">专业级达成度计算</h3>
            <p class="panel-desc">教务管理员和专业负责人可基于课程级结果，读取专业看板、执行三级计算并查询结果。</p>
          </div>
          <el-tag :type="canRunMajorCalculation ? 'success' : canDeleteMajorCalculation ? 'warning' : 'info'">
            {{ canRunMajorCalculation ? '当前角色可操作' : canDeleteMajorCalculation ? '当前角色可清理结果' : '当前角色只可查看说明' }}
          </el-tag>
        </div>

        <el-alert
          v-if="!canRunMajorCalculation"
          :title="
            canDeleteMajorCalculation
              ? '当前角色可以按条件删除专业级结果，但不能读取专业看板或触发专业级计算。'
              : '专业级达成度计算接口只对教务管理员和专业负责人开放。当前角色不能直接触发。'
          "
          type="info"
          show-icon
          class="block-alert"
        />

        <div class="filters">
          <el-select v-model="selectedMajorId" placeholder="请选择专业" style="width: 240px" filterable @change="reloadMajorData">
            <el-option v-for="major in majors" :key="major.id" :label="major.name" :value="major.id" />
          </el-select>
          <el-select v-model="selectedTermId" placeholder="请选择学年学期" style="width: 220px" filterable @change="reloadMajorData">
            <el-option
              v-for="term in schoolYears"
              :key="term.id"
              :label="`${term.yearName} ${term.semesterName}`"
              :value="term.id"
            />
          </el-select>
          <el-input v-model="selectedGrade" placeholder="请输入年级，例如 2022" style="width: 180px" @change="reloadMajorData" />
          <el-switch v-model="majorForceRecalculate" active-text="强制重算" />
        </div>

        <div class="run-bar">
          <el-button :disabled="!canQueryMajorCalculation" @click="reloadMajorData">刷新看板</el-button>
          <el-button :disabled="!canQueryMajorCalculation" @click="loadMajorCalculationResult">查看结果</el-button>
          <el-button
            type="primary"
            :loading="majorCalculating"
            :disabled="!canMajorQueryData || !canRunMajorCalculation || !canRunMajorCalculationNow"
            @click="handleMajorCalculation"
          >
            执行专业级计算
          </el-button>
          <el-button
            type="danger"
            plain
            :disabled="!canQueryMajorCalculation || !canDeleteMajorCalculation || !hasMajorResult"
            @click="handleDeleteMajorResult"
          >
            删除结果
          </el-button>
        </div>

        <div class="summary-cards">
          <el-card shadow="never" class="summary-card">
            <div class="summary-label">专业看板课程数</div>
            <div class="summary-value">{{ majorDashboard?.totalCourses ?? 0 }}</div>
          </el-card>
          <el-card shadow="never" class="summary-card">
            <div class="summary-label">已有课程级数据</div>
            <div class="summary-value">{{ majorDashboard?.coursesWithData ?? 0 }}</div>
          </el-card>
          <el-card shadow="never" class="summary-card">
            <div class="summary-label">可执行专业级计算</div>
            <div class="summary-value">{{ majorDashboard?.canCalculate ? '可以' : '暂不可以' }}</div>
          </el-card>
          <el-card shadow="never" class="summary-card">
            <div class="summary-label">已有专业级结果</div>
            <div class="summary-value">{{ hasMajorResult ? '是' : '否' }}</div>
          </el-card>
        </div>

        <el-alert
          v-if="majorGuideMessage"
          :title="majorGuideMessage"
          :type="majorGuideType"
          show-icon
          class="block-alert"
        />

        <el-alert
          v-if="majorFilterSyncMessage"
          :title="majorFilterSyncMessage"
          type="info"
          show-icon
          class="block-alert"
        />

        <el-alert
          v-if="majorDashboard?.errorMessage"
          :title="majorDashboard.errorMessage"
          type="warning"
          show-icon
          class="block-alert"
        />

        <el-alert
          v-if="majorCalculationError"
          :title="majorCalculationError"
          type="warning"
          show-icon
          class="block-alert"
        />

        <el-table
          v-if="canMajorQueryData && majorDashboard?.courseStatusList?.length"
          v-loading="majorLoading"
          :data="majorDashboard.courseStatusList"
          border
        >
          <el-table-column prop="className" label="教学班" min-width="180" />
          <el-table-column prop="courseId" label="课程ID" width="100" />
          <el-table-column prop="achievementDataCount" label="课程级记录数" width="130" />
          <el-table-column label="课程级状态" width="140">
            <template #default="{ row }">
              <el-tag :type="row.hasAchievementData ? 'success' : 'warning'">
                {{ row.hasAchievementData ? '已具备' : '待先算课程级' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <el-empty
          v-else-if="canMajorQueryData && selectedMajorId"
          description="当前专业条件下暂时没有课程看板数据。可以先确认学年学期、年级和课程级计算结果。"
        />

        <div v-if="majorCalculationResult?.achievements?.length" class="result-grid">
          <el-card shadow="never" class="result-card">
            <template #header>专业级结果摘要</template>
            <ul class="data-list">
              <li>指标点数：{{ majorCalculationResult.totalIndicators ?? 0 }}</li>
              <li>平均达成度：{{ formatNumber(majorCalculationResult.averageAchievement) }}</li>
              <li>最低达成度：{{ formatNumber(majorCalculationResult.minAchievement) }}</li>
              <li>最高达成度：{{ formatNumber(majorCalculationResult.maxAchievement) }}</li>
              <li>阈值：{{ formatNumber(majorCalculationResult.threshold) }}</li>
              <li>毕业要求：{{ majorCalculationResult.meetsGraduationRequirement ? '满足' : '未全部满足' }}</li>
            </ul>
          </el-card>
          <el-card shadow="never" class="result-card wide-card">
            <template #header>指标点结果预览</template>
            <el-table :data="majorCalculationRows" border size="small" empty-text="当前筛选条件下还没有可展示的专业级结果">
              <el-table-column prop="requirementCode" label="毕业要求" width="120" />
              <el-table-column prop="indicatorCode" label="指标点编号" width="120" />
              <el-table-column prop="indicatorName" label="指标点名称" min-width="180" />
              <el-table-column label="达成度" width="110">
                <template #default="{ row }">{{ formatNumber(row.achievement) }}</template>
              </el-table-column>
              <el-table-column label="状态" width="110">
                <template #default="{ row }">
                  <el-tag :type="row.meetsThreshold ? 'success' : 'warning'">
                    {{ row.meetsThreshold ? '达标' : '待提升' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { pageCourses } from '@/api/course'
import {
  calculateCourseAchievement,
  getCourseAchievementCalculationDetail,
  calculateMajorAchievement,
  deleteMajorCalculationResult,
  getCourseAchievementCalculationStatus,
  getMajorCalculationDashboard,
  getMajorCalculationResult
} from '@/api/calculation'
import { exportCourseAchievementReportExcel, exportCourseAchievementReportPdf } from '@/api/report'
import { listMajors } from '@/api/major'
import { listSchoolYears } from '@/api/schoolyear'
import { pageTeachingClasses } from '@/api/teaching-class'
import type {
  AchievementCalculationDetailVO,
  AchievementCalculationResultVO,
  AchievementCalculationStatusVO,
  MajorCalculationDashboardVO,
  MajorCalculationResultVO,
  SysDictSchoolYearVO,
  TeachingClassVO
} from '@/api/backend'

type MajorOption = {
  id: number
  name: string
}

const userStore = useUserStore()
const route = useRoute()

const loading = ref(false)
const majorLoading = ref(false)
const courseCalculating = ref(false)
const majorCalculating = ref(false)
const courseExportingExcel = ref(false)
const courseExportingPdf = ref(false)

const majors = ref<MajorOption[]>([])
const schoolYears = ref<SysDictSchoolYearVO[]>([])
const teachingClasses = ref<TeachingClassVO[]>([])

const selectedCourseClassId = ref<number>()
const selectedMajorId = ref<number>()
const selectedTermId = ref<number>()
const selectedGrade = ref('')
const courseForceRecalculate = ref(false)
const majorForceRecalculate = ref(false)

const courseCalculationStatus = ref<AchievementCalculationStatusVO>()
const courseCalculationResult = ref<AchievementCalculationResultVO>()
const courseCalculationDetail = ref<AchievementCalculationDetailVO>()
const courseCalculationError = ref('')
const lastLoadedMajorKey = ref('')

const majorDashboard = ref<MajorCalculationDashboardVO>()
const majorCalculationResult = ref<MajorCalculationResultVO>()
const majorCalculationError = ref('')

const canViewCourseCalculation = computed(() => userStore.role === 'teacher' || userStore.role === 'admin')
const canRunCourseCalculation = computed(() => userStore.role === 'teacher')
const canMajorQueryData = computed(() => userStore.role === 'edu' || userStore.role === 'leader')
const canRunMajorCalculation = computed(() => userStore.role === 'edu' || userStore.role === 'leader')
const canDeleteMajorCalculation = computed(() => userStore.role === 'admin')

const teacherTeachingClasses = computed(() => {
  if (userStore.role === 'teacher') {
    return teachingClasses.value.filter((item) => item.teacherName === userStore.name)
  }
  return teachingClasses.value
})

const selectedCourseClass = computed(() =>
  teacherTeachingClasses.value.find((item) => item.id === selectedCourseClassId.value)
)

const selectedCourseClassName = computed(() => (selectedCourseClass.value ? buildClassLabel(selectedCourseClass.value) : ''))
const courseLevelOneDetails = computed(() => courseCalculationDetail.value?.levelOneDetails ?? [])
const courseLevelTwoDetails = computed(() => courseCalculationDetail.value?.levelTwoDetails ?? [])
const canExportCourseReport = computed(
  () =>
    canRunCourseCalculation.value &&
    Boolean(selectedCourseClassId.value) &&
    Boolean(courseCalculationStatus.value?.hasCalculationResult)
)

const courseStatusMessage = computed(() => {
  if (!courseCalculationStatus.value) return ''
  return courseCalculationStatus.value.hasCalculationResult
    ? '当前教学班已经存在课程级达成度结果，可以继续重算或推进专业级计算。'
    : '当前教学班还没有课程级达成度结果，建议先执行一次课程级计算。'
})

const courseStatusType = computed<'success' | 'warning'>(() =>
  courseCalculationStatus.value?.hasCalculationResult ? 'success' : 'warning'
)

const courseGuideMessage = computed(() => {
  if (!teacherTeachingClasses.value.length) {
    return ''
  }
  if (!selectedCourseClassId.value) {
    return '先选择一个教学班，再查看当前班级是否已有一级、二级达成度结果。'
  }
  if (courseCalculationError.value) {
    return ''
  }
  if (!canRunCourseCalculation.value && courseCalculationStatus.value?.hasCalculationResult) {
    return '当前角色可以查看这个班级的课程级结果。如果需要导出报表，请使用课程教师账号登录。'
  }
  if (!courseCalculationStatus.value?.hasCalculationResult) {
    return '建议顺序：先确认学生、成绩和大纲配置已经准备完整，再执行课程级计算，得到一级和二级达成度记录。'
  }
  return canExportCourseReport.value
    ? '当前班级课程级结果已具备，可以直接导出课程报表；如果后续要做专业级计算，也可以切到右侧继续查看专业看板。'
    : '当前班级课程级结果已具备。如果后续要做专业级计算，可以切到右侧继续查看专业看板。'
})

const courseGuideType = computed<'info' | 'success'>(() =>
  courseCalculationStatus.value?.hasCalculationResult ? 'success' : 'info'
)

const canQueryMajorCalculation = computed(() => Boolean(selectedMajorId.value && selectedTermId.value && selectedGrade.value.trim()))
const canRunMajorCalculationNow = computed(() => majorDashboard.value?.canCalculate ?? false)
const hasMajorResult = computed(() => (majorCalculationResult.value?.totalRecords ?? 0) > 0)
const currentMajorKey = computed(() => {
  if (!selectedMajorId.value || !selectedTermId.value || !selectedGrade.value.trim()) {
    return ''
  }
  return [selectedMajorId.value, selectedTermId.value, selectedGrade.value.trim()].join('_')
})
const majorFilterSyncMessage = computed(() => {
  if (!canQueryMajorCalculation.value || !lastLoadedMajorKey.value || lastLoadedMajorKey.value === currentMajorKey.value) {
    return ''
  }
  return '你已经切换了专业级筛选条件，页面正在按新的专业、学年学期和年级重新联动结果。'
})
const majorCalculationRows = computed(() => majorCalculationResult.value?.achievements ?? [])
const routeClassId = computed(() => {
  const raw = route.query.classId
  const value = Array.isArray(raw) ? raw[0] : raw
  const numeric = Number(value)
  return Number.isFinite(numeric) && numeric > 0 ? numeric : undefined
})
const routeMajorId = computed(() => {
  const raw = route.query.majorId
  const value = Array.isArray(raw) ? raw[0] : raw
  const numeric = Number(value)
  return Number.isFinite(numeric) && numeric > 0 ? numeric : undefined
})
const routeTermId = computed(() => {
  const raw = route.query.termId
  const value = Array.isArray(raw) ? raw[0] : raw
  const numeric = Number(value)
  return Number.isFinite(numeric) && numeric > 0 ? numeric : undefined
})
const routeGrade = computed(() => {
  const raw = route.query.grade
  const value = Array.isArray(raw) ? raw[0] : raw
  return typeof value === 'string' ? value.trim() : ''
})

const majorGuideMessage = computed(() => {
  if (!canMajorQueryData.value) {
    return ''
  }
  if (!canQueryMajorCalculation.value) {
    return '先补齐专业、学年学期和年级这 3 个条件，页面才会读取专业级看板和结果。'
  }
  if (majorCalculationError.value) {
    return ''
  }
  if (majorFilterSyncMessage.value) {
    return '筛选条件已变化，系统会按新的专业范围刷新看板与结果，避免继续显示旧结果。'
  }
  if (majorDashboard.value?.canCalculate) {
    return hasMajorResult.value
      ? '当前条件下已经有专业级计算结果。你可以重新查看结果，也可以在确认需要时强制重算。'
      : '当前条件下已经具备专业级计算前置条件，可以直接执行三级计算。'
  }
  return '当前还有课程缺少课程级达成度结果，先在左侧完成课程级计算；如果仍失败，请优先检查成绩、课程目标和指标点支撑关系是否完整。'
})

const majorGuideType = computed<'info' | 'success' | 'warning'>(() => {
  if (majorDashboard.value?.canCalculate) {
    return hasMajorResult.value ? 'success' : 'info'
  }
  return 'warning'
})

function buildClassLabel(item: TeachingClassVO) {
  const course = item.courseName || item.courseCode || '未命名课程'
  const className = item.className || `教学班 ${item.id}`
  return `${course} / ${className}`
}

function formatNumber(value?: number | string | null) {
  if (value === undefined || value === null || value === '') return '-'
  const num = Number(value)
  return Number.isNaN(num) ? String(value) : num.toFixed(4)
}

function formatTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

function downloadBlob(blob: Blob, fileName: string) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.click()
  window.URL.revokeObjectURL(url)
}

function getFriendlyErrorMessage(error: unknown, fallback: string) {
  const rawMessage = error instanceof Error ? error.message : fallback

  if (rawMessage.includes('timeout')) {
    return `${fallback}：请求超时，建议确认后端服务和数据库连接状态后再试。`
  }
  if (rawMessage.includes('403') || rawMessage.includes('无权') || rawMessage.includes('权限')) {
    return `${fallback}：当前账号没有操作权限，请切换到对应角色后重试。`
  }
  if (rawMessage.includes('教学班级ID不能为空') || rawMessage.includes('classId')) {
    return `${fallback}：当前教学班选择无效，请重新选择后再试。`
  }
  if (rawMessage.includes('专业') && rawMessage.includes('学期')) {
    return `${fallback}：请确认专业、学年学期和年级条件已经全部选择完整。`
  }
  return rawMessage || fallback
}

function buildMajorRequest() {
  return {
    majorId: selectedMajorId.value!,
    termId: selectedTermId.value!,
    grade: selectedGrade.value.trim(),
    forceRecalculate: majorForceRecalculate.value
  }
}

async function loadCourseCalculationStatus() {
  if (!selectedCourseClassId.value) return

  if (!canViewCourseCalculation.value) {
    courseCalculationStatus.value = undefined
    courseCalculationDetail.value = undefined
    courseCalculationError.value = '当前角色没有课程级计算状态查看权限'
    return
  }

  try {
    courseCalculationStatus.value = await getCourseAchievementCalculationStatus(selectedCourseClassId.value)
    if (courseCalculationStatus.value.hasCalculationResult) {
      courseCalculationDetail.value = await getCourseAchievementCalculationDetail(selectedCourseClassId.value)
      courseCalculationResult.value = courseCalculationResult.value ?? {
        classId: selectedCourseClassId.value
      }
    } else {
      courseCalculationResult.value = undefined
      courseCalculationDetail.value = undefined
    }
    courseCalculationError.value = ''
  } catch (error) {
    courseCalculationResult.value = undefined
    courseCalculationDetail.value = undefined
    courseCalculationError.value = getFriendlyErrorMessage(error, '课程级计算状态获取失败')
  }
}

async function handleCourseCalculation() {
  if (!selectedCourseClassId.value) return

  courseCalculating.value = true
  courseCalculationError.value = ''
  try {
    const result = await calculateCourseAchievement({
      classId: selectedCourseClassId.value,
      forceRecalculate: courseForceRecalculate.value
    })
    courseCalculationResult.value = result

    if (result.success) {
      ElMessage.success('课程级达成度计算完成')
      await loadCourseCalculationStatus()
      await reloadMajorData()
    } else {
      courseCalculationDetail.value = undefined
      courseCalculationError.value = result.errorMessage
        ? `课程级计算失败：${result.errorMessage}`
        : '课程级计算失败：请先检查成绩、课程目标和指标点支撑关系是否已经准备完整。'
      ElMessage.warning(courseCalculationError.value)
    }
  } catch (error) {
    courseCalculationDetail.value = undefined
    courseCalculationError.value = getFriendlyErrorMessage(
      error,
      '课程级计算失败'
    )
    ElMessage.error(courseCalculationError.value)
  } finally {
    courseCalculating.value = false
  }
}

async function handleExportCourseReport(format: 'EXCEL' | 'PDF') {
  if (!selectedCourseClassId.value || !canExportCourseReport.value) return

  const loadingRef = format === 'EXCEL' ? courseExportingExcel : courseExportingPdf
  loadingRef.value = true
  try {
    const blob =
      format === 'EXCEL'
        ? await exportCourseAchievementReportExcel({
            classId: selectedCourseClassId.value,
            exportFormat: 'EXCEL'
          })
        : await exportCourseAchievementReportPdf({
            classId: selectedCourseClassId.value,
            exportFormat: 'PDF'
          })
    const safeClassName = (selectedCourseClassName.value || `教学班_${selectedCourseClassId.value}`).replace(/[\\/:*?"<>|]/g, '_')
    const fileName = `${safeClassName}_课程达成度报表.${format === 'EXCEL' ? 'xlsx' : 'pdf'}`
    downloadBlob(blob, fileName)
    ElMessage.success(`课程级${format}报表已开始下载`)
  } catch (error) {
    const message = getFriendlyErrorMessage(
      error,
      `课程级${format}报表导出失败`
    )
    ElMessage.error(message)
  } finally {
    loadingRef.value = false
  }
}

async function loadMajorDashboard() {
  if (!canQueryMajorCalculation.value || !canMajorQueryData.value) return

  majorLoading.value = true
  try {
    majorDashboard.value = await getMajorCalculationDashboard({
      majorId: selectedMajorId.value!,
      termId: selectedTermId.value!,
      grade: selectedGrade.value.trim(),
      current: 1,
      pageSize: 100
    })
    majorCalculationError.value = ''
  } catch (error) {
    majorCalculationError.value = getFriendlyErrorMessage(error, '专业级计算看板加载失败')
  } finally {
    majorLoading.value = false
  }
}

async function loadMajorCalculationResult() {
  if (!canQueryMajorCalculation.value || !canMajorQueryData.value) return

  majorLoading.value = true
  try {
    majorCalculationResult.value = await getMajorCalculationResult(buildMajorRequest())
    majorCalculationError.value = ''
    lastLoadedMajorKey.value = currentMajorKey.value
    if ((majorCalculationResult.value.totalRecords ?? 0) === 0) {
      ElMessage.info('当前条件下还没有专业级计算结果')
    }
  } catch (error) {
    majorCalculationError.value = getFriendlyErrorMessage(error, '专业级结果查询失败')
  } finally {
    majorLoading.value = false
  }
}

async function handleMajorCalculation() {
  if (!canQueryMajorCalculation.value || !canRunMajorCalculation.value) return

  majorCalculating.value = true
  majorCalculationError.value = ''
  try {
    const result = await calculateMajorAchievement(buildMajorRequest())
    majorCalculationResult.value = result

    if (result.success) {
      ElMessage.success('专业级达成度计算完成')
      await loadMajorDashboard()
      await loadMajorCalculationResult()
    } else {
      majorCalculationError.value = result.errorMessage
        ? `专业级计算失败：${result.errorMessage}`
        : '专业级计算失败：请先确认当前专业下课程级结果已经准备完整。'
      ElMessage.warning(majorCalculationError.value)
    }
  } catch (error) {
    majorCalculationError.value = getFriendlyErrorMessage(error, '专业级计算失败')
    ElMessage.error(majorCalculationError.value)
  } finally {
    majorCalculating.value = false
  }
}

async function handleDeleteMajorResult() {
  if (!canQueryMajorCalculation.value || !canDeleteMajorCalculation.value) return

  try {
    await ElMessageBox.confirm('确认删除当前专业、学年学期和年级条件下的专业级计算结果吗？', '删除确认', {
      type: 'warning'
    })
    await deleteMajorCalculationResult(buildMajorRequest())
    ElMessage.success('专业级计算结果已删除')
    majorCalculationResult.value = undefined
    await loadMajorDashboard()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    const message = error instanceof Error ? error.message : '删除专业级结果失败'
    ElMessage.error(message)
  }
}

async function reloadMajorData() {
  if (!canQueryMajorCalculation.value) {
    majorDashboard.value = undefined
    majorCalculationResult.value = undefined
    lastLoadedMajorKey.value = ''
    return
  }
  if (!canMajorQueryData.value) {
    majorDashboard.value = undefined
    majorCalculationResult.value = undefined
    lastLoadedMajorKey.value = ''
    return
  }
  majorCalculationError.value = ''
  majorDashboard.value = undefined
  majorCalculationResult.value = undefined
  await loadMajorDashboard()
  await loadMajorCalculationResult()
}

async function loadBaseOptions() {
  loading.value = true
  try {
    const [majorList, schoolYearList, classPage, coursePage] = await Promise.all([
      listMajors(),
      listSchoolYears(),
      pageTeachingClasses({ current: 1, pageSize: 500 }),
      pageCourses({ current: 1, pageSize: 500 })
    ])

    const majorMap = new Map<number, string>()
    majorList.forEach((item) => majorMap.set(item.id, item.majorName))
    coursePage.records.forEach((course) => {
      if (course.majorId && course.majorName) {
        majorMap.set(course.majorId, course.majorName)
      }
    })

    majors.value = Array.from(majorMap.entries()).map(([id, name]) => ({ id, name }))
    schoolYears.value = schoolYearList
    teachingClasses.value = classPage.records

    const routeMatchedClass = routeClassId.value
      ? teacherTeachingClasses.value.find((item) => item.id === routeClassId.value)
      : undefined
    const routeMatchedMajor = routeMajorId.value
      ? majors.value.find((item) => item.id === routeMajorId.value)
      : undefined
    const routeMatchedTerm = routeTermId.value
      ? schoolYears.value.find((item) => item.id === routeTermId.value)
      : undefined

    selectedCourseClassId.value = routeMatchedClass?.id ?? teacherTeachingClasses.value[0]?.id
    selectedMajorId.value = routeMatchedMajor?.id ?? selectedMajorId.value ?? majors.value[0]?.id
    selectedTermId.value = routeMatchedTerm?.id ?? selectedTermId.value ?? schoolYears.value[0]?.id

    if (routeGrade.value) {
      selectedGrade.value = routeGrade.value
    } else if (!selectedGrade.value) {
      const currentYear = new Date().getFullYear()
      selectedGrade.value = String(currentYear - 4)
    }

    if (selectedCourseClassId.value && canViewCourseCalculation.value) {
      await loadCourseCalculationStatus()
    }
    await reloadMajorData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '计算中心初始化失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void loadBaseOptions()
})

watch(selectedCourseClassId, () => {
  courseCalculationStatus.value = undefined
  courseCalculationResult.value = undefined
  courseCalculationDetail.value = undefined
  courseCalculationError.value = ''

  if (selectedCourseClassId.value && canViewCourseCalculation.value) {
    void loadCourseCalculationStatus()
  }
})
</script>

<style scoped>
.panel-desc {
  margin: 6px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin: 14px 0;
}

.run-bar {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
  margin-bottom: 14px;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.summary-card {
  border-radius: 14px;
}

.summary-label {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.summary-value {
  margin-top: 8px;
  font-size: 20px;
  font-weight: 600;
}

.block-alert {
  margin-bottom: 14px;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px;
  margin-top: 14px;
}

.result-card {
  border-radius: 14px;
}

.wide-card {
  grid-column: span 2;
}

.data-list {
  margin: 0;
  padding-left: 18px;
  line-height: 1.9;
}

@media (max-width: 900px) {
  .wide-card {
    grid-column: span 1;
  }
}
</style>
