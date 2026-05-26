<template>
  <div class="login">
    <section class="login-panel">
      <div class="login-copy">
        <h1>OBE达成度计算平台</h1>
        <p>面向工程教育认证的毕业要求、课程目标、考核点和三层达成度计算原型。</p>
      </div>
      <el-card class="box" shadow="never">
        <h2>进入系统</h2>
        <p class="muted">选择角色查看对应业务入口</p>
        <el-select v-model="role" style="width: 100%; margin: 18px 0">
          <el-option label="系统管理员" value="admin" />
          <el-option label="教务管理员" value="edu" />
          <el-option label="专业负责人" value="leader" />
          <el-option label="课程教师" value="teacher" />
        </el-select>
        <el-button type="primary" style="width: 100%" @click="submit">登录</el-button>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { Role } from '@/types'

const role = ref<Role>('admin')
const user = useUserStore()
const router = useRouter()
const submit = () => {
  user.login(role.value)
  router.push('/dashboard')
}
</script>

<style scoped>
.login {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #061a34 0%, #0b4fb3 52%, #eef6ff 52%, #f6f9ff 100%);
}

.login-panel {
  display: grid;
  grid-template-columns: 1.2fr 380px;
  gap: 36px;
  align-items: center;
  width: min(980px, 100%);
}

.login-copy {
  color: #fff;
}

.login-copy h1 {
  margin: 0 0 16px;
  font-size: 42px;
  letter-spacing: 0;
}

.login-copy p {
  max-width: 560px;
  margin: 0;
  color: #dceaff;
  font-size: 18px;
  line-height: 1.8;
}

.box {
  border: 0;
  border-radius: 8px;
}

.box h2 {
  margin: 0 0 4px;
}

@media (max-width: 860px) {
  .login-panel {
    grid-template-columns: 1fr;
  }
}
</style>
