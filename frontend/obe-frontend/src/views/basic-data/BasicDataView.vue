<template>
  <div class="page">
    <h1 class="page-title">基础数据管理</h1>
    <p class="page-desc">当前页集中维护课程、专业、毕业要求和指标点，优先把后端已经存在的基础数据能力全部接通。</p>

    <section class="page-grid">
      <div class="panel span-8">
        <div class="toolbar">
          <h3 class="panel-title">课程库</h3>
          <div class="toolbar-actions">
            <el-button @click="openImportDialog">批量导入</el-button>
            <el-button type="primary" @click="openCreateDialog">新增课程</el-button>
          </div>
        </div>
        <el-table v-loading="loadingCourses" :data="courseRows" border>
          <el-table-column prop="code" label="课程代码" width="120" />
          <el-table-column prop="name" label="课程名称" min-width="150" />
          <el-table-column prop="credit" label="学分" width="80" />
          <el-table-column prop="courseNature" label="课程性质" width="100" />
          <el-table-column prop="majorName" label="所属专业" min-width="160" />
          <el-table-column prop="collegeName" label="所属学院" width="140" />
          <el-table-column prop="studentCount" label="学生数" width="90" />
          <el-table-column prop="term" label="学年学期" min-width="170" />
          <el-table-column prop="status" label="状态" width="110">
            <template #default="{ row }"><StatusTag :status="row.status" /></template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEditDialog(row.raw)">编辑</el-button>
              <el-button link type="danger" @click="handleDeleteCourse(row.raw)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel span-4">
        <div class="toolbar">
          <h3 class="panel-title">学院管理</h3>
          <el-button type="primary" @click="openCollegeCreateDialog">新增学院</el-button>
        </div>
        <el-table v-loading="loadingColleges" :data="collegeRows" border size="small">
          <el-table-column prop="collegeName" label="学院名称" min-width="160" />
          <el-table-column label="操作" width="130">
            <template #default="{ row }">
              <el-button link type="primary" @click="openCollegeEditDialog(row.raw)">编辑</el-button>
              <el-button link type="danger" @click="handleDeleteCollege(row.raw)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel span-4">
        <div class="toolbar">
          <h3 class="panel-title">专业管理</h3>
          <el-button type="primary" @click="openMajorCreateDialog">新增专业</el-button>
        </div>
        <el-table v-loading="loadingMajors" :data="majorRows" border size="small">
          <el-table-column prop="majorCode" label="专业代码" width="110" />
          <el-table-column prop="majorName" label="专业名称" min-width="130" />
          <el-table-column prop="collegeName" label="所属学院" min-width="120" />
          <el-table-column label="操作" width="130">
            <template #default="{ row }">
              <el-button link type="primary" @click="openMajorEditDialog(row.raw)">编辑</el-button>
              <el-button link type="danger" @click="handleDeleteMajor(row.raw)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel span-4">
        <div class="toolbar">
          <h3 class="panel-title">学年学期管理</h3>
          <el-button type="primary" @click="openSchoolYearCreateDialog">新增学年学期</el-button>
        </div>
        <el-table v-loading="loadingSchoolYears" :data="schoolYearRows" border size="small">
          <el-table-column prop="yearName" label="学年" min-width="130" />
          <el-table-column prop="semesterName" label="学期" min-width="110" />
          <el-table-column label="操作" width="130">
            <template #default="{ row }">
              <el-button link type="primary" @click="openSchoolYearEditDialog(row.raw)">编辑</el-button>
              <el-button link type="danger" @click="handleDeleteSchoolYear(row.raw)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel span-4">
        <div class="toolbar">
          <h3 class="panel-title">用户管理</h3>
          <el-button type="primary" @click="openUserCreateDialog">新增用户</el-button>
        </div>
        <el-empty v-if="!createdUsers.length" description="本轮只补新增入口，创建后会在这里展示最近新增记录。" />
        <el-table v-else :data="createdUsers" border size="small">
          <el-table-column prop="username" label="用户名" min-width="120" />
          <el-table-column prop="roleCode" label="角色" width="100" />
          <el-table-column prop="collegeName" label="所属学院" min-width="120" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">{{ row.status === 1 ? '启用' : '停用' }}</template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel span-12">
        <div class="toolbar">
          <h3 class="panel-title">毕业要求管理</h3>
          <el-button type="primary" @click="openRequirementCreateDialog">新增毕业要求</el-button>
        </div>
        <el-table v-loading="loadingRequirements" :data="requirementRows" border>
          <el-table-column prop="code" label="毕业要求编号" width="140" />
          <el-table-column prop="name" label="毕业要求名称" min-width="180" />
          <el-table-column prop="majorName" label="所属专业" min-width="150" />
          <el-table-column prop="collegeName" label="所属学院" min-width="140" />
          <el-table-column prop="description" label="描述" min-width="220" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openRequirementEditDialog(row.raw)">编辑</el-button>
              <el-button link type="danger" @click="handleDeleteRequirement(row.raw)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel span-12">
        <div class="toolbar">
          <h3 class="panel-title">指标点管理</h3>
          <el-button type="primary" :disabled="!requirements.length" @click="openIndicatorCreateDialog">新增指标点</el-button>
        </div>
        <el-table v-loading="loadingIndicators" :data="indicatorRows" border>
          <el-table-column prop="requirementCode" label="毕业要求" width="130" />
          <el-table-column prop="code" label="指标点编号" width="120" />
          <el-table-column prop="name" label="指标点名称" min-width="180" />
          <el-table-column prop="description" label="描述" min-width="220" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openIndicatorEditDialog(row.raw)">编辑</el-button>
              <el-button link type="danger" @click="handleDeleteIndicator(row.raw)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>

    <el-dialog v-model="createDialogVisible" :title="editingCourse ? '编辑课程' : '新增课程'" width="520px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="88px">
        <el-form-item label="课程代码" prop="courseCode">
          <el-input v-model="createForm.courseCode" placeholder="例如 CS101" />
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="createForm.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程性质" prop="courseNature">
          <el-select v-model="createForm.courseNature" placeholder="请选择课程性质" style="width: 100%">
            <el-option label="必修" value="必修" />
            <el-option label="选修" value="选修" />
          </el-select>
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="createForm.credit" :min="0.5" :step="0.5" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="所属专业" prop="majorId">
          <el-select v-model="createForm.majorId" placeholder="请选择专业" style="width: 100%" filterable>
            <el-option v-for="major in majors" :key="major.id" :label="major.majorName" :value="major.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingCreate" @click="submitCreateCourse">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="批量导入课程" width="560px" destroy-on-close>
      <div class="import-tips">
        <p>支持上传 `.xlsx` / `.xls`，建议先下载模板再填写，减少校验失败。</p>
        <div class="import-actions">
          <el-button @click="handleDownloadTemplate">下载模板</el-button>
        </div>
      </div>
      <el-upload
        v-model:file-list="importFileList"
        drag
        action="#"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleImportChange"
        :on-remove="handleImportRemove"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-title">拖拽课程 Excel 到这里，或点击选择文件</div>
        <template #tip>
          <div class="muted">导入完成后会自动刷新课程列表。</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingImport" @click="submitImportCourses">开始导入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="majorDialogVisible" :title="editingMajor ? '编辑专业' : '新增专业'" width="520px" destroy-on-close>
      <el-form ref="majorFormRef" :model="majorForm" :rules="majorRules" label-width="88px">
        <el-form-item label="专业代码" prop="majorCode">
          <el-input v-model="majorForm.majorCode" placeholder="例如 CS" />
        </el-form-item>
        <el-form-item label="专业名称" prop="majorName">
          <el-input v-model="majorForm.majorName" placeholder="请输入专业名称" />
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeId">
          <el-select v-model="majorForm.collegeId" placeholder="请选择学院" style="width: 100%">
            <el-option v-for="college in colleges" :key="college.id" :label="college.collegeName" :value="college.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="majorDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingMajor" @click="submitMajor">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="collegeDialogVisible" :title="editingCollege ? '编辑学院' : '新增学院'" width="460px" destroy-on-close>
      <el-form ref="collegeFormRef" :model="collegeForm" :rules="collegeRules" label-width="88px">
        <el-form-item label="学院名称" prop="collegeName">
          <el-input v-model="collegeForm.collegeName" placeholder="请输入学院名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="collegeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingCollege" @click="submitCollege">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="schoolYearDialogVisible"
      :title="editingSchoolYear ? '编辑学年学期' : '新增学年学期'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="schoolYearFormRef" :model="schoolYearForm" :rules="schoolYearRules" label-width="88px">
        <el-form-item label="学年" prop="yearName">
          <el-input v-model="schoolYearForm.yearName" placeholder="例如 2024-2025学年" />
        </el-form-item>
        <el-form-item label="学期" prop="semesterName">
          <el-input v-model="schoolYearForm.semesterName" placeholder="例如 第一学期" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="schoolYearDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingSchoolYear" @click="submitSchoolYear">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="requirementDialogVisible" :title="editingRequirement ? '编辑毕业要求' : '新增毕业要求'" width="560px" destroy-on-close>
      <el-form ref="requirementFormRef" :model="requirementForm" :rules="requirementRules" label-width="96px">
        <el-form-item label="要求编号" prop="requirementCode">
          <el-input v-model="requirementForm.requirementCode" placeholder="例如 GR1" />
        </el-form-item>
        <el-form-item label="要求名称" prop="requirementName">
          <el-input v-model="requirementForm.requirementName" placeholder="请输入毕业要求名称" />
        </el-form-item>
        <el-form-item label="所属专业" prop="majorId">
          <el-select v-model="requirementForm.majorId" placeholder="请选择专业" style="width: 100%" filterable>
            <el-option v-for="major in majors" :key="major.id" :label="major.majorName" :value="major.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="要求描述" prop="description">
          <el-input v-model="requirementForm.description" type="textarea" :rows="4" placeholder="请输入毕业要求描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="requirementDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingRequirement" @click="submitRequirement">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="indicatorDialogVisible" :title="editingIndicator ? '编辑指标点' : '新增指标点'" width="560px" destroy-on-close>
      <el-form ref="indicatorFormRef" :model="indicatorForm" :rules="indicatorRules" label-width="96px">
        <el-form-item label="指标点编号" prop="indicatorCode">
          <el-input v-model="indicatorForm.indicatorCode" placeholder="例如 1.1" />
        </el-form-item>
        <el-form-item label="指标点名称" prop="indicatorName">
          <el-input v-model="indicatorForm.indicatorName" placeholder="请输入指标点名称" />
        </el-form-item>
        <el-form-item label="所属要求" prop="requirementId">
          <el-select v-model="indicatorForm.requirementId" placeholder="请选择毕业要求" style="width: 100%" filterable>
            <el-option
              v-for="requirement in requirements"
              :key="requirement.id"
              :label="`${requirement.requirementCode} ${requirement.requirementName}`"
              :value="requirement.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="指标描述" prop="description">
          <el-input v-model="indicatorForm.description" type="textarea" :rows="4" placeholder="请输入指标点描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="indicatorDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingIndicator" @click="submitIndicator">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="userDialogVisible" title="新增用户" width="520px" destroy-on-close>
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="96px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="初始密码" prop="password">
          <el-input v-model="userForm.password" placeholder="请输入初始密码" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="userForm.roleCode" style="width: 100%">
            <el-option label="课程教师" value="teacher" />
            <el-option label="专业负责人" value="leader" />
            <el-option label="教务管理员" value="edu" />
            <el-option label="系统管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeId">
          <el-select v-model="userForm.collegeId" clearable placeholder="请选择所属学院" style="width: 100%">
            <el-option v-for="college in colleges" :key="college.id" :label="college.collegeName" :value="college.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingUser" @click="submitUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, UploadProps, UploadUserFile } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { createSysUser } from '@/api/auth'
import { createCollege, deleteCollege, listColleges, pageColleges, updateCollege } from '@/api/college'
import { createCourse, deleteCourse, downloadCourseTemplate, importCoursesFromExcel, pageCourses, updateCourse } from '@/api/course'
import {
  createGraduationRequirement,
  createIndicator,
  deleteGraduationRequirement,
  deleteIndicator,
  pageGraduationRequirements,
  pageIndicators,
  updateGraduationRequirement,
  updateIndicator
} from '@/api/indicator'
import { createMajor, deleteMajor, listMajors, pageMajors, updateMajor } from '@/api/major'
import { createSchoolYear, deleteSchoolYear, pageSchoolYears, updateSchoolYear } from '@/api/schoolyear'
import { pageTeachingClasses } from '@/api/teaching-class'
import StatusTag from '@/components/StatusTag/StatusTag.vue'
import type {
  CourseCreateRequest,
  CourseImportResult,
  CourseUpdateRequest,
  CourseVO,
  CreateUserRequest,
  GraduationRequirementCreateRequest,
  GraduationRequirementUpdateRequest,
  GraduationRequirementVO,
  IndicatorPointCreateRequest,
  IndicatorPointUpdateRequest,
  IndicatorPointVO,
  SysDictCollegeCreateRequest,
  SysDictCollegeSimpleVO,
  SysDictCollegeUpdateRequest,
  SysDictCollegeVO,
  SysDictMajorCreateRequest,
  SysDictMajorSimpleVO,
  SysDictMajorUpdateRequest,
  SysDictMajorVO,
  SysDictSchoolYearCreateRequest,
  SysDictSchoolYearUpdateRequest,
  SysDictSchoolYearVO,
  SysUserVO,
  TeachingClassVO
} from '@/api/backend'

const courses = ref<CourseVO[]>([])
const indicators = ref<IndicatorPointVO[]>([])
const requirements = ref<GraduationRequirementVO[]>([])
const majors = ref<SysDictMajorSimpleVO[]>([])
const majorRecords = ref<SysDictMajorVO[]>([])
const colleges = ref<SysDictCollegeSimpleVO[]>([])
const collegeRecords = ref<SysDictCollegeVO[]>([])
const schoolYearRecords = ref<SysDictSchoolYearVO[]>([])
const teachingClasses = ref<TeachingClassVO[]>([])
const userDialogVisible = ref(false)
const submittingUser = ref(false)
const userFormRef = ref<FormInstance>()
const createdUsers = ref<SysUserVO[]>([])

const loadingCourses = ref(false)
const loadingRequirements = ref(false)
const loadingIndicators = ref(false)
const loadingMajors = ref(false)
const loadingColleges = ref(false)
const loadingSchoolYears = ref(false)
const createDialogVisible = ref(false)
const importDialogVisible = ref(false)
const requirementDialogVisible = ref(false)
const indicatorDialogVisible = ref(false)
const majorDialogVisible = ref(false)
const collegeDialogVisible = ref(false)
const schoolYearDialogVisible = ref(false)
const submittingCreate = ref(false)
const submittingImport = ref(false)
const submittingRequirement = ref(false)
const submittingIndicator = ref(false)
const submittingMajor = ref(false)
const submittingCollege = ref(false)
const submittingSchoolYear = ref(false)

const importFileList = ref<UploadUserFile[]>([])
const createFormRef = ref<FormInstance>()
const requirementFormRef = ref<FormInstance>()
const indicatorFormRef = ref<FormInstance>()
const majorFormRef = ref<FormInstance>()
const collegeFormRef = ref<FormInstance>()
const schoolYearFormRef = ref<FormInstance>()

const editingCourse = ref<CourseVO>()
const editingRequirement = ref<GraduationRequirementVO>()
const editingIndicator = ref<IndicatorPointVO>()
const editingMajor = ref<SysDictMajorVO>()
const editingCollege = ref<SysDictCollegeVO>()
const editingSchoolYear = ref<SysDictSchoolYearVO>()

const getCourseStatus = (course: CourseVO) => ((course as CourseVO & { status?: string }).status ?? '进行中')

const createForm = reactive<CourseCreateRequest>({
  courseCode: '',
  courseName: '',
  courseNature: '必修',
  credit: 3,
  majorId: undefined
})

const createRules: FormRules<CourseCreateRequest> = {
  courseCode: [{ required: true, message: '请输入课程代码', trigger: 'blur' }],
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  courseNature: [{ required: true, message: '请选择课程性质', trigger: 'change' }],
  credit: [{ required: true, message: '请输入学分', trigger: 'change' }],
  majorId: [{ required: true, message: '请选择所属专业', trigger: 'change' }]
}

const majorForm = reactive<SysDictMajorCreateRequest>({
  majorCode: '',
  majorName: '',
  collegeId: 0
})

const majorRules: FormRules<SysDictMajorCreateRequest> = {
  majorCode: [{ required: true, message: '请输入专业代码', trigger: 'blur' }],
  majorName: [{ required: true, message: '请输入专业名称', trigger: 'blur' }],
  collegeId: [{ required: true, message: '请选择所属学院', trigger: 'change' }]
}

const collegeForm = reactive<SysDictCollegeCreateRequest>({
  collegeName: ''
})

const collegeRules: FormRules<SysDictCollegeCreateRequest> = {
  collegeName: [{ required: true, message: '请输入学院名称', trigger: 'blur' }]
}

const schoolYearForm = reactive<SysDictSchoolYearCreateRequest>({
  yearName: '',
  semesterName: ''
})

const schoolYearRules: FormRules<SysDictSchoolYearCreateRequest> = {
  yearName: [{ required: true, message: '请输入学年名称', trigger: 'blur' }],
  semesterName: [{ required: true, message: '请输入学期名称', trigger: 'blur' }]
}

const requirementForm = reactive<GraduationRequirementCreateRequest>({
  requirementCode: '',
  requirementName: '',
  description: '',
  majorId: undefined
})

const requirementRules: FormRules<GraduationRequirementCreateRequest> = {
  requirementCode: [{ required: true, message: '请输入毕业要求编号', trigger: 'blur' }],
  requirementName: [{ required: true, message: '请输入毕业要求名称', trigger: 'blur' }],
  majorId: [{ required: true, message: '请选择所属专业', trigger: 'change' }]
}

const indicatorForm = reactive<IndicatorPointCreateRequest>({
  indicatorCode: '',
  indicatorName: '',
  description: '',
  requirementId: 0
})

const userForm = reactive<CreateUserRequest>({
  username: '',
  password: '',
  roleCode: 'teacher',
  collegeId: undefined,
  status: 1
})

const indicatorRules: FormRules<IndicatorPointCreateRequest> = {
  indicatorCode: [{ required: true, message: '请输入指标点编号', trigger: 'blur' }],
  indicatorName: [{ required: true, message: '请输入指标点名称', trigger: 'blur' }],
  requirementId: [{ required: true, message: '请选择所属毕业要求', trigger: 'change' }]
}

const userRules: FormRules<CreateUserRequest> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const getCourseStudentCount = (courseId: number) => {
  const relatedClasses = teachingClasses.value.filter((item) => item.courseId === courseId)
  if (!relatedClasses.length) return '-'
  return relatedClasses.reduce((sum, item) => sum + Number(item.studentCount ?? 0), 0)
}

const getCourseTermLabel = (courseId: number) => {
  const relatedClasses = teachingClasses.value.filter((item) => item.courseId === courseId)
  const termLabels = Array.from(
    new Set(relatedClasses.map((item) => [item.yearName, item.semesterName].filter(Boolean).join(' ')).filter(Boolean))
  )
  return termLabels.length ? termLabels.join(' / ') : '-'
}

const courseRows = computed(() =>
  courses.value.map((course) => ({
    code: course.courseCode,
    name: course.courseName,
    credit: course.credit ?? '-',
    courseNature: course.courseNature || '-',
    majorName: course.majorName || '-',
    collegeName: course.collegeName || '-',
    studentCount: getCourseStudentCount(course.id),
    term: getCourseTermLabel(course.id),
    status: getCourseStatus(course),
    raw: course
  }))
)

const majorRows = computed(() =>
  majorRecords.value.map((major) => ({
    majorCode: major.majorCode,
    majorName: major.majorName,
    collegeName: major.collegeName || '-',
    raw: major
  }))
)

const collegeRows = computed(() =>
  collegeRecords.value.map((college) => ({
    collegeName: college.collegeName,
    raw: college
  }))
)

const schoolYearRows = computed(() =>
  schoolYearRecords.value.map((item) => ({
    yearName: item.yearName,
    semesterName: item.semesterName,
    raw: item
  }))
)

const requirementRows = computed(() =>
  requirements.value.map((requirement) => ({
    code: requirement.requirementCode,
    name: requirement.requirementName,
    majorName: requirement.majorName || '-',
    collegeName: requirement.collegeName || '-',
    description: requirement.description || '-',
    raw: requirement
  }))
)

const indicatorRows = computed(() =>
  indicators.value.map((indicator) => ({
    requirementCode: indicator.requirementCode || '-',
    code: indicator.indicatorCode,
    name: indicator.indicatorName,
    description: indicator.description || '-',
    raw: indicator
  }))
)

const resetCreateForm = () => {
  createForm.courseCode = ''
  createForm.courseName = ''
  createForm.courseNature = '必修'
  createForm.credit = 3
  createForm.majorId = majors.value[0]?.id
  createFormRef.value?.clearValidate()
}

const resetMajorForm = () => {
  majorForm.majorCode = ''
  majorForm.majorName = ''
  majorForm.collegeId = colleges.value[0]?.id || 0
  majorFormRef.value?.clearValidate()
}

const resetCollegeForm = () => {
  collegeForm.collegeName = ''
  collegeFormRef.value?.clearValidate()
}

const resetSchoolYearForm = () => {
  schoolYearForm.yearName = ''
  schoolYearForm.semesterName = ''
  schoolYearFormRef.value?.clearValidate()
}

const resetRequirementForm = () => {
  requirementForm.requirementCode = ''
  requirementForm.requirementName = ''
  requirementForm.description = ''
  requirementForm.majorId = majors.value[0]?.id
  requirementFormRef.value?.clearValidate()
}

const resetIndicatorForm = () => {
  indicatorForm.indicatorCode = ''
  indicatorForm.indicatorName = ''
  indicatorForm.description = ''
  indicatorForm.requirementId = requirements.value[0]?.id || 0
  indicatorFormRef.value?.clearValidate()
}

const resetUserForm = () => {
  userForm.username = ''
  userForm.password = ''
  userForm.roleCode = 'teacher'
  userForm.collegeId = colleges.value[0]?.id
  userForm.status = 1
  userFormRef.value?.clearValidate()
}

const openCreateDialog = () => {
  editingCourse.value = undefined
  resetCreateForm()
  createDialogVisible.value = true
}

const openEditDialog = (course: CourseVO) => {
  editingCourse.value = course
  createForm.courseCode = course.courseCode
  createForm.courseName = course.courseName
  createForm.courseNature = course.courseNature || '必修'
  createForm.credit = Number(course.credit ?? 3)
  createForm.majorId = course.majorId
  createDialogVisible.value = true
}

const openImportDialog = () => {
  importFileList.value = []
  importDialogVisible.value = true
}

const openMajorCreateDialog = () => {
  editingMajor.value = undefined
  resetMajorForm()
  majorDialogVisible.value = true
}

const openMajorEditDialog = (major: SysDictMajorVO) => {
  editingMajor.value = major
  majorForm.majorCode = major.majorCode
  majorForm.majorName = major.majorName
  majorForm.collegeId = Number(major.collegeId || 0)
  majorDialogVisible.value = true
}

const openCollegeCreateDialog = () => {
  editingCollege.value = undefined
  resetCollegeForm()
  collegeDialogVisible.value = true
}

const openCollegeEditDialog = (college: SysDictCollegeVO) => {
  editingCollege.value = college
  collegeForm.collegeName = college.collegeName
  collegeDialogVisible.value = true
}

const openSchoolYearCreateDialog = () => {
  editingSchoolYear.value = undefined
  resetSchoolYearForm()
  schoolYearDialogVisible.value = true
}

const openSchoolYearEditDialog = (schoolYear: SysDictSchoolYearVO) => {
  editingSchoolYear.value = schoolYear
  schoolYearForm.yearName = schoolYear.yearName
  schoolYearForm.semesterName = schoolYear.semesterName
  schoolYearDialogVisible.value = true
}

const openRequirementCreateDialog = () => {
  editingRequirement.value = undefined
  resetRequirementForm()
  requirementDialogVisible.value = true
}

const openRequirementEditDialog = (requirement: GraduationRequirementVO) => {
  editingRequirement.value = requirement
  requirementForm.requirementCode = requirement.requirementCode
  requirementForm.requirementName = requirement.requirementName
  requirementForm.description = requirement.description || ''
  requirementForm.majorId = requirement.majorId
  requirementDialogVisible.value = true
}

const openIndicatorCreateDialog = () => {
  editingIndicator.value = undefined
  resetIndicatorForm()
  indicatorDialogVisible.value = true
}

const openIndicatorEditDialog = (indicator: IndicatorPointVO) => {
  editingIndicator.value = indicator
  indicatorForm.indicatorCode = indicator.indicatorCode
  indicatorForm.indicatorName = indicator.indicatorName
  indicatorForm.description = indicator.description || ''
  indicatorForm.requirementId = indicator.requirementId || requirements.value[0]?.id || 0
  indicatorDialogVisible.value = true
}

const openUserCreateDialog = () => {
  resetUserForm()
  userDialogVisible.value = true
}

const loadCourses = async () => {
  loadingCourses.value = true
  try {
    const coursePage = await pageCourses({ current: 1, pageSize: 200 })
    courses.value = coursePage.records
  } finally {
    loadingCourses.value = false
  }
}

const loadMajorRecords = async () => {
  loadingMajors.value = true
  try {
    const majorPage = await pageMajors({ current: 1, pageSize: 200 })
    majorRecords.value = majorPage.records
  } finally {
    loadingMajors.value = false
  }
}

const loadCollegeRecords = async () => {
  loadingColleges.value = true
  try {
    const collegePage = await pageColleges({ current: 1, pageSize: 200 })
    collegeRecords.value = collegePage.records
  } finally {
    loadingColleges.value = false
  }
}

const loadSchoolYearRecords = async () => {
  loadingSchoolYears.value = true
  try {
    const schoolYearPage = await pageSchoolYears({ current: 1, pageSize: 200 })
    schoolYearRecords.value = schoolYearPage.records
  } finally {
    loadingSchoolYears.value = false
  }
}

const loadIndicators = async () => {
  loadingIndicators.value = true
  try {
    const indicatorPage = await pageIndicators({ current: 1, pageSize: 200 })
    indicators.value = indicatorPage.records
  } finally {
    loadingIndicators.value = false
  }
}

const loadRequirements = async () => {
  loadingRequirements.value = true
  try {
    const requirementPage = await pageGraduationRequirements({ current: 1, pageSize: 200 })
    requirements.value = requirementPage.records
  } finally {
    loadingRequirements.value = false
  }
}

const loadTeachingClasses = async () => {
  const classPage = await pageTeachingClasses({ current: 1, pageSize: 500 })
  teachingClasses.value = classPage.records
}

const loadMajors = async () => {
  majors.value = await listMajors()
  if (!createForm.majorId) {
    createForm.majorId = majors.value[0]?.id
  }
}

const loadColleges = async () => {
  colleges.value = await listColleges()
  if (!majorForm.collegeId) {
    majorForm.collegeId = colleges.value[0]?.id || 0
  }
}

const submitCreateCourse = async () => {
  const isValid = await createFormRef.value?.validate().catch(() => false)
  if (!isValid) return

  submittingCreate.value = true
  try {
    const payload = {
      courseCode: createForm.courseCode.trim(),
      courseName: createForm.courseName.trim(),
      courseNature: createForm.courseNature,
      credit: Number(createForm.credit),
      majorId: createForm.majorId
    }

    if (editingCourse.value) {
      await updateCourse({
        id: editingCourse.value.id,
        ...payload
      } satisfies CourseUpdateRequest)
      ElMessage.success('课程已更新')
    } else {
      await createCourse(payload)
      ElMessage.success('课程已创建')
    }

    createDialogVisible.value = false
    editingCourse.value = undefined
    await loadCourses()
  } catch (error) {
    const message = error instanceof Error ? error.message : '课程保存失败'
    ElMessage.error(message)
  } finally {
    submittingCreate.value = false
  }
}

const handleDeleteCourse = async (course: CourseVO) => {
  try {
    await ElMessageBox.confirm(`确认删除课程 ${course.courseCode} - ${course.courseName} 吗？`, '删除确认', {
      type: 'warning'
    })
    await deleteCourse(course.id)
    ElMessage.success('课程已删除')
    await loadCourses()
  } catch (error) {
    if (error === 'cancel') return
    const message = error instanceof Error ? error.message : '课程删除失败'
    ElMessage.error(message)
  }
}

const submitMajor = async () => {
  const isValid = await majorFormRef.value?.validate().catch(() => false)
  if (!isValid) return

  submittingMajor.value = true
  try {
    const payload = {
      majorCode: majorForm.majorCode.trim(),
      majorName: majorForm.majorName.trim(),
      collegeId: Number(majorForm.collegeId)
    }

    if (editingMajor.value) {
      await updateMajor({
        id: editingMajor.value.id,
        ...payload
      } satisfies SysDictMajorUpdateRequest)
      ElMessage.success('专业已更新')
    } else {
      await createMajor(payload)
      ElMessage.success('专业已创建')
    }

    majorDialogVisible.value = false
    editingMajor.value = undefined
    await Promise.all([loadMajorRecords(), loadMajors(), loadCourses(), loadRequirements()])
  } catch (error) {
    const message = error instanceof Error ? error.message : '专业保存失败'
    ElMessage.error(message)
  } finally {
    submittingMajor.value = false
  }
}

const handleDeleteMajor = async (major: SysDictMajorVO) => {
  try {
    await ElMessageBox.confirm(`确认删除专业 ${major.majorCode} - ${major.majorName} 吗？`, '删除确认', {
      type: 'warning'
    })
    await deleteMajor(major.id)
    ElMessage.success('专业已删除')
    await Promise.all([loadMajorRecords(), loadMajors(), loadCourses(), loadRequirements()])
  } catch (error) {
    if (error === 'cancel') return
    const message = error instanceof Error ? error.message : '专业删除失败'
    ElMessage.error(message)
  }
}

const submitCollege = async () => {
  const isValid = await collegeFormRef.value?.validate().catch(() => false)
  if (!isValid) return

  submittingCollege.value = true
  try {
    const payload = {
      collegeName: collegeForm.collegeName.trim()
    }

    if (editingCollege.value) {
      await updateCollege({
        id: editingCollege.value.id,
        ...payload
      } satisfies SysDictCollegeUpdateRequest)
      ElMessage.success('学院已更新')
    } else {
      await createCollege(payload)
      ElMessage.success('学院已创建')
    }

    collegeDialogVisible.value = false
    editingCollege.value = undefined
    await Promise.all([loadCollegeRecords(), loadColleges(), loadMajorRecords(), loadMajors(), loadCourses(), loadRequirements()])
  } catch (error) {
    const message = error instanceof Error ? error.message : '学院保存失败'
    ElMessage.error(message)
  } finally {
    submittingCollege.value = false
  }
}

const handleDeleteCollege = async (college: SysDictCollegeVO) => {
  try {
    await ElMessageBox.confirm(`确认删除学院 ${college.collegeName} 吗？`, '删除确认', {
      type: 'warning'
    })
    await deleteCollege(college.id)
    ElMessage.success('学院已删除')
    await Promise.all([loadCollegeRecords(), loadColleges(), loadMajorRecords(), loadMajors(), loadCourses(), loadRequirements()])
  } catch (error) {
    if (error === 'cancel') return
    const message = error instanceof Error ? error.message : '学院删除失败'
    ElMessage.error(message)
  }
}

const submitSchoolYear = async () => {
  const isValid = await schoolYearFormRef.value?.validate().catch(() => false)
  if (!isValid) return

  submittingSchoolYear.value = true
  try {
    const payload = {
      yearName: schoolYearForm.yearName.trim(),
      semesterName: schoolYearForm.semesterName.trim()
    }

    if (editingSchoolYear.value) {
      await updateSchoolYear({
        id: editingSchoolYear.value.id,
        ...payload
      } satisfies SysDictSchoolYearUpdateRequest)
      ElMessage.success('学年学期已更新')
    } else {
      await createSchoolYear(payload)
      ElMessage.success('学年学期已创建')
    }

    schoolYearDialogVisible.value = false
    editingSchoolYear.value = undefined
    await Promise.all([loadSchoolYearRecords(), loadTeachingClasses(), loadCourses()])
  } catch (error) {
    const message = error instanceof Error ? error.message : '学年学期保存失败'
    ElMessage.error(message)
  } finally {
    submittingSchoolYear.value = false
  }
}

const handleDeleteSchoolYear = async (schoolYear: SysDictSchoolYearVO) => {
  try {
    await ElMessageBox.confirm(`确认删除 ${schoolYear.yearName} ${schoolYear.semesterName} 吗？`, '删除确认', {
      type: 'warning'
    })
    await deleteSchoolYear(schoolYear.id)
    ElMessage.success('学年学期已删除')
    await Promise.all([loadSchoolYearRecords(), loadTeachingClasses(), loadCourses()])
  } catch (error) {
    if (error === 'cancel') return
    const message = error instanceof Error ? error.message : '学年学期删除失败'
    ElMessage.error(message)
  }
}

const submitRequirement = async () => {
  const isValid = await requirementFormRef.value?.validate().catch(() => false)
  if (!isValid) return

  submittingRequirement.value = true
  try {
    const payload = {
      requirementCode: requirementForm.requirementCode.trim(),
      requirementName: requirementForm.requirementName.trim(),
      description: requirementForm.description?.trim(),
      majorId: requirementForm.majorId
    }

    if (editingRequirement.value) {
      await updateGraduationRequirement({
        id: editingRequirement.value.id,
        ...payload
      } satisfies GraduationRequirementUpdateRequest)
      ElMessage.success('毕业要求已更新')
    } else {
      await createGraduationRequirement(payload)
      ElMessage.success('毕业要求已创建')
    }

    requirementDialogVisible.value = false
    editingRequirement.value = undefined
    await Promise.all([loadRequirements(), loadIndicators()])
  } catch (error) {
    const message = error instanceof Error ? error.message : '毕业要求保存失败'
    ElMessage.error(message)
  } finally {
    submittingRequirement.value = false
  }
}

const handleDeleteRequirement = async (requirement: GraduationRequirementVO) => {
  try {
    await ElMessageBox.confirm(
      `确认删除毕业要求 ${requirement.requirementCode} - ${requirement.requirementName} 吗？`,
      '删除确认',
      { type: 'warning' }
    )
    await deleteGraduationRequirement(requirement.id)
    ElMessage.success('毕业要求已删除')
    await Promise.all([loadRequirements(), loadIndicators()])
  } catch (error) {
    if (error === 'cancel') return
    const message = error instanceof Error ? error.message : '毕业要求删除失败'
    ElMessage.error(message)
  }
}

const submitIndicator = async () => {
  const isValid = await indicatorFormRef.value?.validate().catch(() => false)
  if (!isValid) return

  submittingIndicator.value = true
  try {
    const payload = {
      indicatorCode: indicatorForm.indicatorCode.trim(),
      indicatorName: indicatorForm.indicatorName.trim(),
      description: indicatorForm.description?.trim(),
      requirementId: indicatorForm.requirementId
    }

    if (editingIndicator.value) {
      await updateIndicator({
        id: editingIndicator.value.id,
        ...payload
      } satisfies IndicatorPointUpdateRequest)
      ElMessage.success('指标点已更新')
    } else {
      await createIndicator(payload)
      ElMessage.success('指标点已创建')
    }

    indicatorDialogVisible.value = false
    editingIndicator.value = undefined
    await loadIndicators()
  } catch (error) {
    const message = error instanceof Error ? error.message : '指标点保存失败'
    ElMessage.error(message)
  } finally {
    submittingIndicator.value = false
  }
}

const submitUser = async () => {
  const isValid = await userFormRef.value?.validate().catch(() => false)
  if (!isValid) return

  submittingUser.value = true
  try {
    const userId = await createSysUser({
      username: userForm.username.trim(),
      password: userForm.password,
      roleCode: userForm.roleCode,
      collegeId: userForm.collegeId ?? undefined,
      status: userForm.status ?? 1
    })

    const collegeName = colleges.value.find((item) => item.id === userForm.collegeId)?.collegeName
    createdUsers.value.unshift({
      id: userId,
      username: userForm.username.trim(),
      roleCode: userForm.roleCode,
      collegeName,
      status: userForm.status ?? 1
    })

    userDialogVisible.value = false
    ElMessage.success('用户已创建')
  } catch (error) {
    const message = error instanceof Error ? error.message : '用户创建失败'
    ElMessage.error(message)
  } finally {
    submittingUser.value = false
  }
}

const handleDeleteIndicator = async (indicator: IndicatorPointVO) => {
  try {
    await ElMessageBox.confirm(`确认删除指标点 ${indicator.indicatorCode} - ${indicator.indicatorName} 吗？`, '删除确认', {
      type: 'warning'
    })
    await deleteIndicator(indicator.id)
    ElMessage.success('指标点已删除')
    await loadIndicators()
  } catch (error) {
    if (error === 'cancel') return
    const message = error instanceof Error ? error.message : '指标点删除失败'
    ElMessage.error(message)
  }
}

const handleImportChange: UploadProps['onChange'] = (uploadFile, uploadFiles) => {
  const fileName = uploadFile.name.toLowerCase()
  if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
    ElMessage.error('请上传 Excel 文件')
    importFileList.value = []
    return
  }
  importFileList.value = uploadFiles.slice(-1)
}

const handleImportRemove: UploadProps['onRemove'] = () => {
  importFileList.value = []
}

const handleDownloadTemplate = async () => {
  try {
    const blob = await downloadCourseTemplate()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '课程导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板已开始下载')
  } catch (error) {
    const message = error instanceof Error ? error.message : '模板下载失败'
    ElMessage.error(message)
  }
}

const showImportResult = async (result: CourseImportResult) => {
  if (result.failCount > 0) {
    const preview = (result.failDetails ?? [])
      .slice(0, 5)
      .map((item) => `第 ${item.row || '-'} 行：${item.reason || '导入失败'}`)
      .join('\n')

    await ElMessageBox.alert(
      `总计 ${result.total} 条，成功 ${result.successCount} 条，失败 ${result.failCount} 条。${preview ? `\n\n失败示例：\n${preview}` : ''}`,
      '导入完成',
      { confirmButtonText: '知道了' }
    )
  } else {
    ElMessage.success(`课程导入完成，共成功导入 ${result.successCount} 条`)
  }
}

const submitImportCourses = async () => {
  const file = importFileList.value[0]?.raw
  if (!file) {
    ElMessage.warning('请先选择要导入的 Excel 文件')
    return
  }

  submittingImport.value = true
  try {
    const result = await importCoursesFromExcel(file)
    importDialogVisible.value = false
    importFileList.value = []
    await loadCourses()
    await showImportResult(result)
  } catch (error) {
    const message = error instanceof Error ? error.message : '课程导入失败'
    ElMessage.error(message)
  } finally {
    submittingImport.value = false
  }
}

onMounted(async () => {
  const [collegeSimpleResult, collegePageResult, schoolYearResult, majorSimpleResult, majorPageResult, courseResult, requirementResult, indicatorResult, classResult] =
    await Promise.allSettled([
      loadColleges(),
      loadCollegeRecords(),
      loadSchoolYearRecords(),
      loadMajors(),
      loadMajorRecords(),
      loadCourses(),
      loadRequirements(),
      loadIndicators(),
      loadTeachingClasses()
    ])

  if (collegeSimpleResult.status === 'rejected') {
    ElMessage.error(collegeSimpleResult.reason instanceof Error ? collegeSimpleResult.reason.message : '学院下拉加载失败')
  }
  if (collegePageResult.status === 'rejected') {
    ElMessage.error(collegePageResult.reason instanceof Error ? collegePageResult.reason.message : '学院列表加载失败')
  }
  if (schoolYearResult.status === 'rejected') {
    ElMessage.error(schoolYearResult.reason instanceof Error ? schoolYearResult.reason.message : '学年学期列表加载失败')
  }
  if (majorSimpleResult.status === 'rejected') {
    ElMessage.error(majorSimpleResult.reason instanceof Error ? majorSimpleResult.reason.message : '专业下拉加载失败')
  }
  if (majorPageResult.status === 'rejected') {
    ElMessage.error(majorPageResult.reason instanceof Error ? majorPageResult.reason.message : '专业列表加载失败')
  }
  if (courseResult.status === 'rejected') {
    ElMessage.error(courseResult.reason instanceof Error ? courseResult.reason.message : '课程列表加载失败')
  }
  if (requirementResult.status === 'rejected') {
    ElMessage.error(requirementResult.reason instanceof Error ? requirementResult.reason.message : '毕业要求列表加载失败')
  }
  if (indicatorResult.status === 'rejected') {
    ElMessage.error(indicatorResult.reason instanceof Error ? indicatorResult.reason.message : '指标点列表加载失败')
  }
  if (classResult.status === 'rejected') {
    ElMessage.error(classResult.reason instanceof Error ? classResult.reason.message : '教学班列表加载失败')
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

.import-actions {
  margin-top: 8px;
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
</style>
