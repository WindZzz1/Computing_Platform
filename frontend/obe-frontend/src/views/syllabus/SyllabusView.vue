<template>
  <div class="page">
    <h1 class="page-title">课程大纲配置</h1>
    <p class="page-desc">继续使用真实课程目标、考核点和内部权重接口，不再展示示例达成度或占位字段。</p>

    <section class="page-grid">
      <div class="panel span-12">
        <div class="toolbar">
          <div>
            <h3 class="panel-title">当前课程</h3>
            <span class="muted">{{ currentCourseLabel }}</span>
          </div>
          <div class="toolbar-actions">
            <el-select v-model="currentCourseId" style="width: 280px" @change="reloadCourseData">
              <el-option
                v-for="course in courses"
                :key="course.id"
                :label="`${course.courseCode} - ${course.courseName}`"
                :value="course.id"
              />
            </el-select>
            <el-button @click="reloadCourseData">刷新</el-button>
          </div>
        </div>
      </div>

      <div class="panel span-5">
        <div class="toolbar">
          <h3 class="panel-title">课程目标</h3>
          <div style="display: flex; gap: 8px">
            <el-button type="primary" :disabled="!currentCourseId" @click="openObjectiveCreateDialog">新增目标</el-button>
            <el-button :disabled="!currentCourseId" @click="openObjectiveImportDialog">批量导入</el-button>
          </div>
        </div>
        <el-table v-loading="loading" :data="objectiveRows" border>
          <el-table-column prop="code" label="目标编号" width="120" />
          <el-table-column prop="name" label="目标名称" min-width="180" />
          <el-table-column prop="content" label="目标描述" min-width="240" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button link type="primary" @click="openObjectiveEditDialog(row.raw)">编辑</el-button>
              <el-button link type="danger" @click="handleDeleteObjective(row.raw)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel span-7">
        <div class="toolbar">
          <h3 class="panel-title">内部贡献权重</h3>
          <el-tag :type="weightCheckValid ? 'success' : 'warning'">{{ weightCheckValid ? '当前校验已通过' : '当前校验待处理' }}</el-tag>
        </div>
        <el-alert
          :type="weightCheckValid ? 'success' : 'warning'"
          show-icon
          :closable="false"
          :title="weightCheckMessage"
          style="margin-bottom: 12px"
        />
        <el-table v-loading="loading" :data="weightRows" border>
          <el-table-column prop="objectiveCode" label="课程目标" width="140" />
          <el-table-column
            v-for="indicator in indicators"
            :key="indicator.id"
            :label="indicator.indicatorCode"
            min-width="120"
          >
            <template #default="{ row }">
              <el-input-number
                v-model="row.weights[indicator.id]"
                :min="0"
                :max="1"
                :step="0.1"
                :precision="2"
                controls-position="right"
              />
            </template>
          </el-table-column>
        </el-table>
        <div class="weight-footer">
          <span>列合计</span>
          <span
            v-for="indicator in indicators"
            :key="indicator.id"
            :class="isColumnOk(indicator.id) ? 'success-text' : 'danger-text'"
          >
            {{ indicator.indicatorCode }} = {{ getColumnSum(indicator.id).toFixed(2) }}
          </span>
        </div>
        <div v-if="weightCheckPendingItems.length" class="weight-warning-list">
          <span class="muted">待处理：</span>
          <el-tag
            v-for="item in weightCheckPendingItems"
            :key="item.id"
            type="warning"
            effect="plain"
          >
            {{ item.label }}
          </el-tag>
        </div>
        <el-button type="primary" style="margin-top: 14px" :disabled="!currentCourseId" @click="saveWeights">
          保存
        </el-button>
      </div>

      <div class="panel span-12">
        <div class="toolbar">
          <h3 class="panel-title">考核点管理</h3>
          <div style="display: flex; gap: 8px">
            <el-button type="primary" :disabled="!currentCourseId || !objectives.length" @click="openAssessmentCreateDialog">
              新增考核点
            </el-button>
            <el-button :disabled="!currentCourseId || !objectives.length" @click="openAssessmentImportDialog">批量导入</el-button>
          </div>
        </div>
        <el-table v-loading="loading" :data="assessmentRows" border>
          <el-table-column prop="code" label="考核点编号" width="120" />
          <el-table-column prop="name" label="考核点名称" min-width="220" />
          <el-table-column prop="score" label="分值" width="90" />
          <el-table-column prop="objective" label="对应课程目标" width="220" />
          <el-table-column prop="updatedAt" label="更新时间" min-width="180" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button link type="primary" @click="openAssessmentEditDialog(row.raw)">编辑</el-button>
              <el-button link type="danger" @click="handleDeleteAssessment(row.raw)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>

    <!-- 课程目标批量导入 -->
    <el-dialog v-model="objectiveImportVisible" title="批量导入课程目标" width="560px" destroy-on-close>
      <div class="import-tips">
        <p>支持上传 <code>.xlsx</code> / <code>.xls</code>，字段：课程代码*、目标编号*、目标名称*、目标描述。建议先下载模板再填写。</p>
        <div class="import-actions">
          <el-button @click="handleDownloadObjectiveTemplate">下载模板</el-button>
        </div>
      </div>
      <el-upload
        v-model:file-list="objectiveImportFileList"
        drag
        action="#"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleObjectiveImportChange"
        :on-remove="handleObjectiveImportRemove"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-title">拖拽课程目标 Excel 到这里，或点击选择文件</div>
        <template #tip>
          <div class="muted">导入完成后会自动刷新课程目标列表。</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="objectiveImportVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingObjectiveImport" @click="submitObjectiveImport">开始导入</el-button>
      </template>
    </el-dialog>

    <!-- 考核点批量导入 -->
    <el-dialog v-model="assessmentImportVisible" title="批量导入考核点" width="560px" destroy-on-close>
      <div class="import-tips">
        <p>支持上传 <code>.xlsx</code> / <code>.xls</code>，字段：课程代码*、考核点编号*、考核点名称*、满分值*、关联目标编号*、支撑权重*。<br/>关联目标编号与支撑权重用英文逗号分隔，按位置一一对应。</p>
        <div class="import-actions">
          <el-button @click="handleDownloadAssessmentTemplate">下载模板</el-button>
        </div>
      </div>
      <el-upload
        v-model:file-list="assessmentImportFileList"
        drag
        action="#"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleAssessmentImportChange"
        :on-remove="handleAssessmentImportRemove"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-title">拖拽考核点 Excel 到这里，或点击选择文件</div>
        <template #tip>
          <div class="muted">导入完成后会自动刷新考核点列表。</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="assessmentImportVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingAssessmentImport" @click="submitAssessmentImport">开始导入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="objectiveDialogVisible" :title="objectiveEditing ? '编辑课程目标' : '新增课程目标'" width="520px" destroy-on-close>
      <el-form ref="objectiveFormRef" :model="objectiveForm" :rules="objectiveRules" label-width="88px">
        <el-form-item label="目标编号" prop="objCode">
          <el-input v-model="objectiveForm.objCode" placeholder="例如 CO1" />
        </el-form-item>
        <el-form-item label="目标名称" prop="objName">
          <el-input v-model="objectiveForm.objName" placeholder="请输入目标名称" />
        </el-form-item>
        <el-form-item label="目标描述" prop="objDesc">
          <el-input v-model="objectiveForm.objDesc" type="textarea" :rows="4" placeholder="请输入课程目标描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="objectiveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingObjective" @click="submitObjective">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assessmentDialogVisible" :title="assessmentEditing ? '编辑考核点' : '新增考核点'" width="560px" destroy-on-close>
      <el-form ref="assessmentFormRef" :model="assessmentForm" :rules="assessmentRules" label-width="96px">
        <el-form-item label="考核点编号" prop="pointCode">
          <el-input v-model="assessmentForm.pointCode" placeholder="例如 AP1" />
        </el-form-item>
        <el-form-item label="考核点名称" prop="pointName">
          <el-input v-model="assessmentForm.pointName" placeholder="请输入考核点名称" />
        </el-form-item>
        <el-form-item label="满分值" prop="fullScore">
          <el-input-number v-model="assessmentForm.fullScore" :min="1" :step="1" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="关联目标" prop="objectiveIds">
          <el-select v-model="assessmentForm.objectiveIds" multiple collapse-tags collapse-tags-tooltip style="width: 100%">
            <el-option
              v-for="objective in objectives"
              :key="objective.id"
              :label="`${objective.objCode} ${objective.objName || ''}`.trim()"
              :value="objective.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assessmentDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingAssessment" @click="submitAssessment">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadProps } from 'element-plus'
import { listCourses, listMyCourses } from '@/api/course'
import { useUserStore } from '@/stores/user'
import {
  createAssessmentPoint,
  checkObjectiveIndicatorWeights,
  createCourseObjective,
  deleteAssessmentPoint,
  deleteCourseObjective,
  downloadAssessmentPointTemplate,
  downloadCourseObjectiveTemplate,
  getAssessmentPoint,
  getCourseObjective,
  importAssessmentPointsFromExcel,
  importCourseObjectivesFromExcel,
  listAssessmentPoints,
  listAvailableIndicators,
  listCourseObjectives,
  listObjectiveIndicatorWeights,
  saveObjectiveIndicatorWeights,
  updateAssessmentPoint,
  updateCourseObjective
} from '@/api/syllabus'
import type {
  AssessmentPointCreateRequest,
  AssessmentPointUpdateRequest,
  AssessmentPointVO,
  CourseObjectiveCreateRequest,
  CourseObjectiveUpdateRequest,
  CourseObjectiveVO,
  CourseSimpleVO,
  IndicatorPointVO
} from '@/api/backend'

type WeightRow = {
  objectiveId: number
  objectiveCode: string
  weights: Record<number, number>
}

type ObjectiveRow = {
  id: number
  code: string
  name: string
  content: string
  raw: CourseObjectiveVO
}

type AssessmentRow = {
  id: number
  code: string
  name: string
  score: number | string
  objective: string
  updatedAt: string
  raw: AssessmentPointVO
}

const route = useRoute()
const loading = ref(false)
const courses = ref<CourseSimpleVO[]>([])
const currentCourseId = ref<number>()
const objectives = ref<CourseObjectiveVO[]>([])
const assessments = ref<AssessmentPointVO[]>([])
const indicators = ref<IndicatorPointVO[]>([])
const weightRows = ref<WeightRow[]>([])
const weightCheckLoading = ref(false)
const weightCheckValid = ref(true)
const weightCheckMessage = ref('当前课程指标点内部权重列合计应为 1.00。')
const weightCheckPendingItems = ref<Array<{ id: number; label: string }>>([])
const objectiveDialogVisible = ref(false)
const assessmentDialogVisible = ref(false)
const objectiveEditing = ref<CourseObjectiveVO>()
const assessmentEditing = ref<AssessmentPointVO>()
const savingObjective = ref(false)
const savingAssessment = ref(false)
const objectiveFormRef = ref<FormInstance>()
const assessmentFormRef = ref<FormInstance>()

const objectiveForm = reactive<CourseObjectiveCreateRequest>({
  courseId: 0,
  objCode: '',
  objName: '',
  objDesc: ''
})

const assessmentForm = reactive<AssessmentPointCreateRequest>({
  courseId: 0,
  pointCode: '',
  pointName: '',
  fullScore: 100,
  objectiveIds: []
})

const objectiveRules: FormRules<CourseObjectiveCreateRequest> = {
  objCode: [{ required: true, message: '请输入目标编号', trigger: 'blur' }],
  objName: [{ required: true, message: '请输入目标名称', trigger: 'blur' }]
}

// 课程目标导入
const objectiveImportVisible = ref(false)
const objectiveImportFileList = ref<UploadProps['fileList']>([])
const submittingObjectiveImport = ref(false)

// 考核点导入
const assessmentImportVisible = ref(false)
const assessmentImportFileList = ref<UploadProps['fileList']>([])
const submittingAssessmentImport = ref(false)

const assessmentRules: FormRules<AssessmentPointCreateRequest> = {
  pointCode: [{ required: true, message: '请输入考核点编号', trigger: 'blur' }],
  pointName: [{ required: true, message: '请输入考核点名称', trigger: 'blur' }],
  fullScore: [{ required: true, message: '请输入满分值', trigger: 'change' }],
  objectiveIds: [{ required: true, message: '请至少选择一个课程目标', trigger: 'change' }]
}

const currentCourseLabel = computed(() => {
  const course = courses.value.find((item) => item.id === currentCourseId.value)
  return course ? `${course.courseCode} - ${course.courseName}` : '未选择课程'
})
const routeCourseId = computed(() => {
  const raw = route.query.courseId
  const value = Array.isArray(raw) ? raw[0] : raw
  const numeric = Number(value)
  return Number.isFinite(numeric) && numeric > 0 ? numeric : undefined
})

const objectiveRows = computed<ObjectiveRow[]>(() =>
  objectives.value.map((objective) => ({
    id: objective.id,
    code: objective.objCode,
    name: objective.objName || '-',
    content: objective.objDesc || '-',
    raw: objective
  }))
)

const assessmentRows = computed<AssessmentRow[]>(() =>
  assessments.value.map((assessment) => ({
    id: assessment.id,
    code: assessment.pointCode,
    name: assessment.pointName,
    score: assessment.fullScore ?? '-',
    objective:
      assessment.objCode || assessment.objectives?.map((item: CourseObjectiveVO) => item.objCode).join(' / ') || '-',
    updatedAt: assessment.updateTime || assessment.createTime || '-',
    raw: assessment
  }))
)

const getColumnSum = (indicatorId: number) =>
  weightRows.value.reduce((sum, row) => sum + (Number(row.weights[indicatorId]) || 0), 0)

const isColumnOk = (indicatorId: number) => Math.abs(getColumnSum(indicatorId) - 1) <= 0.001

const buildWeightItems = () =>
  weightRows.value.flatMap((row) =>
    Object.entries(row.weights)
      .filter(([, value]) => Number(value) > 0)
      .map(([indicatorId, value]) => ({
        objectiveId: row.objectiveId,
        indicatorId: Number(indicatorId),
        innerWeight: Number(value)
      }))
  )

const updateWeightCheckState = async () => {
  if (!currentCourseId.value || !indicators.value.length) {
    weightCheckValid.value = true
    weightCheckMessage.value = '当前课程暂无指标点，可先继续维护课程目标和考核点。'
    weightCheckPendingItems.value = []
    return
  }

  weightCheckLoading.value = true
  try {
    const checkResult = await checkObjectiveIndicatorWeights(currentCourseId.value, buildWeightItems())
    weightCheckValid.value = Boolean(checkResult.valid)

    const pendingItems = indicators.value
      .map((indicator) => {
        const sum = Number(checkResult.indicatorWeightSumMap?.[indicator.id] ?? getColumnSum(indicator.id))
        return {
          id: indicator.id,
          label: `${indicator.indicatorCode} 当前合计 ${sum.toFixed(2)}`,
          sum
        }
      })
      .filter((item) => Math.abs(item.sum - 1) > 0.001)
      .map(({ id, label }) => ({
        id,
        label
      }))

    weightCheckPendingItems.value = pendingItems
    weightCheckMessage.value = checkResult.valid
      ? '当前内部贡献权重校验已通过，可以直接保存。'
      : pendingItems.length
        ? `当前还有 ${pendingItems.length} 个指标点权重未通过，建议先调整到列合计 1.00。`
        : '当前内部贡献权重还未通过校验，请继续调整各指标点列合计。'
  } catch (error) {
    weightCheckValid.value = false
    weightCheckPendingItems.value = []
    weightCheckMessage.value = error instanceof Error ? error.message : '权重校验结果读取失败'
  } finally {
    weightCheckLoading.value = false
  }
}

const resetObjectiveForm = () => {
  objectiveForm.courseId = currentCourseId.value ?? 0
  objectiveForm.objCode = ''
  objectiveForm.objName = ''
  objectiveForm.objDesc = ''
  objectiveFormRef.value?.clearValidate()
}

const resetAssessmentForm = () => {
  assessmentForm.courseId = currentCourseId.value ?? 0
  assessmentForm.pointCode = ''
  assessmentForm.pointName = ''
  assessmentForm.fullScore = 100
  assessmentForm.objectiveIds = []
  assessmentFormRef.value?.clearValidate()
}

const openObjectiveCreateDialog = () => {
  objectiveEditing.value = undefined
  resetObjectiveForm()
  objectiveDialogVisible.value = true
}

const openObjectiveEditDialog = async (objective: CourseObjectiveVO) => {
  try {
    const detail = await getCourseObjective(objective.id)
    objectiveEditing.value = detail
    objectiveForm.courseId = detail.courseId
    objectiveForm.objCode = detail.objCode
    objectiveForm.objName = detail.objName || ''
    objectiveForm.objDesc = detail.objDesc || ''
    objectiveDialogVisible.value = true
  } catch (error) {
    const message = error instanceof Error ? error.message : '课程目标详情加载失败'
    ElMessage.error(message)
  }
}

const openAssessmentCreateDialog = () => {
  assessmentEditing.value = undefined
  resetAssessmentForm()
  assessmentDialogVisible.value = true
}

const openAssessmentEditDialog = async (assessment: AssessmentPointVO) => {
  try {
    const detail = await getAssessmentPoint(assessment.id)
    assessmentEditing.value = detail
    assessmentForm.courseId = detail.courseId
    assessmentForm.pointCode = detail.pointCode
    assessmentForm.pointName = detail.pointName
    assessmentForm.fullScore = Number(detail.fullScore ?? 100)
    assessmentForm.objectiveIds = detail.objectiveIds?.length
      ? [...detail.objectiveIds]
      : detail.objectiveId
        ? [detail.objectiveId]
        : []
    assessmentDialogVisible.value = true
  } catch (error) {
    const message = error instanceof Error ? error.message : '考核点详情加载失败'
    ElMessage.error(message)
  }
}

const reloadCourseData = async () => {
  if (!currentCourseId.value) return

  loading.value = true
  try {
    const [objectivePage, assessmentPage, indicatorList, weightList] = await Promise.all([
      listCourseObjectives(currentCourseId.value),
      listAssessmentPoints(currentCourseId.value),
      listAvailableIndicators(currentCourseId.value),
      listObjectiveIndicatorWeights(currentCourseId.value)
    ])

    objectives.value = objectivePage.records
    assessments.value = assessmentPage.records
    indicators.value = indicatorList

    const rowMap = new Map<number, WeightRow>()
    for (const objective of objectives.value) {
      rowMap.set(objective.id, {
        objectiveId: objective.id,
        objectiveCode: objective.objCode,
        weights: Object.fromEntries(indicators.value.map((indicator) => [indicator.id, 0]))
      })
    }

    for (const item of weightList) {
      const row = rowMap.get(item.objectiveId)
      if (row) {
        row.weights[item.indicatorId] = Number(item.innerWeight ?? 0)
      }
    }

    weightRows.value = Array.from(rowMap.values())
    await updateWeightCheckState()
  } catch (error) {
    const message = error instanceof Error ? error.message : '课程大纲数据加载失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const submitObjective = async () => {
  const isValid = await objectiveFormRef.value?.validate().catch(() => false)
  if (!isValid || !currentCourseId.value) return

  savingObjective.value = true
  try {
    if (objectiveEditing.value) {
      await updateCourseObjective({
        id: objectiveEditing.value.id,
        courseId: currentCourseId.value,
        objCode: objectiveForm.objCode.trim(),
        objName: objectiveForm.objName.trim(),
        objDesc: objectiveForm.objDesc?.trim()
      } satisfies CourseObjectiveUpdateRequest)
      ElMessage.success('课程目标已更新')
    } else {
      await createCourseObjective({
        courseId: currentCourseId.value,
        objCode: objectiveForm.objCode.trim(),
        objName: objectiveForm.objName.trim(),
        objDesc: objectiveForm.objDesc?.trim()
      })
      ElMessage.success('课程目标已创建')
    }

    objectiveDialogVisible.value = false
    await reloadCourseData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '课程目标保存失败'
    ElMessage.error(message)
  } finally {
    savingObjective.value = false
  }
}

const submitAssessment = async () => {
  const isValid = await assessmentFormRef.value?.validate().catch(() => false)
  if (!isValid || !currentCourseId.value) return

  const payload = {
    courseId: currentCourseId.value,
    pointCode: assessmentForm.pointCode.trim(),
    pointName: assessmentForm.pointName.trim(),
    fullScore: Number(assessmentForm.fullScore),
    objectiveIds: [...(assessmentForm.objectiveIds || [])]
  }

  savingAssessment.value = true
  try {
    if (assessmentEditing.value) {
      await updateAssessmentPoint({
        id: assessmentEditing.value.id,
        ...payload
      } satisfies AssessmentPointUpdateRequest)
      ElMessage.success('考核点已更新')
    } else {
      await createAssessmentPoint(payload)
      ElMessage.success('考核点已创建')
    }

    assessmentDialogVisible.value = false
    await reloadCourseData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '考核点保存失败'
    ElMessage.error(message)
  } finally {
    savingAssessment.value = false
  }
}

const handleDeleteObjective = async (objective: CourseObjectiveVO) => {
  try {
    await ElMessageBox.confirm(`确认删除课程目标 ${objective.objCode} 吗？`, '删除确认', {
      type: 'warning'
    })
    await deleteCourseObjective(objective.id)
    ElMessage.success('课程目标已删除')
    await reloadCourseData()
  } catch (error) {
    if (error === 'cancel') return
    const message = error instanceof Error ? error.message : '课程目标删除失败'
    ElMessage.error(message)
  }
}

const handleDeleteAssessment = async (assessment: AssessmentPointVO) => {
  try {
    await ElMessageBox.confirm(`确认删除考核点 ${assessment.pointCode} 吗？`, '删除确认', {
      type: 'warning'
    })
    await deleteAssessmentPoint(assessment.id)
    ElMessage.success('考核点已删除')
    await reloadCourseData()
  } catch (error) {
    if (error === 'cancel') return
    const message = error instanceof Error ? error.message : '考核点删除失败'
    ElMessage.error(message)
  }
}

// ===== 课程目标批量导入 =====
const openObjectiveImportDialog = () => {
  objectiveImportFileList.value = []
  objectiveImportVisible.value = true
}

const handleObjectiveImportChange: UploadProps['onChange'] = (uploadFile, uploadFiles) => {
  const fileName = uploadFile.name.toLowerCase()
  if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
    ElMessage.warning('仅支持上传以 .xlsx 或 .xls 结尾的 Excel 文件')
    return
  }
  objectiveImportFileList.value = uploadFiles.slice(-1)
}

const handleObjectiveImportRemove: UploadProps['onRemove'] = () => {
  objectiveImportFileList.value = []
}

const handleDownloadObjectiveTemplate = async () => {
  try {
    const blob = await downloadCourseObjectiveTemplate()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '课程目标导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板已开始下载')
  } catch (error) {
    const message = error instanceof Error ? error.message : '模板下载失败'
    ElMessage.error(message)
  }
}

const submitObjectiveImport = async () => {
  const file = objectiveImportFileList.value?.[0]?.raw
  if (!file) {
    ElMessage.warning('请先选择要导入的 Excel 文件')
    return
  }

  submittingObjectiveImport.value = true
  try {
    const result = await importCourseObjectivesFromExcel(file as File)
    objectiveImportVisible.value = false
    objectiveImportFileList.value = []
    await reloadCourseData()
    await showImportResult(result as Record<string, unknown>)
  } catch (error) {
    const message = error instanceof Error ? error.message : '课程目标导入失败'
    ElMessage.error(message)
  } finally {
    submittingObjectiveImport.value = false
  }
}

// ===== 考核点批量导入 =====
const openAssessmentImportDialog = () => {
  assessmentImportFileList.value = []
  assessmentImportVisible.value = true
}

const handleAssessmentImportChange: UploadProps['onChange'] = (uploadFile, uploadFiles) => {
  const fileName = uploadFile.name.toLowerCase()
  if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
    ElMessage.warning('仅支持上传以 .xlsx 或 .xls 结尾的 Excel 文件')
    return
  }
  assessmentImportFileList.value = uploadFiles.slice(-1)
}

const handleAssessmentImportRemove: UploadProps['onRemove'] = () => {
  assessmentImportFileList.value = []
}

const handleDownloadAssessmentTemplate = async () => {
  try {
    const blob = await downloadAssessmentPointTemplate()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '考核点导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板已开始下载')
  } catch (error) {
    const message = error instanceof Error ? error.message : '模板下载失败'
    ElMessage.error(message)
  }
}

const submitAssessmentImport = async () => {
  const file = assessmentImportFileList.value?.[0]?.raw
  if (!file) {
    ElMessage.warning('请先选择要导入的 Excel 文件')
    return
  }

  submittingAssessmentImport.value = true
  try {
    const result = await importAssessmentPointsFromExcel(file as File)
    assessmentImportVisible.value = false
    assessmentImportFileList.value = []
    await reloadCourseData()
    await showImportResult(result as Record<string, unknown>)
  } catch (error) {
    const message = error instanceof Error ? error.message : '考核点导入失败'
    ElMessage.error(message)
  } finally {
    submittingAssessmentImport.value = false
  }
}

// 通用导入结果展示
const showImportResult = async (result: Record<string, unknown>) => {
  const failCount = Number(result.failCount || 0)
  if (failCount > 0) {
    const failDetails = result.failDetails as Array<Record<string, string>> | undefined
    const preview = (failDetails ?? [])
      .slice(0, 5)
      .map((item) => `第 ${item.row || '-'} 行：${item.reason || '导入失败'}`)
      .join('\n')

    await ElMessageBox.alert(
      `总计 ${result.total} 条，成功 ${result.successCount} 条，失败 ${failCount} 条。${preview ? `\n\n失败示例：\n${preview}` : ''}`,
      '导入完成',
      { confirmButtonText: '知道了' }
    )
  } else {
    ElMessage.success(`导入完成，共成功导入 ${result.successCount} 条`)
  }
}

const saveWeights = async () => {
  if (!currentCourseId.value) return

  const items = buildWeightItems()

  try {
    const checkResult = await checkObjectiveIndicatorWeights(currentCourseId.value, items)
    if (!checkResult.valid) {
      const indicatorSummary = indicators.value
        .map((indicator) => {
          const sum = checkResult.indicatorWeightSumMap?.[indicator.id] ?? getColumnSum(indicator.id).toFixed(2)
          return `${indicator.indicatorCode}=${sum}`
        })
        .join('，')

      ElMessage.warning(`各指标点内部权重合计必须为 1，当前校验未通过：${indicatorSummary}`)
      return
    }

    await saveObjectiveIndicatorWeights(currentCourseId.value, items)
    await updateWeightCheckState()
    ElMessage.success('内部贡献权重已保存')
  } catch (error) {
    const message = error instanceof Error ? error.message : '权重保存失败'
    ElMessage.error(message)
  }
}

onMounted(async () => {
  try {
    const userStore = useUserStore()
    courses.value = userStore.role === 'teacher' ? await listMyCourses() : await listCourses()
    const routeMatchedCourse = routeCourseId.value
      ? courses.value.find((item) => item.id === routeCourseId.value)
      : undefined
    currentCourseId.value = routeMatchedCourse?.id ?? courses.value[0]?.id
    await reloadCourseData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '课程列表加载失败'
    ElMessage.error(message)
  }
})
</script>

<style scoped>
.toolbar-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.import-tips {
  margin-bottom: 16px;
}

.import-tips p {
  margin: 0 0 8px;
  color: #606266;
  font-size: 14px;
}

.import-actions {
  margin-bottom: 8px;
}

.upload-icon {
  font-size: 48px;
  color: #c0c4cc;
}

.upload-title {
  margin-top: 12px;
  color: #606266;
  font-size: 14px;
}

.weight-footer {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  padding: 12px;
  border: 1px solid var(--line);
  border-top: 0;
  border-radius: 0 0 8px 8px;
  background: #fbfdff;
  font-weight: 700;
}

.weight-warning-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}
</style>
