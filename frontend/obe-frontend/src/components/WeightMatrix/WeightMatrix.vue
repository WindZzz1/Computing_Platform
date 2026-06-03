<template>
  <div v-loading="loading">
    <el-table :data="rows" border height="430" size="small">
      <el-table-column fixed prop="courseName" label="课程名称" min-width="180" />
      <el-table-column v-for="indicator in indicators" :key="indicator.id" :label="indicator.indicatorCode" min-width="110" align="center">
        <template #default="{ row }">
          <el-input-number
            v-model="row.weights[indicator.id]"
            :min="0"
            :max="1"
            :step="0.05"
            :precision="2"
            controls-position="right"
            class="cell-input"
          />
        </template>
      </el-table-column>
    </el-table>
    <div class="sum-row">
      <span>列合计</span>
      <div v-for="indicator in indicators" :key="indicator.id" :class="ok(indicator.id) ? 'success-text' : 'danger-text'">
        {{ indicator.indicatorCode }}: {{ sum(indicator.id).toFixed(2) }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface MatrixIndicator {
  id: number
  indicatorCode: string
}

interface MatrixRow {
  courseId: number
  courseName: string
  weights: Record<number, number>
}

const props = defineProps<{
  loading?: boolean
  indicators: MatrixIndicator[]
  rows: MatrixRow[]
}>()

const loading = computed(() => props.loading ?? false)
const indicators = computed(() => props.indicators ?? [])
const rows = computed(() => props.rows ?? [])

const sum = (id: number) => rows.value.reduce((acc, row) => acc + (Number(row.weights[id]) || 0), 0)
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
