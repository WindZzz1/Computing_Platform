<template>
  <div class="login">
    <section class="login-panel">
      <div class="login-copy">
        <h1>OBE 达成度计算平台</h1>
        <p>面向专业认证的毕业要求达成度统一计算平台，支持四类角色协同完成基础数据配置、权重配置、达成度计算与报表导出。</p>
      </div>
      <el-card class="box" shadow="never">
        <h2>进入系统</h2>
        <p class="muted">请输入后端环境中的真实账号和密码</p>
        <el-form label-position="top" @submit.prevent="submit">
          <el-form-item label="用户名">
            <el-input v-model="username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="password" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-alert
            v-if="loginError"
            type="error"
            :closable="false"
            show-icon
            :title="loginError"
            style="margin-bottom: 16px"
          />
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="登录后的角色和学院信息将以当前后端账号返回结果为准。"
            style="margin-bottom: 16px"
          />
          <el-button type="primary" style="width: 100%" :loading="loading" @click="submit">登录</el-button>
        </el-form>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getDefaultRoute } from '@/utils/roleAccess'

const username = ref('')
const password = ref('')
const loginError = ref('')
const loading = ref(false)
const user = useUserStore()
const router = useRouter()

const getAxiosResponseMessage = (error: unknown) => {
  if (!axios.isAxiosError(error)) {
    return ''
  }

  const data = error.response?.data
  if (!data) {
    return ''
  }
  if (typeof data === 'string') {
    return data
  }
  if (typeof data === 'object' && 'message' in data) {
    return String((data as { message?: unknown }).message ?? '')
  }
  return ''
}

const isCredentialError = (message: string) =>
  /账号|账户|用户|用户名|密码|凭证|不存在|错误|无效|unauthorized|forbidden/i.test(message)

const normalizeLoginErrorMessage = (error: unknown) => {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status
    const backendMessage = getAxiosResponseMessage(error)

    if (error.code === 'ECONNABORTED' || /timeout/i.test(error.message)) {
      return '登录请求超时，请检查网络或稍后重试'
    }
    if (status === 502) {
      return '后端服务暂时不可用，请检查后端服务或 Nginx 代理是否正常'
    }
    if (status === 503 || status === 504) {
      return '后端服务响应超时或暂不可用，请稍后重试'
    }
    if (!error.response) {
      return '无法连接后端服务，请检查网络连接或确认后端是否已启动'
    }
    if (status === 400 || status === 401 || status === 403) {
      return isCredentialError(backendMessage) ? '账号或密码错误，请重新输入' : backendMessage || '登录权限校验失败，请检查账号状态'
    }

    return backendMessage || `登录失败，服务返回 HTTP ${status ?? '未知'}`
  }

  if (error instanceof Error) {
    if (isCredentialError(error.message)) {
      return '账号或密码错误，请重新输入'
    }
    if (/timeout|超时/i.test(error.message)) {
      return '登录请求超时，请检查网络或稍后重试'
    }
    if (/network|failed to fetch|无法连接/i.test(error.message)) {
      return '无法连接后端服务，请检查网络连接或确认后端是否已启动'
    }
    return error.message || '登录失败，请稍后重试'
  }

  return '登录失败，请稍后重试'
}

const submit = async () => {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loginError.value = ''
  loading.value = true
  try {
    await user.login(username.value, password.value)
    loginError.value = ''
    await router.push(getDefaultRoute(user.role))
  } catch (error) {
    const message = normalizeLoginErrorMessage(error)
    loginError.value = message
    ElMessage.error(message)
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
