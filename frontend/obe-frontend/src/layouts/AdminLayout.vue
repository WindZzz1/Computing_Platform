<template>
  <el-container class="shell">
    <el-aside width="224px" class="side">
      <div class="brand">OBE 达成度计算平台</div>
      <el-menu router :default-active="$route.path" class="menu">
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-sub-menu index="basic">
          <template #title>
            <el-icon><Files /></el-icon>
            <span>基础数据管理</span>
          </template>
          <el-menu-item index="/basic-data">专业课程库</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/matrix">
          <el-icon><Grid /></el-icon>
          <span>矩阵配置</span>
        </el-menu-item>
        <el-sub-menu index="course">
          <template #title>
            <el-icon><Notebook /></el-icon>
            <span>课程大纲管理</span>
          </template>
          <el-menu-item index="/syllabus">课程目标与权重</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="score">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>成绩管理与计算</span>
          </template>
          <el-menu-item index="/score">成绩导入与预览</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="major">
          <template #title>
            <el-icon><Aim /></el-icon>
            <span>专业级计算</span>
          </template>
          <el-menu-item index="/calculation">计算看板</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="report">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>报表与导出</span>
          </template>
          <el-menu-item index="/report">报表中心</el-menu-item>
        </el-sub-menu>
      </el-menu>
      <div class="user-card">
        <el-avatar :size="34">{{ user.name.slice(0, 1) }}</el-avatar>
        <div>
          <b>{{ user.name }}</b>
          <span>{{ user.roleName }}</span>
        </div>
        <el-button link class="logout" @click="logout">退出</el-button>
      </div>
    </el-aside>
    <el-container>
      <el-header class="top">
        <div class="top-left">
          <el-icon><Menu /></el-icon>
          <span>{{ $route.meta.title }}</span>
        </div>
        <div class="top-right">
          <el-badge :value="13" class="badge">
            <el-icon><Bell /></el-icon>
          </el-badge>
          <el-icon><QuestionFilled /></el-icon>
          <el-avatar :size="30">{{ user.name.slice(0, 1) }}</el-avatar>
          <span>{{ user.roleName }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  Aim,
  ArrowDown,
  Bell,
  DataAnalysis,
  Document,
  Files,
  Grid,
  House,
  Menu,
  Notebook,
  QuestionFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const user = useUserStore()
const router = useRouter()

const logout = () => {
  user.logout()
  router.push('/login')
}
</script>

<style scoped>
.shell {
  min-height: 100vh;
}

.side {
  position: sticky;
  top: 0;
  height: 100vh;
  padding: 14px;
  background: linear-gradient(180deg, var(--nav), var(--nav-2));
  color: #fff;
}

.brand {
  display: flex;
  align-items: center;
  height: 48px;
  padding: 0 10px;
  font-size: 18px;
  font-weight: 850;
}

.menu {
  height: calc(100vh - 142px);
  border-right: 0;
  background: transparent;
}

.menu :deep(.el-menu-item),
.menu :deep(.el-sub-menu__title) {
  height: 45px;
  margin: 5px 0;
  border-radius: 6px;
  color: #dbe8f8;
}

.menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: var(--primary);
}

.menu :deep(.el-menu-item:hover),
.menu :deep(.el-sub-menu__title:hover) {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.menu :deep(.el-menu) {
  background: transparent;
}

.user-card {
  display: grid;
  grid-template-columns: 34px 1fr auto;
  align-items: center;
  gap: 10px;
  min-height: 58px;
  padding: 10px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.08);
}

.user-card span {
  display: block;
  margin-top: 2px;
  color: #b8c9dd;
  font-size: 12px;
}

.logout {
  color: #d8e8ff;
}

.top {
  height: 64px;
  border-bottom: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.94);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 4px 20px rgba(40, 60, 90, 0.05);
}

.top-left,
.top-right {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #192b45;
  font-weight: 700;
}

.top-right {
  gap: 14px;
  color: #334155;
  font-size: 14px;
}

.badge {
  line-height: 1;
}
</style>
