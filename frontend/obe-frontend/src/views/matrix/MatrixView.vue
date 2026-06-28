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
            <el-dropdown :disabled="!selectedMajorId">
              <el-button :disabled="!selectedMajorId">
                批量导入<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="openGraduationImportDialog">导入毕业要求</el-dropdown-item>
                  <el-dropdown-item @click="openIndicatorImportDialog">导入指标点</el-dropdown-item>
                  <el-dropdown-item @click="openMatrixImportDialog">导入宏观矩阵</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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

    <!-- 毕业要求导入 -->
    <el-dialog v-model="graduationImportVisible" title="批量导入毕业要求" width="560px" destroy-on-close>
      <div class="import-tips">
        <p>字段：专业代码*、毕业要求编号*、毕业要求名称*、毕业要求描述</p>
        <div class="import-actions">
          <el-button @click="handleDownloadGraduationTemplate">下载模板</el-button>
        </div>
      </div>
      <el-upload
        v-model:file-list="graduationImportFileList"
        drag action="#" :auto-upload="false" :limit="1" accept=".xlsx,.xls"
        :on-change="handleGraduationImportChange" :on-remove="handleGraduationImportRemove"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-title">拖拽毕业要求 Excel 到这里</div>
        <template #tip><div class="muted">导入完成后请刷新页面查看。</div></template>
      </el-upload>
      <template #footer>
        <el-button @click="graduationImportVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingGraduationImport" @click="submitGraduationImport">开始导入</el-button>
      </template>
    </el-dialog>

    <!-- 指标点导入 -->
    <el-dialog v-model="indicatorImportVisible" title="批量导入指标点" width="560px" destroy-on-close>
      <div class="import-tips">
        <p>字段：毕业要求编码*、指标点编号*、指标点名称*、指标点描述</p>
        <div class="import-actions">
          <el-button @click="handleDownloadIndicatorTemplate">下载模板</el-button>
        </div>
      </div>
      <el-upload
        v-model:file-list="indicatorImportFileList"
        drag action="#" :auto-upload="false" :limit="1" accept=".xlsx,.xls"
        :on-change="handleIndicatorImportChange" :on-remove="handleIndicatorImportRemove"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-title">拖拽指标点 Excel 到这里</div>
        <template #tip><div class="muted">导入完成后请刷新页面查看。</div></template>
      </el-upload>
      <template #footer>
        <el-button @click="indicatorImportVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingIndicatorImport" @click="submitIndicatorImport">开始导入</el-button>
      </template>
    </el-dialog>

    <!-- 宏观矩阵导入 -->
    <el-dialog v-model="matrixImportVisible" title="批量导入宏观支撑矩阵" width="560px" destroy-on-close>
      <div class="import-tips">
        <p>字段：专业代码*、课程代码*、指标点编号*、宏观总支撑权重*（0~1之间的小数）</p>
        <div class="import-actions">
          <el-button @click="handleDownloadMatrixTemplate">下载模板</el-button>
        </div>
      </div>
      <el-upload
        v-model:file-list="matrixImportFileList"
        drag action="#" :auto-upload="false" :limit="1" accept=".xlsx,.xls"
        :on-change="handleMatrixImportChange" :on-remove="handleMatrixImportRemove"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-title">拖拽宏观支撑矩阵 Excel 到这里</div>
        <template #tip><div class="muted">导入完成后会自动刷新矩阵数据。</div></template>
      </el-upload>
      <template #footer>
        <el-button @click="matrixImportVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingMatrixImport" @click="submitMatrixImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, UploadFilled } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import type { UploadProps } from 'element-plus'
import WeightMatrix from '@/components/WeightMatrix/WeightMatrix.vue'
import { listMajors } from '@/api/major'
import { checkMatrixConfig, getMatrixConfig, saveMatrixConfig, importMatrixFromExcel, downloadMatrixTemplate } from '@/api/matrix'
import {
  importGraduationRequirementsFromExcel,
  downloadGraduationRequirementTemplate,
  importIndicatorPointsFromExcel,
  downloadIndicatorPointTemplate
} from '@/api/indicator'
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

// 毕业要求导入
const graduationImportVisible = ref(false)
const graduationImportFileList = ref<UploadProps['fileList']>([])
const submittingGraduationImport = ref(false)
// 指标点导入
const indicatorImportVisible = ref(false)
const indicatorImportFileList = ref<UploadProps['fileList']>([])
const submittingIndicatorImport = ref(false)
// 宏观矩阵导入
const matrixImportVisible = ref(false)
const matrixImportFileList = ref<UploadProps['fileList']>([])
const submittingMatrixImport = ref(false)

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

// ===== 通用导入结果展示 =====
const showImportResult = async (result: Record<string, unknown>) => {
  const failCount = Number(result.failCount || 0)
  if (failCount > 0) {
    const failDetails = result.failDetails as Array<Record<string, string>> | undefined
    const preview = (failDetails ?? []).slice(0, 5)
      .map((item) => `第 ${item.row || '-'} 行：${item.reason || '导入失败'}`).join('\n')
    await ElMessageBox.alert(
      `总计 ${result.total} 条，成功 ${result.successCount} 条，失败 ${failCount} 条。${preview ? `\n\n失败示例：\n${preview}` : ''}`,
      '导入完成', { confirmButtonText: '知道了' }
    )
  } else {
    ElMessage.success(`导入完成，共成功导入 ${result.successCount} 条`)
  }
}

// ===== 毕业要求导入 =====
const openGraduationImportDialog = () => { graduationImportFileList.value = []; graduationImportVisible.value = true }
const handleGraduationImportChange: UploadProps['onChange'] = (uf, ufs) => {
  const fn = uf.name.toLowerCase()
  if (!fn.endsWith('.xlsx') && !fn.endsWith('.xls')) { ElMessage.warning('仅支持 .xlsx/.xls 文件'); return }
  graduationImportFileList.value = ufs.slice(-1)
}
const handleGraduationImportRemove: UploadProps['onRemove'] = () => { graduationImportFileList.value = [] }
const handleDownloadGraduationTemplate = async () => {
  try {
    const blob = await downloadGraduationRequirementTemplate()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a'); a.href = url; a.download = '毕业要求导入模板.xlsx'; a.click()
    window.URL.revokeObjectURL(url); ElMessage.success('模板已开始下载')
  } catch (e) { ElMessage.error(e instanceof Error ? e.message : '模板下载失败') }
}
const submitGraduationImport = async () => {
  const file = graduationImportFileList.value?.[0]?.raw
  if (!file) { ElMessage.warning('请先选择文件'); return }
  submittingGraduationImport.value = true
  try {
    const result = await importGraduationRequirementsFromExcel(file as File)
    graduationImportVisible.value = false; graduationImportFileList.value = []
    await showImportResult(result as Record<string, unknown>)
  } catch (e) { ElMessage.error(e instanceof Error ? e.message : '导入失败') }
  finally { submittingGraduationImport.value = false }
}

// ===== 指标点导入 =====
const openIndicatorImportDialog = () => { indicatorImportFileList.value = []; indicatorImportVisible.value = true }
const handleIndicatorImportChange: UploadProps['onChange'] = (uf, ufs) => {
  const fn = uf.name.toLowerCase()
  if (!fn.endsWith('.xlsx') && !fn.endsWith('.xls')) { ElMessage.warning('仅支持 .xlsx/.xls 文件'); return }
  indicatorImportFileList.value = ufs.slice(-1)
}
const handleIndicatorImportRemove: UploadProps['onRemove'] = () => { indicatorImportFileList.value = [] }
const handleDownloadIndicatorTemplate = async () => {
  try {
    const blob = await downloadIndicatorPointTemplate()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a'); a.href = url; a.download = '指标点导入模板.xlsx'; a.click()
    window.URL.revokeObjectURL(url); ElMessage.success('模板已开始下载')
  } catch (e) { ElMessage.error(e instanceof Error ? e.message : '模板下载失败') }
}
const submitIndicatorImport = async () => {
  const file = indicatorImportFileList.value?.[0]?.raw
  if (!file) { ElMessage.warning('请先选择文件'); return }
  submittingIndicatorImport.value = true
  try {
    const result = await importIndicatorPointsFromExcel(file as File)
    indicatorImportVisible.value = false; indicatorImportFileList.value = []
    await showImportResult(result as Record<string, unknown>)
  } catch (e) { ElMessage.error(e instanceof Error ? e.message : '导入失败') }
  finally { submittingIndicatorImport.value = false }
}

// ===== 宏观矩阵导入 =====
const openMatrixImportDialog = () => { matrixImportFileList.value = []; matrixImportVisible.value = true }
const handleMatrixImportChange: UploadProps['onChange'] = (uf, ufs) => {
  const fn = uf.name.toLowerCase()
  if (!fn.endsWith('.xlsx') && !fn.endsWith('.xls')) { ElMessage.warning('仅支持 .xlsx/.xls 文件'); return }
  matrixImportFileList.value = ufs.slice(-1)
}
const handleMatrixImportRemove: UploadProps['onRemove'] = () => { matrixImportFileList.value = [] }
const handleDownloadMatrixTemplate = async () => {
  try {
    const blob = await downloadMatrixTemplate()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a'); a.href = url; a.download = '宏观支撑矩阵导入模板.xlsx'; a.click()
    window.URL.revokeObjectURL(url); ElMessage.success('模板已开始下载')
  } catch (e) { ElMessage.error(e instanceof Error ? e.message : '模板下载失败') }
}
const submitMatrixImport = async () => {
  const file = matrixImportFileList.value?.[0]?.raw
  if (!file) { ElMessage.warning('请先选择文件'); return }
  submittingMatrixImport.value = true
  try {
    const result = await importMatrixFromExcel(file as File)
    matrixImportVisible.value = false; matrixImportFileList.value = []
    await reloadMatrix()
    await showImportResult(result as Record<string, unknown>)
  } catch (e) { ElMessage.error(e instanceof Error ? e.message : '导入失败') }
  finally { submittingMatrixImport.value = false }
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

.matrix-warning-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}

.import-tips { margin-bottom: 16px; }
.import-tips p { margin: 0 0 8px; color: #606266; font-size: 14px; }
.import-actions { margin-bottom: 8px; }
.upload-icon { font-size: 48px; color: #c0c4cc; }
.upload-title { margin-top: 12px; color: #606266; font-size: 14px; }
</style>
