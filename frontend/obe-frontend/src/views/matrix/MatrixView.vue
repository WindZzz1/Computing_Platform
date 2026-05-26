<template>
  <div class="page">
    <h1 class="page-title">宏观支撑矩阵配置</h1>
    <p class="page-desc">专业负责人配置课程到毕业要求指标点的总支撑权重 Wc。每个指标点列合计应为 1.00，用于第三级专业达成度计算。</p>

    <section class="page-grid">
      <div class="panel span-12">
        <div class="toolbar">
          <div class="formula">Gk = Σ(Ek × Wc)，其中 Ek 来自课程级指标点达成度，Wc 来自 matrix_course_indicator。</div>
          <div>
            <el-select v-model="plan" style="width: 230px">
              <el-option label="计算机科学与技术 2023" value="2023" />
              <el-option label="计算机科学与技术 2024" value="2024" />
            </el-select>
            <el-button type="primary">保存</el-button>
          </div>
        </div>
        <WeightMatrix />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import WeightMatrix from '@/components/WeightMatrix/WeightMatrix.vue'

const plan = ref('2023')
</script>
