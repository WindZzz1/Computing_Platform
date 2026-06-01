<template>
  <div class="login">
    <section class="login-panel">
      <div class="login-copy">
        <h1>OBE达成度计算平台</h1>
        <p>面向工程教育认证的毕业要求、课程目标、考核点和三层达成度计算原型。</p>
      </div>
      <el-card class="box" shadow="never">
        <h2>进入系统</h2>
        <p class="muted">使用后端账号登录，系统会自动保存 JWT Token</p>
        <el-form label-position="top" @submit.prevent>
          <el-form-item label="账号">
            <el-input v-model="form.username" placeholder="请输入账号" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="submit">登录</el-button>
        </el-form>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const form = reactive({
  username: 'admin',
  password: '123456'
})
const loading = ref(false)
const user = useUserStore()
const router = useRouter()
const submit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    await user.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
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
