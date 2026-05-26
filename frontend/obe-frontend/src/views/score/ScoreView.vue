<template>
  <div class="page">
    <h1 class="page-title">成绩导入与预览</h1>
    <p class="page-desc">课程教师导入学生考核点原始成绩，系统按考核点归集到课程目标，再计算课程级指标点达成度。</p>

    <section class="page-grid">
      <div class="panel span-4">
        <h3 class="panel-title">成绩导入</h3>
        <ExcelUpload />
        <div class="import-actions">
          <el-button type="primary">导入 Excel 模板</el-button>
          <el-button>下载模板</el-button>
        </div>
      </div>

      <div class="panel span-8">
        <div class="toolbar">
          <h3 class="panel-title">成绩预览</h3>
          <el-button type="primary" @click="locked = true">计算并锁定课程</el-button>
        </div>
        <el-table :data="scoreRows" border>
          <el-table-column prop="sid" label="学号" width="130" />
          <el-table-column prop="name" label="姓名" width="90" />
          <el-table-column prop="homework" label="平时成绩（10分）" />
          <el-table-column prop="experiment" label="实验（20分）" />
          <el-table-column prop="mid" label="期中测试（20分）" />
          <el-table-column prop="final" label="期末考试（50分）" />
          <el-table-column label="操作" width="90">
            <template #default><el-button link type="primary">编辑</el-button></template>
          </el-table-column>
        </el-table>
        <el-alert
          v-if="locked"
          title="课程已锁定：一级学生-课程目标达成度与二级课程-指标点达成度已生成"
          type="success"
          show-icon
          style="margin-top: 12px"
        />
      </div>

      <div class="panel span-12">
        <h3 class="panel-title">课程级计算结果</h3>
        <el-table :data="results" border>
          <el-table-column prop="objective" label="课程目标" />
          <el-table-column prop="achievement" label="达成度" width="110" />
          <el-table-column prop="classAvg" label="班级平均得分率" width="150" />
          <el-table-column prop="indicator" label="支撑指标点" />
          <el-table-column prop="indicatorAchievement" label="课程级达成度 Ek" width="150" />
        </el-table>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { scoreRows } from '@/api/mock'
import ExcelUpload from '@/components/ExcelUpload/ExcelUpload.vue'

const locked = ref(false)
const results = [
  { objective: '目标1-1 知识', achievement: '0.72', classAvg: '72.00%', indicator: '3.1（专业知识）', indicatorAchievement: '0.72' },
  { objective: '目标2-1 能力', achievement: '0.68', classAvg: '68.00%', indicator: '3.3（工具实践）', indicatorAchievement: '0.715' },
  { objective: '目标2-2 能力', achievement: '0.75', classAvg: '75.00%', indicator: '3.4（信息技术组织）', indicatorAchievement: '0.80' },
  { objective: '目标3-1 价值', achievement: '0.80', classAvg: '80.00%', indicator: '8.3（职业规范）', indicatorAchievement: '0.80' }
]
</script>

<style scoped>
.import-actions {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}
</style>
