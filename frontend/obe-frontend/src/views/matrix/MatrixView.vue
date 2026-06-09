<template>
  <div class="page">
    <h1 class="page-title">宏观支撑矩阵配置</h1>
    <p class="page-desc">页面已连接真实后端矩阵配置接口，会根据专业自动加载课程与指标点数据。</p>

    <section class="page-grid">
      <div class="panel span-12">
        <div class="toolbar">
          <div class="formula">
            {{ currentMajorLabel }}
          </div>
          <div class="toolbar-actions">
            <el-select v-model="selectedMajorId" style="width: 260px" @change="reloadMatrix">
              <el-option
                v-for="major in majors"
                :key="major.id"
                :label="major.majorName"
                :value="major.id"
              />
            </el-select>
            <el-button @click="reloadMatrix">刷新</el-button>
            <el-button type="primary" :loading="saving" :disabled="!selectedMajorId" @click="saveMatrix">保存</el-button>
          </div>
        </div>
        <el-alert
          :type="matrixCheckValid ? 'success' : 'warning'"
          show-icon
          :closable="false"
          :title="matrixCheckMessage"
          style="margin-bottom: 12px"
        />
        <WeightMatrix :loading="loading" :indicators="indicatorRows" :rows="matrixRows" />
        <div class="matrix-footer">
          <span class="matrix-footer-label">列合计</span>
          <span
            v-for="indicator in indicatorRows"
            :key="indicator.id"
            :class="isColumnOk(indicator.id) ? 'success-text' : 'danger-text'"
          >
            {{ indicator.indicatorCode }} = {{ getColumnSum(indicator.id).toFixed(2) }}
          </span>
        </div>
        <div v-if="matrixCheckPendingItems.length" class="matrix-warning-list">
          <span class="formula">待处理：</span>
          <el-tag
            v-for="item in matrixCheckPendingItems"
            :key="item.id"
            type="warning"
            effect="plain"
          >
            {{ item.label }}
          </el-tag>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import WeightMatrix from '@/components/WeightMatrix/WeightMatrix.vue'
import { listMajors } from '@/api/major'
import { checkMatrixConfig, getMatrixConfig, saveMatrixConfig } from '@/api/matrix'
import type { MatrixConfigVO, MatrixWeightCheckVO, SysDictMajorSimpleVO } from '@/api/backend'

type MatrixRow = {
  courseId: number
  courseName: string
  weights: Record<number, number>
}

const loading = ref(false)
const route = useRoute()
const saving = ref(false)
const majors = ref<SysDictMajorSimpleVO[]>([])
const selectedMajorId = ref<number>()
const matrixConfig = ref<MatrixConfigVO>()
const indicatorRows = ref<MatrixConfigVO['indicators']>([])
const matrixRows = ref<MatrixRow[]>([])
const matrixCheck = ref<MatrixWeightCheckVO>()

const currentMajorLabel = computed(() => {
  if (!matrixConfig.value?.majorName) return '当前专业'
  return `当前专业：${matrixConfig.value.majorName}`
})

const matrixCheckValid = computed(() => matrixCheck.value?.valid ?? !invalidIndicators.value.length)
const matrixCheckMessage = computed(() => {
  if (!indicatorRows.value.length) return '当前专业暂无指标点，可先继续准备矩阵基础数据。'
  if (matrixCheck.value?.message) return matrixCheck.value.message
  return matrixCheckValid.value
    ? '当前矩阵权重校验已通过，可以直接保存。'
    : '当前矩阵权重还未通过校验，请先把各指标点列合计调整到 1.00。'
})

const matrixCheckPendingItems = computed(() =>
  invalidIndicators.value.map((indicator) => ({
    id: indicator.id,
    label: `${indicator.indicatorCode} 当前合计 ${getColumnSum(indicator.id).toFixed(2)}`
  }))
)

const getColumnSum = (indicatorId: number) =>
  matrixRows.value.reduce((acc, row) => acc + (Number(row.weights[indicatorId]) || 0), 0)

const isColumnOk = (indicatorId: number) => Math.abs(getColumnSum(indicatorId) - 1) <= 0.001

const invalidIndicators = computed(() =>
  indicatorRows.value.filter((indicator) => Math.abs(getColumnSum(indicator.id) - 1) > 0.001)
)

const buildMatrixItems = () =>
  matrixRows.value.flatMap((row) =>
    Object.entries(row.weights)
      .filter(([, value]) => Number(value) > 0)
      .map(([indicatorId, value]) => ({
        courseId: row.courseId,
        indicatorId: Number(indicatorId),
        totalWeight: Number(Number(value).toFixed(2))
      }))
  )

const updateMatrixCheckState = async () => {
  if (!selectedMajorId.value || !indicatorRows.value.length || !matrixRows.value.length) {
    matrixCheck.value = {
      valid: true,
      message: '当前专业还没有可校验的矩阵课程或指标点数据。'
    }
    return
  }

  try {
    matrixCheck.value = await checkMatrixConfig(selectedMajorId.value, buildMatrixItems())
  } catch (error) {
    matrixCheck.value = {
      valid: false,
      message: error instanceof Error ? error.message : '矩阵校验结果读取失败'
    }
  }
}

const rebuildMatrixRows = (config: MatrixConfigVO) => {
  indicatorRows.value = config.indicators ?? []
  matrixRows.value = config.courses.map((course) => {
    const weights = Object.fromEntries(indicatorRows.value.map((indicator) => [indicator.id, 0]))
    for (const item of config.matrixData) {
      if (item.courseId === course.id) {
        weights[item.indicatorId] = Number(item.totalWeight ?? 0)
      }
    }

    return {
      courseId: course.id,
      courseName: `${course.courseCode} - ${course.courseName}`,
      weights
    }
  })
}

const reloadMatrix = async () => {
  if (!selectedMajorId.value) return

  loading.value = true
  try {
    matrixConfig.value = await getMatrixConfig(selectedMajorId.value)
    rebuildMatrixRows(matrixConfig.value)
    await updateMatrixCheckState()
  } catch (error) {
    const message = error instanceof Error ? error.message : '矩阵配置加载失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const saveMatrix = async () => {
  if (!selectedMajorId.value) return

  if (!matrixRows.value.length || !indicatorRows.value.length) {
    ElMessage.warning('当前专业下没有可保存的课程或指标点数据')
    return
  }

  if (invalidIndicators.value.length) {
    ElMessage.warning(`以下指标点列合计不为 1：${invalidIndicators.value.map((item) => item.indicatorCode).join('、')}`)
    return
  }

  const items = buildMatrixItems()

  saving.value = true
  try {
    const checkResult = await checkMatrixConfig(selectedMajorId.value, items)
    if (!checkResult.valid) {
      ElMessage.warning(checkResult.message || '矩阵权重校验未通过，请先调整后再保存')
      return
    }

    await saveMatrixConfig(selectedMajorId.value, items)
    ElMessage.success('矩阵配置已保存')
    await reloadMatrix()
  } catch (error) {
    const message = error instanceof Error ? error.message : '矩阵配置保存失败'
    ElMessage.error(message)
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    majors.value = await listMajors()
    const routeMajorRaw = route.query.majorId
    const routeMajorValue = Array.isArray(routeMajorRaw) ? routeMajorRaw[0] : routeMajorRaw
    const routeMajorId = Number(routeMajorValue)
    const matchedMajor = Number.isFinite(routeMajorId) ? majors.value.find((item) => item.id === routeMajorId) : undefined
    selectedMajorId.value = matchedMajor?.id ?? majors.value[0]?.id
    await reloadMatrix()
  } catch (error) {
    const message = error instanceof Error ? error.message : '专业列表加载失败'
    ElMessage.error(message)
  }
})
</script>

<style scoped>
.toolbar-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.formula {
  color: var(--muted);
}

.matrix-footer {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin-top: 12px;
  padding: 12px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fbfdff;
  font-weight: 700;
}

.matrix-footer-label {
  color: #1e3555;
}

.matrix-warning-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}
</style>
