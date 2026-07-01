<template>
  <div class="page">
    <h1 class="page-title">基础数据管理</h1>
    <p class="page-desc">集中维护课程、专业、学院、学年学期、用户账号、毕业要求与指标点等基础数据，为后续计算与报表提供数据支撑。</p>

    <section class="page-grid">
      <div v-if="canManageCourseSection" class="panel span-8">
        <div class="toolbar">
          <h3 class="panel-title">课程库</h3>
          <div class="toolbar-actions">
            <el-button @click="openImportDialog">批量导入</el-button>
            <el-button type="primary" :disabled="!availableCourseMajors.length" @click="openCreateDialog">新增课程</el-button>
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

      <div v-if="canManageStudentSection" class="panel span-12">
        <div class="toolbar">
          <h3 class="panel-title">学生库</h3>
          <div class="toolbar-actions">
            <el-button @click="openStudentImportDialog">批量导入</el-button>
            <el-button type="primary" :disabled="!availableCourseMajors.length" @click="openStudentCreateDialog">新增学生</el-button>
          </div>
        </div>
        <div style="display:flex;gap:8px;margin-bottom:12px;flex-wrap:wrap;align-items:center;">
          <el-input v-model="studentQuery.studentNo" placeholder="按学号搜索" clearable style="width:160px" @keyup.enter="searchStudents" />
          <el-input v-model="studentQuery.studentName" placeholder="按姓名搜索" clearable style="width:140px" @keyup.enter="searchStudents" />
          <el-select v-model="studentQuery.majorId" placeholder="全部专业" clearable style="width:180px">
            <el-option v-for="major in availableCourseMajors" :key="major.id" :label="major.majorName" :value="major.id" />
          </el-select>
          <el-button type="primary" @click="searchStudents">查询</el-button>
          <el-button @click="resetStudentQuery">重置</el-button>
        </div>
        <el-table v-loading="loadingStudents" :data="studentRows" border size="small">
          <el-table-column prop="studentNo" label="学号" width="130" />
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="grade" label="年级" width="90" />
          <el-table-column prop="majorName" label="所属专业" min-width="150" />
          <el-table-column prop="collegeName" label="所属学院" min-width="130" />
          <el-table-column prop="className" label="班级" min-width="160" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openStudentEditDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDeleteStudent(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div style="display:flex;justify-content:flex-end;margin-top:12px;">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="studentTotal"
            :current-page="studentQuery.current"
            :page-size="studentQuery.pageSize"
            :page-sizes="[10, 20, 50]"
            @current-change="onStudentPageChange"
            @size-change="onStudentSizeChange"
          />
        </div>
      </div>

      <div v-if="canManageDictionarySection" class="panel span-4">
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

      <div v-if="canManageDictionarySection" class="panel span-4">
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

      <div v-if="canManageDictionarySection" class="panel span-4">
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

      <div v-if="canManageUserSection" class="panel span-4">
        <div class="toolbar">
          <h3 class="panel-title">用户管理</h3>
          <el-button type="primary" @click="openUserCreateDialog">新增用户</el-button>
        </div>
        <div class="user-toolbar">
          <el-radio-group v-model="selectedUserRole" size="small" @change="handleUserRoleChange">
            <el-radio-button v-for="option in userRoleOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </el-radio-button>
          </el-radio-group>
          <div class="muted user-hint">当前展示后端真实列表，新增成功后会自动刷新当前角色。</div>
        </div>
        <div v-if="recentCreatedUser" class="user-created-note">
          最近新增：{{ recentCreatedUser.username }}（{{ getRoleLabel(recentCreatedUser.roleCode) }}）
        </div>
        <el-empty
          v-if="!loadingUsers && !userRows.length"
          :description="`${currentUserRoleLabel}列表暂时为空，创建后会自动显示在这里。`"
        />
        <el-table v-else v-loading="loadingUsers" :data="userRows" border size="small">
          <el-table-column prop="username" label="用户名" min-width="120" />
          <el-table-column prop="roleLabel" label="角色" width="100" />
          <el-table-column prop="collegeName" label="所属学院" min-width="120" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">{{ row.status === 1 ? '启用' : '停用' }}</template>
          </el-table-column>
          <el-table-column prop="createTimeLabel" label="创建时间" min-width="150" />
        </el-table>
      </div>

      <div v-if="canManageRequirementSection" class="panel span-12">
        <div class="toolbar">
          <h3 class="panel-title">毕业要求管理</h3>
          <div class="toolbar-actions">
            <el-button @click="openRequirementImportDialog">批量导入</el-button>
            <el-button type="primary" :disabled="!availableRequirementMajors.length" @click="openRequirementCreateDialog">新增毕业要求</el-button>
          </div>
        </div>
        <div class="filter-bar" style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap; margin: 12px 0;">
          <el-select v-model="requirementFilter.majorId" placeholder="按专业筛选" clearable filterable style="width: 200px">
            <el-option v-for="major in majors" :key="major.id" :label="major.majorName" :value="major.id" />
          </el-select>
          <el-input v-model="requirementFilter.code" placeholder="编号搜索（如 GR1）" clearable style="width: 170px" />
          <el-input v-model="requirementFilter.name" placeholder="名称搜索" clearable style="width: 200px" />
          <span style="color: var(--muted); font-size: 13px;">共 {{ requirementRows.length }} 条</span>
          <el-button @click="resetRequirementFilter">重置</el-button>
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

      <div v-if="canManageRequirementSection" class="panel span-12">
        <div class="toolbar">
          <h3 class="panel-title">指标点管理</h3>
          <div class="toolbar-actions">
            <el-button @click="openIndicatorImportDialog">批量导入</el-button>
            <el-button type="primary" :disabled="!requirements.length" @click="openIndicatorCreateDialog">新增指标点</el-button>
          </div>
        </div>
        <div class="filter-bar" style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap; margin: 12px 0;">
          <el-select v-model="indicatorFilter.requirementId" placeholder="按毕业要求筛选" clearable filterable style="width: 240px">
            <el-option v-for="req in requirements" :key="req.id" :label="`${req.requirementCode} ${req.requirementName}`" :value="req.id" />
          </el-select>
          <el-input v-model="indicatorFilter.code" placeholder="编号搜索（如 1.1）" clearable style="width: 160px" />
          <el-input v-model="indicatorFilter.name" placeholder="名称搜索" clearable style="width: 200px" />
          <span style="color: var(--muted); font-size: 13px;">共 {{ indicatorRows.length }} 条</span>
          <el-button @click="resetIndicatorFilter">重置</el-button>
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
            <el-option v-for="major in availableCourseMajors" :key="major.id" :label="major.majorName" :value="major.id" />
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

    <el-dialog v-model="studentImportVisible" title="批量导入学生" width="560px" destroy-on-close>
      <div class="import-tips">
        <p>支持上传 `.xlsx` / `.xls`，字段：姓名、学号、年级、专业代码（班级在绑定教学班时自动回填）。建议先下载模板再填写。</p>
        <div class="import-actions">
          <el-button @click="handleDownloadStudentTemplate">下载模板</el-button>
        </div>
      </div>
      <el-upload
        v-model:file-list="studentImportFileList"
        drag
        action="#"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleStudentImportChange"
        :on-remove="handleStudentImportRemove"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-title">拖拽学生 Excel 到这里，或点击选择文件</div>
        <template #tip>
          <div class="muted">导入完成后会自动刷新学生列表。</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="studentImportVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingStudentImport" @click="submitImportStudents">开始导入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="studentDialogVisible" :title="editingStudent ? '编辑学生' : '新增学生'" width="520px" destroy-on-close>
      <el-form ref="studentFormRef" :model="studentForm" :rules="studentRules" label-width="88px">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="studentForm.studentNo" placeholder="例如 202301001" />
        </el-form-item>
        <el-form-item label="姓名" prop="studentName">
          <el-input v-model="studentForm.studentName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="studentForm.grade" placeholder="例如 2023" />
        </el-form-item>
        <el-form-item label="所属专业" prop="majorId">
          <el-select v-model="studentForm.majorId" placeholder="请选择专业" style="width: 100%">
            <el-option v-for="major in availableCourseMajors" :key="major.id" :label="major.majorName" :value="major.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="studentDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingStudent" @click="submitStudentForm">保存</el-button>
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
            <el-option v-for="major in availableRequirementMajors" :key="major.id" :label="major.majorName" :value="major.id" />
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

    <!-- 毕业要求导入 -->
    <el-dialog v-model="requirementImportVisible" title="批量导入毕业要求" width="560px" destroy-on-close>
      <div class="import-tips">
        <p>字段：专业代码*、毕业要求编号*、毕业要求名称*、毕业要求描述</p>
        <div class="import-actions">
          <el-button @click="handleDownloadRequirementTemplate">下载模板</el-button>
        </div>
      </div>
      <el-upload
        v-model:file-list="requirementImportFileList"
        drag action="#" :auto-upload="false" :limit="1" accept=".xlsx,.xls"
        :on-change="handleRequirementImportChange" :on-remove="handleRequirementImportRemove"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-title">拖拽毕业要求 Excel 到这里</div>
        <template #tip><div class="muted">导入完成后请刷新页面查看。</div></template>
      </el-upload>
      <template #footer>
        <el-button @click="requirementImportVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingRequirementImport" @click="submitRequirementImport">开始导入</el-button>
      </template>
    </el-dialog>

    <!-- 指标点导入 -->
    <el-dialog v-model="indicatorImportVisible" title="批量导入指标点" width="560px" destroy-on-close>
      <div class="import-tips">
        <p>字段：毕业要求编码*、指标点编号*、指标点名称*、指标点描述</p>
        <div class="import-actions">
          <el-button @click="handleDownloadIndicatorTemplate">下载模板</el-button>
        </div>
      </div>
      <el-upload
        v-model:file-list="indicatorImportFileList"
        drag action="#" :auto-upload="false" :limit="1" accept=".xlsx,.xls"
        :on-change="handleIndicatorImportChange" :on-remove="handleIndicatorImportRemove"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-title">拖拽指标点 Excel 到这里</div>
        <template #tip><div class="muted">导入完成后请刷新页面查看。</div></template>
      </el-upload>
      <template #footer>
        <el-button @click="indicatorImportVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingIndicatorImport" @click="submitIndicatorImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, UploadProps, UploadUserFile } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { createSysUser, listUsersByRole } from '@/api/auth'
import { createCollege, deleteCollege, getCollege, listColleges, pageColleges, updateCollege } from '@/api/college'
import { createCourse, deleteCourse, downloadCourseTemplate, getCourse, importCoursesFromExcel, pageCourses, updateCourse } from '@/api/course'
import {
  createGraduationRequirement,
  createIndicator,
  deleteGraduationRequirement,
  deleteIndicator,
  downloadGraduationRequirementTemplate,
  downloadIndicatorPointTemplate,
  getGraduationRequirement,
  getIndicator,
  importGraduationRequirementsFromExcel,
  importIndicatorPointsFromExcel,
  pageGraduationRequirements,
  pageIndicators,
  updateGraduationRequirement,
  updateIndicator
} from '@/api/indicator'
import { createMajor, deleteMajor, getMajor, listMajors, pageMajors, updateMajor } from '@/api/major'
import { createSchoolYear, deleteSchoolYear, getSchoolYear, pageSchoolYears, updateSchoolYear } from '@/api/schoolyear'
import { pageTeachingClasses, importStudentsFromExcel, downloadStudentTemplate } from '@/api/teaching-class'
import { createStudent, deleteStudent, pageStudents, updateStudent } from '@/api/student'
import StatusTag from '@/components/StatusTag/StatusTag.vue'
import { useUserStore } from '@/stores/user'
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
  StudentAddRequest,
  StudentUpdateRequest,
  StudentVO,
  TeachingClassVO
} from '@/api/backend'
import { normalizePageFields } from '@/api/backend'

const userStore = useUserStore()
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
const recentCreatedUser = ref<SysUserVO>()
const selectedUserRole = ref<'teacher' | 'leader' | 'edu' | 'admin'>('teacher')
const roleUsers = ref<SysUserVO[]>([])
const loadingUsers = ref(false)

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
// 毕业要求导入
const requirementImportVisible = ref(false)
const requirementImportFileList = ref<UploadUserFile[]>([])
const submittingRequirementImport = ref(false)
// 指标点导入
const indicatorImportVisible = ref(false)
const indicatorImportFileList = ref<UploadUserFile[]>([])
const submittingIndicatorImport = ref(false)

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

const userRoleOptions = [
  { label: '课程教师', value: 'teacher' },
  { label: '专业负责人', value: 'leader' },
  { label: '教务管理员', value: 'edu' },
  { label: '系统管理员', value: 'admin' }
] as const

const roleLabelMap: Record<string, string> = {
  teacher: '课程教师',
  leader: '专业负责人',
  edu: '教务管理员',
  admin: '系统管理员'
}

const getRoleLabel = (roleCode?: string) => roleLabelMap[roleCode || ''] || roleCode || '-'
const canManageCourseSection = computed(() => userStore.role === 'edu')
const canManageDictionarySection = computed(() => userStore.role === 'admin')
const canManageUserSection = computed(() => userStore.role === 'admin')
const canManageRequirementSection = computed(() => userStore.role === 'leader')
const canManageStudentSection = computed(() => userStore.role === 'edu')

// ===== 学生库 =====
const studentRows = ref<StudentVO[]>([])
const studentTotal = ref(0)
const loadingStudents = ref(false)
const studentQuery = reactive({
  current: 1,
  pageSize: 10,
  studentNo: '',
  studentName: '',
  majorId: null as number | null
})
const studentImportVisible = ref(false)
const studentImportFileList = ref<UploadUserFile[]>([])
const submittingStudentImport = ref(false)

const loadStudents = async () => {
  loadingStudents.value = true
  try {
    const page = await pageStudents({
      current: studentQuery.current,
      pageSize: studentQuery.pageSize,
      studentNo: studentQuery.studentNo || undefined,
      studentName: studentQuery.studentName || undefined,
      majorId: studentQuery.majorId || undefined
    })
    studentRows.value = page.records
    studentTotal.value = normalizePageFields(page, { current: studentQuery.current, pageSize: studentQuery.pageSize }).total
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '加载学生列表失败')
  } finally {
    loadingStudents.value = false
  }
}

const searchStudents = () => {
  studentQuery.current = 1
  loadStudents()
}

const resetStudentQuery = () => {
  studentQuery.studentNo = ''
  studentQuery.studentName = ''
  studentQuery.majorId = null
  studentQuery.current = 1
  loadStudents()
}

const onStudentPageChange = (p: number) => {
  studentQuery.current = p
  loadStudents()
}

const onStudentSizeChange = (s: number) => {
  studentQuery.pageSize = s
  studentQuery.current = 1
  loadStudents()
}

const openStudentImportDialog = () => {
  studentImportFileList.value = []
  studentImportVisible.value = true
}

const handleStudentImportChange: UploadProps['onChange'] = (uploadFile, uploadFiles) => {
  const fileName = uploadFile.name.toLowerCase()
  if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
    ElMessage.error('请上传 Excel 文件')
    studentImportFileList.value = []
    return
  }
  studentImportFileList.value = uploadFiles.slice(-1)
}

const handleStudentImportRemove: UploadProps['onRemove'] = () => {
  studentImportFileList.value = []
}

const handleDownloadStudentTemplate = async () => {
  try {
    const blob = await downloadStudentTemplate()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '学生导入模板.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '模板下载失败')
  }
}

const submitImportStudents = async () => {
  const file = studentImportFileList.value[0]?.raw
  if (!file) {
    ElMessage.warning('请先选择学生 Excel 文件')
    return
  }
  submittingStudentImport.value = true
  try {
    const result = await importStudentsFromExcel(file)
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
      ElMessage.success(`学生导入完成，共成功导入 ${result.successCount} 条`)
    }
    studentImportVisible.value = false
    studentImportFileList.value = []
    await loadStudents()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '学生导入失败')
  } finally {
    submittingStudentImport.value = false
  }
}

// ===== 学生增删改 =====
const studentDialogVisible = ref(false)
const editingStudent = ref<StudentVO>()
const studentFormRef = ref<FormInstance>()
const submittingStudent = ref(false)
const studentForm = reactive<StudentAddRequest>({
  studentNo: '',
  studentName: '',
  grade: '',
  majorId: undefined as unknown as number
})
const studentRules: FormRules<StudentAddRequest> = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  studentName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  majorId: [{ required: true, message: '请选择专业', trigger: 'change' }]
}

const resetStudentForm = () => {
  studentForm.studentNo = ''
  studentForm.studentName = ''
  studentForm.grade = ''
  studentForm.majorId = availableCourseMajors.value[0]?.id ?? (undefined as unknown as number)
}

const openStudentCreateDialog = () => {
  editingStudent.value = undefined
  resetStudentForm()
  studentDialogVisible.value = true
}

const openStudentEditDialog = (row: StudentVO) => {
  editingStudent.value = row
  studentForm.studentNo = row.studentNo ?? ''
  studentForm.studentName = row.name ?? ''
  studentForm.grade = row.grade ?? ''
  studentForm.majorId = (row.majorId ?? undefined) as unknown as number
  studentDialogVisible.value = true
}

const submitStudentForm = async () => {
  const isValid = await studentFormRef.value?.validate().catch(() => false)
  if (!isValid) return
  submittingStudent.value = true
  try {
    const payload = {
      studentNo: studentForm.studentNo.trim(),
      studentName: studentForm.studentName.trim(),
      grade: studentForm.grade?.trim() ?? '',
      majorId: Number(studentForm.majorId)
    }
    if (editingStudent.value) {
      await updateStudent({ id: editingStudent.value.id, ...payload } as StudentUpdateRequest)
      ElMessage.success('学生已更新')
    } else {
      await createStudent(payload)
      ElMessage.success('学生已创建')
    }
    studentDialogVisible.value = false
    editingStudent.value = undefined
    await loadStudents()
  } catch (error) {
    const message = error instanceof Error ? error.message : '学生保存失败'
    ElMessage.error(message)
  } finally {
    submittingStudent.value = false
  }
}

const handleDeleteStudent = async (row: StudentVO) => {
  try {
    await ElMessageBox.confirm(`确认删除学生 ${row.studentNo} - ${row.name} 吗？`, '删除确认', {
      type: 'warning'
    })
    await deleteStudent(row.id)
    ElMessage.success('学生已删除')
    await loadStudents()
  } catch (error) {
    if (error === 'cancel') return
    const message = error instanceof Error ? error.message : '学生删除失败'
    ElMessage.error(message)
  }
}

const formatDateTime = (value?: string) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
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

const currentUserRoleLabel = computed(() => getRoleLabel(selectedUserRole.value))

const userRows = computed(() =>
  roleUsers.value.map((user) => ({
    ...user,
    roleLabel: getRoleLabel(user.roleCode),
    collegeName: user.collegeName || '-',
    createTimeLabel: formatDateTime(user.createTime)
  }))
)

const requirementFilter = reactive({ majorId: undefined as number | undefined, code: '', name: '' })
const indicatorFilter = reactive({ requirementId: undefined as number | undefined, code: '', name: '' })

const resetRequirementFilter = () => {
  requirementFilter.majorId = undefined
  requirementFilter.code = ''
  requirementFilter.name = ''
}
const resetIndicatorFilter = () => {
  indicatorFilter.requirementId = undefined
  indicatorFilter.code = ''
  indicatorFilter.name = ''
}

const requirementRows = computed(() =>
  requirements.value
    .filter((requirement) => {
      if (requirementFilter.majorId && requirement.majorId !== requirementFilter.majorId) return false
      const code = requirementFilter.code.trim().toLowerCase()
      if (code && !(requirement.requirementCode || '').toLowerCase().includes(code)) return false
      const name = requirementFilter.name.trim().toLowerCase()
      if (name && !(requirement.requirementName || '').toLowerCase().includes(name)) return false
      return true
    })
    .map((requirement) => ({
      code: requirement.requirementCode,
      name: requirement.requirementName,
      majorName: requirement.majorName || '-',
      collegeName: requirement.collegeName || '-',
      description: requirement.description || '-',
      raw: requirement
    }))
)

const indicatorRows = computed(() =>
  indicators.value
    .filter((indicator) => {
      if (indicatorFilter.requirementId && indicator.requirementId !== indicatorFilter.requirementId) return false
      const code = indicatorFilter.code.trim().toLowerCase()
      if (code && !(indicator.indicatorCode || '').toLowerCase().includes(code)) return false
      const name = indicatorFilter.name.trim().toLowerCase()
      if (name && !(indicator.indicatorName || '').toLowerCase().includes(name)) return false
      return true
    })
    .map((indicator) => ({
      requirementCode: indicator.requirementCode || '-',
      code: indicator.indicatorCode,
      name: indicator.indicatorName,
      description: indicator.description || '-',
      raw: indicator
    }))
)

const availableCourseMajors = computed<SysDictMajorSimpleVO[]>(() => {
  if (majors.value.length) {
    return majors.value
  }

  const majorMap = new Map<number, SysDictMajorSimpleVO>()
  for (const course of courses.value) {
    if (course.majorId && course.majorName) {
      majorMap.set(course.majorId, {
        id: course.majorId,
        majorName: course.majorName
      } as SysDictMajorSimpleVO)
    }
  }
  return Array.from(majorMap.values())
})

const availableRequirementMajors = computed<SysDictMajorSimpleVO[]>(() => {
  if (majors.value.length) {
    return majors.value
  }

  const majorMap = new Map<number, SysDictMajorSimpleVO>()
  for (const requirement of requirements.value) {
    if (requirement.majorId && requirement.majorName) {
      majorMap.set(requirement.majorId, {
        id: requirement.majorId,
        majorName: requirement.majorName
      } as SysDictMajorSimpleVO)
    }
  }
  for (const course of courses.value) {
    if (course.majorId && course.majorName) {
      majorMap.set(course.majorId, {
        id: course.majorId,
        majorName: course.majorName
      } as SysDictMajorSimpleVO)
    }
  }
  return Array.from(majorMap.values())
})

const resetCreateForm = () => {
  createForm.courseCode = ''
  createForm.courseName = ''
  createForm.courseNature = '必修'
  createForm.credit = 3
  createForm.majorId = availableCourseMajors.value[0]?.id
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
  requirementForm.majorId = availableRequirementMajors.value[0]?.id
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

const openEditDialog = async (course: CourseVO) => {
  try {
    const detail = await getCourse(course.id)
    editingCourse.value = detail
    createForm.courseCode = detail.courseCode
    createForm.courseName = detail.courseName
    createForm.courseNature = detail.courseNature || '必修'
    createForm.credit = Number(detail.credit ?? 3)
    createForm.majorId = detail.majorId
    createDialogVisible.value = true
  } catch (error) {
    const message = error instanceof Error ? error.message : '课程详情加载失败'
    ElMessage.error(message)
  }
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

const openMajorEditDialog = async (major: SysDictMajorVO) => {
  try {
    const detail = await getMajor(major.id)
    editingMajor.value = detail
    majorForm.majorCode = detail.majorCode
    majorForm.majorName = detail.majorName
    majorForm.collegeId = Number(detail.collegeId || 0)
    majorDialogVisible.value = true
  } catch (error) {
    const message = error instanceof Error ? error.message : '专业详情加载失败'
    ElMessage.error(message)
  }
}

const openCollegeCreateDialog = () => {
  editingCollege.value = undefined
  resetCollegeForm()
  collegeDialogVisible.value = true
}

const openCollegeEditDialog = async (college: SysDictCollegeVO) => {
  try {
    const detail = await getCollege(college.id)
    editingCollege.value = detail
    collegeForm.collegeName = detail.collegeName
    collegeDialogVisible.value = true
  } catch (error) {
    const message = error instanceof Error ? error.message : '学院详情加载失败'
    ElMessage.error(message)
  }
}

const openSchoolYearCreateDialog = () => {
  editingSchoolYear.value = undefined
  resetSchoolYearForm()
  schoolYearDialogVisible.value = true
}

const openSchoolYearEditDialog = async (schoolYear: SysDictSchoolYearVO) => {
  try {
    const detail = await getSchoolYear(schoolYear.id)
    editingSchoolYear.value = detail
    schoolYearForm.yearName = detail.yearName
    schoolYearForm.semesterName = detail.semesterName
    schoolYearDialogVisible.value = true
  } catch (error) {
    const message = error instanceof Error ? error.message : '学年学期详情加载失败'
    ElMessage.error(message)
  }
}

const openRequirementCreateDialog = () => {
  editingRequirement.value = undefined
  resetRequirementForm()
  requirementDialogVisible.value = true
}

const openRequirementEditDialog = async (requirement: GraduationRequirementVO) => {
  try {
    const detail = await getGraduationRequirement(requirement.id)
    editingRequirement.value = detail
    requirementForm.requirementCode = detail.requirementCode
    requirementForm.requirementName = detail.requirementName
    requirementForm.description = detail.description || ''
    requirementForm.majorId = detail.majorId
    requirementDialogVisible.value = true
  } catch (error) {
    const message = error instanceof Error ? error.message : '毕业要求详情加载失败'
    ElMessage.error(message)
  }
}

// ===== 毕业要求导入 =====
const openRequirementImportDialog = () => {
  requirementImportFileList.value = []
  requirementImportVisible.value = true
}

const handleRequirementImportChange: UploadProps['onChange'] = (uploadFile, uploadFiles) => {
  const fileName = uploadFile.name.toLowerCase()
  if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
    ElMessage.warning('仅支持 .xlsx/.xls 文件')
    requirementImportFileList.value = []
    return
  }
  requirementImportFileList.value = uploadFiles.slice(-1)
}

const handleRequirementImportRemove: UploadProps['onRemove'] = () => {
  requirementImportFileList.value = []
}

const handleDownloadRequirementTemplate = async () => {
  try {
    const blob = await downloadGraduationRequirementTemplate()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a'); a.href = url; a.download = '毕业要求导入模板.xlsx'; a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板已开始下载')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '模板下载失败')
  }
}

const submitRequirementImport = async () => {
  const file = requirementImportFileList.value[0]?.raw
  if (!file) { ElMessage.warning('请先选择文件'); return }
  submittingRequirementImport.value = true
  try {
    const result = await importGraduationRequirementsFromExcel(file as File)
    requirementImportVisible.value = false
    requirementImportFileList.value = []
    await showRequirementIndicatorImportResult(result as Record<string, unknown>)
    await Promise.all([loadRequirements(), loadIndicators()])
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '毕业要求导入失败')
  } finally {
    submittingRequirementImport.value = false
  }
}

// ===== 指标点导入 =====
const openIndicatorImportDialog = () => {
  indicatorImportFileList.value = []
  indicatorImportVisible.value = true
}

const handleIndicatorImportChange: UploadProps['onChange'] = (uploadFile, uploadFiles) => {
  const fileName = uploadFile.name.toLowerCase()
  if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
    ElMessage.warning('仅支持 .xlsx/.xls 文件')
    indicatorImportFileList.value = []
    return
  }
  indicatorImportFileList.value = uploadFiles.slice(-1)
}

const handleIndicatorImportRemove: UploadProps['onRemove'] = () => {
  indicatorImportFileList.value = []
}

const handleDownloadIndicatorTemplate = async () => {
  try {
    const blob = await downloadIndicatorPointTemplate()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a'); a.href = url; a.download = '指标点导入模板.xlsx'; a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板已开始下载')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '模板下载失败')
  }
}

const submitIndicatorImport = async () => {
  const file = indicatorImportFileList.value[0]?.raw
  if (!file) { ElMessage.warning('请先选择文件'); return }
  submittingIndicatorImport.value = true
  try {
    const result = await importIndicatorPointsFromExcel(file as File)
    indicatorImportVisible.value = false
    indicatorImportFileList.value = []
    await showRequirementIndicatorImportResult(result as Record<string, unknown>)
    await loadIndicators()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '指标点导入失败')
  } finally {
    submittingIndicatorImport.value = false
  }
}

const showRequirementIndicatorImportResult = async (result: Record<string, unknown>) => {
  const failCount = Number(result.failCount || 0)
  if (failCount > 0) {
    const failDetails = result.failDetails as Array<Record<string, string>> | undefined
    const preview = (failDetails ?? []).slice(0, 5)
      .map((item) => `第 ${item.row || '-'} 行：${item.reason || '导入失败'}`).join('\n')
    await ElMessageBox.alert(
      `总计 ${result.total} 条，成功 ${result.successCount} 条，失败 ${failCount} 条。${preview ? `\n\n失败示例：\n${preview}` : ''}`,
      '导入完成', { confirmButtonText: '知道了' }
    )
  } else {
    ElMessage.success(`导入完成，共成功导入 ${result.successCount} 条`)
  }
}

const openIndicatorCreateDialog = () => {
  editingIndicator.value = undefined
  resetIndicatorForm()
  indicatorDialogVisible.value = true
}

const openIndicatorEditDialog = async (indicator: IndicatorPointVO) => {
  try {
    const detail = await getIndicator(indicator.id)
    editingIndicator.value = detail
    indicatorForm.indicatorCode = detail.indicatorCode
    indicatorForm.indicatorName = detail.indicatorName
    indicatorForm.description = detail.description || ''
    indicatorForm.requirementId = detail.requirementId || requirements.value[0]?.id || 0
    indicatorDialogVisible.value = true
  } catch (error) {
    const message = error instanceof Error ? error.message : '指标点详情加载失败'
    ElMessage.error(message)
  }
}

const openUserCreateDialog = () => {
  resetUserForm()
  userDialogVisible.value = true
}

const loadUsersByRole = async (roleCode = selectedUserRole.value) => {
  loadingUsers.value = true
  try {
    roleUsers.value = await listUsersByRole(roleCode)
  } finally {
    loadingUsers.value = false
  }
}

const handleUserRoleChange = async () => {
  try {
    await loadUsersByRole()
  } catch (error) {
    const message = error instanceof Error ? error.message : '用户列表加载失败'
    ElMessage.error(message)
  }
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
    recentCreatedUser.value = {
      id: userId,
      username: userForm.username.trim(),
      roleCode: userForm.roleCode,
      collegeName,
      status: userForm.status ?? 1
    }

    userDialogVisible.value = false
    if (selectedUserRole.value !== userForm.roleCode) {
      selectedUserRole.value = userForm.roleCode as 'teacher' | 'leader' | 'edu' | 'admin'
    }
    await loadUsersByRole()
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
  const loaders: Array<{ label: string; task: Promise<unknown> }> = []

  if (canManageDictionarySection.value) {
    loaders.push(
      { label: '学院下拉', task: loadColleges() },
      { label: '学院列表', task: loadCollegeRecords() },
      { label: '学年学期列表', task: loadSchoolYearRecords() },
      { label: '专业下拉', task: loadMajors() },
      { label: '专业列表', task: loadMajorRecords() }
    )
  }

  if (canManageCourseSection.value) {
    loaders.push(
      { label: '课程列表', task: loadCourses() },
      { label: '教学班列表', task: loadTeachingClasses() }
    )
  }

  if (canManageStudentSection.value) {
    loaders.push(
      { label: '专业下拉', task: loadMajors() },
      { label: '学生列表', task: loadStudents() }
    )
  }

  if (canManageRequirementSection.value) {
    loaders.push(
      { label: '专业下拉', task: loadMajors() },
      { label: '毕业要求列表', task: loadRequirements() },
      { label: '指标点列表', task: loadIndicators() }
    )
  }

  if (canManageUserSection.value) {
    loaders.push({ label: '用户列表', task: loadUsersByRole() })
  }

  const loadResults = await Promise.allSettled(loaders.map((item) => item.task))
  loadResults.forEach((result, index) => {
    if (result.status === 'rejected') {
      const reason = result.reason instanceof Error ? result.reason.message : `${loaders[index].label}加载失败`
      ElMessage.error(reason)
    }
  })
})
</script>

<style scoped>
.user-toolbar {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 12px;
}

.user-hint {
  line-height: 1.4;
}

.user-created-note {
  margin-bottom: 12px;
  color: #22543d;
  font-size: 13px;
  font-weight: 600;
}

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
