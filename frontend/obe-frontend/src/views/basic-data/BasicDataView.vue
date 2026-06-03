<template>
  <div class="page">
    <h1 class="page-title">基础数据管理</h1>
    <p class="page-desc">维护学院、专业、学年学期、课程库、教学班、学生名单、毕业要求与二级指标点，为三层达成度计算提供统一数据源。</p>

    <section class="page-grid">
      <div class="panel span-4">
        <div class="toolbar">
          <h3 class="panel-title">学院字典</h3>
          <el-tag type="success">后端接口</el-tag>
        </div>
        <el-table v-loading="loading" :data="collegeRows" border size="small">
          <el-table-column prop="collegeName" label="学院名称" />
          <el-table-column prop="collegeCode" label="学院编码" width="120">
            <template #default="{ row }">{{ row.collegeCode || '--' }}</template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel span-4">
        <div class="toolbar">
          <h3 class="panel-title">专业字典</h3>
          <el-tag type="success">后端接口</el-tag>
        </div>
        <el-table v-loading="loading" :data="majorRows" border size="small">
          <el-table-column prop="majorName" label="专业名称" />
          <el-table-column prop="majorCode" label="专业代码" width="120" />
          <el-table-column prop="collegeName" label="所属学院" min-width="120">
            <template #default="{ row }">{{ row.collegeName || '--' }}</template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel span-4">
        <div class="toolbar">
          <h3 class="panel-title">学年学期</h3>
          <el-tag type="success">后端接口</el-tag>
        </div>
        <el-table v-loading="loading" :data="schoolYearRows" border size="small">
          <el-table-column prop="yearName" label="学年" />
          <el-table-column prop="semesterName" label="学期" width="120" />
        </el-table>
      </div>

      <div class="panel span-8">
        <div class="toolbar">
          <h3 class="panel-title">计算机科学与技术专业课程库</h3>
          <div>
            <el-tag>mock展示</el-tag>
            <el-button>批量导入</el-button>
            <el-button type="primary">新增课程</el-button>
          </div>
        </div>
        <el-table :data="courses" border>
          <el-table-column prop="code" label="课程代码" width="120" />
          <el-table-column prop="name" label="课程名称" min-width="150" />
          <el-table-column prop="credit" label="学分" width="80" />
          <el-table-column prop="teacher" label="主讲教师" width="110" />
          <el-table-column prop="studentCount" label="学生数" width="90" />
          <el-table-column prop="term" label="学年学期" min-width="170" />
          <el-table-column prop="status" label="状态" width="110">
            <template #default="{ row }"><StatusTag :status="row.status" /></template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel span-4">
        <h3 class="panel-title">角色与权限</h3>
        <el-table :data="roles" border size="small">
          <el-table-column prop="role" label="角色" width="110" />
          <el-table-column prop="scope" label="主要权限" />
        </el-table>
      </div>

      <div class="panel span-12">
        <div class="toolbar">
          <h3 class="panel-title">毕业要求与二级指标点</h3>
          <el-tag>mock展示</el-tag>
          <el-button type="primary">导入指标点</el-button>
        </div>
        <el-table :data="indicators" border>
          <el-table-column prop="requirement" label="毕业要求" width="130" />
          <el-table-column prop="code" label="指标点编号" width="120" />
          <el-table-column prop="name" label="指标点名称" />
          <el-table-column label="专业级达成度" width="140">
            <template #default="{ row }">{{ row.achievement.toFixed(3) }}</template>
          </el-table-column>
          <el-table-column label="数据表映射" width="170">
            <template #default>graduation_requirement / indicator</template>
          </el-table-column>
        </el-table>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { courses, indicators } from '@/api/mock'
import { pageColleges, pageMajors, pageSchoolYears, type CollegeVO, type MajorVO, type SchoolYearVO } from '@/api/dict'
import StatusTag from '@/components/StatusTag/StatusTag.vue'

const loading = ref(false)
const collegeRows = ref<CollegeVO[]>([])
const majorRows = ref<MajorVO[]>([])
const schoolYearRows = ref<SchoolYearVO[]>([])

const roles = [
  { role: '管理员', scope: '用户、学院、专业字典维护' },
  { role: '教务', scope: '课程库、教学班、学生名单导入' },
  { role: '专业负责人', scope: '毕业要求、宏观支撑矩阵、专业级计算' },
  { role: '主讲教师', scope: '课程目标、考核点、成绩与课程级计算' }
]

const loadDictData = async () => {
  loading.value = true
  try {
    const [collegePage, majorPage, schoolYearPage] = await Promise.all([
      pageColleges({ pageSize: 20 }),
      pageMajors({ pageSize: 20 }),
      pageSchoolYears({ pageSize: 20 })
    ])
    collegeRows.value = collegePage.records || []
    majorRows.value = majorPage.records || []
    schoolYearRows.value = schoolYearPage.records || []
  } finally {
    loading.value = false
  }
}

onMounted(loadDictData)
</script>
