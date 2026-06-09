<template>
  <div class="page">
    <h1 class="page-title">教学班与成绩准备</h1>
    <p class="page-desc">这一页把教学班、学生导入、成绩模板下载、成绩导入和成绩查询都接通，方便继续联调课程计算链路。</p>

    <section class="page-grid">
      <div class="panel span-12">
        <div class="toolbar">
          <div>
            <h3 class="panel-title">教学班管理</h3>
            <span class="muted">先维护教学班，再导入学生并绑定到当前班级。</span>
          </div>
          <div class="toolbar-actions">
            <el-input
              v-model="classKeyword"
              placeholder="按教学班名称搜索"
              style="width: 220px"
              clearable
              @keyup.enter="loadTeachingClasses"
            />
            <el-button @click="loadTeachingClasses">刷新</el-button>
            <el-button type="primary" @click="openClassCreateDialog">新增教学班</el-button>
          </div>
        </div>

        <el-table v-loading="classLoading" :data="teachingClasses" border @row-click="handleSelectClass">
          <el-table-column prop="className" label="教学班名称" min-width="180" />
          <el-table-column prop="courseName" label="课程" min-width="180" />
          <el-table-column prop="teacherName" label="授课教师" width="140" />
          <el-table-column label="学年学期" min-width="180">
            <template #default="{ row }">{{ formatTerm(row) }}</template>
          </el-table-column>
          <el-table-column prop="studentCount" label="学生数" width="90" />
          <el-table-column prop="updateTime" label="更新时间" min-width="180" />
          <el-table-column label="操作" width="210">
            <template #default="{ row }">
              <el-button link type="primary" @click.stop="openClassEditDialog(row)">编辑</el-button>
              <el-button link type="primary" @click.stop="handleSelectClass(row)">进入班级</el-button>
              <el-button link type="danger" @click.stop="handleDeleteClass(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="selectedClass" class="selected-class-bar">
          <el-tag type="success">当前教学班：{{ selectedClass.className }}</el-tag>
          <span>{{ selectedClass.courseName || '未绑定课程' }}</span>
          <span>{{ selectedClass.teacherName || '未分配教师' }}</span>
          <span>{{ formatTerm(selectedClass) }}</span>
        </div>
      </div>

      <div class="panel span-4">
        <h3 class="panel-title">学生与成绩导入</h3>

        <div class="import-block">
          <div class="section-title">学生导入</div>
          <el-alert
            type="info"
            show-icon
            :closable="false"
            title="当前支持三种方式：先导入系统库、直接导入当前教学班、按学号批量绑定。系统学生库列表暂未开放查询，所以当前还不能在页面中直接选人。"
            style="margin-bottom: 12px"
          />
          <el-upload
            v-model:file-list="studentFileList"
            drag
            action="#"
            :auto-upload="false"
            :limit="1"
            accept=".xlsx,.xls"
            :on-change="handleStudentFileChange"
            :on-remove="handleStudentFileRemove"
          >
            <el-icon class="upload-icon"><UploadFilled /></el-icon>
            <div class="upload-title">拖拽学生 Excel 到这里，或点击选择文件</div>
            <template #tip>
              <div class="muted">支持先导入系统库再绑定，也支持直接导入当前教学班。</div>
            </template>
          </el-upload>
          <div class="import-actions">
            <el-button type="primary" :loading="importingStudents" @click="submitStudentImport">导入系统库</el-button>
            <el-button :loading="downloadingTemplate" @click="handleDownloadTemplate">系统库模板</el-button>
            <el-button
              type="success"
              :disabled="!selectedClassId"
              :loading="importingStudentsToClass"
              @click="submitStudentImportToClass"
            >
              直接导入当前教学班
            </el-button>
            <el-button
              :disabled="!selectedClassId"
              :loading="downloadingClassTemplate"
              @click="handleDownloadClassTemplate"
            >
              教学班模板
            </el-button>
          </div>
          <el-alert
            v-if="lastImportSummary"
            type="success"
            show-icon
            :closable="false"
            :title="lastImportSummary"
            style="margin-top: 12px"
          />
        </div>

        <el-divider content-position="left">成绩导入</el-divider>

        <div class="import-block">
          <div class="section-title">当前教学班成绩模板</div>
          <div class="muted">先选择教学班，再下载专属成绩模板并回传成绩 Excel。</div>
          <el-upload
            v-model:file-list="gradeFileList"
            drag
            action="#"
            :auto-upload="false"
            :limit="1"
            accept=".xlsx,.xls"
            :on-change="handleGradeFileChange"
            :on-remove="handleGradeFileRemove"
            style="margin-top: 12px"
          >
            <el-icon class="upload-icon"><UploadFilled /></el-icon>
            <div class="upload-title">拖拽成绩 Excel 到这里，或点击选择文件</div>
            <template #tip>
              <div class="muted">模板会按当前教学班生成，导入后会自动刷新成绩预览。</div>
            </template>
          </el-upload>
          <div class="import-actions">
            <el-button
              type="primary"
              :disabled="!selectedClassId"
              :loading="gradeTemplateLoading"
              @click="handleDownloadGradeTemplate"
            >
              下载成绩模板
            </el-button>
            <el-button
              type="success"
              :disabled="!selectedClassId"
              :loading="gradeImporting"
              @click="submitGradeImport"
            >
              导入成绩
            </el-button>
          </div>
          <el-alert
            v-if="lastGradeImportSummary"
            type="success"
            show-icon
            :closable="false"
            :title="lastGradeImportSummary"
            style="margin-top: 12px"
          />
        </div>
      </div>

      <div class="panel span-8">
        <div class="toolbar">
          <div>
            <h3 class="panel-title">班级学生预览</h3>
            <span class="muted">如果学生已经在系统库中，优先用按学号批量绑定；如果还没进系统，再走 Excel 导入。</span>
          </div>
          <div class="toolbar-actions">
            <el-button type="primary" plain :disabled="!selectedClassId" @click="openBindDialog">按学号批量绑定</el-button>
            <el-button type="primary" plain :disabled="!selectedClassId" @click="loadGradeEntries()">刷新成绩</el-button>
          </div>
        </div>

        <el-table v-loading="loading" :data="previewRows" border>
          <el-table-column prop="sid" label="学号" width="130" />
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column prop="collegeName" label="学院" min-width="140" />
          <el-table-column prop="majorName" label="专业" min-width="140" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button
                link
                type="danger"
                :loading="unbindingStudentNo === row.sid"
                @click="handleUnbindStudent(row)"
              >
                移出
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel span-12">
        <div class="toolbar">
          <div>
            <h3 class="panel-title">成绩录入预览</h3>
            <span class="muted">按教学班查看已导入的成绩记录，可按考核点筛选、手动修改单条成绩，并清空当前班级成绩。</span>
          </div>
          <div class="toolbar-actions">
            <el-select v-model="gradeQuery.pointId" clearable placeholder="全部考核点" style="width: 220px">
              <el-option
                v-for="point in assessments"
                :key="point.id"
                :label="`${point.pointCode} ${point.pointName}`"
                :value="point.id"
              />
            </el-select>
            <el-button :disabled="!selectedClassId" :loading="gradeLoading" @click="loadGradeEntries(1)">查询成绩</el-button>
            <el-button
              type="danger"
              plain
              :disabled="!selectedClassId || !gradeRows.length"
              :loading="deletingGrades"
              @click="handleDeleteGrades"
            >
              清空当前班成绩
            </el-button>
          </div>
        </div>

        <el-table v-loading="gradeLoading" :data="gradeRows" border>
          <el-table-column prop="studentNo" label="学号" width="140" />
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="pointCode" label="考核点编号" min-width="140" />
          <el-table-column prop="pointName" label="考核点名称" min-width="220" />
          <el-table-column prop="score" label="得分" width="100" />
          <el-table-column prop="fullScore" label="满分" width="100" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" @click="openGradeEditDialog(row)">手动修改</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="table-footer">
          <span class="muted">{{ gradeTableSummary }}</span>
          <el-pagination
            :current-page="gradeQuery.current || 1"
            :page-size="gradeQuery.pageSize || 20"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            :total="gradeTotal"
            @current-change="handleGradeCurrentChange"
            @size-change="handleGradeSizeChange"
          />
        </div>
      </div>

      <div class="panel span-12">
        <h3 class="panel-title">课程级计算准备状态</h3>
        <el-table v-loading="loading" :data="results" border>
          <el-table-column prop="objective" label="课程目标" min-width="180" />
          <el-table-column prop="indicator" label="支撑指标点" min-width="220" />
          <el-table-column prop="assessmentCount" label="考核点数" width="110" />
          <el-table-column prop="studentCount" label="学生数" width="100" />
          <el-table-column prop="status" label="状态" width="160" />
          <el-table-column prop="hint" label="说明" min-width="220" />
        </el-table>
      </div>
    </section>

    <el-dialog v-model="classDialogVisible" :title="classEditing ? '编辑教学班' : '新增教学班'" width="560px" destroy-on-close>
      <el-form ref="classFormRef" :model="classForm" :rules="classRules" label-width="96px">
        <el-form-item label="教学班名称" prop="className">
          <el-input v-model="classForm.className" placeholder="请输入教学班名称" />
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="classForm.courseId" style="width: 100%">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="`${course.courseCode} - ${course.courseName}`"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherId">
          <el-select v-model="classForm.teacherId" style="width: 100%">
            <el-option v-for="teacher in teachers" :key="teacher.id" :label="teacher.username" :value="teacher.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学年学期" prop="termId">
          <el-select v-model="classForm.termId" style="width: 100%">
            <el-option
              v-for="term in schoolYears"
              :key="term.id"
              :label="`${term.yearName} / ${term.semesterName}`"
              :value="term.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="classDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingClass" @click="submitClass">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="bindDialogVisible" title="按学号绑定教学班" width="560px" destroy-on-close>
      <el-alert
        type="info"
        show-icon
        :closable="false"
        title="这里适合绑定已经存在于系统学生库中的学生。若学生还没导入系统，请先使用左侧的系统库导入或直接导入当前教学班。"
        style="margin-bottom: 12px"
      />
      <p class="muted dialog-tip">每行一名学生，支持 `学号` 或 `学号,姓名` 两种格式。</p>
      <el-input
        v-model="bindStudentText"
        type="textarea"
        :rows="10"
        placeholder="例如：&#10;20230001,张三&#10;20230002,李四"
      />
      <div class="muted bind-preview">当前预计绑定 {{ bindStudentPreviewCount }} 名学生，提交后会自动刷新当前教学班学生列表。</div>
      <template #footer>
        <el-button @click="bindDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="bindingStudents" @click="submitBindStudents">绑定到当前教学班</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="gradeEditDialogVisible" title="手动修改成绩" width="520px" destroy-on-close>
      <el-alert
        type="info"
        show-icon
        :closable="false"
        title="这里适合微调单条成绩。提交后会直接调用后端真实更新接口，并刷新当前成绩列表。"
        style="margin-bottom: 16px"
      />
      <el-form label-width="92px">
        <el-form-item label="学号">
          <el-input :model-value="gradeEditForm.studentNo" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input :model-value="gradeEditForm.studentName" disabled />
        </el-form-item>
        <el-form-item label="考核点">
          <el-input :model-value="gradeEditForm.pointLabel" disabled />
        </el-form-item>
        <el-form-item label="满分">
          <el-input :model-value="String(gradeEditForm.fullScore ?? '-')" disabled />
        </el-form-item>
        <el-form-item label="得分">
          <el-input-number
            v-model="gradeEditForm.score"
            :min="0"
            :max="gradeEditForm.fullScore ?? undefined"
            :precision="2"
            :step="1"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="gradeEditDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingGradeEdit" @click="submitGradeEdit">保存成绩</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, UploadProps, UploadUserFile } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { listUsersByRole } from '@/api/auth'
import { listCourses } from '@/api/course'
import { deleteClassGrades, downloadGradeTemplate, importGrades, queryGrades, updateGrades } from '@/api/grade-entry'
import { listSchoolYears } from '@/api/schoolyear'
import {
  createTeachingClass,
  deleteTeachingClass,
  downloadClassStudentTemplate,
  downloadStudentTemplate,
  getTeachingClass,
  getTeachingClassStudents,
  importStudentsFromExcel,
  importStudentsToClassFromExcel,
  importStudentsToClass,
  pageTeachingClasses,
  unbindStudentFromClass,
  updateTeachingClass
} from '@/api/teaching-class'
import { listAssessmentPoints, listAvailableIndicators, listCourseObjectives } from '@/api/syllabus'
import type {
  AssessmentPointVO,
  CourseObjectiveVO,
  CourseSimpleVO,
  GradeEntryQueryRequest,
  GradeImportResultVO,
  IndicatorPointVO,
  StudentImportResult,
  StudentScoreVO,
  StudentVO,
  SysDictSchoolYearVO,
  SysUserVO,
  TeachingClassCreateRequest,
  TeachingClassUpdateRequest,
  TeachingClassVO
} from '@/api/backend'

type ClassFormState = TeachingClassCreateRequest

const loading = ref(false)
const route = useRoute()
const classLoading = ref(false)
const importingStudents = ref(false)
const importingStudentsToClass = ref(false)
const bindingStudents = ref(false)
const downloadingTemplate = ref(false)
const downloadingClassTemplate = ref(false)
const gradeImporting = ref(false)
const gradeTemplateLoading = ref(false)
const gradeLoading = ref(false)
const deletingGrades = ref(false)
const savingClass = ref(false)
const savingGradeEdit = ref(false)
const unbindingStudentNo = ref<string>()
const bindDialogVisible = ref(false)
const classDialogVisible = ref(false)
const gradeEditDialogVisible = ref(false)
const classEditing = ref<TeachingClassVO>()
const selectedClassId = ref<number>()
const selectedClassDetail = ref<TeachingClassVO>()
const classKeyword = ref('')
const teachingClasses = ref<TeachingClassVO[]>([])
const students = ref<StudentVO[]>([])
const objectives = ref<CourseObjectiveVO[]>([])
const assessments = ref<AssessmentPointVO[]>([])
const indicators = ref<IndicatorPointVO[]>([])
const courses = ref<CourseSimpleVO[]>([])
const teachers = ref<SysUserVO[]>([])
const schoolYears = ref<SysDictSchoolYearVO[]>([])
const studentFileList = ref<UploadUserFile[]>([])
const gradeFileList = ref<UploadUserFile[]>([])
const lastImportResult = ref<StudentImportResult>()
const lastGradeImportResult = ref<GradeImportResultVO>()
const bindStudentText = ref('')
const gradeRows = ref<StudentScoreVO[]>([])
const gradeTotal = ref(0)
const classFormRef = ref<FormInstance>()
const gradeQuery = reactive<GradeEntryQueryRequest>({
  current: 1,
  pageSize: 20
})

const gradeEditForm = reactive<{
  id?: number
  studentId: number
  studentNo: string
  studentName: string
  pointId: number
  pointLabel: string
  fullScore?: number
  score?: number
}>({
  id: undefined,
  studentId: 0,
  studentNo: '',
  studentName: '',
  pointId: 0,
  pointLabel: '',
  fullScore: undefined,
  score: undefined
})

const classForm = reactive<ClassFormState>({
  className: '',
  courseId: 0,
  teacherId: 0,
  termId: 0
})

const classRules: FormRules<ClassFormState> = {
  className: [{ required: true, message: '请输入教学班名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择授课教师', trigger: 'change' }],
  termId: [{ required: true, message: '请选择学年学期', trigger: 'change' }]
}

const selectedClass = computed(() => selectedClassDetail.value ?? teachingClasses.value.find((item) => item.id === selectedClassId.value))

const lastImportSummary = computed(() => {
  const result = lastImportResult.value
  if (!result) return ''
  const total = result.total ?? result.totalCount ?? result.successCount + result.failCount
  return `最近一次学生导入：总计 ${total} 条，成功 ${result.successCount} 条，失败 ${result.failCount} 条。`
})

const lastGradeImportSummary = computed(() => {
  const result = lastGradeImportResult.value
  if (!result) return ''

  const summary = [`最近一次成绩导入：学生 ${result.studentCount ?? 0} 名，成绩 ${result.scoreCount ?? 0} 条。`]
  if (result.warningMessages?.length) {
    summary.push(`警告 ${result.warningMessages.length} 条`)
  }
  if (result.errorMessages?.length) {
    summary.push(`错误 ${result.errorMessages.length} 条`)
  }
  return summary.join(' ')
})

const previewRows = computed(() =>
  students.value.map((student) => ({
    sid: student.studentNo,
    name: student.name,
    collegeName: student.collegeName || '-',
    majorName: student.majorName || '-'
  }))
)

const bindStudentPreviewCount = computed(() =>
  bindStudentText.value
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean).length
)

const results = computed(() => {
  if (!selectedClass.value) {
    return []
  }

  if (!objectives.value.length) {
    return [
      {
        objective: selectedClass.value.courseName || '当前课程',
        indicator: '待配置课程目标',
        assessmentCount: 0,
        studentCount: students.value.length,
        status: '待配置',
        hint: '当前课程还没有课程目标，暂时无法继续做课程级计算准备。'
      }
    ]
  }

  return objectives.value.map((objective, index) => {
    const indicator = indicators.value[index] ?? indicators.value[0]
    return {
      objective: `${objective.objCode} ${objective.objName || ''}`.trim(),
      indicator: indicator ? `${indicator.indicatorCode} ${indicator.indicatorName}` : '待关联指标点',
      assessmentCount: assessments.value.filter((item) =>
        item.objectiveIds?.includes(objective.id) || item.objectiveId === objective.id
      ).length,
      studentCount: students.value.length,
      status: assessments.value.length ? (gradeRows.value.length ? '已有成绩数据' : '待录入成绩') : '待配置考核点',
      hint: assessments.value.length
        ? gradeRows.value.length
          ? '成绩录入与查询已接通，可继续联调课程计算和报表。'
          : '成绩录入与查询已接通，等待教师导入当前教学班成绩。'
        : '先补课程考核点，再进入成绩计算。'
    }
  })
})

const gradeTableSummary = computed(() => {
  if (!selectedClassId.value) {
    return '请先选择教学班后再查看成绩。'
  }
  if (!gradeTotal.value) {
    return '当前教学班还没有已导入的成绩记录。'
  }
  return `当前共 ${gradeTotal.value} 条成绩记录，本页展示 ${gradeRows.value.length} 条。`
})

const routeClassId = computed(() => {
  const raw = route.query.classId
  const value = Array.isArray(raw) ? raw[0] : raw
  const numeric = Number(value)
  return Number.isFinite(numeric) && numeric > 0 ? numeric : undefined
})

const formatTerm = (row: Pick<TeachingClassVO, 'yearName' | 'semesterName'>) =>
  row.yearName || row.semesterName ? `${row.yearName || '-'} / ${row.semesterName || '-'}` : '-'

const resetClassForm = () => {
  classForm.className = ''
  classForm.courseId = 0
  classForm.teacherId = 0
  classForm.termId = 0
  classFormRef.value?.clearValidate()
}

const openClassCreateDialog = async () => {
  classEditing.value = undefined
  resetClassForm()
  await ensureClassFormOptions()
  classDialogVisible.value = true
}

const openClassEditDialog = async (row: TeachingClassVO) => {
  classEditing.value = row
  await ensureClassFormOptions()
  classForm.className = row.className
  classForm.courseId = Number(row.courseId || 0)
  classForm.teacherId = Number(row.teacherId || 0)
  classForm.termId = Number(row.termId || 0)
  classDialogVisible.value = true
}

const ensureClassFormOptions = async () => {
  const taskEntries: Array<{
    label: string
    promise: Promise<void>
  }> = []

  if (!courses.value.length) {
    taskEntries.push({
      label: '课程列表',
      promise: listCourses().then((result) => {
        courses.value = result
      })
    })
  }
  if (!teachers.value.length) {
    taskEntries.push({
      label: '教师列表',
      promise: listUsersByRole('teacher').then((result) => {
        teachers.value = result
      })
    })
  }
  if (!schoolYears.value.length) {
    taskEntries.push({
      label: '学年学期列表',
      promise: listSchoolYears().then((result) => {
        schoolYears.value = result
      })
    })
  }

  if (!taskEntries.length) {
    return
  }

  const loadResults = await Promise.allSettled(taskEntries.map((item) => item.promise))
  const failedLabels: string[] = []

  loadResults.forEach((result, index) => {
    if (result.status === 'rejected') {
      failedLabels.push(taskEntries[index].label)
    }
  })

  if (failedLabels.length) {
    ElMessage.warning(`弹窗已打开，但以下数据加载失败：${failedLabels.join('、')}。可刷新后重试。`)
  }

  if (!courses.value.length) {
    ElMessage.warning('当前没有可选课程，请先检查课程数据。')
  }
  if (!teachers.value.length) {
    ElMessage.warning('当前没有可选教师，请先检查教师账号数据。')
  }
  if (!schoolYears.value.length) {
    ElMessage.warning('当前没有可选学年学期，请先检查学年学期数据。')
  }
}

const handleStudentFileChange: UploadProps['onChange'] = (uploadFile, uploadFiles) => {
  const fileName = uploadFile.name.toLowerCase()
  if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
    ElMessage.error('请上传 Excel 文件')
    studentFileList.value = []
    return
  }
  studentFileList.value = uploadFiles.slice(-1)
}

const handleStudentFileRemove: UploadProps['onRemove'] = () => {
  studentFileList.value = []
}

const handleGradeFileChange: UploadProps['onChange'] = (uploadFile, uploadFiles) => {
  const fileName = uploadFile.name.toLowerCase()
  if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
    ElMessage.error('请上传 Excel 文件')
    gradeFileList.value = []
    return
  }
  gradeFileList.value = uploadFiles.slice(-1)
}

const handleGradeFileRemove: UploadProps['onRemove'] = () => {
  gradeFileList.value = []
}

const showImportResult = async (result: StudentImportResult, title: string) => {
  if (result.failCount > 0) {
    const preview = (result.failDetails ?? [])
      .slice(0, 5)
      .map((item) => `第 ${item.row || '-'} 行：${item.reason || '导入失败'}`)
      .join('\n')
    await ElMessageBox.alert(
      `${title}\n总计 ${result.total ?? result.totalCount ?? result.successCount + result.failCount} 条，成功 ${result.successCount} 条，失败 ${result.failCount} 条。${preview ? `\n\n失败示例：\n${preview}` : ''}`,
      '导入完成',
      {
        confirmButtonText: '知道了'
      }
    )
  } else {
    ElMessage.success(`${title}，成功 ${result.successCount} 条`)
  }
}

const showGradeImportResult = async (result: GradeImportResultVO) => {
  const details: string[] = [`导入学生数：${result.studentCount ?? 0}`, `成绩记录数：${result.scoreCount ?? 0}`]

  if (result.warningMessages?.length) {
    details.push('', '警告信息：', ...result.warningMessages.slice(0, 8))
  }
  if (result.errorMessages?.length) {
    details.push('', '错误信息：', ...result.errorMessages.slice(0, 8))
  }

  if (result.warningMessages?.length || result.errorMessages?.length || result.success === false) {
    await ElMessageBox.alert(details.join('\n'), '成绩导入结果', {
      confirmButtonText: '知道了'
    })
    return
  }

  ElMessage.success(`成绩导入完成，已写入 ${result.scoreCount ?? 0} 条成绩记录`)
}

const readFileAsBase64 = (file: File) =>
  new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const content = typeof reader.result === 'string' ? reader.result : ''
      resolve(content.includes(',') ? content.split(',')[1] : content)
    }
    reader.onerror = () => reject(new Error('读取 Excel 文件失败'))
    reader.readAsDataURL(file)
  })

const downloadBlob = (blob: Blob, fileName: string) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.click()
  window.URL.revokeObjectURL(url)
}

const loadCourseDetails = async (courseId?: number) => {
  objectives.value = []
  assessments.value = []
  indicators.value = []

  if (!courseId) return

  const [objectiveResult, assessmentResult, indicatorResult] = await Promise.allSettled([
    listCourseObjectives(courseId),
    listAssessmentPoints(courseId),
    listAvailableIndicators(courseId)
  ])

  if (objectiveResult.status === 'fulfilled') {
    objectives.value = objectiveResult.value.records
  }
  if (assessmentResult.status === 'fulfilled') {
    assessments.value = assessmentResult.value.records
  }
  if (indicatorResult.status === 'fulfilled') {
    indicators.value = indicatorResult.value
  }
}

const loadTeachingClasses = async () => {
  classLoading.value = true
  try {
    const page = await pageTeachingClasses({
      current: 1,
      pageSize: 200,
      className: classKeyword.value || undefined
    })
    teachingClasses.value = page.records

    if (selectedClassId.value && !teachingClasses.value.some((item) => item.id === selectedClassId.value)) {
      selectedClassId.value = undefined
      selectedClassDetail.value = undefined
      students.value = []
      objectives.value = []
      assessments.value = []
      indicators.value = []
      gradeRows.value = []
      gradeTotal.value = 0
    }

    const routeMatchedClass = routeClassId.value
      ? teachingClasses.value.find((item) => item.id === routeClassId.value)
      : undefined

    if (routeMatchedClass) {
      selectedClassId.value = routeMatchedClass.id
      await reloadPreview()
      return
    }

    if (!selectedClassId.value && teachingClasses.value.length) {
      selectedClassId.value = teachingClasses.value[0].id
      await reloadPreview()
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '教学班列表加载失败'
    ElMessage.error(message)
  } finally {
    classLoading.value = false
  }
}

const handleSelectClass = async (row: TeachingClassVO) => {
  selectedClassId.value = row.id
  await reloadPreview()
}

const submitClass = async () => {
  const isValid = await classFormRef.value?.validate().catch(() => false)
  if (!isValid) return

  savingClass.value = true
  try {
    if (classEditing.value) {
      await updateTeachingClass({
        id: classEditing.value.id,
        className: classForm.className.trim(),
        courseId: Number(classForm.courseId),
        teacherId: Number(classForm.teacherId),
        termId: Number(classForm.termId)
      } satisfies TeachingClassUpdateRequest)
      ElMessage.success('教学班已更新')
    } else {
      await createTeachingClass({
        className: classForm.className.trim(),
        courseId: Number(classForm.courseId),
        teacherId: Number(classForm.teacherId),
        termId: Number(classForm.termId)
      } satisfies TeachingClassCreateRequest)
      ElMessage.success('教学班已创建')
    }

    classDialogVisible.value = false
    await loadTeachingClasses()
  } catch (error) {
    const message = error instanceof Error ? error.message : '教学班保存失败'
    ElMessage.error(message)
  } finally {
    savingClass.value = false
  }
}

const handleDeleteClass = async (row: TeachingClassVO) => {
  try {
    await ElMessageBox.confirm(`确认删除教学班 ${row.className} 吗？`, '删除确认', {
      type: 'warning'
    })
    await deleteTeachingClass(row.id)
    if (selectedClassId.value === row.id) {
      selectedClassId.value = undefined
      selectedClassDetail.value = undefined
      students.value = []
      objectives.value = []
      assessments.value = []
      indicators.value = []
      gradeRows.value = []
      gradeTotal.value = 0
    }
    ElMessage.success('教学班已删除')
    await loadTeachingClasses()
  } catch (error) {
    if (error === 'cancel') return
    const message = error instanceof Error ? error.message : '教学班删除失败'
    ElMessage.error(message)
  }
}

const handleDownloadTemplate = async () => {
  downloadingTemplate.value = true
  try {
    const blob = await downloadStudentTemplate()
    downloadBlob(blob, '学生导入模板.xlsx')
    ElMessage.success('学生模板已开始下载')
  } catch (error) {
    const message = error instanceof Error ? error.message : '模板下载失败'
    ElMessage.error(message)
  } finally {
    downloadingTemplate.value = false
  }
}

const handleDownloadClassTemplate = async () => {
  if (!selectedClassId.value) {
    ElMessage.warning('请先选择教学班')
    return
  }

  downloadingClassTemplate.value = true
  try {
    const blob = await downloadClassStudentTemplate()
    const fileName = `${selectedClass.value?.className || '教学班学生'}-导入模板.xlsx`
    downloadBlob(blob, fileName)
    ElMessage.success('教学班学生模板已开始下载')
  } catch (error) {
    const message = error instanceof Error ? error.message : '教学班模板下载失败'
    ElMessage.error(message)
  } finally {
    downloadingClassTemplate.value = false
  }
}

const handleDownloadGradeTemplate = async () => {
  if (!selectedClassId.value) {
    ElMessage.warning('请先选择教学班')
    return
  }

  gradeTemplateLoading.value = true
  try {
    const blob = await downloadGradeTemplate(selectedClassId.value)
    const fileName = `${selectedClass.value?.className || '成绩录入'}-模板.xlsx`
    downloadBlob(blob, fileName)
    ElMessage.success('成绩模板已开始下载')
  } catch (error) {
    const message = error instanceof Error ? error.message : '成绩模板下载失败'
    ElMessage.error(message)
  } finally {
    gradeTemplateLoading.value = false
  }
}

const submitStudentImport = async () => {
  const file = studentFileList.value[0]?.raw
  if (!file) {
    ElMessage.warning('请先选择学生 Excel 文件')
    return
  }

  importingStudents.value = true
  try {
    const result = await importStudentsFromExcel(file)
    lastImportResult.value = result
    studentFileList.value = []
    ElMessage.success('学生已导入系统库，可继续按学号绑定到当前教学班')
    await showImportResult(result, '学生导入系统库完成')
  } catch (error) {
    const message = error instanceof Error ? error.message : '学生导入失败'
    ElMessage.error(message)
  } finally {
    importingStudents.value = false
  }
}

const submitStudentImportToClass = async () => {
  if (!selectedClassId.value) {
    ElMessage.warning('请先选择教学班')
    return
  }

  const file = studentFileList.value[0]?.raw
  if (!file) {
    ElMessage.warning('请先选择学生 Excel 文件')
    return
  }

  importingStudentsToClass.value = true
  try {
    const result = await importStudentsToClassFromExcel(selectedClassId.value, file)
    lastImportResult.value = result
    studentFileList.value = []
    await reloadPreview()
    await showImportResult(result, '教学班学生 Excel 导入完成')
    if (result.successCount > 0) {
      ElMessage.success(`当前教学班已新增 ${result.successCount} 名学生`)
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '教学班学生导入失败'
    ElMessage.error(message)
  } finally {
    importingStudentsToClass.value = false
  }
}

const submitGradeImport = async () => {
  if (!selectedClassId.value) {
    ElMessage.warning('请先选择教学班')
    return
  }

  const file = gradeFileList.value[0]?.raw
  if (!file) {
    ElMessage.warning('请先选择成绩 Excel 文件')
    return
  }

  gradeImporting.value = true
  try {
    const excelFile = await readFileAsBase64(file)
    const result = await importGrades({
      classId: selectedClassId.value,
      excelFile
    })
    lastGradeImportResult.value = result
    gradeFileList.value = []
    await loadGradeEntries(1)
    await showGradeImportResult(result)
  } catch (error) {
    const message = error instanceof Error ? error.message : '成绩导入失败'
    ElMessage.error(message)
  } finally {
    gradeImporting.value = false
  }
}

const openBindDialog = () => {
  if (!selectedClassId.value) {
    ElMessage.warning('请先选择教学班')
    return
  }
  bindStudentText.value = ''
  bindDialogVisible.value = true
}

const submitBindStudents = async () => {
  if (!selectedClassId.value) {
    ElMessage.warning('请先选择教学班')
    return
  }

  const lines = bindStudentText.value
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)

  if (!lines.length) {
    ElMessage.warning('请先输入要绑定的学生学号')
    return
  }

  const parsedStudents = lines.map((line) => {
    const [studentNo, studentName] = line.split(/[,\uff0c\t]/).map((item) => item?.trim() || '')
    return {
      studentNo,
      studentName
    }
  })

  if (parsedStudents.some((item) => !item.studentNo)) {
    ElMessage.warning('存在空学号，请检查输入内容')
    return
  }

  bindingStudents.value = true
  try {
    const result = await importStudentsToClass(selectedClassId.value, parsedStudents)
    bindDialogVisible.value = false
    bindStudentText.value = ''
    await reloadPreview()
    await showImportResult(result, '教学班学生绑定完成')
    if (result.successCount > 0) {
      ElMessage.success(`当前教学班已新增 ${result.successCount} 名学生`)
    } else {
      ElMessage.warning('本次没有新增学生，请检查学号是否已经绑定，或学生是否还未导入系统库')
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '学生绑定失败'
    ElMessage.error(message)
  } finally {
    bindingStudents.value = false
  }
}

const handleUnbindStudent = async (row: { sid: string; name: string }) => {
  if (!selectedClassId.value) {
    ElMessage.warning('请先选择教学班')
    return
  }

  const targetStudent = students.value.find((student) => student.studentNo === row.sid)
  if (!targetStudent?.id) {
    ElMessage.warning('未找到要移出的学生')
    return
  }

  try {
    await ElMessageBox.confirm(`确认将 ${row.name}（${row.sid}）移出当前教学班吗？`, '移出确认', {
      type: 'warning'
    })

    unbindingStudentNo.value = row.sid
    await unbindStudentFromClass(selectedClassId.value, targetStudent.id)
    await reloadPreview()
    ElMessage.success('学生已移出当前教学班')
  } catch (error) {
    if (error === 'cancel') return
    const message = error instanceof Error ? error.message : '移出学生失败'
    ElMessage.error(message)
  } finally {
    unbindingStudentNo.value = undefined
  }
}

const loadGradeEntries = async (page = gradeQuery.current || 1) => {
  if (!selectedClassId.value) {
    gradeRows.value = []
    gradeTotal.value = 0
    return
  }

  gradeLoading.value = true
  try {
    const result = await queryGrades({
      classId: selectedClassId.value,
      pointId: gradeQuery.pointId,
      current: page,
      pageSize: gradeQuery.pageSize
    })
    gradeRows.value = result.records
    gradeTotal.value = result.total
    gradeQuery.current = result.current
    gradeQuery.pageSize = result.size
  } catch (error) {
    const message = error instanceof Error ? error.message : '成绩数据加载失败'
    ElMessage.error(message)
  } finally {
    gradeLoading.value = false
  }
}

const handleGradeCurrentChange = async (page: number) => {
  gradeQuery.current = page
  await loadGradeEntries(page)
}

const handleGradeSizeChange = async (size: number) => {
  gradeQuery.pageSize = size
  gradeQuery.current = 1
  await loadGradeEntries(1)
}

const handleDeleteGrades = async () => {
  if (!selectedClassId.value || !selectedClass.value) {
    ElMessage.warning('请先选择教学班')
    return
  }

  try {
    await ElMessageBox.confirm(`确认清空教学班 ${selectedClass.value.className} 的全部成绩吗？`, '清空确认', {
      type: 'warning'
    })

    deletingGrades.value = true
    await deleteClassGrades(selectedClassId.value)
    gradeRows.value = []
    gradeTotal.value = 0
    lastGradeImportResult.value = undefined
    ElMessage.success('当前教学班成绩已清空')
    await loadGradeEntries(1)
  } catch (error) {
    if (error === 'cancel') return
    const message = error instanceof Error ? error.message : '清空成绩失败'
    ElMessage.error(message)
  } finally {
    deletingGrades.value = false
  }
}

const openGradeEditDialog = (row: StudentScoreVO) => {
  gradeEditForm.id = row.id
  gradeEditForm.studentId = row.studentId
  gradeEditForm.studentNo = row.studentNo
  gradeEditForm.studentName = row.name
  gradeEditForm.pointId = row.pointId
  gradeEditForm.pointLabel = [row.pointCode, row.pointName].filter(Boolean).join(' ').trim() || `考核点 ${row.pointId}`
  gradeEditForm.fullScore = row.fullScore ?? undefined
  gradeEditForm.score = row.score == null ? undefined : Number(row.score)
  gradeEditDialogVisible.value = true
}

const submitGradeEdit = async () => {
  if (!selectedClassId.value) {
    ElMessage.warning('请先选择教学班')
    return
  }

  if (gradeEditForm.score == null) {
    ElMessage.warning('请先填写成绩')
    return
  }

  if (gradeEditForm.fullScore != null && gradeEditForm.score > gradeEditForm.fullScore) {
    ElMessage.warning(`得分不能超过满分 ${gradeEditForm.fullScore}`)
    return
  }

  savingGradeEdit.value = true
  try {
    await updateGrades({
      classId: selectedClassId.value,
      scores: [
        {
          id: gradeEditForm.id,
          studentId: gradeEditForm.studentId,
          pointId: gradeEditForm.pointId,
          score: gradeEditForm.score
        }
      ]
    })
    gradeEditDialogVisible.value = false
    ElMessage.success('成绩已更新')
    await loadGradeEntries(gradeQuery.current || 1)
  } catch (error) {
    const message = error instanceof Error ? error.message : '成绩更新失败'
    ElMessage.error(message)
  } finally {
    savingGradeEdit.value = false
  }
}

const reloadPreview = async () => {
  const classId = selectedClassId.value
  if (!classId) {
    gradeRows.value = []
    gradeTotal.value = 0
    return
  }

  loading.value = true
  try {
    selectedClassDetail.value = await getTeachingClass(classId)
    const current = selectedClassDetail.value
    students.value = await getTeachingClassStudents(current.id)
    await loadCourseDetails(current.courseId)
    if (gradeQuery.pointId && !assessments.value.some((item) => item.id === gradeQuery.pointId)) {
      gradeQuery.pointId = undefined
    }
    gradeQuery.current = 1
    await loadGradeEntries(1)
  } catch (error) {
    const message = error instanceof Error ? error.message : '班级预览数据加载失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await Promise.allSettled([
    listCourses().then((result) => {
      courses.value = result
    }),
    listUsersByRole('teacher').then((result) => {
      teachers.value = result
    }),
    listSchoolYears().then((result) => {
      schoolYears.value = result
    })
  ])

  await loadTeachingClasses()
})
</script>

<style scoped>
.toolbar-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.upload-icon {
  margin-top: 10px;
  color: var(--primary);
  font-size: 40px;
}

.upload-title {
  margin: 8px 0;
  color: #1e3555;
  font-weight: 700;
}

.import-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.import-actions {
  display: flex;
  gap: 10px;
  margin-top: 14px;
  flex-wrap: wrap;
}

.dialog-tip {
  margin: 0 0 12px;
}

.bind-preview {
  margin-top: 10px;
  line-height: 1.5;
}

.section-title {
  color: #1e3555;
  font-weight: 700;
}

.selected-class-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 14px;
  color: #4c5f79;
}

.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-top: 16px;
  flex-wrap: wrap;
}
</style>
