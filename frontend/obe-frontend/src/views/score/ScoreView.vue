<template>
  <div class="page">
    <h1 class="page-title">教学班与成绩准备</h1>
    <p class="page-desc">这一页先把后端现有的教学班、学生绑定、课程目标和指标点接口全部接通，方便继续做成绩与计算链路。</p>

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
        <h3 class="panel-title">学生导入</h3>
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
            <div class="muted">先导入学生到系统库，再把导入成功的学号批量绑定到当前教学班。</div>
          </template>
        </el-upload>
        <div class="import-actions">
          <el-button type="primary" :loading="importingStudents" @click="submitStudentImport">导入学生</el-button>
          <el-button :loading="downloadingTemplate" @click="handleDownloadTemplate">下载模板</el-button>
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

      <div class="panel span-8">
        <div class="toolbar">
          <h3 class="panel-title">班级学生预览</h3>
          <div class="toolbar-actions">
            <el-button type="primary" plain :disabled="!selectedClassId" @click="openBindDialog">按学号批量绑定</el-button>
            <el-button type="primary" :disabled="!previewRows.length" @click="locked = true">保留计算入口</el-button>
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

        <el-alert
          v-if="locked"
          title="成绩明细导入与正式计算接口还没补齐，当前先保留班级与学生准备链路。"
          type="success"
          show-icon
          style="margin-top: 12px"
        />
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
      <p class="muted dialog-tip">每行一名学生，支持 `学号` 或 `学号,姓名` 两种格式。</p>
      <el-input
        v-model="bindStudentText"
        type="textarea"
        :rows="10"
        placeholder="例如：&#10;20230001,张三&#10;20230002,李四"
      />
      <template #footer>
        <el-button @click="bindDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="bindingStudents" @click="submitBindStudents">绑定到当前教学班</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, UploadProps, UploadUserFile } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { listUsersByRole } from '@/api/auth'
import { listCourses } from '@/api/course'
import { listSchoolYears } from '@/api/schoolyear'
import {
  createTeachingClass,
  deleteTeachingClass,
  downloadStudentTemplate,
  getTeachingClassStudents,
  importStudentsFromExcel,
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
  IndicatorPointVO,
  StudentImportResult,
  StudentVO,
  SysDictSchoolYearVO,
  SysUserVO,
  TeachingClassCreateRequest,
  TeachingClassUpdateRequest,
  TeachingClassVO
} from '@/api/backend'

type ClassFormState = TeachingClassCreateRequest

const loading = ref(false)
const classLoading = ref(false)
const locked = ref(false)
const importingStudents = ref(false)
const bindingStudents = ref(false)
const downloadingTemplate = ref(false)
const savingClass = ref(false)
const unbindingStudentNo = ref<string>()
const bindDialogVisible = ref(false)
const classDialogVisible = ref(false)
const classEditing = ref<TeachingClassVO>()
const selectedClassId = ref<number>()
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
const lastImportResult = ref<StudentImportResult>()
const bindStudentText = ref('')
const classFormRef = ref<FormInstance>()

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

const selectedClass = computed(() => teachingClasses.value.find((item) => item.id === selectedClassId.value))

const lastImportSummary = computed(() => {
  const result = lastImportResult.value
  if (!result) return ''
  const total = result.total ?? result.totalCount ?? result.successCount + result.failCount
  return `最近一次学生导入：总计 ${total} 条，成功 ${result.successCount} 条，失败 ${result.failCount} 条。`
})

const previewRows = computed(() =>
  students.value.map((student) => ({
    sid: student.studentNo,
    name: student.name,
    collegeName: student.collegeName || '-',
    majorName: student.majorName || '-'
  }))
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
      status: assessments.value.length ? '待成绩数据' : '待配置考核点',
      hint: assessments.value.length ? '课程目标和考核点已接通，等待成绩明细接口。' : '先补课程考核点，再进入成绩计算。'
    }
  })
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

  const results = await Promise.allSettled(taskEntries.map((item) => item.promise))
  const failedLabels: string[] = []

  results.forEach((result, index) => {
    if (result.status === 'fulfilled') {
      return
    }
    failedLabels.push(taskEntries[index].label)
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
      students.value = []
      objectives.value = []
      assessments.value = []
      indicators.value = []
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
      students.value = []
      objectives.value = []
      assessments.value = []
      indicators.value = []
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
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '学生导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('学生模板已开始下载')
  } catch (error) {
    const message = error instanceof Error ? error.message : '模板下载失败'
    ElMessage.error(message)
  } finally {
    downloadingTemplate.value = false
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

const reloadPreview = async () => {
  const current = selectedClass.value
  if (!current?.id) return

  loading.value = true
  try {
    students.value = await getTeachingClassStudents(current.id)
    await loadCourseDetails(current.courseId)
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

.import-actions {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}

.dialog-tip {
  margin: 0 0 12px;
}

.selected-class-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 14px;
  color: #4c5f79;
}
</style>
