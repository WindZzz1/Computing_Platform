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
        <WeightMatrix :loading="loading" :indicators="indicatorRows" :rows="matrixRows" />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import WeightMatrix from '@/components/WeightMatrix/WeightMatrix.vue'
import { listMajors } from '@/api/major'
import { checkMatrixConfig, getMatrixConfig, saveMatrixConfig } from '@/api/matrix'
import type { MatrixConfigVO, SysDictMajorSimpleVO } from '@/api/backend'

type MatrixRow = {
  courseId: number
  courseName: string
  weights: Record<number, number>
}

const loading = ref(false)
const saving = ref(false)
const majors = ref<SysDictMajorSimpleVO[]>([])
const selectedMajorId = ref<number>()
const matrixConfig = ref<MatrixConfigVO>()
const indicatorRows = ref<MatrixConfigVO['indicators']>([])
const matrixRows = ref<MatrixRow[]>([])

const currentMajorLabel = computed(() => {
  if (!matrixConfig.value?.majorName) return '当前专业'
  return `当前专业：${matrixConfig.value.majorName}`
})

const getColumnSum = (indicatorId: number) =>
  matrixRows.value.reduce((acc, row) => acc + (Number(row.weights[indicatorId]) || 0), 0)

const invalidIndicators = computed(() =>
  indicatorRows.value.filter((indicator) => Math.abs(getColumnSum(indicator.id) - 1) > 0.001)
)

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

  const items = matrixRows.value.flatMap((row) =>
    Object.entries(row.weights)
      .filter(([, value]) => Number(value) > 0)
      .map(([indicatorId, value]) => ({
        courseId: row.courseId,
        indicatorId: Number(indicatorId),
        totalWeight: Number(Number(value).toFixed(2))
      }))
  )

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
    selectedMajorId.value = majors.value[0]?.id
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
</style>
