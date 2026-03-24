<template>
  <div class="auth-page">
    <div class="auth-container">
      <!-- 标题区域 -->
      <header class="auth-header">
        <h1>EMOTIONHUB</h1>
        <div class="divider"></div>
        <p class="meta">ARCHIVE ACCESS PORTAL</p>
      </header>

      <!-- Tab切换 -->
      <div class="folder-tabs">
        <div class="tab" :class="{ active: mode === 'login' }" @click="switchMode('login')">
          LOGIN
        </div>
        <div class="tab" :class="{ active: mode === 'register' }" @click="switchMode('register')">
          REGISTER
        </div>
      </div>

      <!-- 错误提示框 -->
      <div v-if="errorMessage" class="error-banner">
        <div class="error-icon">⚠</div>
        <div class="error-content">
          <div class="error-title">ACCESS DENIED</div>
          <div class="error-message">{{ errorMessage }}</div>
        </div>
        <button class="error-close" @click="errorMessage = ''">✕</button>
      </div>

      <!-- 登录表单 -->
      <div v-if="mode === 'login'" class="archive-card">
        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label class="meta">USERNAME</label>
            <input
              v-model="loginForm.username"
              type="text"
              class="archive-input"
              placeholder="Enter username..."
              required
              autocomplete="username"
            />
          </div>

          <div class="form-group">
            <label class="meta">PASSWORD</label>
            <input
              v-model="loginForm.password"
              type="password"
              class="archive-input"
              placeholder="Enter password..."
              required
              autocomplete="current-password"
            />
          </div>

          <!-- 测试账号提示 -->
          <div class="test-account-hint">
            <div class="meta" style="margin-bottom: 0.5rem;">Test Account:</div>
            <div class="hint-text">alice_chen / password123</div>
          </div>

          <button type="submit" class="archive-button" :disabled="loading">
            {{ loading ? 'AUTHENTICATING...' : 'ACCESS ARCHIVE' }}
          </button>
        </form>
      </div>

      <!-- 注册表单 -->
      <div v-else class="archive-card">
        <form @submit.prevent="handleRegister">
          <div class="form-group">
            <label class="meta">USERNAME</label>
            <input
              v-model="registerForm.username"
              type="text"
              class="archive-input"
              placeholder="Choose username..."
              required
              autocomplete="username"
            />
          </div>

          <div class="form-group">
            <label class="meta">NICKNAME</label>
            <input
              v-model="registerForm.nickname"
              type="text"
              class="archive-input"
              placeholder="Display name..."
              required
              autocomplete="name"
            />
          </div>

          <div class="form-group">
            <label class="meta">EMAIL</label>
            <input
              v-model="registerForm.email"
              type="email"
              class="archive-input"
              placeholder="your.email@domain.com"
              required
              autocomplete="email"
            />
          </div>

          <div class="form-group">
            <label class="meta">PASSWORD</label>
            <input
              v-model="registerForm.password"
              type="password"
              class="archive-input"
              placeholder="Enter password..."
              required
              autocomplete="new-password"
            />
          </div>

          <button type="submit" class="archive-button" :disabled="loading">
            {{ loading ? 'PROCESSING...' : 'CREATE ACCOUNT' }}
          </button>
        </form>
      </div>

      <!-- 返回链接 -->
      <div class="back-link">
        <router-link to="/" class="meta">← RETURN TO PLAZA</router-link>
        <span class="meta" style="margin: 0 1rem;">|</span>
        <router-link to="/admin/login" class="meta admin-link">ADMIN PANEL →</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const mode = ref<'login' | 'register'>('login')
const loading = ref(false)
const errorMessage = ref('')

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  nickname: '',
  email: '',
  password: ''
})

// 切换模式时清空错误信息
const switchMode = (newMode: 'login' | 'register') => {
  mode.value = newMode
  errorMessage.value = ''
}

const handleLogin = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    await userStore.login(loginForm)

    // 🔥 管理员账号不允许在普通用户登录页登录
    if (userStore.isAdmin) {
      errorMessage.value = 'Admin accounts cannot login here. Please use Admin Panel.'
      ElMessage.warning('管理员请使用管理员后台登录')
      userStore.logout()
      return
    }

    ElMessage.success('Authentication successful')
    router.push('/')
  } catch (error: any) {
    console.error('Login error:', error)

    // 提取错误信息
    let msg = 'Login failed. Please check your credentials.'
    if (error.response?.data?.message) {
      msg = error.response.data.message
    } else if (error.message) {
      msg = error.message
    }

    // 显示在自定义错误框和Element Plus消息中
    errorMessage.value = msg
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    await userStore.register(registerForm)
    ElMessage.success('Account created successfully')
    router.push('/')
  } catch (error: any) {
    console.error('Register error:', error)

    // 提取错误信息
    let msg = 'Registration failed. Please try again.'
    if (error.response?.data?.message) {
      msg = error.response.data.message
    } else if (error.message) {
      msg = error.message
    }

    // 显示在自定义错误框和Element Plus消息中
    errorMessage.value = msg
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.auth-container {
  width: 100%;
  max-width: 500px;
}

.auth-header {
  text-align: center;
  margin-bottom: 2rem;

  h1 {
    font-size: 3rem;
    margin-bottom: 1rem;
  }

  .meta {
    margin-top: 1rem;
  }
}

.form-group {
  margin-bottom: 1.5rem;

  label {
    display: block;
    margin-bottom: 0.5rem;
  }

  .archive-input {
    width: 100%;
  }
}

.archive-button {
  width: 100%;
  margin-top: 1rem;
}

.back-link {
  text-align: center;
  margin-top: 2rem;
  display: flex;
  justify-content: center;
  align-items: center;

  a {
    color: $color-bordeaux;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }

    &.admin-link {
      color: #C62828;
      font-weight: 700;
    }
  }
}

// 错误提示框
.error-banner {
  background-color: #FFEBEE;
  border: 3px solid #C62828;
  padding: 1.25rem;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  box-shadow: 4px 4px 0 rgba(198, 40, 40, 0.2);
  position: relative;

  .error-icon {
    font-size: 1.75rem;
    color: #C62828;
    font-weight: bold;
    line-height: 1;
    flex-shrink: 0;
  }

  .error-content {
    flex: 1;

    .error-title {
      font-family: $font-mono;
      font-size: 0.875rem;
      font-weight: 700;
      color: #B71C1C;
      letter-spacing: 0.15em;
      margin-bottom: 0.5rem;
    }

    .error-message {
      font-family: $font-body;
      font-size: 0.95rem;
      color: #C62828;
      font-weight: 600;
      line-height: 1.5;
    }
  }

  .error-close {
    background: none;
    border: none;
    color: #C62828;
    font-size: 1.5rem;
    cursor: pointer;
    padding: 0;
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    line-height: 1;

    &:hover {
      color: #B71C1C;
      font-weight: bold;
    }
  }
}

// 测试账号提示
.test-account-hint {
  background-color: rgba(92, 1, 32, 0.05);
  border: 1px dashed $color-bordeaux;
  padding: 1rem;
  margin-bottom: 1rem;
  text-align: center;

  .hint-text {
    font-family: $font-mono;
    font-size: 0.95rem;
    color: $color-bordeaux;
    font-weight: 600;
    letter-spacing: 0.05em;
  }
}
</style>
