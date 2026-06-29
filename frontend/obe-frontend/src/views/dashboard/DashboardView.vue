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
          <div class="toolbar-actions">
            <el-select
              v-model="selectedMajorId"
              style="width: 220px"
              clearable
              placeholder="选择专业查看详情"
              @change="reloadMajorBoard"
            >
              <el-option v-for="major in majors" :key="major.id" :label="major.majorName" :value="major.id" />
            </el-select>
            <el-select
              v-if="schoolYears.length"
              v-model="selectedTermId"
              style="width: 220px"
              clearable
              placeholder="选择学年学期"
              @change="reloadMajorBoard"
            >
              <el-option
                v-for="term in schoolYears"
                :key="term.id"
                :label="`${term.yearName} ${term.semesterName}`"
                :value="term.id"
              />
            </el-select>
            <el-input-number
              v-else-if="canUseManualMajorTermInput"
              v-model="selectedTermId"
              :min="1"
              :step="1"
              controls-position="right"
              style="width: 220px"
              placeholder="输入 termId"
              @change="reloadMajorBoard"
            />
            <el-input
              v-model="selectedGrade"
              style="width: 150px"
              clearable
              placeholder="年级，如 2022"
              @change="reloadMajorBoard"
            />
          </div>
        </div>

        <el-table v-loading="majorLoading" :data="majorOverviewRows" border>
          <el-table-column prop="label" label="项目" min-width="150" />
          <el-table-column prop="value" label="当前值" min-width="140" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.statusType">{{ row.statusLabel || row.status }}</el-tag>
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
        <el-alert
          v-if="majorCalculationSummaryMessage"
          :title="majorCalculationSummaryMessage"
          :type="majorCalculationSummaryType"
          show-icon
          style="margin-top: 12px"
        />
        <div
          v-if="selectedMajorId && (readyMajorCourseStatusCount || pendingMajorCourseStatusList.length)"
          class="major-course-status-board"
        >
          <div class="major-course-status-head">
            <div>
              <div class="major-course-status-title">专业级计算前置明细</div>
              <div class="muted">首页直接显示当前专业还差哪些课程级结果，方便继续完善数据。</div>
            </div>
            <el-tag :type="pendingMajorCourseStatusList.length ? 'warning' : 'success'" effect="light">
              {{ pendingMajorCourseStatusList.length ? `待补 ${pendingMajorCourseStatusList.length}` : '已全部齐备' }}
            </el-tag>
          </div>

          <div class="major-course-status-metrics">
            <div class="major-course-status-metric">
              <strong>{{ readyMajorCourseStatusCount }}</strong>
              <span>已具备课程级结果</span>
            </div>
            <div class="major-course-status-metric warning">
              <strong>{{ pendingMajorCourseStatusList.length }}</strong>
              <span>仍待补齐</span>
            </div>
          </div>

          <div v-if="pendingMajorCourseStatusList.length" class="pending-class-list major-pending-list">
            <div class="pending-class-title">待补课程级结果的课程 / 教学班</div>
            <div
              v-for="item in pendingMajorCourseStatusPreview"
              :key="`${item.classId ?? 'class'}-${item.courseId ?? 'course'}`"
              class="pending-class-item"
            >
              <span>{{ item.className || `班级 ${item.classId ?? '-'}` }}</span>
              <small>{{ formatPendingMajorCourseMeta(item) }}</small>
            </div>
            <div v-if="pendingMajorCourseStatusList.length > pendingMajorCourseStatusPreview.length" class="major-status-more">
              还有 {{ pendingMajorCourseStatusList.length - pendingMajorCourseStatusPreview.length }} 门课程待补，可前往计算中心继续排查。
            </div>
          </div>
          <el-alert
            v-else
            title="当前专业相关课程都已有课程级结果，可以直接继续专业级计算或复核结果。"
            type="success"
            :closable="false"
            show-icon
            style="margin-top: 14px"
          />
        </div>
        <div class="action-strip">
          <el-button type="primary" @click="goToCalculationWithHint(primaryCalculationAction)">
            {{ primaryCalculationAction.buttonText }}
          </el-button>
          <el-button v-if="canAccessScore" plain @click="navigateToScore">去成绩页补课程级前置数据</el-button>
          <el-button v-if="canAccessMatrix" plain @click="navigateToMatrix">去矩阵页检查支撑关系</el-button>
        </div>
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

      <div class="panel span-4">
        <div class="toolbar">
          <div>
            <h3 class="panel-title">课程级结果概览卡片</h3>
            <span class="muted">单独看当前全局教学班的课程级计算覆盖情况。</span>
          </div>
          <el-tag :type="courseCalculationCardType">{{ courseCalculationCardTag }}</el-tag>
        </div>

        <div class="course-card-grid">
          <div class="course-card-metric">
            <div class="course-card-label">已完成教学班</div>
            <div class="course-card-value">{{ classesWithCalculationCount }}</div>
            <div class="muted">已经形成课程级结果</div>
          </div>
          <div class="course-card-metric">
            <div class="course-card-label">待补教学班</div>
            <div class="course-card-value">{{ classesWithoutCalculationCount }}</div>
            <div class="muted">仍需补课程级结果</div>
          </div>
          <div class="course-card-metric">
            <div class="course-card-label">覆盖率</div>
            <div class="course-card-value">{{ courseCalculationCoverageText }}</div>
            <div class="muted">{{ courseCalculationCardHint }}</div>
          </div>
        </div>

        <el-alert
          :title="courseCalculationCardMessage"
          :type="courseCalculationCardType"
          show-icon
          style="margin-top: 14px"
        />

        <div v-if="pendingCourseCalculationClasses.length" class="pending-class-list">
          <div class="pending-class-title">优先处理的教学班</div>
          <div
            v-for="item in pendingCourseCalculationClasses.slice(0, 3)"
            :key="item.id"
            class="pending-class-item"
          >
            <span>{{ item.className }}</span>
            <small>{{ item.courseName || '未绑定课程' }}</small>
          </div>
        </div>

        <el-button style="margin-top: 14px" type="primary" plain @click="goToCalculationWithHint(courseCalculationAction)">
          {{ courseCalculationAction.buttonText }}
        </el-button>
      </div>

      <div class="panel span-4">
        <div class="toolbar">
          <div>
            <h3 class="panel-title">专业级结果概览卡片</h3>
            <span class="muted">围绕当前筛选条件，单独看专业级结果是否已经生成。</span>
          </div>
          <el-tag :type="majorResultCardType">{{ majorResultCardTag }}</el-tag>
        </div>

        <div class="course-card-grid">
          <div class="course-card-metric">
            <div class="course-card-label">当前结果状态</div>
            <div class="course-card-value">{{ majorResultStatusText }}</div>
            <div class="muted">按当前专业、学期、年级读取</div>
          </div>
          <div class="course-card-metric">
            <div class="course-card-label">平均达成度</div>
            <div class="course-card-value">{{ majorAverageAchievementText }}</div>
            <div class="muted">当前专业级结果摘要</div>
          </div>
          <div class="course-card-metric">
            <div class="course-card-label">达标状态</div>
            <div class="course-card-value">{{ majorThresholdStatusText }}</div>
            <div class="muted">{{ majorThresholdHint }}</div>
          </div>
        </div>

        <el-alert
          :title="majorResultCardMessage"
          :type="majorResultCardType"
          show-icon
          style="margin-top: 14px"
        />

        <div v-if="majorResultIndicatorPreview.length" class="pending-class-list">
          <div class="pending-class-title">当前结果中的指标点预览</div>
          <div
            v-for="item in majorResultIndicatorPreview"
            :key="item.indicatorCode || item.indicatorId"
            class="pending-class-item"
          >
            <span>{{ item.indicatorCode || item.indicatorName || '未命名指标点' }}</span>
            <small>{{ formatNumber(item.achievement) }}</small>
          </div>
        </div>

        <el-button style="margin-top: 14px" type="primary" plain @click="goToCalculationWithHint(majorCalculationAction)">
          {{ majorCalculationAction.buttonText }}
        </el-button>
      </div>

      <div class="panel span-4">
        <h3 class="panel-title">最近数据变更</h3>
        <p class="muted" style="margin-bottom: 12px">{{ recentCalculationFocus }}</p>
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

      <div class="panel span-4">
        <div class="toolbar">
          <div>
            <h3 class="panel-title">系统提示</h3>
            <span class="muted">根据当前首页状态，把最值得优先处理的问题拆得更细一点。</span>
          </div>
          <el-tag :type="noticeSummaryType">{{ noticeSummaryTag }}</el-tag>
        </div>
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
        <div class="toolbar">
          <div>
            <h3 class="panel-title">计算结果状态摘要</h3>
            <span class="muted">根据首页当前选择的专业、学年学期和年级，展示最值得优先处理的下一步。</span>
          </div>
          <el-tag :type="calculationStatusTagType">{{ calculationStatusTagText }}</el-tag>
        </div>

        <el-empty
          v-if="!selectedMajorId"
          description="先选择专业，再结合学年学期和年级查看当前首页可读取到的真实计算结果。"
        >
          <template #image>
            <div class="empty-guide-box">
              <div class="empty-guide-title">还没有专业上下文</div>
              <div class="muted">首页暂时无法判断课程级和专业级结果，要先明确查看哪个专业。</div>
              <div class="empty-guide-actions">
                <el-button type="primary" @click="scrollToTop">回到顶部先选专业</el-button>
              </div>
            </div>
          </template>
        </el-empty>

        <div v-else-if="summaryGuideState === 'need-filters'" class="empty-guide-box">
          <div class="empty-guide-title">还缺专业级筛选条件</div>
          <div class="muted">当前专业已经选定，但还缺学年学期或年级，所以首页还无法判断专业级结果。</div>
          <div class="empty-guide-actions">
            <el-button type="primary" @click="scrollToTop">回到顶部补齐筛选条件</el-button>
            <el-button plain @click="navigateToCalculation">直接去计算中心查看</el-button>
          </div>
        </div>

        <div v-else-if="summaryGuideState === 'need-course-results'" class="empty-guide-box">
          <div class="empty-guide-title">还没有补齐课程级结果</div>
          <div class="muted">课程级结果还没全部形成，专业级计算暂时还推不下去，建议先补课程级这一层。</div>
          <div class="empty-guide-actions">
            <el-button type="primary" @click="navigateToCalculation">去计算中心补课程级结果</el-button>
            <el-button v-if="canAccessScore" plain @click="navigateToScore">去成绩页检查前置数据</el-button>
          </div>
        </div>

        <div v-else-if="summaryGuideState === 'need-major-result'" class="empty-guide-box">
          <div class="empty-guide-title">可以生成专业级结果了</div>
          <div class="muted">课程级结果和前置条件已经具备，但当前还没有最终专业级结果，下一步最适合直接执行专业级计算。</div>
          <div class="empty-guide-actions">
            <el-button type="primary" @click="navigateToCalculation">去计算中心执行专业级计算</el-button>
          </div>
        </div>

        <div v-else class="status-summary">
          <div class="status-summary-item">
            <div class="status-summary-label">课程级计算概况</div>
            <div class="status-summary-value">
              {{ classesWithCalculationCount }} / {{ allTeachingClasses.length || 0 }} 个教学班已有结果
            </div>
            <div class="muted">{{ courseCalculationHint }}</div>
          </div>

          <div class="status-summary-item">
            <div class="status-summary-label">专业级计算概况</div>
            <div class="status-summary-value">
              {{ currentMajorHasCalculationResult ? '当前条件下已生成结果' : '当前条件下暂未生成结果' }}
            </div>
            <div class="muted">{{ majorCalculationHint }}</div>
          </div>

          <div class="status-summary-item">
            <div class="status-summary-label">推荐下一步</div>
            <div class="status-summary-value">{{ nextActionTitle }}</div>
            <div class="muted">{{ nextActionDesc }}</div>
          </div>
        </div>
      </div>

      <div class="panel span-12">
        <h3 class="panel-title">
          快捷入口
          <el-tag style="margin-left: 8px" type="info" effect="light">{{ quickEntrySummaryTag }}</el-tag>
          <el-button link type="primary" @click="openDoc">查看接口文档</el-button>
        </h3>
        <p class="muted" style="margin-bottom: 12px">{{ quickEntrySummaryDesc }}</p>
        <div class="quick-grid">
          <button v-for="entry in quickEntries" :key="entry.label" class="quick-button" @click="openQuickEntry(entry)">
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
import { useRoute, useRouter } from 'vue-router'
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
import { getCourseAchievementCalculationStatus, getMajorCalculationDashboard, getMajorCalculationResult } from '@/api/calculation'
import { listCourses, pageCourses } from '@/api/course'
import { pageGraduationRequirements, pageIndicators } from '@/api/indicator'
import { listMajors } from '@/api/major'
import { getMatrixConfig, checkMatrixConfig } from '@/api/matrix'
import { apiDocUrl } from '@/api/request'
import { listSchoolYears } from '@/api/schoolyear'
import { pageTeachingClasses } from '@/api/teaching-class'
import { canAccessFeature, type FeatureKey } from '@/utils/roleAccess'
import type {
  AchievementCalculationStatusVO,
  CourseSimpleVO,
  CourseVO,
  GraduationRequirementVO,
  IndicatorPointVO,
  MajorCalculationAchievementItem,
  MajorCalculationDashboardVO,
  MajorCalculationResultVO,
  MatrixConfigVO,
  MatrixWeightCheckVO,
  SysDictMajorSimpleVO,
  SysDictSchoolYearVO,
  TeachingClassVO
} from '@/api/backend'

type OverviewRow = {
  label: string
  value: string
  status: string
  statusLabel?: string
  statusType: 'success' | 'warning' | 'info'
  hint: string
}

type RecentRecord = {
  key: string
  time: string
  sortTime: string
  title: string
  detail: string
  tag: string
  tagType: 'primary' | 'success' | 'warning'
  priority: number
}

type NoticeItem = {
  title: string
  desc: string
  tag: string
  type: 'success' | 'warning' | 'info'
  level: 'base' | 'course' | 'major' | 'matrix'
}

type CalculationEntryAction = {
  buttonText: string
  message: string
}

type QuickEntry = {
  label: string
  path: string
  icon: typeof Document
  desc: string
  priority: number
  feature?: FeatureKey
  recommended?: boolean
}

const user = useUserStore()
const route = useRoute()
const router = useRouter()

const canAccessScore = computed(() => canAccessFeature(user.role, 'score'))
const canAccessMatrix = computed(() => canAccessFeature(user.role, 'matrix'))
const canLoadCourseCatalog = computed(() => user.role === 'admin' || user.role === 'edu' || user.role === 'leader')
const canLoadRequirementCatalog = computed(() => user.role === 'admin' || user.role === 'leader')
const canLoadTeachingClassCatalog = computed(() => user.role === 'admin' || user.role === 'edu' || user.role === 'leader')
const canLoadSchoolYearCatalog = computed(() => user.role === 'admin' || user.role === 'edu' || user.role === 'leader')
const canLoadMatrixData = computed(() => user.role === 'admin' || user.role === 'edu' || user.role === 'leader')
const canLoadMajorCalculation = computed(() => user.role === 'admin' || user.role === 'edu' || user.role === 'leader')
const canLoadGlobalCourseStatus = computed(() => user.role === 'admin' || user.role === 'teacher' || user.role === 'leader')
const canUseManualMajorTermInput = computed(() => canLoadMajorCalculation.value && !canLoadSchoolYearCatalog.value)

const pieEl = ref<HTMLDivElement>()
let pieChart: echarts.ECharts | undefined

const pageLoading = ref(false)
const majorLoading = ref(false)

const majors = ref<SysDictMajorSimpleVO[]>([])
const selectedMajorId = ref<number>()
const schoolYears = ref<SysDictSchoolYearVO[]>([])
const selectedTermId = ref<number>()
const selectedGrade = ref('')

const courseList = ref<CourseSimpleVO[]>([])
const allCourseRows = ref<CourseVO[]>([])
const allTeachingClasses = ref<TeachingClassVO[]>([])
const allIndicators = ref<IndicatorPointVO[]>([])
const allRequirements = ref<GraduationRequirementVO[]>([])
const allCalculationStatuses = ref<AchievementCalculationStatusVO[]>([])

const majorCourseRows = ref<CourseVO[]>([])
const majorRequirements = ref<GraduationRequirementVO[]>([])
const majorIndicators = ref<IndicatorPointVO[]>([])
const majorTeachingClasses = ref<TeachingClassVO[]>([])
const majorMatrixConfig = ref<MatrixConfigVO>()
const majorMatrixCheck = ref<MatrixWeightCheckVO>()
const majorCalculationDashboard = ref<MajorCalculationDashboardVO>()
const majorCalculationResult = ref<MajorCalculationResultVO>()

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
const classesWithCalculationCount = computed(() =>
  canLoadGlobalCourseStatus.value ? allCalculationStatuses.value.filter((item) => item.hasCalculationResult).length : 0
)
const classesWithoutCalculationCount = computed(() =>
  canLoadGlobalCourseStatus.value ? Math.max(allTeachingClasses.value.length - classesWithCalculationCount.value, 0) : 0
)
const pendingCourseCalculationClasses = computed(() => {
  if (!canLoadGlobalCourseStatus.value) {
    return []
  }
  const calculatedClassIds = new Set(
    allCalculationStatuses.value.filter((item) => item.hasCalculationResult).map((item) => item.classId)
  )
  return allTeachingClasses.value.filter((item) => !calculatedClassIds.has(item.id))
})
const routeClassId = computed(() => {
  const raw = route.query.classId
  const value = Array.isArray(raw) ? raw[0] : raw
  const numeric = Number(value)
  return Number.isFinite(numeric) && numeric > 0 ? numeric : undefined
})
const routeCourseId = computed(() => {
  const raw = route.query.courseId
  const value = Array.isArray(raw) ? raw[0] : raw
  const numeric = Number(value)
  return Number.isFinite(numeric) && numeric > 0 ? numeric : undefined
})
const preferredContextCourseId = computed(() => {
  const pendingMajorCourseId = pendingMajorCourseStatusList.value.find((item) => item.courseId)?.courseId
  if (pendingMajorCourseId) return pendingMajorCourseId
  if (pendingCourseCalculationClasses.value.length) return pendingCourseCalculationClasses.value[0].courseId
  if (majorTeachingClasses.value.length) return majorTeachingClasses.value[0].courseId
  if (allTeachingClasses.value.length) return allTeachingClasses.value[0].courseId
  if (routeCourseId.value) return routeCourseId.value
  return undefined
})
const preferredContextClassId = computed(() => {
  const pendingMajorClassId = pendingMajorCourseStatusList.value.find((item) => item.classId)?.classId
  if (pendingMajorClassId) return pendingMajorClassId
  if (pendingCourseCalculationClasses.value.length) return pendingCourseCalculationClasses.value[0].id
  if (majorTeachingClasses.value.length) return majorTeachingClasses.value[0].id
  if (allTeachingClasses.value.length) return allTeachingClasses.value[0].id
  if (routeClassId.value) return routeClassId.value
  return undefined
})
const courseCalculationCoverageRate = computed(() => {
  if (!canLoadGlobalCourseStatus.value) return 0
  if (!allTeachingClasses.value.length) return 0
  return classesWithCalculationCount.value / allTeachingClasses.value.length
})
const courseCalculationCoverageText = computed(() =>
  canLoadGlobalCourseStatus.value ? `${Math.round(courseCalculationCoverageRate.value * 100)}%` : '-'
)
const courseCalculationCardTag = computed(() => {
  if (!canLoadGlobalCourseStatus.value) return '角色受限'
  if (!allTeachingClasses.value.length) return '待建班'
  if (!classesWithoutCalculationCount.value) return '已覆盖'
  return '待补结果'
})
const courseCalculationCardType = computed<'success' | 'warning' | 'info'>(() => {
  if (!canLoadGlobalCourseStatus.value) return 'info'
  if (!allTeachingClasses.value.length) return 'info'
  if (!classesWithoutCalculationCount.value) return 'success'
  return 'warning'
})
const courseCalculationCardHint = computed(() => {
  if (!canLoadGlobalCourseStatus.value) return '当前角色不能读取全局状态'
  if (!allTeachingClasses.value.length) return '先准备教学班'
  if (!classesWithoutCalculationCount.value) return '全部班级已覆盖'
  return `还差 ${classesWithoutCalculationCount.value} 个班级`
})
const courseCalculationCardMessage = computed(() => {
  if (!canLoadGlobalCourseStatus.value) {
    return '当前角色暂不支持读取全局课程级状态，首页不再自动请求这组接口。'
  }
  if (!allTeachingClasses.value.length) {
    return '当前还没有教学班数据，建议先去成绩页补教学班、学生和成绩。'
  }
  if (!classesWithoutCalculationCount.value) {
    return '当前所有教学班都已有课程级结果，这一层已经全部覆盖。'
  }
  return `当前全局还有 ${classesWithoutCalculationCount.value} 个教学班未完成课程级计算，建议先处理这些班级。`
})
const currentMajorHasCalculationResult = computed(() => (majorCalculationResult.value?.totalRecords ?? 0) > 0)
const majorAverageAchievementText = computed(() =>
  currentMajorHasCalculationResult.value ? formatNumber(majorCalculationResult.value?.averageAchievement) : '-'
)
const majorResultStatusText = computed(() => {
  if (!selectedMajorId.value) return '待选择专业'
  if (!selectedTermId.value || !selectedGrade.value.trim()) return '待补筛选条件'
  return currentMajorHasCalculationResult.value ? '已生成' : '未生成'
})
const majorThresholdStatusText = computed(() => {
  if (!currentMajorHasCalculationResult.value) return '-'
  return majorCalculationResult.value?.meetsGraduationRequirement ? '已达标' : '未达标'
})
const majorThresholdHint = computed(() => {
  if (!currentMajorHasCalculationResult.value) return '生成结果后显示'
  return `阈值 ${formatNumber(majorCalculationResult.value?.threshold)}`
})
const majorResultCardTag = computed(() => {
  if (!selectedMajorId.value) return '待选择'
  if (!selectedTermId.value || !selectedGrade.value.trim()) return '待补条件'
  if (currentMajorHasCalculationResult.value) return '结果可用'
  if (majorCalculationDashboard.value?.canCalculate) return '可计算'
  return '待补前置'
})
const majorResultCardType = computed<'success' | 'warning' | 'info'>(() => {
  if (!selectedMajorId.value) return 'info'
  if (!selectedTermId.value || !selectedGrade.value.trim()) return 'info'
  if (currentMajorHasCalculationResult.value) return 'success'
  if (majorCalculationDashboard.value?.canCalculate) return 'info'
  return 'warning'
})
const majorResultCardMessage = computed(() => {
  if (!selectedMajorId.value) {
    return '先选择专业，首页才能开始读取当前专业的专业级结果状态。'
  }
  if (!selectedTermId.value || !selectedGrade.value.trim()) {
    return '补齐学年学期和年级后，这张卡片才能显示当前专业的真实专业级结果。'
  }
  if (currentMajorHasCalculationResult.value) {
    return `当前专业级结果已生成，共 ${majorCalculationResult.value?.totalRecords ?? 0} 条记录，可继续查看细节。`
  }
  if (majorCalculationDashboard.value?.canCalculate) {
    return '当前专业已经具备专业级计算条件，但还没有生成最终结果，可以直接去计算中心执行。'
  }
  return majorCalculationDashboard.value?.errorMessage || '当前专业级结果还没有生成，通常是课程级结果或前置条件还不完整。'
})
const majorResultIndicatorPreview = computed<MajorCalculationAchievementItem[]>(() =>
  (majorCalculationResult.value?.achievements ?? []).slice(0, 3)
)
const noticeSummaryTag = computed(() => {
  if (!selectedMajorId.value) return '待选专业'
  if (!selectedTermId.value || !selectedGrade.value.trim()) return '待补筛选条件'
  if (classesWithoutCalculationCount.value) return '优先补课程级'
  if (majorCalculationDashboard.value?.canCalculate && !currentMajorHasCalculationResult.value) return '可推进专业级'
  if (currentMajorHasCalculationResult.value) return '可复核结果'
  if (!matrixCheckValid.value) return '矩阵待完善'
  return '继续完善'
})
const noticeSummaryType = computed<'success' | 'warning' | 'info'>(() => {
  if (!selectedMajorId.value) return 'info'
  if (!selectedTermId.value || !selectedGrade.value.trim()) return 'info'
  if (classesWithoutCalculationCount.value) return 'warning'
  if (majorCalculationDashboard.value?.canCalculate && !currentMajorHasCalculationResult.value) return 'info'
  if (currentMajorHasCalculationResult.value) return 'success'
  if (!matrixCheckValid.value) return 'warning'
  return 'info'
})
const summaryGuideState = computed<'ready' | 'need-filters' | 'need-course-results' | 'need-major-result'>(() => {
  if (!selectedTermId.value || !selectedGrade.value.trim()) return 'need-filters'
  if (classesWithoutCalculationCount.value) return 'need-course-results'
  if (!currentMajorHasCalculationResult.value) return 'need-major-result'
  return 'ready'
})
const courseCalculationAction = computed<CalculationEntryAction>(() => {
  if (!allTeachingClasses.value.length) {
    return {
      buttonText: '先准备教学班再去计算中心',
      message: '当前还没有教学班，建议先去成绩页补教学班、学生和成绩，再进入计算中心。'
    }
  }
  if (classesWithoutCalculationCount.value) {
    return {
      buttonText: `去计算中心补 ${classesWithoutCalculationCount.value} 个班的课程级结果`,
      message: '首页检测到还有教学班未形成课程级结果，进入计算中心后优先处理课程级计算。'
    }
  }
  return {
    buttonText: '去计算中心复核课程级结果',
    message: '当前所有教学班都已有课程级结果，进入计算中心后适合做课程级结果复核。'
  }
})
const majorCalculationAction = computed<CalculationEntryAction>(() => {
  if (!selectedMajorId.value) {
    return {
      buttonText: '先选择专业再去计算中心',
      message: '首页还没锁定专业，建议先选专业，再进入计算中心查看对应专业级结果。'
    }
  }
  if (!selectedTermId.value || !selectedGrade.value.trim()) {
    return {
      buttonText: '补齐筛选条件后去计算中心',
      message: '专业级结果依赖学年学期和年级，补齐条件后再进入计算中心更合适。'
    }
  }
  if (currentMajorHasCalculationResult.value) {
    return {
      buttonText: '去计算中心查看专业级结果',
      message: '当前专业级结果已生成，进入计算中心后可以继续查看专业级结果细节。'
    }
  }
  if (majorCalculationDashboard.value?.canCalculate) {
    return {
      buttonText: '去计算中心执行专业级计算',
      message: '课程级前置条件已经齐备，进入计算中心后优先执行专业级计算。'
    }
  }
  return {
    buttonText: '去计算中心检查专业级前置条件',
    message: '当前专业级结果还没准备好，进入计算中心后可以继续排查课程级结果和专业级前置条件。'
  }
})
const primaryCalculationAction = computed<CalculationEntryAction>(() => {
  if (currentMajorHasCalculationResult.value) {
    return majorCalculationAction.value
  }
  if (classesWithoutCalculationCount.value) {
    return courseCalculationAction.value
  }
  if (majorCalculationDashboard.value?.canCalculate) {
    return majorCalculationAction.value
  }
  return courseCalculationAction.value
})
const readyMajorCourseStatusCount = computed(
  () => majorCalculationDashboard.value?.courseStatusList?.filter((item) => item.hasAchievementData).length ?? 0
)
const pendingMajorCourseStatusList = computed(
  () => (majorCalculationDashboard.value?.courseStatusList ?? []).filter((item) => !item.hasAchievementData)
)
const pendingMajorCourseStatusPreview = computed(() => pendingMajorCourseStatusList.value.slice(0, 4))

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
    value: canLoadCourseCatalog.value ? String(courseList.value.length) : '-',
    sub: canLoadCourseCatalog.value ? '来自课程列表接口' : '当前角色不读取课程总表',
    icon: Files,
    tone: ''
  },
  {
    label: '毕业要求',
    value: canLoadRequirementCatalog.value ? String(allRequirements.value.length) : '-',
    sub: canLoadRequirementCatalog.value ? `指标点 ${allIndicators.value.length} 条` : '当前角色不读取毕业要求总表',
    icon: Reading,
    tone: 'green'
  },
  {
    label: '教学班',
    value: canLoadTeachingClassCatalog.value ? String(allTeachingClasses.value.length) : '-',
    sub: canLoadTeachingClassCatalog.value
      ? `覆盖课程 ${new Set(allTeachingClasses.value.map((item) => item.courseId).filter(Boolean)).size} 门`
      : '当前角色不读取教学班总表',
    icon: School,
    tone: 'yellow'
  },
  {
    label: '课程级计算',
    value: canLoadGlobalCourseStatus.value ? `${classesWithCalculationCount.value}/${allTeachingClasses.value.length || 0}` : '-',
    sub: !canLoadGlobalCourseStatus.value
      ? '当前角色不读取全局课程级状态'
      : classesWithoutCalculationCount.value
        ? `仍有 ${classesWithoutCalculationCount.value} 个班未计算`
        : '全部教学班已有课程级结果',
    icon: Aim,
    tone: !canLoadGlobalCourseStatus.value ? 'lock' : classesWithoutCalculationCount.value ? 'yellow' : 'green'
  },
  {
    label: '专业级结果',
    value: currentMajorHasCalculationResult.value ? '已生成' : '未生成',
    sub: selectedMajorName.value ? `${selectedMajorName.value} 当前条件结果状态` : '选择专业后查看',
    icon: Memo,
    tone: currentMajorHasCalculationResult.value ? 'green' : 'lock'
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
        statusLabel: majorCourseRows.value.length ? '课程已齐备' : '课程仍缺失',
        statusType: majorCourseRows.value.length ? 'success' : 'warning',
        hint: majorCourseRows.value.length ? '该专业已存在课程数据' : '这个专业下还没有查到课程'
      },
      {
        label: '毕业要求 / 指标点',
        value: `${majorRequirements.value.length} / ${majorIndicators.value.length}`,
        status: majorIndicators.value.length ? '正常' : '缺少',
        statusLabel: majorIndicators.value.length ? '指标点已可用' : '指标点仍缺失',
        statusType: majorIndicators.value.length ? 'success' : 'warning',
        hint: majorIndicators.value.length ? '毕业要求与指标点接口可正常返回' : '还没有查到该专业的指标点'
      },
      {
        label: '矩阵配置课程',
        value: `${configuredCourseCount.value} 门`,
        status: configuredCourseCount.value ? '已配置' : '未配置',
        statusLabel: configuredCourseCount.value ? '矩阵已挂接' : '矩阵待挂接',
        statusType: configuredCourseCount.value ? 'success' : 'warning',
        hint: configuredCourseCount.value
          ? '这些课程已经在课程-指标点矩阵中配置关系'
        : '还没有课程进入矩阵配置'
    },
      {
        label: '教学班覆盖',
        value: `${majorTeachingClasses.value.length} 个班`,
        status: majorTeachingClasses.value.length ? '已开课' : '未开课',
        statusLabel: majorTeachingClasses.value.length ? '教学班已覆盖' : '教学班待建立',
        statusType: majorTeachingClasses.value.length ? 'success' : 'warning',
        hint: majorTeachingClasses.value.length ? '该专业已有教学班与学生规模数据' : '暂时没有查到教学班'
      },
      {
        label: '课程级计算',
        value: `${majorCalculationDashboard.value?.coursesWithData ?? 0} / ${majorCalculationDashboard.value?.totalCourses ?? 0}`,
        status: majorCalculationDashboard.value?.canCalculate ? '已具备' : '待补齐',
        statusLabel: majorCalculationDashboard.value?.canCalculate ? '课程级已齐备' : '课程级待补齐',
        statusType: majorCalculationDashboard.value?.canCalculate ? 'success' : 'warning',
        hint: pendingMajorCourseStatusList.value.length
          ? `还差 ${pendingMajorCourseStatusList.value.length} 门课程未形成课程级结果，优先处理 ${pendingMajorCourseStatusPreview.value
              .map((item) => item.className || `班级${item.classId ?? '-'}`)
              .join('、')}${pendingMajorCourseStatusList.value.length > pendingMajorCourseStatusPreview.value.length ? ' 等' : ''}`
          : majorCalculationDashboard.value?.errorMessage || '课程级结果齐全后，才可以继续专业级计算'
      },
      {
        label: '专业级结果',
        value: currentMajorHasCalculationResult.value ? `${majorCalculationResult.value?.totalRecords ?? 0} 条` : '暂无结果',
        status: currentMajorHasCalculationResult.value ? '已生成' : '未生成',
        statusLabel: currentMajorHasCalculationResult.value ? '结果已生成' : '结果待生成',
        statusType: currentMajorHasCalculationResult.value ? 'success' : 'warning',
        hint: currentMajorHasCalculationResult.value
          ? `平均达成度 ${formatNumber(majorCalculationResult.value?.averageAchievement)}`
        : '当前条件下还没有专业级计算结果'
    },
      {
        label: '矩阵校验',
        value: matrixCheckValid.value ? '通过' : '未通过',
        status: matrixCheckValid.value ? '就绪' : '待完善',
        statusLabel: matrixCheckValid.value ? '矩阵已通过' : '矩阵待完善',
        statusType: matrixCheckValid.value ? 'success' : 'warning',
        hint: matrixCheckMessage.value || '已按矩阵校验接口检查权重合计'
      },
      {
        label: '指标点就绪数',
        value: `${readyIndicatorCount.value} / ${majorIndicators.value.length}`,
        status: readyIndicatorCount.value === majorIndicators.value.length && majorIndicators.value.length ? '齐全' : '待补充',
        statusLabel:
          readyIndicatorCount.value === majorIndicators.value.length && majorIndicators.value.length
            ? '指标点已就绪'
            : '指标点待补充',
        statusType:
          readyIndicatorCount.value === majorIndicators.value.length && majorIndicators.value.length ? 'success' : 'warning',
        hint: '只有权重列和约等于 1 的指标点，才算真正可用于后续计算'
    }
  ]
})

const recentRecords = computed<RecentRecord[]>(() => {
  const calculationRecords: RecentRecord[] = []

  if (selectedMajorId.value && selectedMajorName.value && selectedTermId.value && selectedGrade.value.trim()) {
    if (majorCalculationResult.value?.calcEndTime) {
      calculationRecords.push({
        key: `major-result-${selectedMajorId.value}-${selectedTermId.value}-${selectedGrade.value.trim()}`,
        time: formatRecordTime(majorCalculationResult.value.calcEndTime),
        sortTime: majorCalculationResult.value.calcEndTime,
        title: `${selectedMajorName.value} 专业级结果已更新`,
        detail: `平均达成度 ${formatNumber(majorCalculationResult.value.averageAchievement)} · ${majorCalculationResult.value.totalRecords ?? 0} 条记录`,
        tag: '专业级结果',
        tagType: 'warning',
        priority: 0
      })
    } else if (majorCalculationDashboard.value?.canCalculate) {
      calculationRecords.push({
        key: `major-ready-${selectedMajorId.value}-${selectedTermId.value}-${selectedGrade.value.trim()}`,
        time: `${selectedGrade.value.trim()} 级`,
        sortTime: buildFallbackSortTime(),
        title: `${selectedMajorName.value} 已具备专业级计算条件`,
        detail: `课程级结果已覆盖 ${majorCalculationDashboard.value.coursesWithData ?? 0} / ${majorCalculationDashboard.value.totalCourses ?? 0} 门课程`,
        tag: '待执行',
        tagType: 'warning',
        priority: 1
      })
    } else if (pendingMajorCourseStatusList.value.length) {
      calculationRecords.push({
        key: `major-pending-${selectedMajorId.value}-${selectedTermId.value}-${selectedGrade.value.trim()}`,
        time: `${selectedGrade.value.trim()} 级`,
        sortTime: buildFallbackSortTime(),
        title: `${selectedMajorName.value} 仍有课程未满足专业级前置条件`,
        detail: `还差 ${pendingMajorCourseStatusList.value.length} 门课程完成课程级结果写入`,
        tag: '待补齐',
        tagType: 'warning',
        priority: 1
      })
    }
  }

  const courseStatusRecords = allCalculationStatuses.value
    .filter((item) => item.hasCalculationResult && item.className)
    .slice(0, 2)
    .map((item) => ({
      key: `calc-class-${item.classId}`,
      time: `班级 ${item.classId ?? '-'}`,
      sortTime: buildFallbackSortTime(),
      title: `${item.className} 已有课程级结果`,
      detail: `一级 ${item.levelOneRecordCount ?? 0} 条 · 二级 ${item.levelTwoRecordCount ?? 0} 条`,
      tag: '课程级结果',
      tagType: 'success' as const,
      priority: 2
    }))

  const pendingStatusRecords = pendingCourseCalculationClasses.value.slice(0, 2).map((item) => ({
    key: `pending-class-${item.id}`,
    time: `班级 ${item.id}`,
    sortTime: buildFallbackSortTime(),
    title: `${item.className} 仍待补课程级结果`,
    detail: `${item.courseName || '未绑定课程'} · 建议优先去计算中心处理`,
    tag: '待补齐',
    tagType: 'warning' as const,
    priority: 0
  }))

  const overviewStatusRecords: RecentRecord[] = []
  if (selectedMajorId.value && selectedMajorName.value) {
    overviewStatusRecords.push({
      key: `overview-major-${selectedMajorId.value}`,
      time: selectedGrade.value.trim() ? `${selectedGrade.value.trim()} 级` : '当前条件',
      sortTime: buildFallbackSortTime(),
      title: `${selectedMajorName.value} 当前概览状态已更新`,
      detail: currentMajorHasCalculationResult.value
        ? `专业级结果已生成，当前平均达成度 ${formatNumber(majorCalculationResult.value?.averageAchievement)}`
        : majorCalculationDashboard.value?.canCalculate
          ? '当前条件下已经具备专业级计算条件'
          : '当前条件下仍在补课程级结果或前置条件',
      tag: '概览状态',
      tagType: currentMajorHasCalculationResult.value ? 'success' : 'primary',
      priority: currentMajorHasCalculationResult.value ? 1 : 2
    })
  }

  const courseRecords = allCourseRows.value.slice(0, 4).map((course) => ({
    key: `course-${course.id}`,
    time: formatRecordTime(course.updateTime || course.createTime),
    sortTime: course.updateTime || course.createTime || '',
    title: course.courseName,
    detail: `${course.courseCode} · ${course.majorName || '未绑定专业'}`,
    tag: '课程',
    tagType: 'primary' as const,
    priority: 3
  }))

  const classRecords = allTeachingClasses.value.slice(0, 4).map((item) => ({
    key: `class-${item.id}`,
    time: formatRecordTime(item.updateTime || item.createTime),
    sortTime: item.updateTime || item.createTime || '',
    title: item.className,
    detail: `${item.courseName || '-'} · ${item.teacherName || '未分配教师'}`,
    tag: '教学班',
    tagType: 'success' as const,
    priority: 3
  }))

  return [...calculationRecords, ...overviewStatusRecords, ...courseStatusRecords, ...pendingStatusRecords, ...courseRecords, ...classRecords]
    .sort((a, b) => {
      if (a.priority !== b.priority) {
        return a.priority - b.priority
      }
      return String(b.sortTime).localeCompare(String(a.sortTime))
    })
    .slice(0, 6)
})

const notices = computed(() => {
  const dynamicNotices: NoticeItem[] = [
    {
      title: '首页已切换为真实数据看板',
      desc: '不再使用演示用达成度和假通知，全部来自当前已经接通的后端接口。',
      tag: '已完成',
      type: 'success' as const,
      level: 'base'
    }
  ]

  if (!selectedMajorId.value) {
    dynamicNotices.push({
      title: '下一步先选择专业',
      desc: '选定专业后，首页才能联动课程级状态、矩阵校验和专业级结果提示。',
      tag: '待处理',
      type: 'info' as const,
      level: 'base'
    })
    return dynamicNotices
  }

  if (!selectedTermId.value || !selectedGrade.value.trim()) {
    dynamicNotices.push({
      title: '专业级结果还缺查询条件',
      desc: '补齐学年学期和年级后，首页才能继续读取当前专业的专业级结果和可计算状态。',
      tag: '待补条件',
      type: 'warning' as const,
      level: 'major'
    })
  } else if (currentMajorHasCalculationResult.value) {
    dynamicNotices.push({
      title: '当前专业已生成专业级结果',
      desc: `当前条件下已生成 ${majorCalculationResult.value?.totalRecords ?? 0} 条结果，适合继续看细节或准备报表。`,
      tag: '结果可用',
      type: 'success' as const,
      level: 'major'
    })
  } else if (majorCalculationDashboard.value?.canCalculate) {
    dynamicNotices.push({
      title: '当前专业可以继续专业级计算',
      desc: '课程级结果已经具备，下一步最直接的是去计算中心执行专业级计算并写入结果。',
      tag: '可推进',
      type: 'info' as const,
      level: 'major'
    })
  } else {
    dynamicNotices.push({
      title: '当前专业还在补前置数据',
      desc: majorCalculationDashboard.value?.errorMessage || '课程级结果、矩阵权重或教学班条件还没完全齐备。',
      tag: '待补齐',
      type: 'warning' as const,
      level: 'major'
    })
  }

  if (classesWithoutCalculationCount.value) {
    dynamicNotices.push({
      title: '仍有教学班缺少课程级结果',
      desc: `全局还有 ${classesWithoutCalculationCount.value} 个教学班未形成课程级结果，首页建议继续优先补这一层。`,
      tag: '课程级待补',
      type: 'warning' as const,
      level: 'course'
    })
  } else {
    dynamicNotices.push({
      title: '课程级计算已全部覆盖',
      desc: '所有教学班都已有课程级结果，可以把重心更多放到专业级结果复核与首页展示。',
      tag: '已覆盖',
      type: 'success' as const,
      level: 'course'
    })
  }

  if (!matrixCheckValid.value && selectedMajorId.value) {
    dynamicNotices.push({
      title: '当前专业矩阵还需要再检查',
      desc: matrixCheckMessage.value || '矩阵权重还没有完全通过校验，会影响后续专业级计算稳定性。',
      tag: '矩阵待完善',
      type: 'warning' as const,
      level: 'matrix'
    })
  } else if (selectedMajorId.value) {
    dynamicNotices.push({
      title: '当前专业矩阵校验通过',
      desc: `当前专业已有 ${readyIndicatorCount.value} 个指标点达到可用状态，适合继续推进真实结果展示。`,
      tag: '矩阵就绪',
      type: 'success' as const,
      level: 'matrix'
    })
  }

  return dynamicNotices
    .sort((a, b) => {
      const priorityMap: Record<NoticeItem['level'], number> = {
        course: 0,
        major: 1,
        matrix: 2,
        base: 3
      }
      return priorityMap[a.level] - priorityMap[b.level]
    })
    .slice(0, 4)
})

const quickEntries = computed<QuickEntry[]>(() => {
  const entries: QuickEntry[] = [
    {
      label: '计算中心',
      path: '/calculation',
      icon: Aim,
      desc: currentMajorHasCalculationResult.value
          ? '当前优先查看专业级结果细节'
          : classesWithoutCalculationCount.value
            ? '当前优先补课程级计算结果'
            : majorCalculationDashboard.value?.canCalculate
              ? '当前可直接推进专业级计算'
              : '课程级与专业级计算入口',
      priority: 0,
      recommended: true
    },
    {
      label: '成绩导入',
      path: '/score',
      icon: DataAnalysis,
      desc: classesWithoutCalculationCount.value ? '建议优先回填课程级前置数据' : '学生与教学班数据',
      priority: classesWithoutCalculationCount.value ? 1 : 4,
      feature: 'score'
    },
    {
      label: '支撑矩阵',
      path: '/matrix',
      icon: Grid,
      desc: matrixCheckValid.value ? '当前矩阵校验已通过' : '建议优先检查矩阵权重',
      priority: matrixCheckValid.value ? 5 : 2,
      feature: 'matrix'
    },
    {
      label: '基础数据',
      path: '/basic-data',
      icon: Document,
      desc: selectedMajorId.value ? `当前聚焦 ${selectedMajorName.value || '专业数据'}` : '课程、毕业要求、指标点',
      priority: selectedMajorId.value ? 3 : 1,
      feature: 'basicData'
    },
    {
      label: '课程大纲',
      path: '/syllabus',
      icon: Notebook,
      desc: '课程目标与考核点',
      priority: 6,
      feature: 'syllabus'
    },
    {
      label: '报表准备',
      path: '/report',
      icon: Memo,
      desc: currentMajorHasCalculationResult.value ? '当前已更适合继续准备报表' : '报表导出能力现状',
      priority: currentMajorHasCalculationResult.value ? 2 : 7,
      feature: 'report'
    }
  ]

  return entries
    .filter((entry) => !entry.feature || canAccessFeature(user.role, entry.feature))
    .sort((a, b) => a.priority - b.priority)
})
const quickEntrySummaryTag = computed(() => {
  if (classesWithoutCalculationCount.value) return '优先补课程级'
  if (majorCalculationDashboard.value?.canCalculate && !currentMajorHasCalculationResult.value) return '可推进专业级'
  if (currentMajorHasCalculationResult.value) return '可复核结果'
  if (!matrixCheckValid.value) return '先检查矩阵'
  return '按当前状态推荐'
})
const quickEntrySummaryDesc = computed(() => {
  if (classesWithoutCalculationCount.value) {
    return '首页已把“成绩导入、计算中心、基础数据”排在更前面，方便先补课程级结果。'
  }
  if (majorCalculationDashboard.value?.canCalculate && !currentMajorHasCalculationResult.value) {
    return '首页已把“计算中心、报表准备、基础数据”排在更前面，方便直接推进专业级计算。'
  }
  if (currentMajorHasCalculationResult.value) {
    return '首页已把“计算中心、报表准备、基础数据”排在更前面，方便继续复核结果和准备报表。'
  }
  if (!matrixCheckValid.value) {
    return '首页已把“支撑矩阵、基础数据、计算中心”排在更前面，方便先补矩阵前置条件。'
  }
  return '快捷入口会按当前首页状态自动重排，把更值得优先进入的页面放在前面。'
})

const majorCalculationSummaryMessage = computed(() => {
  if (!selectedMajorId.value) {
    return ''
  }
  if (!selectedTermId.value || !selectedGrade.value.trim()) {
    return '补齐学年学期和年级后，首页才能同步读取当前专业的真实计算结果。'
  }
  if (currentMajorHasCalculationResult.value) {
    return `当前专业在所选条件下已生成专业级结果，平均达成度 ${formatNumber(majorCalculationResult.value?.averageAchievement)}。`
  }
  if (majorCalculationDashboard.value?.canCalculate) {
    return '当前专业已具备专业级计算前置条件，但还没有最终结果，可以前往计算中心执行。'
  }
  return majorCalculationDashboard.value?.errorMessage || '当前还有课程未完成课程级计算，首页会继续提示未就绪状态。'
})

const recentCalculationFocus = computed(() => {
  if (!selectedMajorId.value) {
    return '首页现在已经能读取真实计算接口，下一步先选择专业，再继续看课程级与专业级状态。'
  }
  if (!selectedTermId.value || !selectedGrade.value.trim()) {
    return '当前专业已切到真实看板，但专业级结果还缺筛选条件，补齐后首页会自动联动状态。'
  }
  if (currentMajorHasCalculationResult.value) {
    return `当前专业最近一次结果可直接从首页看到，平均达成度 ${formatNumber(majorCalculationResult.value?.averageAchievement)}。`
  }
  if (majorCalculationDashboard.value?.canCalculate) {
    return `当前专业已有 ${readyMajorCourseStatusCount.value} 门课程具备课程级结果，已经可以继续专业级计算。`
  }
  if (pendingMajorCourseStatusList.value.length) {
    const pendingNames = pendingMajorCourseStatusList.value
      .slice(0, 2)
      .map((item) => item.className || `班级${item.classId ?? '-'}`)
      .join('、')
    return `当前专业还差 ${pendingMajorCourseStatusList.value.length} 门课程完成课程级结果，优先处理 ${pendingNames}${pendingMajorCourseStatusList.value.length > 2 ? ' 等' : ''}。`
  }
  return '首页已展示真实业务数据，可继续完善穿透台账与计算结果的联动展示。'
})

const majorCalculationSummaryType = computed<'success' | 'warning' | 'info'>(() => {
  if (currentMajorHasCalculationResult.value) {
    return 'success'
  }
  if (majorCalculationDashboard.value?.canCalculate) {
    return 'info'
  }
  return 'warning'
})

const calculationStatusTagText = computed(() => {
  if (!selectedMajorId.value) return '待选择条件'
  if (currentMajorHasCalculationResult.value) return '已有专业级结果'
  if (majorCalculationDashboard.value?.canCalculate) return '可继续专业级计算'
  return '优先补课程级前置数据'
})

const calculationStatusTagType = computed<'success' | 'warning' | 'info'>(() => {
  if (!selectedMajorId.value) return 'info'
  if (currentMajorHasCalculationResult.value) return 'success'
  if (majorCalculationDashboard.value?.canCalculate) return 'info'
  return 'warning'
})

const courseCalculationHint = computed(() => {
  if (!allTeachingClasses.value.length) {
    return '当前系统里还没有教学班，建议先在成绩页建立教学班并导入学生、成绩。'
  }
  if (!classesWithoutCalculationCount.value) {
    return '所有教学班都已有课程级结果，可以把重点放到专业级计算和结果复核。'
  }
  return `还有 ${classesWithoutCalculationCount.value} 个教学班没有课程级结果，首页建议优先去成绩页和计算中心处理。`
})

const majorCalculationHint = computed(() => {
  if (!selectedTermId.value || !selectedGrade.value.trim()) {
    return '补齐学年学期和年级后，首页才能对应读取专业级真实结果。'
  }
  if (currentMajorHasCalculationResult.value) {
    return `当前条件下已生成 ${majorCalculationResult.value?.totalRecords ?? 0} 条专业级结果，可继续查看详细结果与达标情况。`
  }
  if (majorCalculationDashboard.value?.canCalculate) {
    return '前置条件已具备，但还没有专业级结果，建议直接去计算中心执行专业级计算。'
  }
  return majorCalculationDashboard.value?.errorMessage || '当前条件下还没有达到专业级计算的前置条件。'
})

const nextActionTitle = computed(() => {
  if (!selectedMajorId.value) return '先选择一个专业'
  if (!selectedTermId.value || !selectedGrade.value.trim()) return '补齐学年学期和年级条件'
  if (currentMajorHasCalculationResult.value) return '查看并复核专业级结果'
  if (majorCalculationDashboard.value?.canCalculate) return '去计算中心执行专业级计算'
  if (classesWithoutCalculationCount.value) return '先补课程级计算结果'
  return '继续完善矩阵和教学班前置数据'
})

const nextActionDesc = computed(() => {
  if (!selectedMajorId.value) return '首页当前还无法判断该专业的真实计算情况。'
  if (!selectedTermId.value || !selectedGrade.value.trim()) return '专业级结果和看板都依赖条件筛选。'
  if (currentMajorHasCalculationResult.value) {
    return '当前首页已经能看到真实结果状态，下一步更适合去计算中心看细节。'
  }
  if (majorCalculationDashboard.value?.canCalculate) {
    return '课程级结果已齐备，专业级结果还没生成，适合直接推进。'
  }
  if (classesWithoutCalculationCount.value) {
    return '仍有教学班缺少课程级结果，先补这一层最划算。'
  }
  return '当前结果不足以支撑专业级计算，建议先补矩阵、教学班或成绩数据。'
})

function formatNumber(value?: number | string | null) {
  if (value === undefined || value === null || value === '') return '-'
  const num = Number(value)
  return Number.isNaN(num) ? String(value) : num.toFixed(4)
}

function formatRecordTime(value?: string | null) {
  if (!value) return '时间未知'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return `${date.getMonth() + 1}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function formatPendingMajorCourseMeta(item: { courseId?: number; classId?: number; achievementDataCount?: number }) {
  const courseLabel = item.courseId ? `课程 ID ${item.courseId}` : '课程待确认'
  const classLabel = item.classId ? `班级 ID ${item.classId}` : '班级待确认'
  const countLabel =
    item.achievementDataCount !== undefined && item.achievementDataCount !== null
      ? `当前记录 ${item.achievementDataCount} 条`
      : '当前还没有课程级结果'
  return `${courseLabel} · ${classLabel} · ${countLabel}`
}

function buildFallbackSortTime() {
  return new Date().toISOString()
}

function buildPageQuery() {
  const query: Record<string, string> = {}
  if (selectedMajorId.value) query.majorId = String(selectedMajorId.value)
  if (selectedTermId.value) query.termId = String(selectedTermId.value)
  if (selectedGrade.value.trim()) query.grade = selectedGrade.value.trim()
  if (preferredContextClassId.value) query.classId = String(preferredContextClassId.value)
  if (preferredContextCourseId.value) query.courseId = String(preferredContextCourseId.value)
  return query
}

function buildRouteLocation(path: string) {
  const query = buildPageQuery()
  if (path === '/matrix') {
    return {
      path,
      query: selectedMajorId.value ? { majorId: String(selectedMajorId.value) } : {}
    }
  }
  return { path, query }
}

function navigateToCalculation() {
  void router.push(buildRouteLocation('/calculation'))
}

function navigateToScore() {
  void router.push(buildRouteLocation('/score'))
}

function navigateToMatrix() {
  void router.push(buildRouteLocation('/matrix'))
}

function openQuickEntry(entry: QuickEntry) {
  void router.push(buildRouteLocation(entry.path))
}

function goToCalculationWithHint(action: CalculationEntryAction) {
  ElMessage.info(action.message)
  window.setTimeout(() => {
    void router.push(buildRouteLocation('/calculation'))
  }, 120)
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const openDoc = () => {
  const docWindow = window.open(apiDocUrl, '_blank', 'noopener,noreferrer')
  if (docWindow) {
    docWindow.opener = null
  }
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

const mergeMajorOptions = (items: Array<{ id?: number | null; majorId?: number | null; majorName?: string | null }>) => {
  const majorMap = new Map(majors.value.map((item) => [item.id, item]))
  items.forEach((item) => {
    // 用真正的专业 id（课程/毕业要求挂在 majorId 上），不能用记录自身 id，
    // 否则每条课程/毕业要求都会被当成一个"专业"，产生大量同名重复项
    const majorId = Number(item.majorId)
    const majorName = item.majorName?.trim()
    if (!Number.isFinite(majorId) || !majorName) {
      return
    }
    // 仅补充字典里没有的专业，避免覆盖 listMajors 返回的完整项
    if (!majorMap.has(majorId)) {
      majorMap.set(majorId, {
        id: majorId,
        majorName
      })
    }
  })
  majors.value = Array.from(majorMap.values())
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
    majorCalculationDashboard.value = undefined
    majorCalculationResult.value = undefined
    await nextTick()
    renderPieChart()
    return
  }

  majorLoading.value = true
  try {
    const [coursePage, requirementPage, indicatorPage, classPage, matrix] = await Promise.all([
      canLoadCourseCatalog.value
        ? pageCourses({ current: 1, pageSize: 300, majorId: selectedMajorId.value })
        : Promise.resolve({ records: [] } as any),
      canLoadRequirementCatalog.value
        ? pageGraduationRequirements({ current: 1, pageSize: 200, majorId: selectedMajorId.value })
        : Promise.resolve({ records: [] } as any),
      canLoadRequirementCatalog.value ? pageIndicators({ current: 1, pageSize: 300 }) : Promise.resolve({ records: [] } as any),
      canLoadTeachingClassCatalog.value ? pageTeachingClasses({ current: 1, pageSize: 500 }) : Promise.resolve({ records: [] } as any),
      canLoadMatrixData.value ? getMatrixConfig(selectedMajorId.value) : Promise.resolve(undefined)
    ])

    majorCourseRows.value = coursePage.records
    majorRequirements.value = requirementPage.records
    mergeMajorOptions(coursePage.records)
    mergeMajorOptions(requirementPage.records)
    majorIndicators.value = indicatorPage.records.filter((item: IndicatorPointVO) =>
      item.requirementId ? requirementPage.records.some((req: GraduationRequirementVO) => req.id === item.requirementId) : false
    )
    majorTeachingClasses.value = classPage.records.filter(
      (item: TeachingClassVO) => item.courseId && coursePage.records.some((course: CourseVO) => course.id === item.courseId)
    )
    majorMatrixConfig.value = matrix

    const matrixItems = buildMatrixItems()
    majorMatrixCheck.value = canLoadMatrixData.value && matrixItems.length
      ? await checkMatrixConfig(selectedMajorId.value, matrixItems)
      : {
          valid: false,
          message: canLoadMatrixData.value ? '当前专业还没有矩阵配置数据' : '当前角色不读取矩阵配置接口',
          columnSums: {}
        }

    if (canLoadMajorCalculation.value && selectedTermId.value && selectedGrade.value.trim()) {
      const request = {
        majorId: selectedMajorId.value,
        termId: selectedTermId.value,
        grade: selectedGrade.value.trim()
      }
      const [dashboard, result] = await Promise.all([
        getMajorCalculationDashboard({ ...request, current: 1, pageSize: 100 }),
        getMajorCalculationResult(request)
      ])
      majorCalculationDashboard.value = dashboard
      majorCalculationResult.value = result
    } else {
      majorCalculationDashboard.value = undefined
      majorCalculationResult.value = undefined
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
    const [majorResult, schoolYearResult, courseListResult, coursePageResult, classPageResult, requirementPageResult, indicatorPageResult] =
      await Promise.all([
        user.role === 'admin' || user.role === 'leader' || user.role === 'edu' ? listMajors() : Promise.resolve([]),
        canLoadSchoolYearCatalog.value ? listSchoolYears() : Promise.resolve([]),
        canLoadCourseCatalog.value ? listCourses() : Promise.resolve([]),
        canLoadCourseCatalog.value ? pageCourses({ current: 1, pageSize: 300 }) : Promise.resolve({ records: [] } as any),
        canLoadTeachingClassCatalog.value ? pageTeachingClasses({ current: 1, pageSize: 500 }) : Promise.resolve({ records: [] } as any),
        canLoadRequirementCatalog.value ? pageGraduationRequirements({ current: 1, pageSize: 300 }) : Promise.resolve({ records: [] } as any),
        canLoadRequirementCatalog.value ? pageIndicators({ current: 1, pageSize: 500 }) : Promise.resolve({ records: [] } as any)
      ])

    majors.value = majorResult
    schoolYears.value = schoolYearResult
    courseList.value = courseListResult
    allCourseRows.value = coursePageResult.records
    allTeachingClasses.value = classPageResult.records
    allRequirements.value = requirementPageResult.records
    allIndicators.value = indicatorPageResult.records
    mergeMajorOptions(courseListResult)
    mergeMajorOptions(coursePageResult.records)
    mergeMajorOptions(requirementPageResult.records)

    if (!selectedMajorId.value && majors.value.length) {
      selectedMajorId.value = majors.value[0].id
    }
    if (!selectedTermId.value && schoolYears.value.length) {
      selectedTermId.value = schoolYears.value[0].id
    }
    if (!selectedGrade.value) {
      selectedGrade.value = String(new Date().getFullYear() - 4)
    }

    if (canLoadGlobalCourseStatus.value) {
      const statusResults = await Promise.allSettled(
        classPageResult.records.map((item: TeachingClassVO) => getCourseAchievementCalculationStatus(item.id))
      )
      allCalculationStatuses.value = statusResults
        .filter((item): item is PromiseFulfilledResult<AchievementCalculationStatusVO> => item.status === 'fulfilled')
        .map((item) => item.value)
    } else {
      allCalculationStatuses.value = []
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

.action-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}

.major-course-status-board {
  margin-top: 14px;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid #e8edf5;
  background: linear-gradient(180deg, #f9fbff 0%, #ffffff 100%);
}

.major-course-status-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.major-course-status-title {
  color: #20324d;
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 4px;
}

.major-course-status-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 14px;
}

.major-course-status-metric {
  display: grid;
  gap: 4px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #f4f8ff;
  border: 1px solid #dfe8f5;
}

.major-course-status-metric.warning {
  background: #fff8ef;
  border-color: #f5dfb6;
}

.major-course-status-metric strong {
  color: #20324d;
  font-size: 22px;
  line-height: 1;
}

.major-course-status-metric span {
  color: #6c7b90;
  font-size: 13px;
}

.status-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
}

.status-summary-item {
  border: 1px solid #e8edf5;
  border-radius: 14px;
  padding: 16px;
  background: #f9fbff;
}

.status-summary-label {
  color: #6c7b90;
  font-size: 13px;
}

.status-summary-value {
  margin: 10px 0 8px;
  color: #20324d;
  font-size: 18px;
  font-weight: 600;
}

.course-card-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-top: 14px;
}

.course-card-metric {
  padding: 14px;
  border-radius: 14px;
  background: #f7faff;
  border: 1px solid #e8edf5;
}

.course-card-label {
  color: #6c7b90;
  font-size: 13px;
}

.course-card-value {
  margin: 8px 0 6px;
  color: #20324d;
  font-size: 24px;
  font-weight: 700;
}

.pending-class-list {
  margin-top: 14px;
  display: grid;
  gap: 8px;
}

.major-pending-list {
  margin-top: 16px;
}

.pending-class-title {
  color: #20324d;
  font-size: 13px;
  font-weight: 600;
}

.pending-class-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #fff8ef;
  color: #7a5a1f;
}

.pending-class-item small {
  color: #9a7d47;
}

.major-status-more {
  color: #6c7b90;
  font-size: 12px;
}

.empty-guide-box {
  display: grid;
  gap: 12px;
  padding: 18px;
  border-radius: 16px;
  background: #f8fbff;
  border: 1px solid #e6eef8;
}

.empty-guide-title {
  color: #20324d;
  font-size: 18px;
  font-weight: 700;
}

.empty-guide-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>
