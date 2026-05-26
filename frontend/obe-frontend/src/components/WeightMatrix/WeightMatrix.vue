<template>
  <el-table :data="rows" border height="430" size="small">
    <el-table-column fixed prop="course" label="课程名称" width="140" />
    <el-table-column v-for="ind in indicators" :key="ind.id" :label="ind.code" min-width="106" align="center">
      <template #default="{ row }">
        <el-input-number
          v-if="(row.weights[ind.id] || 0) > 0"
          v-model="row.weights[ind.id]"
          :min="0"
          :max="1"
          :step="0.05"
          :precision="2"
          controls-position="right"
          class="cell-input"
        />
        <span v-else class="muted">--</span>
      </template>
    </el-table-column>
  </el-table>
  <div class="sum-row">
    <span>列合计</span>
    <div v-for="ind in indicators" :key="ind.id" :class="ok(ind.id) ? 'success-text' : 'danger-text'">
      {{ ind.code }}: {{ sum(ind.id).toFixed(2) }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { indicators, supportMatrixRows } from '@/api/mock'

const rows = ref(supportMatrixRows.map((row) => ({ course: row.course, weights: { ...row.weights } })))
const sum = (id: number) => rows.value.reduce((acc, row) => acc + (row.weights[id] || 0), 0)
const ok = (id: number) => Math.abs(sum(id) - 1) <= 0.001
</script>

<style scoped>
.sum-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px;
  padding: 12px;
  border: 1px solid var(--line);
  border-top: 0;
  border-radius: 0 0 8px 8px;
  background: #fbfdff;
  font-weight: 700;
}
</style>
