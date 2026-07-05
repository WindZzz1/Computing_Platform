<template>
  <div class="page">
    <h1 class="page-title">报表与导出</h1>
    <p class="page-desc">本页按当前登录角色展示真实可调用的报表接口，并对各报表接口的就绪状态与前置条件给出明确提示。</p>

    <el-alert
      :title="roleSummary.title"
      :type="roleSummary.type"
      :description="roleSummary.description"
      show-icon
      :closable="false"
      class="summary-alert"
    />

    <section v-if="canUseMajorPrep" class="page-grid top-grid">
      <div class="panel span-12">
        <div class="toolbar">
          <h3 class="panel-title">指标点支撑情况</h3>
          <el-select v-model="selectedMajorId" style="width: 260px" placeholder="选择专业" @change="reloadMajorSupportData">
            <el-option v-for="major in majors" :key="major.id" :label="major.majorName" :value="major.id" />
          </el-select>
        </div>
        <el-table v-loading="supportLoading" :data="indicatorRows" border empty-text="当前专业暂无指标点或矩阵配置">
          <el-table-column prop="code" label="指标点" width="100" />
          <el-table-column prop="requirement" label="所属毕业要求" min-width="140" />
          <el-table-column prop="courseCount" label="支撑课程数" width="110" />
          <el-table-column prop="weightSum" label="矩阵列合计" width="120" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="row.ready ? 'success' : 'warning'">{{ row.ready ? '已就绪' : '待完善' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>

    <section v-if="canUseCourseReport" class="panel action-panel">
      <div class="toolbar">
        <h3 class="panel-title">课程报表</h3>
      </div>
      <div class="form-grid">
        <el-select
          v-model="selectedClassId"
          :placeholder="courseClassSelectPlaceholder"
          style="width: 320px"
          :disabled="courseClassSelectorDisabled"
        >
          <el-option v-for="item in courseClasses" :key="item.id" :label="buildClassLabel(item)" :value="item.id" />
        </el-select>
        <el-button :loading="courseActionLoading" @click="handleDownloadCourseTemplate">下载报表模板</el-button>
        <el-button type="primary" :loading="courseActionLoading" :disabled="!selectedClassId" @click="handleLoadCourseReport">查询报表数据</el-button>
        <el-tooltip :disabled="canExportCourseReport" content="请先查询课程报表数据，再导出文件" placement="top">
          <span>
            <el-button :loading="courseActionLoading" :disabled="!canExportCourseReport" @click="handleExportCourseReport('EXCEL')">
              导出 Excel
            </el-button>
          </span>
        </el-tooltip>
        <el-tooltip :disabled="canExportCourseReport" content="请先查询课程报表数据，再导出文件" placement="top">
          <span>
            <el-button :loading="courseActionLoading" :disabled="!canExportCourseReport" @click="handleExportCourseReport('PDF')">
              导出 PDF
            </el-button>
          </span>
        </el-tooltip>
      </div>
      <el-alert
        v-if="!hasCourseClassContext"
        title="当前账号下还没有可用教学班"
        type="info"
        description="请先在成绩管理页创建教学班并完成课程、教师、学期绑定，再生成课程报表。"
        show-icon
        :closable="false"
        class="section-alert"
      />
      <el-alert
        v-if="courseStatus"
        :title="courseStatus.title"
        :type="courseStatus.type"
        :description="courseStatus.description"
        show-icon
        :closable="false"
        class="section-alert"
      />

      <div v-if="selectedClassId" class="result-grid" style="margin-bottom: 16px">
        <el-card shadow="never" class="result-card">
          <template #header>课程报表前置状态</template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="教学班">{{ selectedCourseClassLabel }}</el-descriptions-item>
            <el-descriptions-item label="课程">{{ selectedCourseClass?.courseName || courseReportData?.courseName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="班级学生数">{{ selectedCourseClass?.studentCount ?? courseReportData?.studentCount ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="成绩记录数">{{ courseReportPrep.scoreRecordCount }}</el-descriptions-item>
            <el-descriptions-item label="课程目标数">{{ courseReportPrep.objectiveCount }}</el-descriptions-item>
            <el-descriptions-item label="考核点数">{{ courseReportPrep.assessmentCount }}</el-descriptions-item>
            <el-descriptions-item label="课程级结果">{{ courseReportPrep.calculationStatusText }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never" class="result-card">
          <template #header>操作建议</template>
          <div class="notice-list">
            <div v-for="item in courseReportPrepHints" :key="item.title" class="notice-item">
              <div>
                <div class="notice-title">{{ item.title }}</div>
                <div class="muted">{{ item.desc }}</div>
              </div>
              <el-tag :type="item.type" effect="light">{{ item.tag }}</el-tag>
            </div>
          </div>
        </el-card>
      </div>

      <div v-if="courseReportData" class="result-grid">
        <el-card shadow="never" class="result-card">
          <template #header>课程报表返回摘要</template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="教学班">{{ courseReportData.className || courseReportData.classId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="课程">{{ courseReportData.courseName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="教师">{{ courseReportData.teacherName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="学生数">{{ courseReportData.studentCount ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="课程目标数">{{ courseReportData.objectiveSummaries?.length ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="指标点结果数">{{ courseReportData.indicatorAchievements?.length ?? 0 }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never" class="result-card wide-result-card">
          <template #header>课程目标汇总预览</template>
          <div v-if="courseObjectiveSummaryRows.length" ref="courseObjectiveChartRef" class="report-chart-box"></div>
          <el-table :data="courseObjectiveSummaryRows" border empty-text="当前报表没有返回课程目标汇总数据">
            <el-table-column prop="objectiveCode" label="课程目标编号" width="140" />
            <el-table-column prop="objectiveName" label="课程目标名称" min-width="180" />
            <el-table-column prop="studentCount" label="学生数" width="100" />
            <el-table-column prop="classAverageText" label="班级平均达成度" width="140" />
            <el-table-column prop="passRateText" label="达标率" width="120" />
          </el-table>
        </el-card>

        <el-card shadow="never" class="result-card wide-result-card">
          <template #header>指标点达成结果预览</template>
          <div v-if="courseIndicatorAchievementRows.length" ref="courseIndicatorChartRef" class="report-chart-box"></div>
          <el-table :data="courseIndicatorAchievementRows" border empty-text="当前报表没有返回指标点达成结果">
            <el-table-column prop="indicatorCode" label="指标点编号" width="140" />
            <el-table-column prop="indicatorName" label="指标点名称" min-width="180" />
            <el-table-column prop="achievementText" label="达成度" width="120" />
            <el-table-column prop="calculationTimeText" label="计算时间" min-width="180" />
          </el-table>
        </el-card>
      </div>
      <el-empty
        v-else-if="hasCourseClassContext && !courseActionLoading"
        description="当前还没有课程报表返回结果。你可以先下载模板，或者直接查询一次报表数据验证接口状态。"
        class="section-empty"
      />
    </section>

    <section v-if="canUseMajorReport" class="panel action-panel">
      <div class="toolbar">
        <h3 class="panel-title">专业报表</h3>
      </div>
      <div class="form-grid">
        <el-select v-model="selectedMajorId" placeholder="选择专业" style="width: 220px" :disabled="!majors.length" @change="reloadMajorSupportData">
          <el-option v-for="major in majors" :key="major.id" :label="major.majorName" :value="major.id" />
        </el-select>
        <el-select v-if="schoolYears.length" v-model="selectedTermId" placeholder="选择学期" style="width: 220px">
          <el-option
            v-for="term in schoolYears"
            :key="term.id"
            :label="`${term.yearName} ${term.semesterName}`"
            :value="term.id"
          />
        </el-select>
        <el-input-number
          v-else
          v-model="selectedTermId"
          :min="1"
          :step="1"
          controls-position="right"
          style="width: 220px"
          placeholder="输入学期 ID"
        />
        <el-input v-model.trim="selectedGrade" placeholder="输入年级，例如 2021" style="width: 200px" />
        <el-button type="primary" :loading="majorActionLoading" :disabled="!canSubmitMajorRequest" @click="handleLoadMajorRadar">查询雷达图数据</el-button>
        <el-button :loading="majorActionLoading" :disabled="!canSubmitMajorRequest" @click="handleLoadPenetrationAccount">查询穿透式台账</el-button>
        <el-tooltip :disabled="canExportMajorAccount" content="请先查询穿透式台账，再导出台账" placement="top">
          <span>
            <el-button :loading="majorActionLoading" :disabled="!canExportMajorAccount" @click="handleExportMajorAccount">
              导出台账 Excel
            </el-button>
          </span>
        </el-tooltip>
        <el-tooltip :disabled="canExportMajorIndicator" content="请先查询雷达图数据，再导出达成度" placement="top">
          <span>
            <el-button :loading="majorActionLoading" :disabled="!canExportMajorIndicator" @click="handleExportMajorIndicator('EXCEL')">
              导出达成度 Excel
            </el-button>
          </span>
        </el-tooltip>
        <el-tooltip :disabled="canExportMajorIndicator" content="请先查询雷达图数据，再导出达成度" placement="top">
          <span>
            <el-button :loading="majorActionLoading" :disabled="!canExportMajorIndicator" @click="handleExportMajorIndicator('PDF')">
              导出达成度 PDF
            </el-button>
          </span>
        </el-tooltip>
      </div>
      <el-alert
        v-if="!majors.length || (!schoolYears.length && !canUseManualTermInput)"
        title="当前还缺少专业或学期基础数据"
        type="info"
        description="请先在基础数据管理中补齐专业、毕业要求、学期信息，再生成专业报表。"
        show-icon
        :closable="false"
        class="section-alert"
      />
      <el-alert
        v-else-if="canUseManualTermInput"
        title="当前角色暂无学期目录，请手动输入学期 ID"
        type="info"
        description="当前账号暂未开放学期列表查询，请直接输入目标学期的 ID 后继续操作。"
        show-icon
        :closable="false"
        class="section-alert"
      />
      <el-alert
        v-if="majorStatus"
        :title="majorStatus.title"
        :type="majorStatus.type"
        :description="majorStatus.description"
        show-icon
        :closable="false"
        class="section-alert"
      />
      <div v-if="majorStatus?.action === 'goCalculation'" class="section-action">
        <el-button type="primary" @click="goCalculation">前往计算中心</el-button>
      </div>

      <div v-if="majorRadarData || majorPenetrationData" class="result-grid">
        <el-card v-if="majorRadarData" shadow="never" class="result-card wide-result-card">
          <template #header>雷达图指标点结果预览</template>
          <div v-if="majorRadarRows.length" ref="majorRadarChartRef" class="radar-chart-box"></div>
          <el-table :data="majorRadarRows" border empty-text="当前雷达图结果没有返回指标点数据">
            <el-table-column prop="requirementCode" label="毕业要求编号" width="140" />
            <el-table-column prop="indicatorCode" label="指标点编号" width="140" />
            <el-table-column prop="indicatorName" label="指标点名称" min-width="180" />
            <el-table-column prop="achievementText" label="达成度" width="120" />
          </el-table>
        </el-card>

        <el-card v-if="majorPenetrationData" shadow="never" class="result-card">
          <template #header>穿透式台账接口返回摘要</template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="专业">{{ majorPenetrationData.majorInfo?.majorName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="学年学期">
              {{ [majorPenetrationData.majorInfo?.yearName, majorPenetrationData.majorInfo?.semesterName].filter(Boolean).join(' / ') || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="年级">{{ majorPenetrationData.majorInfo?.grade || selectedGrade || '-' }}</el-descriptions-item>
            <el-descriptions-item label="课程数">{{ majorPenetrationData.majorInfo?.totalCourses ?? majorPenetrationData.courses?.length ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="学生数">{{ majorPenetrationData.majorInfo?.totalStudents ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="整体达成度">{{ formatPercent(majorPenetrationData.majorInfo?.overallAchievement) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card v-if="majorPenetrationData" shadow="never" class="result-card wide-result-card">
          <template #header>穿透式台账准备概览</template>
          <div class="penetration-metric-grid">
            <div class="penetration-metric-card">
              <span class="penetration-metric-label">课程层记录</span>
              <strong class="penetration-metric-value">{{ majorPenetrationCourseRows.length }}</strong>
              <span class="metric-tip">按课程 / 教学班展开</span>
            </div>
            <div class="penetration-metric-card">
              <span class="penetration-metric-label">学生课程目标记录</span>
              <strong class="penetration-metric-value">{{ majorPenetrationObjectiveRows.length }}</strong>
              <span class="metric-tip">用于查看学生层达成度</span>
            </div>
            <div class="penetration-metric-card">
              <span class="penetration-metric-label">考核点记录</span>
              <strong class="penetration-metric-value">{{ majorPenetrationAssessmentRows.length }}</strong>
              <span class="metric-tip">考核点层过程数据</span>
            </div>
            <div class="penetration-metric-card">
              <span class="penetration-metric-label">原始成绩记录</span>
              <strong class="penetration-metric-value">{{ majorPenetrationScoreRows.length }}</strong>
              <span class="metric-tip">学生得分与达成度明细</span>
            </div>
          </div>
        </el-card>

        <el-card v-if="majorPenetrationData" shadow="never" class="result-card wide-result-card">
          <template #header>课程层结果预览</template>
          <el-table :data="majorPenetrationCourseRows" border empty-text="当前穿透式台账没有返回课程层数据">
            <el-table-column prop="courseCode" label="课程编号" width="120" />
            <el-table-column prop="courseName" label="课程名称" min-width="160" />
            <el-table-column prop="className" label="教学班" min-width="160" />
            <el-table-column prop="teacherName" label="教师" width="120" />
            <el-table-column prop="studentCount" label="学生数" width="100" />
            <el-table-column prop="achievementText" label="课程指标点达成度" width="150" />
          </el-table>
        </el-card>

        <el-card v-if="majorPenetrationData" shadow="never" class="result-card wide-result-card">
          <template #header>学生课程目标结果预览</template>
          <el-table :data="majorPenetrationObjectiveRows" border empty-text="当前穿透式台账没有返回学生课程目标数据">
            <el-table-column prop="studentNo" label="学号" width="120" />
            <el-table-column prop="studentName" label="姓名" width="120" />
            <el-table-column prop="courseName" label="课程名称" min-width="160" />
            <el-table-column prop="objectiveAchievementsText" label="课程目标达成度" min-width="220" />
            <el-table-column prop="averageAchievementText" label="平均达成度" width="130" />
          </el-table>
        </el-card>

        <el-card v-if="majorPenetrationData" shadow="never" class="result-card wide-result-card">
          <template #header>考核点层结果预览</template>
          <el-table :data="majorPenetrationAssessmentRows" border empty-text="当前穿透式台账没有返回考核点层数据">
            <el-table-column prop="courseName" label="课程名称" min-width="160" />
            <el-table-column prop="assessmentPointCode" label="考核点编号" width="130" />
            <el-table-column prop="assessmentPointName" label="考核点名称" min-width="180" />
            <el-table-column prop="objectiveCode" label="课程目标编号" width="130" />
            <el-table-column prop="fullScoreText" label="满分" width="100" />
            <el-table-column prop="weightText" label="权重" width="100" />
          </el-table>
        </el-card>

        <el-card v-if="majorPenetrationData" shadow="never" class="result-card wide-result-card">
          <template #header>学生原始得分预览</template>
          <el-table :data="majorPenetrationScoreRows" border empty-text="当前穿透式台账没有返回学生原始得分数据">
            <el-table-column prop="studentNo" label="学号" width="120" />
            <el-table-column prop="studentName" label="姓名" width="120" />
            <el-table-column prop="assessmentPointCode" label="考核点编号" width="130" />
            <el-table-column prop="assessmentPointName" label="考核点名称" min-width="160" />
            <el-table-column prop="scoreText" label="得分" width="100" />
            <el-table-column prop="achievementText" label="达成度" width="120" />
          </el-table>
        </el-card>
      </div>
      <el-empty
        v-else-if="majors.length && schoolYears.length && !majorActionLoading"
        description="当前还没有专业报表返回结果。请先选好专业、学期和年级，再发起真实接口查询。"
        class="section-empty"
      />
    </section>

    <section v-if="canUseMatrixLedger" class="panel action-panel">
      <div class="toolbar">
        <h3 class="panel-title">宏观支撑矩阵台账</h3>
        <el-tag :type="matrixLedgerRows.length ? 'success' : 'warning'">{{ matrixLedgerRows.length ? '基础数据已具备' : '等待矩阵配置' }}</el-tag>
      </div>
      <div class="form-grid">
        <el-select v-model="selectedMajorId" placeholder="选择专业" style="width: 260px" @change="reloadMajorSupportData">
          <el-option v-for="major in majors" :key="major.id" :label="major.majorName" :value="major.id" />
        </el-select>
        <el-button :disabled="!matrixLedgerRows.length" @click="exportMatrixLedgerExcel">导出 Excel</el-button>
        <el-button :disabled="!matrixLedgerRows.length" @click="printMatrixLedger">打印台账</el-button>
      </div>
      <el-table :data="matrixLedgerRows.slice(0, 6)" border empty-text="当前专业还没有矩阵台账数据">
        <el-table-column prop="courseCode" label="课程代码" width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="140" />
        <el-table-column prop="indicatorCode" label="指标点" width="110" />
        <el-table-column prop="requirement" label="毕业要求" min-width="160" />
        <el-table-column prop="totalWeight" label="权重" width="100" />
      </el-table>
    </section>

    <section v-if="canUseCourseReport" class="panel action-panel">
      <div class="toolbar">
        <h3 class="panel-title">学生考核点原始成绩明细</h3>
        <el-tag type="success">教师可查询</el-tag>
      </div>
      <div class="form-grid">
        <el-select
          v-model="selectedClassId"
          :placeholder="courseClassSelectPlaceholder"
          style="width: 320px"
          :disabled="courseClassSelectorDisabled"
        >
          <el-option v-for="item in courseClasses" :key="item.id" :label="buildClassLabel(item)" :value="item.id" />
        </el-select>
        <el-select
          v-model="rawScorePointId"
          placeholder="选择考核点，可选"
          style="width: 260px"
          clearable
          :disabled="!rawScoreAssessmentPoints.length || !canLoadRawScoreMetadata"
        >
          <el-option
            v-for="point in rawScoreAssessmentPoints"
            :key="point.id"
            :label="`${point.pointCode} ${point.pointName}`"
            :value="point.id"
          />
        </el-select>
        <el-input
          v-model.trim="rawScoreStudentNo"
          placeholder="按学号筛选，可选"
          style="width: 220px"
          clearable
          @keyup.enter="handleLoadRawScores(1)"
        />
        <el-button
          type="primary"
          :loading="rawScoreLoading"
          :disabled="!selectedClassId"
          @click="handleLoadRawScores(1)"
        >
          查询原始成绩
        </el-button>
        <el-button
          :loading="rawScoreExporting"
          :disabled="!rawScoreRows.length"
          @click="handleExportRawScores"
        >
          导出 CSV
        </el-button>
      </div>
      <el-alert
        v-if="rawScoreStatus"
        :title="rawScoreStatus.title"
        :type="rawScoreStatus.type"
        :description="rawScoreStatus.description"
        show-icon
        :closable="false"
        class="section-alert"
      />
      <el-table
        v-loading="rawScoreLoading"
        :data="rawScoreRows"
        border
        empty-text="当前还没有查询到学生原始成绩明细"
      >
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="pointCode" label="考核点编号" width="150" />
        <el-table-column prop="pointName" label="考核点名称" min-width="220" />
        <el-table-column prop="scoreText" label="得分" width="100" />
        <el-table-column prop="fullScoreText" label="满分" width="100" />
      </el-table>
      <div class="table-footer">
        <span class="metric-tip">
          当前共 {{ rawScorePage.total || 0 }} 条原始成绩记录
        </span>
        <el-pagination
          :current-page="rawScoreQuery.current || 1"
          :page-size="rawScoreQuery.pageSize || 20"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          :total="rawScorePage.total || 0"
          @current-change="handleRawScoreCurrentChange"
          @size-change="handleRawScoreSizeChange"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import * as XLSX from 'xlsx'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getCourseAchievementCalculationStatus } from '@/api/calculation'
import { pageCourses } from '@/api/course'
import { queryGrades } from '@/api/grade-entry'
import { pageGraduationRequirements, pageIndicators } from '@/api/indicator'
import { listMajors } from '@/api/major'
import { checkMatrixConfig, getMatrixConfig } from '@/api/matrix'
import {
  downloadCourseAchievementTemplate,
  exportCourseAchievementReportExcel,
  exportCourseAchievementReportPdf,
  exportMajorIndicatorAchievementExcel,
  exportMajorIndicatorAchievementPdf,
  exportMajorPenetrationAccountExcel,
  getCourseAchievementReportData,
  getMajorPenetrationAccount,
  getMajorReportRadarData
} from '@/api/report'
import { listSchoolYears } from '@/api/schoolyear'
import { listAssessmentPoints, listCourseObjectives } from '@/api/syllabus'
import { getTeachingClassStudents, listMyTeachingClasses, pageTeachingClasses } from '@/api/teaching-class'
import { useUserStore } from '@/stores/user'
import type {
  AssessmentPointVO,
  AchievementCalculationStatusVO,
  CourseAchievementReportVO,
  CourseObjectiveVO,
  GraduationRequirementVO,
  IndicatorPointVO,
  MajorAchievementRadarVO,
  MatrixConfigVO,
  MatrixWeightCheckVO,
  PageResponse,
  PenetrationAccountVO,
  StudentScoreVO,
  SysDictMajorSimpleVO,
  SysDictSchoolYearVO,
  StudentVO,
  TeachingClassVO
} from '@/api/backend'

type IndicatorSupportRow = {
  code: string
  requirement: string
  courseCount: number
  weightSum: string
  ready: boolean
}

type MatrixLedgerRow = {
  majorName: string
  courseCode: string
  courseName: string
  indicatorCode: string
  indicatorName: string
  requirement: string
  totalWeight: string
}

type StatusState = {
  title: string
  description: string
  type: 'success' | 'warning' | 'info' | 'error'
  action?: 'goCalculation'
}

const user = useUserStore()
const route = useRoute()
const router = useRouter()
const majorRadarChartRef = ref<HTMLDivElement>()
const courseObjectiveChartRef = ref<HTMLDivElement>()
const courseIndicatorChartRef = ref<HTMLDivElement>()
let majorRadarChart: echarts.ECharts | undefined
let courseObjectiveChart: echarts.ECharts | undefined
let courseIndicatorChart: echarts.ECharts | undefined

const canUseCourseReport = computed(() => user.role === 'teacher' || user.role === 'admin')
const canUseMajorReport = computed(() => user.role === 'leader' || user.role === 'edu' || user.role === 'admin')
const canUseMajorPrep = computed(() => user.role === 'admin')
const canUseMatrixLedger = computed(() => user.role === 'admin')
const canUseManualTermInput = computed(() => canUseMajorReport.value && !schoolYears.value.length)

const supportLoading = ref(false)
const courseActionLoading = ref(false)
const majorActionLoading = ref(false)
const rawScoreLoading = ref(false)
const rawScoreExporting = ref(false)

const majors = ref<SysDictMajorSimpleVO[]>([])
const schoolYears = ref<SysDictSchoolYearVO[]>([])
const courseClasses = ref<TeachingClassVO[]>([])
const rawScoreAssessmentPoints = ref<AssessmentPointVO[]>([])
const courseObjectives = ref<CourseObjectiveVO[]>([])
const courseCalculationStatus = ref<AchievementCalculationStatusVO>()
const classStudentCache = new Map<number, StudentVO[]>()

const selectedMajorId = ref<number>()
const selectedTermId = ref<number>()
const selectedGrade = ref('')
const selectedClassId = ref<number>()
const directCourseId = ref<number>()
const rawScorePointId = ref<number>()
const rawScoreStudentNo = ref('')

const requirements = ref<GraduationRequirementVO[]>([])
const indicators = ref<IndicatorPointVO[]>([])
const matrixConfig = ref<MatrixConfigVO>()
const matrixCheck = ref<MatrixWeightCheckVO>()

const courseReportData = ref<CourseAchievementReportVO>()
const majorRadarData = ref<MajorAchievementRadarVO>()
const majorPenetrationData = ref<PenetrationAccountVO>()
const rawScorePage = ref<PageResponse<StudentScoreVO>>({
  records: [],
  total: 0,
  size: 20,
  current: 1,
  pages: 0
})
const rawScoreQuery = ref({
  current: 1,
  pageSize: 20
})

const courseStatus = ref<StatusState>()
const majorStatus = ref<StatusState>()
const rawScoreStatus = ref<StatusState>()

const selectedMajor = computed(() => majors.value.find((item) => item.id === selectedMajorId.value))
const selectedCourseClass = computed(() => courseClasses.value.find((item) => item.id === selectedClassId.value))
const hasCourseClassContext = computed(() => Boolean(selectedClassId.value || courseClasses.value.length))
const resolvedCourseId = computed(() => Number(selectedCourseClass.value?.courseId || directCourseId.value || 0) || undefined)
const canLoadRawScoreMetadata = computed(() => Boolean(resolvedCourseId.value))
const courseClassSelectorDisabled = computed(() => !courseClasses.value.length)
const courseClassSelectPlaceholder = computed(() => '选择教学班')
const selectedCourseClassLabel = computed(() => {
  if (selectedCourseClass.value) {
    return selectedCourseClass.value.className || buildClassLabel(selectedCourseClass.value)
  }
  return selectedClassId.value ? `教学班 ${selectedClassId.value}` : '-'
})
const canSubmitMajorRequest = computed(() => Boolean(selectedMajorId.value && selectedTermId.value && selectedGrade.value))
const canExportCourseReport = computed(() => Boolean(selectedClassId.value && courseReportData.value && !courseActionLoading.value))
const canExportMajorAccount = computed(() => Boolean(canSubmitMajorRequest.value && majorPenetrationData.value && !majorActionLoading.value))
const canExportMajorIndicator = computed(() => Boolean(canSubmitMajorRequest.value && majorRadarData.value && !majorActionLoading.value))
const courseReportPrep = computed(() => ({
  objectiveCount: courseObjectives.value.length,
  assessmentCount: rawScoreAssessmentPoints.value.length,
  scoreRecordCount: rawScorePage.value.total || 0,
  hasCalculationResult: courseCalculationStatus.value?.hasCalculationResult ?? false,
  calculationStatusText: courseCalculationStatus.value?.hasCalculationResult ? '已生成' : '未生成'
}))
const courseReportPrepHints = computed(() => {
  const items: Array<{ title: string; desc: string; tag: string; type: 'success' | 'warning' | 'info' }> = []

  items.push(
    courseReportPrep.value.objectiveCount > 0
      ? { title: '课程目标已配置', desc: `当前课程已有 ${courseReportPrep.value.objectiveCount} 个课程目标。`, tag: '已就绪', type: 'success' }
      : {
          title: '课程目标待确认',
          desc: canLoadRawScoreMetadata.value
            ? '先去课程大纲页补齐课程目标，否则报表很难生成有效汇总。'
            : '当前拿不到教学班课程详情，需先用报表查询结果反推课程信息，或让后端补教师侧教学班详情接口。',
          tag: canLoadRawScoreMetadata.value ? '待处理' : '待补详情',
          type: canLoadRawScoreMetadata.value ? 'warning' : 'info'
        }
  )

  items.push(
    courseReportPrep.value.assessmentCount > 0
      ? { title: '考核点已配置', desc: `当前课程已有 ${courseReportPrep.value.assessmentCount} 个考核点。`, tag: '已就绪', type: 'success' }
      : {
          title: '考核点待确认',
          desc: canLoadRawScoreMetadata.value
            ? '先去课程大纲页补齐考核点，再继续导入成绩并生成报表。'
            : '当前缺少课程详情，页面暂时不能自动读取考核点列表。',
          tag: canLoadRawScoreMetadata.value ? '待处理' : '待补详情',
          type: canLoadRawScoreMetadata.value ? 'warning' : 'info'
        }
  )

  items.push(
    courseReportPrep.value.scoreRecordCount > 0
      ? { title: '成绩记录已存在', desc: `当前教学班已查询到 ${courseReportPrep.value.scoreRecordCount} 条成绩记录。`, tag: '可用', type: 'success' }
      : { title: '还没有成绩记录', desc: '建议先去成绩页导入或检查当前教学班成绩，再回来验证课程报表。', tag: '待导入', type: 'info' }
  )

  items.push(
    courseReportPrep.value.hasCalculationResult
      ? { title: '课程级结果已生成', desc: '当前教学班已经形成课程级达成度结果，后续生成课程报表与展示计算结果会更顺。', tag: '已生成', type: 'success' }
      : { title: '课程级结果未生成', desc: '建议先去计算中心补课程级计算结果，再回来生成课程报表。', tag: '待计算', type: 'info' }
  )

  return items
})
const courseObjectiveSummaryRows = computed(() =>
  (courseReportData.value?.objectiveSummaries ?? []).map((item) => ({
    objectiveCode: item.objectiveCode || '-',
    objectiveName: item.objectiveName || '-',
    studentCount: item.studentCount ?? 0,
    classAverageText: formatPercent(item.classAverage),
    passRateText: formatPercent(item.passRate)
  }))
)
const courseIndicatorAchievementRows = computed(() =>
  (courseReportData.value?.indicatorAchievements ?? []).map((item) => ({
    indicatorCode: item.indicatorCode || '-',
    indicatorName: item.indicatorName || '-',
    achievementText: formatPercent(item.achievement),
    calculationTimeText: formatDateTime(item.calculationTime)
  }))
)
const majorRadarRows = computed(() =>
  (majorRadarData.value?.indicatorPoints ?? []).map((item) => ({
    requirementCode: item.requirementCode || '-',
    indicatorCode: item.indicatorCode || '-',
    indicatorName: item.indicatorName || '-',
    achievementText: formatPercent(item.achievement)
  }))
)
const majorRadarSeriesData = computed(() =>
  (majorRadarData.value?.indicatorPoints ?? []).map((item) => ({
    name: item.indicatorCode || item.indicatorName || `指标点${item.indicatorId ?? ''}`,
    fullName: [item.indicatorCode, item.indicatorName].filter(Boolean).join(' ').trim(),
    value: Number(item.achievement ?? 0),
    requirement: item.requirementCode || item.requirementName || '-'
  }))
)
const majorPenetrationCourseRows = computed(() =>
  (majorPenetrationData.value?.courses ?? []).map((item) => {
    const record = toPlainRecord(item)
    return {
      courseCode: readRecordText(record, 'courseCode'),
      courseName: readRecordText(record, 'courseName'),
      className: readRecordText(record, 'className'),
      teacherName: readRecordText(record, 'teacherName'),
      studentCount: readRecordText(record, 'studentCount'),
      achievementText: formatPercent(record.courseIndicatorAchievement)
    }
  })
)
const majorPenetrationObjectiveRows = computed(() =>
  (majorPenetrationData.value?.studentObjectives ?? []).map((item) => {
    const record = toPlainRecord(item)
    return {
      studentNo: readRecordText(record, 'studentNo'),
      studentName: readRecordText(record, 'studentName'),
      courseName: readRecordText(record, 'courseName'),
      objectiveAchievementsText: formatObjectiveAchievements(record.objectiveAchievements),
      averageAchievementText: formatPercent(record.averageAchievement)
    }
  })
)
const majorPenetrationAssessmentRows = computed(() =>
  (majorPenetrationData.value?.assessmentPoints ?? []).map((item) => {
    const record = toPlainRecord(item)
    return {
      courseName: readRecordText(record, 'courseName'),
      assessmentPointCode: readRecordText(record, 'assessmentPointCode'),
      assessmentPointName: readRecordText(record, 'assessmentPointName'),
      objectiveCode: readRecordText(record, 'objectiveCode'),
      fullScoreText: formatDecimal(record.fullScore),
      weightText: formatPercent(record.weight)
    }
  })
)
const majorPenetrationScoreRows = computed(() =>
  (majorPenetrationData.value?.studentScores ?? []).map((item) => {
    const record = toPlainRecord(item)
    return {
      studentNo: readRecordText(record, 'studentNo'),
      studentName: readRecordText(record, 'studentName'),
      assessmentPointCode: readRecordText(record, 'assessmentPointCode'),
      assessmentPointName: readRecordText(record, 'assessmentPointName'),
      scoreText: formatDecimal(record.score),
      achievementText: formatPercent(record.achievement)
    }
  })
)
const rawScoreRows = computed(() =>
  (rawScorePage.value.records ?? [])
    .map((item) => ({
      ...item,
      scoreText: formatDecimal(item.score),
      fullScoreText: formatDecimal(item.fullScore)
    }))
)
const routeClassId = computed(() => {
  const raw = route.query.classId
  const value = Array.isArray(raw) ? raw[0] : raw
  const numeric = Number(value)
  return Number.isFinite(numeric) && numeric > 0 ? numeric : undefined
})
const routeCourseId = computed(() => {
  const raw = route.query.courseId
  const value = Array.isArray(raw) ? raw[0] : raw
  const numeric = Number(value)
  return Number.isFinite(numeric) && numeric > 0 ? numeric : undefined
})
const routeMajorId = computed(() => {
  const raw = route.query.majorId
  const value = Array.isArray(raw) ? raw[0] : raw
  const numeric = Number(value)
  return Number.isFinite(numeric) && numeric > 0 ? numeric : undefined
})
const routeTermId = computed(() => {
  const raw = route.query.termId
  const value = Array.isArray(raw) ? raw[0] : raw
  const numeric = Number(value)
  return Number.isFinite(numeric) && numeric > 0 ? numeric : undefined
})
const routeGrade = computed(() => {
  const raw = route.query.grade
  const value = Array.isArray(raw) ? raw[0] : raw
  return typeof value === 'string' ? value.trim() : ''
})

const mergeMajorOptions = (items: Array<{ id?: number | null; majorId?: number | null; majorName?: string | null }>) => {
  const majorMap = new Map(majors.value.map((item) => [item.id, item]))
  items.forEach((item) => {
    // 用真正的专业 id（课程/毕业要求挂在 majorId 上），不能用记录自身 id，
    // 否则每条课程/毕业要求都会被当成一个"专业"，产生大量同名重复项
    const majorId = Number(item.majorId)
    const majorName = item.majorName?.trim()
    if (!Number.isFinite(majorId) || !majorName) {
      return
    }
    // 仅补充字典里没有的专业，避免覆盖 listMajors 返回的完整项
    if (!majorMap.has(majorId)) {
      majorMap.set(majorId, {
        id: majorId,
        majorName
      })
    }
  })
  majors.value = Array.from(majorMap.values())
}

const roleSummary = computed<StatusState>(() => {
  if (user.role === 'admin') {
    return {
      title: '当前角色可使用课程报表与专业报表',
      description:
        '管理员会同时显示课程报表、专业报表和矩阵台账准备能力；其中专业报表如果没有学期目录，会自动切换为手动输入学期 ID。',
      type: 'success'
    }
  }

  if (canUseCourseReport.value) {
    return {
      title: '当前角色可生成课程报表',
      description: '课程报表模板下载、原始成绩明细查询、课程报表数据与 Excel/PDF 导出均已实现，可直接使用。',
      type: 'success'
    }
  }

  if (canUseMajorReport.value) {
    return {
      title: '当前角色可生成专业报表',
      description: '专业雷达图、穿透式台账、达成度 Excel/PDF 导出均已实现，可直接使用；若提示需先计算三级达成度，请先到「计算中心」执行专业级计算。',
      type: 'success'
    }
  }

  return {
    title: '当前角色以报表准备信息为主',
    description: '当前角色暂时以查看矩阵台账准备情况和基础数据就绪度为主。',
    type: 'info'
  }
})

const indicatorRows = computed<IndicatorSupportRow[]>(() => {
  const config = matrixConfig.value
  const matrixData = config?.matrixData ?? []
  const columnSums = matrixCheck.value?.columnSums ?? {}

  return indicators.value.map((indicator) => {
    const linkedItems = matrixData.filter((item) => item.indicatorId === indicator.id)
    const rawSum = columnSums[indicator.id]
    const numericSum = rawSum === undefined || rawSum === null || rawSum === '' ? undefined : Number(rawSum)
    const ready = linkedItems.length > 0 && numericSum !== undefined && Math.abs(numericSum - 1) <= 0.001

    return {
      code: indicator.indicatorCode,
      requirement: indicator.requirementCode || indicator.requirementName || '-',
      courseCount: linkedItems.length,
      weightSum: numericSum === undefined || Number.isNaN(numericSum) ? '-' : numericSum.toFixed(3),
      ready
    }
  })
})

const matrixLedgerRows = computed<MatrixLedgerRow[]>(() => {
  const config = matrixConfig.value
  if (!config) {
    return []
  }

  const courseMap = new Map(
    (config.courses ?? []).map((course) => [
      course.id,
      {
        courseCode: course.courseCode,
        courseName: course.courseName
      }
    ])
  )

  const indicatorMap = new Map(
    (config.indicators ?? []).map((indicator) => [
      indicator.id,
      {
        indicatorCode: indicator.indicatorCode,
        indicatorName: indicator.indicatorName,
        requirementId: indicator.requirementId
      }
    ])
  )

  const requirementMap = new Map(
    requirements.value.map((item) => [
      item.id,
      item.requirementCode ? `${item.requirementCode} ${item.requirementName}` : item.requirementName
    ])
  )

  return (config.matrixData ?? []).map((item) => {
    const course = courseMap.get(item.courseId)
    const indicator = indicatorMap.get(item.indicatorId)

    return {
      majorName: config.majorName || selectedMajor.value?.majorName || '-',
      courseCode: course?.courseCode || '-',
      courseName: course?.courseName || '-',
      indicatorCode: indicator?.indicatorCode || '-',
      indicatorName: indicator?.indicatorName || '-',
      requirement: indicator?.requirementId ? requirementMap.get(indicator.requirementId) || '-' : '-',
      totalWeight: Number(item.totalWeight ?? 0).toFixed(3)
    }
  })
})

const buildClassLabel = (item: TeachingClassVO) => {
  const course = item.courseName || item.courseCode || '未命名课程'
  const className = item.className || `教学班 ${item.id}`
  return `${course} / ${className}`
}

const toPlainRecord = (value: unknown): Record<string, unknown> =>
  value && typeof value === 'object' ? (value as Record<string, unknown>) : {}

const readRecordText = (record: Record<string, unknown>, key: string) => {
  const value = record[key]
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  return String(value)
}

const formatPercent = (value?: unknown) => {
  if (value === undefined || value === null || Number.isNaN(Number(value))) {
    return '-'
  }
  return `${(Number(value) * 100).toFixed(2)}%`
}

const formatDecimal = (value?: unknown) => {
  if (value === undefined || value === null || Number.isNaN(Number(value))) {
    return '-'
  }
  return Number(value).toFixed(2)
}

const escapeHtml = (value?: unknown) =>
  String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

const formatDateTime = (value?: unknown) => {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ').slice(0, 19)
}

const formatObjectiveAchievements = (value: unknown) => {
  const record = toPlainRecord(value)
  const entries = Object.entries(record)
  if (!entries.length) {
    return '-'
  }
  return entries
    .map(([key, achievement]) => `${key}: ${formatPercent(achievement)}`)
    .join(' / ')
}

const normalizeBackendMessage = (message: string, scene: 'course' | 'major') => {
  // 专业报表前置：三级达成度尚未计算（当前唯一真实会出现的“未完成”类提示）
  if (message.includes('尚未计算三级达成度')) {
    return '该专业本学期本年级尚未计算三级达成度，请先到「计算中心」执行专业级计算后再查询。'
  }
  // 专业报表前置：scope 内无教学班
  if (message.includes('无教学班级数据')) {
    return '该专业本学期本年级暂无教学班级数据，请先在成绩管理页创建教学班并绑定课程/学期。'
  }
  // 课程报表前置：教学班不存在
  if (scene === 'course' && message.includes('教学班不存在')) {
    return '当前教学班不存在，请确认教学班 ID 后重试。'
  }

  if (message.includes('无权') || message.includes('403')) {
    return scene === 'course'
      ? '当前账号没有课程报表调用权限，请确认当前登录角色是否已放开课程报表能力。'
      : '当前账号没有专业报表调用权限，请确认当前登录角色是否已放开专业报表能力。'
  }

  return message
}

const setCourseStatus = (title: string, description: string, type: StatusState['type']) => {
  courseStatus.value = { title, description, type }
}

const setMajorStatus = (
  title: string,
  description: string,
  type: StatusState['type'],
  action?: StatusState['action']
) => {
  majorStatus.value = { title, description, type, action }
}

const goCalculation = () => router.push('/calculation')

const setRawScoreStatus = (title: string, description: string, type: StatusState['type']) => {
  rawScoreStatus.value = { title, description, type }
}

const ensureCourseClassSelected = () => {
  if (!selectedClassId.value) {
    ElMessage.warning('请先选择教学班')
    return false
  }
  return true
}

const ensureMajorRequestReady = () => {
  if (!selectedMajorId.value) {
    ElMessage.warning('请先选择专业')
    return false
  }
  if (!selectedTermId.value) {
    ElMessage.warning('请先选择学期')
    return false
  }
  if (!selectedGrade.value) {
    ElMessage.warning('请先输入年级')
    return false
  }
  return true
}

const ensureRawScoreClassSelected = () => {
  if (!selectedClassId.value) {
    ElMessage.warning('请先选择教学班')
    return false
  }
  return true
}

const loadRawScoreAssessmentPoints = async () => {
  rawScoreAssessmentPoints.value = []
  courseObjectives.value = []
  rawScorePointId.value = undefined
  courseCalculationStatus.value = undefined

  if (!selectedClassId.value) {
    return
  }

  const selectedClass = courseClasses.value.find((item) => item.id === selectedClassId.value)
  if (!resolvedCourseId.value) {
    return
  }

  try {
    const [assessmentPage, objectivePage, calculationStatus] = await Promise.all([
      listAssessmentPoints(resolvedCourseId.value),
      listCourseObjectives(resolvedCourseId.value),
      getCourseAchievementCalculationStatus(selectedClassId.value)
    ])
    courseObjectives.value = objectivePage.records
    courseCalculationStatus.value = calculationStatus
    const page = assessmentPage
    rawScoreAssessmentPoints.value = page.records
  } catch (error) {
    const message = error instanceof Error ? error.message : '考核点列表加载失败'
    ElMessage.warning(message)
  }
}

const buildMajorRequest = () => ({
  majorId: Number(selectedMajorId.value),
  termId: Number(selectedTermId.value),
  grade: selectedGrade.value
})

const reloadMajorSupportData = async () => {
  if (!selectedMajorId.value || !canUseMajorPrep.value) {
    return
  }

  supportLoading.value = true
  try {
    const [requirementPage, indicatorPage, matrix] = await Promise.all([
      pageGraduationRequirements({ current: 1, pageSize: 200, majorId: selectedMajorId.value }),
      pageIndicators({ current: 1, pageSize: 200 }),
      getMatrixConfig(selectedMajorId.value)
    ])

    requirements.value = requirementPage.records
    indicators.value = indicatorPage.records
    matrixConfig.value = matrix

    const matrixItems = (matrix.matrixData ?? []).map((item) => ({
      courseId: item.courseId,
      indicatorId: item.indicatorId,
      totalWeight: Number(item.totalWeight ?? 0)
    }))

    matrixCheck.value = matrixItems.length
      ? await checkMatrixConfig(selectedMajorId.value, matrixItems)
      : {
          valid: false,
          message: '当前专业还没有矩阵配置',
          columnSums: {}
        }
  } catch (error) {
    const message = error instanceof Error ? error.message : '报表准备数据加载失败'
    ElMessage.error(message)
  } finally {
    supportLoading.value = false
  }
}

const downloadBlob = (blob: Blob, fileName: string) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.click()
  window.URL.revokeObjectURL(url)
}

const getClassStudentsCached = async (classId: number) => {
  const cached = classStudentCache.get(classId)
  if (cached) {
    return cached
  }
  const students = await getTeachingClassStudents(classId)
  classStudentCache.set(classId, students)
  return students
}

const resolveRawScoreStudentId = async (classId: number) => {
  const studentNo = rawScoreStudentNo.value.trim()
  if (!studentNo) {
    return undefined
  }

  const students = await getClassStudentsCached(classId)
  return students.find((student) => student.studentNo?.includes(studentNo))?.id
}

const resetRawScorePage = (page = 1) => {
  rawScorePage.value = {
    records: [],
    total: 0,
    size: rawScoreQuery.value.pageSize,
    current: page,
    pages: 0
  }
}

const handleDownloadCourseTemplate = async () => {
  courseActionLoading.value = true
  try {
    const blob = await downloadCourseAchievementTemplate()
    downloadBlob(blob, '课程目标达成情况评价报表模板.xlsx')
    setCourseStatus('模板下载成功', '课程报表模板接口可正常访问，说明教师报表入口已经接通。', 'success')
  } catch (error) {
    const message = error instanceof Error ? error.message : '课程报表模板下载失败'
    const normalized = normalizeBackendMessage(message, 'course')
    setCourseStatus('模板下载失败', normalized, 'error')
    ElMessage.error(normalized)
  } finally {
    courseActionLoading.value = false
  }
}

const handleLoadCourseReport = async () => {
  if (!ensureCourseClassSelected()) {
    return
  }

  courseActionLoading.value = true
  courseReportData.value = undefined
  try {
    const data = await getCourseAchievementReportData({
      classId: Number(selectedClassId.value),
      exportFormat: 'EXCEL',
      includeStudentDetails: true,
      includeIndicatorAchievement: true
    })

    courseReportData.value = data
    await nextTick()
    renderCourseObjectiveChart()
    renderCourseIndicatorChart()
    setCourseStatus('课程报表数据查询成功', '后端已返回课程报表结果，可以继续补前端展示细节。', 'success')
  } catch (error) {
    const message = error instanceof Error ? error.message : '课程报表数据查询失败'
    const normalized = normalizeBackendMessage(message, 'course')
    setCourseStatus('课程报表数据查询失败', normalized, 'warning')
    ElMessage.warning(normalized)
  } finally {
    courseActionLoading.value = false
  }
}

const handleExportCourseReport = async (format: 'EXCEL' | 'PDF') => {
  if (!ensureCourseClassSelected()) {
    return
  }
  if (!courseReportData.value) {
    const message = '请先查询课程报表数据，再导出文件'
    setCourseStatus('暂无法导出课程报表', message, 'info')
    ElMessage.info(message)
    return
  }

  courseActionLoading.value = true
  try {
    const payload = {
      classId: Number(selectedClassId.value),
      exportFormat: format,
      includeStudentDetails: true,
      includeIndicatorAchievement: true
    }

    const blob =
      format === 'EXCEL'
        ? await exportCourseAchievementReportExcel(payload)
        : await exportCourseAchievementReportPdf(payload)

    const suffix = format === 'EXCEL' ? 'xlsx' : 'pdf'
    const selectedClass = courseClasses.value.find((item) => item.id === selectedClassId.value)
    const fileName = `${selectedClass?.className || '课程报表'}.${suffix}`
    downloadBlob(blob, fileName)
    setCourseStatus(`${format} 导出成功`, '当前导出接口已返回文件流，可以继续完善文件命名与导出入口。', 'success')
  } catch (error) {
    const message = error instanceof Error ? error.message : `${format} 导出失败`
    const normalized = normalizeBackendMessage(message, 'course')
    setCourseStatus(`${format} 导出失败`, normalized, 'warning')
    ElMessage.warning(normalized)
  } finally {
    courseActionLoading.value = false
  }
}

const handleLoadMajorRadar = async () => {
  if (!ensureMajorRequestReady()) {
    return
  }

  majorActionLoading.value = true
  majorRadarData.value = undefined
  try {
    const data = await getMajorReportRadarData(buildMajorRequest())
    majorRadarData.value = data
    await nextTick()
    renderMajorRadarChart()
    setMajorStatus('雷达图接口查询成功', '后端已返回雷达图数据，可以继续补图表渲染。', 'success')
  } catch (error) {
    const message = error instanceof Error ? error.message : '雷达图接口查询失败'
    const normalized = normalizeBackendMessage(message, 'major')
    const needCalc = message.includes('尚未计算三级达成度')
    setMajorStatus('雷达图接口查询失败', normalized, 'warning', needCalc ? 'goCalculation' : undefined)
    ElMessage.warning(normalized)
  } finally {
    majorActionLoading.value = false
  }
}

const renderMajorRadarChart = () => {
  if (!majorRadarChartRef.value || !majorRadarSeriesData.value.length) {
    majorRadarChart?.dispose()
    majorRadarChart = undefined
    return
  }

  if (!majorRadarChart) {
    majorRadarChart = echarts.init(majorRadarChartRef.value)
  }

  const maxAchievement = Math.max(...majorRadarSeriesData.value.map((item) => item.value), 1)
  const axisMax = Math.max(1, Number((Math.ceil(maxAchievement * 10) / 10).toFixed(1)))

  majorRadarChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: () =>
        majorRadarSeriesData.value
          .map((item) => `${item.fullName || item.name}: ${formatPercent(item.value)} (${item.requirement})`)
          .join('<br/>')
    },
    radar: {
      radius: '62%',
      center: ['50%', '54%'],
      indicator: majorRadarSeriesData.value.map((item) => ({
        name: item.name,
        max: axisMax
      })),
      splitNumber: 5,
      axisName: {
        color: '#20324d',
        fontSize: 12
      },
      splitArea: {
        areaStyle: {
          color: ['rgba(23,118,242,0.06)', 'rgba(23,118,242,0.03)']
        }
      },
      splitLine: {
        lineStyle: {
          color: '#d8e4f2'
        }
      },
      axisLine: {
        lineStyle: {
          color: '#d8e4f2'
        }
      }
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: majorRadarSeriesData.value.map((item) => Number(item.value.toFixed(4))),
            name: `${selectedMajor.value?.majorName || majorRadarData.value?.majorName || '当前专业'}达成度`,
            areaStyle: {
              color: 'rgba(23,118,242,0.22)'
            },
            lineStyle: {
              color: '#1776f2',
              width: 2
            },
            itemStyle: {
              color: '#1776f2'
            },
            symbolSize: 8
          }
        ]
      }
    ]
  })
}

const renderCourseObjectiveChart = () => {
  if (!courseObjectiveChartRef.value || !courseObjectiveSummaryRows.value.length) {
    courseObjectiveChart?.dispose()
    courseObjectiveChart = undefined
    return
  }

  if (!courseObjectiveChart) {
    courseObjectiveChart = echarts.init(courseObjectiveChartRef.value)
  }

  courseObjectiveChart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      bottom: 0
    },
    grid: {
      left: 48,
      right: 20,
      top: 24,
      bottom: 52
    },
    xAxis: {
      type: 'category',
      axisLabel: {
        interval: 0,
        color: '#4b5d79'
      },
      data: courseObjectiveSummaryRows.value.map((item) => item.objectiveCode)
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 1,
      axisLabel: {
        color: '#4b5d79',
        formatter: (value: number) => `${Math.round(value * 100)}%`
      },
      splitLine: {
        lineStyle: {
          color: '#e8edf5'
        }
      }
    },
    series: [
      {
        name: '班级平均达成度',
        type: 'bar',
        barMaxWidth: 38,
        itemStyle: {
          color: '#1776f2',
          borderRadius: [8, 8, 0, 0]
        },
        data: courseReportData.value?.objectiveSummaries?.map((item) => Number(item.classAverage ?? 0)) ?? []
      },
      {
        name: '达标率',
        type: 'line',
        smooth: true,
        itemStyle: {
          color: '#f59e0b'
        },
        lineStyle: {
          color: '#f59e0b',
          width: 2
        },
        data: courseReportData.value?.objectiveSummaries?.map((item) => Number(item.passRate ?? 0)) ?? []
      }
    ]
  })
}

const renderCourseIndicatorChart = () => {
  if (!courseIndicatorChartRef.value || !courseIndicatorAchievementRows.value.length) {
    courseIndicatorChart?.dispose()
    courseIndicatorChart = undefined
    return
  }

  if (!courseIndicatorChart) {
    courseIndicatorChart = echarts.init(courseIndicatorChartRef.value)
  }

  courseIndicatorChart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: 48,
      right: 20,
      top: 24,
      bottom: 52
    },
    xAxis: {
      type: 'category',
      axisLabel: {
        interval: 0,
        color: '#4b5d79'
      },
      data: courseIndicatorAchievementRows.value.map((item) => item.indicatorCode)
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 1,
      axisLabel: {
        color: '#4b5d79',
        formatter: (value: number) => `${Math.round(value * 100)}%`
      },
      splitLine: {
        lineStyle: {
          color: '#e8edf5'
        }
      }
    },
    series: [
      {
        type: 'bar',
        barMaxWidth: 42,
        itemStyle: {
          color: '#22c55e',
          borderRadius: [8, 8, 0, 0]
        },
        data: courseReportData.value?.indicatorAchievements?.map((item) => Number(item.achievement ?? 0)) ?? []
      }
    ]
  })
}

const resizeCharts = () => {
  majorRadarChart?.resize()
  courseObjectiveChart?.resize()
  courseIndicatorChart?.resize()
}

const handleLoadPenetrationAccount = async () => {
  if (!ensureMajorRequestReady()) {
    return
  }

  majorActionLoading.value = true
  majorPenetrationData.value = undefined
  try {
    const data = await getMajorPenetrationAccount(buildMajorRequest())
    majorPenetrationData.value = data
    setMajorStatus('穿透式台账查询成功', '后端已返回台账数据，可以继续补详情展示。', 'success')
  } catch (error) {
    const message = error instanceof Error ? error.message : '穿透式台账查询失败'
    const normalized = normalizeBackendMessage(message, 'major')
    const needCalc = message.includes('尚未计算三级达成度')
    setMajorStatus('穿透式台账查询失败', normalized, 'warning', needCalc ? 'goCalculation' : undefined)
    ElMessage.warning(normalized)
  } finally {
    majorActionLoading.value = false
  }
}

const handleExportMajorAccount = async () => {
  if (!ensureMajorRequestReady()) {
    return
  }
  if (!majorPenetrationData.value) {
    const message = '请先查询穿透式台账，再导出台账 Excel'
    setMajorStatus('暂无法导出专业台账', message, 'info')
    ElMessage.info(message)
    return
  }

  majorActionLoading.value = true
  try {
    const blob = await exportMajorPenetrationAccountExcel(buildMajorRequest())
    const fileName = `${selectedMajor.value?.majorName || '专业'}_${selectedGrade.value}_穿透式台账.xlsx`
    downloadBlob(blob, fileName)
    setMajorStatus('专业台账导出成功', '后端已返回 Excel 文件流，可以继续补导出入口和文件命名优化。', 'success')
  } catch (error) {
    const message = error instanceof Error ? error.message : '专业台账导出失败'
    const normalized = normalizeBackendMessage(message, 'major')
    const needCalc = message.includes('尚未计算三级达成度')
    setMajorStatus('专业台账导出失败', normalized, 'warning', needCalc ? 'goCalculation' : undefined)
    ElMessage.warning(normalized)
  } finally {
    majorActionLoading.value = false
  }
}

const handleExportMajorIndicator = async (format: 'EXCEL' | 'PDF') => {
  if (!ensureMajorRequestReady()) {
    return
  }
  if (!majorRadarData.value) {
    const message = '请先查询雷达图数据，再导出专业达成度'
    setMajorStatus('暂无法导出专业达成度', message, 'info')
    ElMessage.info(message)
    return
  }

  majorActionLoading.value = true
  try {
    const blob =
      format === 'EXCEL'
        ? await exportMajorIndicatorAchievementExcel(buildMajorRequest())
        : await exportMajorIndicatorAchievementPdf(buildMajorRequest())
    const suffix = format === 'EXCEL' ? 'xlsx' : 'pdf'
    const fileName = `${selectedMajor.value?.majorName || '专业'}_${selectedGrade.value}_专业达成度.${suffix}`
    downloadBlob(blob, fileName)
    setMajorStatus(
      `${format === 'EXCEL' ? 'Excel' : 'PDF'} 导出成功`,
      '后端已返回专业指标点达成度文件流，可直接下载使用。',
      'success'
    )
  } catch (error) {
    const message = error instanceof Error ? error.message : '专业指标点达成度导出失败'
    const normalized = normalizeBackendMessage(message, 'major')
    const needCalc = message.includes('尚未计算三级达成度')
    setMajorStatus('专业指标点达成度导出失败', normalized, 'warning', needCalc ? 'goCalculation' : undefined)
    ElMessage.warning(normalized)
  } finally {
    majorActionLoading.value = false
  }
}

const handleLoadRawScores = async (page = rawScoreQuery.value.current || 1) => {
  if (!ensureRawScoreClassSelected()) {
    return
  }

  rawScoreLoading.value = true
  try {
    const classId = Number(selectedClassId.value)
    const studentId = await resolveRawScoreStudentId(classId)
    if (rawScoreStudentNo.value.trim() && !studentId) {
      rawScoreQuery.value.current = page
      resetRawScorePage(page)
      setRawScoreStatus('未查询到该学生', '当前教学班中没有匹配该学号的学生，请检查学号后重新查询。', 'warning')
      return
    }

    const data = await queryGrades({
      classId,
      pointId: rawScorePointId.value,
      studentId,
      current: page,
      pageSize: rawScoreQuery.value.pageSize
    })
    rawScoreQuery.value.current = page
    rawScorePage.value = data
    setRawScoreStatus(
      '原始成绩查询成功',
      rawScorePointId.value
        ? '已按当前教学班和考核点读取原始成绩明细，可继续按学号筛选和导出。'
        : '已从真实成绩录入接口读取当前教学班的考核点原始成绩明细，可继续筛选和导出。',
      'success'
    )
  } catch (error) {
    const message = error instanceof Error ? error.message : '原始成绩查询失败'
    const normalized = normalizeBackendMessage(message, 'course')
    setRawScoreStatus('原始成绩查询失败', normalized, 'warning')
    ElMessage.warning(normalized)
  } finally {
    rawScoreLoading.value = false
  }
}

const handleExportRawScores = async () => {
  if (!rawScoreRows.value.length) {
    ElMessage.warning('当前没有可导出的原始成绩记录')
    return
  }

  rawScoreExporting.value = true
  try {
    const headers = ['学号', '姓名', '考核点编号', '考核点名称', '得分', '满分']
    const rows = rawScoreRows.value.map((item) => [
      item.studentNo,
      item.name,
      item.pointCode || '-',
      item.pointName || '-',
      item.scoreText,
      item.fullScoreText
    ])
    const csvContent = [headers, ...rows]
      .map((row) => row.map((cell) => `"${String(cell ?? '').replace(/"/g, '""')}"`).join(','))
      .join('\r\n')
    const selectedClass = courseClasses.value.find((item) => item.id === selectedClassId.value)
    const fileName = `${selectedClass?.className || '教学班'}_学生原始成绩明细.csv`
    downloadBlob(new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' }), fileName)
    ElMessage.success('学生原始成绩明细已开始下载')
  } finally {
    rawScoreExporting.value = false
  }
}

const handleRawScoreCurrentChange = (page: number) => {
  void handleLoadRawScores(page)
}

const handleRawScoreSizeChange = (size: number) => {
  rawScoreQuery.value.pageSize = size
  rawScoreQuery.value.current = 1
  void handleLoadRawScores(1)
}

const exportMatrixLedgerExcel = () => {
  if (!matrixLedgerRows.value.length) {
    ElMessage.warning('当前专业还没有矩阵台账数据，暂时无法导出')
    return
  }

  const headers = ['专业', '课程代码', '课程名称', '指标点编号', '指标点名称', '所属毕业要求', '权重']
  const rows = matrixLedgerRows.value.map((item) => [
    item.majorName,
    item.courseCode,
    item.courseName,
    item.indicatorCode,
    item.indicatorName,
    item.requirement,
    item.totalWeight
  ])

  const worksheet = XLSX.utils.aoa_to_sheet([headers, ...rows])
  worksheet['!cols'] = [{ wch: 18 }, { wch: 14 }, { wch: 24 }, { wch: 14 }, { wch: 24 }, { wch: 28 }, { wch: 10 }]
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, '宏观支撑矩阵台账')
  const workbookBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' })

  const fileName = `${selectedMajor.value?.majorName || '专业'}-宏观支撑矩阵台账.xlsx`
  downloadBlob(
    new Blob([workbookBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
    fileName
  )
  ElMessage.success('矩阵台账 Excel 已开始下载')
}

const printMatrixLedger = () => {
  if (!matrixLedgerRows.value.length) {
    ElMessage.warning('当前专业还没有矩阵台账数据，暂时无法导出')
    return
  }

  const tableRows = matrixLedgerRows.value
    .map(
      (item) => `
        <tr>
          <td>${escapeHtml(item.majorName)}</td>
          <td>${escapeHtml(item.courseCode)}</td>
          <td>${escapeHtml(item.courseName)}</td>
          <td>${escapeHtml(item.indicatorCode)}</td>
          <td>${escapeHtml(item.indicatorName)}</td>
          <td>${escapeHtml(item.requirement)}</td>
          <td>${escapeHtml(item.totalWeight)}</td>
        </tr>
      `
    )
    .join('')

  const title = `${selectedMajor.value?.majorName || '专业'} - 宏观支撑矩阵台账`
  const htmlContent = `
    <!doctype html>
    <html lang="zh-CN">
      <head>
        <meta charset="UTF-8" />
        <title>${escapeHtml(title)}</title>
        <style>
          body {
            font-family: "Microsoft YaHei", sans-serif;
            margin: 24px;
            color: #1f2937;
          }
          h1 {
            font-size: 20px;
            margin-bottom: 8px;
          }
          p {
            margin: 0 0 16px;
            color: #4b5563;
          }
          table {
            width: 100%;
            border-collapse: collapse;
            font-size: 12px;
          }
          th, td {
            border: 1px solid #d1d5db;
            padding: 8px;
            text-align: left;
            vertical-align: top;
          }
          th {
            background: #f3f4f6;
          }
        </style>
      </head>
      <body>
        <h1>${escapeHtml(title)}</h1>
        <p>共 ${matrixLedgerRows.value.length} 条矩阵台账记录，可在打印窗口中直接另存为 PDF。</p>
        <table>
          <thead>
            <tr>
              <th>专业</th>
              <th>课程代码</th>
              <th>课程名称</th>
              <th>指标点编号</th>
              <th>指标点名称</th>
              <th>所属毕业要求</th>
              <th>权重</th>
            </tr>
          </thead>
          <tbody>${tableRows}</tbody>
        </table>
      </body>
    </html>
  `
  const printUrl = URL.createObjectURL(new Blob([htmlContent], { type: 'text/html;charset=utf-8' }))
  const printWindow = window.open(printUrl, '_blank', 'width=1200,height=800')
  if (!printWindow) {
    URL.revokeObjectURL(printUrl)
    ElMessage.warning('浏览器拦截了打印窗口，请允许弹窗后重试')
    return
  }

  printWindow.focus()
  printWindow.addEventListener('load', () => {
    printWindow.print()
    window.setTimeout(() => URL.revokeObjectURL(printUrl), 1000)
  })
}

onMounted(async () => {
  try {
    const loaders: Array<Promise<unknown>> = []

    if (canUseCourseReport.value) {
      if (user.role === 'teacher') {
        loaders.push(
          listMyTeachingClasses().then((list) => {
            courseClasses.value = list
            const routeMatchedClass = routeClassId.value
              ? list.find((item) => item.id === routeClassId.value)
              : undefined
            selectedClassId.value = routeMatchedClass?.id ?? list[0]?.id
            if (!list.length) {
              setCourseStatus('当前没有主讲的教学班', '请联系教务管理员为你分配教学班并完成课程、教师、学期绑定，再生成课程报表。', 'info')
              setRawScoreStatus('当前没有主讲的教学班', '请先准备教学班与成绩数据，再查询学生原始成绩明细。', 'info')
            }
          })
        )
      } else if (user.role === 'admin') {
        loaders.push(
          pageTeachingClasses({ current: 1, pageSize: 500 }).then((page) => {
            courseClasses.value = page.records
            const routeMatchedClass = routeClassId.value
              ? page.records.find((item) => item.id === routeClassId.value)
              : undefined
            selectedClassId.value = routeMatchedClass?.id ?? page.records[0]?.id
            if (!page.records.length) {
              setCourseStatus('当前暂无可用教学班', '请先在成绩管理页创建教学班并绑定课程、教师、学期，再生成课程报表。', 'info')
              setRawScoreStatus('当前暂无可用教学班', '请先在成绩管理页准备教学班和成绩数据，再查询学生原始成绩明细。', 'info')
            }
          })
        )
      }
    }

    if (canUseMajorPrep.value || canUseMajorReport.value) {
      loaders.push(
        Promise.all([
          user.role === 'admin' ? listMajors() : Promise.resolve([]),
          user.role === 'admin' || user.role === 'edu' || user.role === 'leader' ? listSchoolYears() : Promise.resolve([]),
          user.role === 'admin' || user.role === 'edu' ? pageCourses({ current: 1, pageSize: 500 }) : Promise.resolve({ records: [] } as any),
          user.role === 'leader' ? pageGraduationRequirements({ current: 1, pageSize: 500 }) : Promise.resolve({ records: [] } as any)
        ]).then(([majorList, termList, coursePage, requirementPage]) => {
          majors.value = majorList
          mergeMajorOptions(coursePage.records)
          mergeMajorOptions(requirementPage.records)
          schoolYears.value = termList
          const routeMatchedMajor = routeMajorId.value
            ? majors.value.find((item) => item.id === routeMajorId.value)
            : undefined
          const routeMatchedTerm = routeTermId.value
            ? termList.find((item) => item.id === routeTermId.value)
            : undefined
          selectedMajorId.value = routeMatchedMajor?.id ?? routeMajorId.value ?? majors.value[0]?.id
          selectedTermId.value = routeMatchedTerm?.id ?? routeTermId.value ?? termList[0]?.id
          if (routeGrade.value) {
            selectedGrade.value = routeGrade.value
          }
          if (!majors.value.length || !termList.length) {
            setMajorStatus('专业报表条件未就绪', '当前还缺少专业或学期基础数据，请先补齐基础数据后再生成专业报表。', 'info')
          }
        })
      )
    }

    await Promise.all(loaders)

    if (selectedMajorId.value && canUseMajorPrep.value) {
      await reloadMajorSupportData()
    }
    if (selectedClassId.value && canUseCourseReport.value) {
      await loadRawScoreAssessmentPoints()
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '报表页初始化失败'
    ElMessage.error(message)
  }

  window.addEventListener('resize', resizeCharts)
})

watch(selectedClassId, () => {
  resetRawScorePage(1)
  rawScoreStudentNo.value = ''
  rawScoreStatus.value = undefined

  if (selectedClassId.value && canUseCourseReport.value) {
    classStudentCache.delete(selectedClassId.value)
    void loadRawScoreAssessmentPoints()
  } else {
    rawScoreAssessmentPoints.value = []
    rawScorePointId.value = undefined
  }
})

watch(
  () => majorRadarSeriesData.value.length,
  async (length) => {
    if (!length) {
      majorRadarChart?.dispose()
      majorRadarChart = undefined
      return
    }
    await nextTick()
    renderMajorRadarChart()
  }
)

watch(
  () => courseObjectiveSummaryRows.value.length,
  async (length) => {
    if (!length) {
      courseObjectiveChart?.dispose()
      courseObjectiveChart = undefined
      return
    }
    await nextTick()
    renderCourseObjectiveChart()
  }
)

watch(
  () => courseIndicatorAchievementRows.value.length,
  async (length) => {
    if (!length) {
      courseIndicatorChart?.dispose()
      courseIndicatorChart = undefined
      return
    }
    await nextTick()
    renderCourseIndicatorChart()
  }
)

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  majorRadarChart?.dispose()
  courseObjectiveChart?.dispose()
  courseIndicatorChart?.dispose()
})
</script>

<style scoped>
.top-grid {
  margin-bottom: 20px;
}

.summary-alert {
  margin-bottom: 20px;
}

.summary-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.metric-card {
  border: 1px solid var(--line);
}

.metric-label,
.metric-tip {
  display: block;
}

.metric-label {
  color: #4b5d79;
  font-size: 13px;
}

.metric-value {
  display: block;
  margin: 10px 0 6px;
  color: #123259;
  font-size: 28px;
  line-height: 1.1;
}

.metric-tip {
  color: #6b7b93;
  font-size: 12px;
}

.status-overview-list {
  display: grid;
  gap: 10px;
}

.status-overview-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  padding: 12px 14px;
  border: 1px solid #e8edf5;
  border-left: 4px solid #9bb0c8;
  border-radius: 8px;
  background: #fbfdff;
}

.status-overview-item--success {
  border-left-color: #67c23a;
  background: #fbfef8;
}

.status-overview-item--info {
  border-left-color: #909399;
}

.status-overview-title-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 5px;
}

.status-overview-title {
  color: #1e3555;
  font-weight: 700;
}

.status-overview-role {
  color: #6b7b93;
  font-size: 12px;
}

.status-overview-tip {
  margin: 0;
  color: #5d6d84;
  font-size: 12px;
  line-height: 1.55;
}

.status-overview-tag {
  justify-self: end;
  min-width: 74px;
  text-align: center;
}

.penetration-metric-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.penetration-metric-card {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
  border: 1px solid #e8edf5;
}

.penetration-metric-label {
  color: #4b5d79;
  font-size: 13px;
}

.penetration-metric-value {
  color: #123259;
  font-size: 24px;
  line-height: 1.1;
}

.action-panel {
  margin-top: 20px;
}

.form-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.section-alert {
  margin-bottom: 16px;
}

.section-action {
  margin-bottom: 16px;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 16px;
}

.result-card {
  border: 1px solid var(--line);
}

.report-chart-box {
  height: 320px;
  margin-bottom: 16px;
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
  border: 1px solid #e8edf5;
}

.radar-chart-box {
  height: 360px;
  margin-bottom: 16px;
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
  border: 1px solid #e8edf5;
}

.wide-result-card {
  grid-column: 1 / -1;
}

.section-empty {
  padding: 18px 0 6px;
}

@media (max-width: 720px) {
  .status-overview-item {
    grid-template-columns: 1fr;
  }

  .status-overview-tag {
    justify-self: start;
  }
}
</style>
