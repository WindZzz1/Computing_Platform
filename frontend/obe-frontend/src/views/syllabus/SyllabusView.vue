<template>
  <div class="page">
    <h1 class="page-title">课程大纲配置</h1>
    <p class="page-desc">课程教师维护课程目标 CO、课程目标到指标点的内部贡献权重 wjk，以及考核点和满分值。</p>

    <section class="page-grid">
      <div class="panel span-5">
        <h3 class="panel-title">课程目标</h3>
        <el-table :data="objectives" border>
          <el-table-column prop="code" label="课程目标" width="110" />
          <el-table-column prop="content" label="目标描述" />
          <el-table-column label="示例达成度" width="110">
            <template #default="{ row }">{{ row.achievement.toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel span-7">
        <div class="toolbar">
          <h3 class="panel-title">内部贡献权重</h3>
          <el-tag type="success">列合计必须为 1.00</el-tag>
        </div>
        <el-table :data="weightRows" border>
          <el-table-column prop="objective" label="目标点" width="130" />
          <el-table-column label="3.1（专业知识）">
            <template #default="{ row }"><el-input-number v-model="row.w31" :min="0" :max="1" :step="0.1" :precision="2" /></template>
          </el-table-column>
          <el-table-column label="3.3（工具实践）">
            <template #default="{ row }"><el-input-number v-model="row.w33" :min="0" :max="1" :step="0.1" :precision="2" /></template>
          </el-table-column>
          <el-table-column label="3.4（信息技术组织）">
            <template #default="{ row }"><el-input-number v-model="row.w34" :min="0" :max="1" :step="0.1" :precision="2" /></template>
          </el-table-column>
        </el-table>
        <div class="weight-footer">
          <span>列合计</span>
          <span class="success-text">3.1 = 1.00</span>
          <span class="success-text">3.3 = 1.00</span>
          <span class="success-text">3.4 = 1.00</span>
        </div>
        <el-button type="primary" style="margin-top: 14px">保存</el-button>
      </div>

      <div class="panel span-12">
        <h3 class="panel-title">考核点管理</h3>
        <el-table :data="assessments" border>
          <el-table-column prop="name" label="考核点名称" />
          <el-table-column prop="score" label="分值" width="90" />
          <el-table-column label="对应课程目标" width="150">
            <template #default="{ row }">{{ objectives.find((item) => item.id === row.objectiveId)?.code }}</template>
          </el-table-column>
          <el-table-column prop="method" label="考核方式" width="120" />
          <el-table-column label="操作" width="150">
            <template #default>
              <el-button link type="primary">编辑</el-button>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { assessments, objectives } from '@/api/mock'

const weightRows = ref([
  { objective: '目标1-1 知识', w31: 1.0, w33: 0, w34: 0 },
  { objective: '目标2-1 能力', w31: 0, w33: 0.5, w34: 0 },
  { objective: '目标2-2 能力', w31: 0, w33: 0.5, w34: 0 },
  { objective: '目标3-1 价值', w31: 0, w33: 0, w34: 1.0 }
])
</script>

<style scoped>
.weight-footer {
  display: flex;
  gap: 24px;
  padding: 12px;
  border: 1px solid var(--line);
  border-top: 0;
  border-radius: 0 0 8px 8px;
  background: #fbfdff;
  font-weight: 700;
}
</style>
