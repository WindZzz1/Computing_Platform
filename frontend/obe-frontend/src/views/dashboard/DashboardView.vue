<template>
  <div class="page">
    <section class="metric-grid">
      <div v-for="metric in metrics" :key="metric.label" class="metric-card">
        <div class="metric-icon" :class="metric.tone">
          <el-icon :size="24"><component :is="metric.icon" /></el-icon>
        </div>
        <div>
          <div class="metric-label">{{ metric.label }}</div>
          <div class="metric-value">{{ metric.value }}</div>
          <div class="metric-sub">{{ metric.sub }}</div>
        </div>
      </div>
    </section>

    <section class="page-grid">
      <div class="panel span-5">
        <h3 class="panel-title">毕业要求达成度雷达图</h3>
        <div ref="radarEl" class="chart-box"></div>
      </div>
      <div class="panel span-4">
        <h3 class="panel-title">课程计算状态</h3>
        <div ref="pieEl" class="chart-box"></div>
      </div>
      <div class="panel span-3">
        <h3 class="panel-title">最近计算记录</h3>
        <div class="record-list">
          <div v-for="record in records" :key="record.time" class="record-item">
            <b>{{ record.time }}</b>
            <span>课程：{{ record.course }}</span>
            <span>计算人：{{ record.user }}</span>
            <el-tag :type="record.type === '专业级计算' ? 'success' : 'primary'" effect="light">{{ record.type }}</el-tag>
          </div>
        </div>
      </div>

      <div class="panel span-5">
        <h3 class="panel-title">
          通知公告
          <el-button link type="primary">查看更多</el-button>
        </h3>
        <div class="notice-list">
          <div v-for="notice in notices" :key="notice.title" class="notice-item">
            <span>{{ notice.title }}</span>
            <span class="muted">{{ notice.date }}</span>
          </div>
        </div>
      </div>
      <div class="panel span-7">
        <h3 class="panel-title">快捷入口</h3>
        <div class="quick-grid">
          <button v-for="entry in quickEntries" :key="entry.label" class="quick-button" @click="$router.push(entry.path)">
            <el-icon :size="24"><component :is="entry.icon" /></el-icon>
            <span>{{ entry.label }}</span>
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { Aim, Checked, DataAnalysis, Document, Files, Grid, Lock, Notebook, Reading, User } from '@element-plus/icons-vue'
import { courses, indicators } from '@/api/mock'

const radarEl = ref<HTMLDivElement>()
const pieEl = ref<HTMLDivElement>()
let radarChart: echarts.ECharts | undefined
let pieChart: echarts.ECharts | undefined

const metrics = [
  { label: '毕业要求', value: '8', sub: '二级指标点 24', icon: Reading, tone: '' },
  { label: '课程总数', value: '48', sub: '教学班 128', icon: Files, tone: '' },
  { label: '学生总数', value: '2568', sub: '本届学生', icon: User, tone: 'green' },
  { label: '已锁定课程', value: '32', sub: '锁定率 66.67%', icon: Checked, tone: 'yellow' },
  { label: '专业达成度', value: '计算完成', sub: '最近计算：2024-05-20', icon: Lock, tone: 'lock' }
]

const records = [
  { time: '2024-05-20 14:30:25', type: '专业级计算', course: '计算机科学与技术 2023', user: '教务管理员' },
  { time: '2024-05-18 09:15:11', type: '课程级计算', course: '数据结构', user: '张老师' },
  { time: '2024-05-17 16:45:33', type: '课程级计算', course: '高等数学B', user: '李老师' }
]

const notices = [
  { title: '关于2024届毕业要求达成度计算的通知', date: '2024-05-15' },
  { title: '请各位老师及时完成课程成绩录入与计算', date: '2024-05-10' },
  { title: '系统维护通知（5月25日 22:00-24:00）', date: '2024-05-05' }
]

const quickEntries = [
  { label: '毕业要求管理', path: '/basic-data', icon: Document },
  { label: '宏观支撑矩阵', path: '/matrix', icon: Grid },
  { label: '课程大纲配置', path: '/syllabus', icon: Notebook },
  { label: '成绩导入', path: '/score', icon: DataAnalysis },
  { label: '计算状态看板', path: '/calculation', icon: Aim },
  { label: '报表导出', path: '/report', icon: Document }
]

const renderCharts = () => {
  if (!radarEl.value || !pieEl.value) return
  radarChart = echarts.init(radarEl.value)
  pieChart = echarts.init(pieEl.value)
  radarChart.setOption({
    tooltip: {},
    radar: {
      radius: '68%',
      indicator: indicators.slice(0, 8).map((item) => ({ name: `${item.code}${item.name}`, max: 1 })),
      splitNumber: 5,
      axisName: { color: '#1f3656', fontSize: 12 },
      splitArea: { areaStyle: { color: ['#f8fbff', '#eef6ff'] } },
      axisLine: { lineStyle: { color: '#c9d8eb' } },
      splitLine: { lineStyle: { color: '#c9d8eb' } }
    },
    series: [
      {
        type: 'radar',
        data: [{ value: indicators.slice(0, 8).map((item) => item.achievement), name: '达成度' }],
        areaStyle: { color: 'rgba(23, 118, 242, 0.14)' },
        lineStyle: { color: '#1776f2', width: 2 },
        itemStyle: { color: '#1776f2' }
      }
    ]
  })
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', right: 10, top: 'middle' },
    color: ['#4ec66b', '#f7c243', '#ed5d52'],
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['38%', '50%'],
        avoidLabelOverlap: true,
        label: { show: false },
        data: [
          { value: courses.filter((item) => item.status === '已锁定').length, name: '已锁定' },
          { value: courses.filter((item) => item.status === '待计算').length, name: '未计算' },
          { value: courses.filter((item) => item.status === '未提交').length, name: '未提交' }
        ]
      }
    ]
  })
}

const resizeCharts = () => {
  radarChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  renderCharts()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  radarChart?.dispose()
  pieChart?.dispose()
})
</script>
