<template>
  <div class="page">
    <h1 class="page-title">专业级计算看板</h1>
    <p class="page-desc">专业负责人或教务管理员在课程完成计算并锁定后，执行第三级专业指标点最终达成度 Gk 计算。</p>

    <section class="page-grid">
      <div class="panel span-8">
        <div class="toolbar">
          <h3 class="panel-title">课程锁定状态</h3>
          <el-select v-model="plan" style="width: 220px">
            <el-option label="计算机科学与技术 2023" value="2023" />
          </el-select>
        </div>
        <el-table :data="courses" border>
          <el-table-column prop="name" label="课程名称" />
          <el-table-column prop="teacher" label="所属教师" width="110" />
          <el-table-column prop="credit" label="教学班" width="90" />
          <el-table-column prop="studentCount" label="学生数" width="90" />
          <el-table-column prop="status" label="计算状态" width="120">
            <template #default="{ row }"><StatusTag :status="row.status" /></template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button link type="primary">{{ row.status === '已锁定' ? '查看' : '提醒' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="run-bar">
          <el-button>刷新状态</el-button>
          <el-button type="primary" :disabled="!canRun" @click="done = true">执行专业级计算</el-button>
        </div>
        <el-alert v-if="!canRun" title="仍有课程未锁定，暂不能执行专业级计算。" type="warning" show-icon />
        <el-alert v-if="done" title="专业级指标点最终达成度已写入 result_major_indicator。" type="success" show-icon />
      </div>

      <div class="panel span-4">
        <h3 class="panel-title">三层计算链路</h3>
        <el-steps direction="vertical" :active="3" finish-status="success">
          <el-step title="一级" description="学生考核点成绩 -> 学生课程目标达成度" />
          <el-step title="二级" description="课程目标达成度 + wjk -> 课程指标点 Ek" />
          <el-step title="三级" description="课程指标点 Ek + Wc -> 专业指标点 Gk" />
        </el-steps>
      </div>

      <div class="panel span-12">
        <h3 class="panel-title">专业指标点结果</h3>
        <el-table :data="indicators" border>
          <el-table-column prop="code" label="毕业要求指标点" width="150" />
          <el-table-column prop="name" label="指标点名称" />
          <el-table-column label="最终达成度 Gk" width="150">
            <template #default="{ row }">{{ row.achievement.toFixed(3) }}</template>
          </el-table-column>
          <el-table-column label="评价" width="120">
            <template #default="{ row }">
              <el-tag :type="row.achievement >= 0.7 ? 'success' : 'warning'">{{ row.achievement >= 0.7 ? '达成' : '待改进' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { courses, indicators } from '@/api/mock'
import StatusTag from '@/components/StatusTag/StatusTag.vue'

const plan = ref('2023')
const done = ref(false)
const canRun = computed(() => courses.every((course) => course.status === '已锁定'))
</script>

<style scoped>
.run-bar {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin: 14px 0;
}
</style>
