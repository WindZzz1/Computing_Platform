<template>
  <div class="page">
    <h1 class="page-title">报表与导出</h1>
    <p class="page-desc">导出课程目标达成情况、专业毕业要求达成度报告、支撑矩阵台账和可追溯 Excel 明细。</p>

    <section class="page-grid">
      <div class="panel span-5">
        <h3 class="panel-title">单项达成度表</h3>
        <el-table :data="indicators" border>
          <el-table-column prop="code" label="指标点" width="90" />
          <el-table-column label="达成度" width="100">
            <template #default="{ row }">{{ row.achievement.toFixed(3) }}</template>
          </el-table-column>
          <el-table-column prop="name" label="说明" />
        </el-table>
      </div>

      <div class="panel span-7">
        <h3 class="panel-title">可导出报表</h3>
        <el-table :data="reports" border>
          <el-table-column prop="name" label="报表名称" />
          <el-table-column prop="role" label="可导出角色" width="210" />
          <el-table-column prop="source" label="数据来源表" width="240" />
          <el-table-column label="操作" width="180">
            <template #default>
              <el-button type="primary" link>导出 Excel</el-button>
              <el-button type="primary" link>导出 PDF</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { indicators } from '@/api/mock'

const reports = [
  { name: '课程目标达成情况评价表', role: '课程教师', source: 'result_student_objective / result_course_indicator' },
  { name: '专业毕业要求达成度报告', role: '专业负责人、教务管理员', source: 'result_major_indicator' },
  { name: '宏观支撑矩阵台账', role: '专业负责人', source: 'matrix_course_indicator' },
  { name: '学生考核点原始成绩明细', role: '课程教师、教务管理员', source: 'student_score / assessment_point' }
]
</script>
