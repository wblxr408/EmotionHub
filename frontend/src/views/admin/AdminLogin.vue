<template>
  <div class="admin-login-page">
    <div class="admin-login-container">
      <!-- 标题区域 -->
      <header class="admin-header">
        <div class="admin-badge">RESTRICTED ACCESS</div>
        <h1>ADMIN PANEL</h1>
        <div class="divider"></div>
        <p class="meta">AUTHORIZED PERSONNEL ONLY</p>
      </header>

      <!-- 错误提示框 -->
      <div v-if="errorMessage" class="error-banner">
        <div class="error-icon">⚠</div>
        <div class="error-content">
          <div class="error-title">AUTHENTICATION FAILED</div>
          <div class="error-message">{{ errorMessage }}</div>
        </div>
        <button class="error-close" @click="errorMessage = ''">✕</button>
      </div>

      <!-- 登录表单 -->
      <div class="admin-card">
        <form @submit.prevent="handleAdminLogin">
          <div class="form-group">
            <label class="meta">ADMIN USERNAME</label>
            <input
              v-model="loginForm.username"
              type="text"
              class="admin-input"
              placeholder="Enter admin username..."
              required
              autocomplete="username"
            />
          </div>

          <div class="form-group">
            <label class="meta">PASSWORD</label>
            <input
              v-model="loginForm.password"
              type="password"
              class="admin-input"
              placeholder="Enter password..."
              required
              autocomplete="current-password"
            />
          </div>

          <!-- 管理员测试账号提示 -->
          <div class="admin-hint">
            <div class="meta" style="margin-bottom: 0.5rem;">Admin Test Account:</div>
            <div class="hint-text">admin_ops / password123</div>
          </div>

          <button type="submit" class="admin-button" :disabled="loading">
            {{ loading ? 'AUTHENTICATING...' : 'ADMIN LOGIN' }}
          </button>
        </form>
      </div>

      <!-- 返回链接 -->
      <div class="back-link">
        <router-link to="/login" class="meta">← USER LOGIN</router-link>
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

const loading = ref(false)
const errorMessage = ref('')

const loginForm = reactive({
  username: '',
  password: ''
})

const handleAdminLogin = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    await userStore.login(loginForm)

    // 严格检查管理员权限
    if (!userStore.isAdmin) {
      errorMessage.value = 'Access denied: Admin credentials required'
      ElMessage.error('仅管理员账号可访问后台')
      userStore.logout()
      return
    }

    ElMessage.success('Admin authentication successful')
    router.push('/admin')
  } catch (error: any) {
    console.error('Admin login error:', error)

    let msg = 'Authentication failed. Please check your credentials.'
    if (error.response?.data?.message) {
      msg = error.response.data.message
    } else if (error.message) {
      msg = error.message
    }

    errorMessage.value = msg
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: linear-gradient(135deg, #1a1a1a 0%, #2d1b1b 100%);
}

.admin-login-container {
  width: 100%;
  max-width: 500px;
}

.admin-header {
  text-align: center;
  margin-bottom: 2rem;

  .admin-badge {
    display: inline-block;
    background-color: #C62828;
    color: white;
    padding: 0.5rem 1.5rem;
    font-family: $font-mono;
    font-size: 0.75rem;
    font-weight: 700;
    letter-spacing: 0.2em;
    margin-bottom: 1.5rem;
    border: 3px solid darken(#C62828, 10%);
    box-shadow: 4px 4px 0 rgba(0, 0, 0, 0.3);
  }

  h1 {
    font-size: 3rem;
    margin-bottom: 1rem;
    color: #fff;
    text-shadow: 4px 4px 0 rgba(0, 0, 0, 0.3);
  }

  .divider {
    width: 100px;
    height: 4px;
    background-color: $color-bordeaux;
    margin: 1.5rem auto;
  }

  .meta {
    margin-top: 1rem;
    color: rgba(255, 255, 255, 0.7);
  }
}

.admin-card {
  background-color: rgba(255, 255, 255, 0.95);
  border: 4px solid $color-bordeaux;
  padding: 2.5rem;
  box-shadow: 8px 8px 0 rgba(92, 1, 32, 0.3);
}

.form-group {
  margin-bottom: 1.5rem;

  label {
    display: block;
    margin-bottom: 0.5rem;
    color: $color-bordeaux;
  }

  .admin-input {
    width: 100%;
    padding: 1rem;
    border: 3px solid $color-bordeaux;
    background-color: #fff;
    font-family: $font-mono;
    font-size: 1rem;
    transition: all 0.2s;

    &:focus {
      outline: none;
      border-color: darken($color-bordeaux, 10%);
      box-shadow: 0 0 0 3px rgba(92, 1, 32, 0.1);
    }
  }
}

.admin-button {
  width: 100%;
  margin-top: 1rem;
  padding: 1.25rem;
  background-color: $color-bordeaux;
  color: white;
  border: 3px solid darken($color-bordeaux, 10%);
  font-family: $font-mono;
  font-size: 1rem;
  font-weight: 700;
  letter-spacing: 0.15em;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 4px 4px 0 rgba(0, 0, 0, 0.2);

  &:hover:not(:disabled) {
    background-color: darken($color-bordeaux, 5%);
    transform: translate(-2px, -2px);
    box-shadow: 6px 6px 0 rgba(0, 0, 0, 0.2);
  }

  &:active:not(:disabled) {
    transform: translate(2px, 2px);
    box-shadow: 2px 2px 0 rgba(0, 0, 0, 0.2);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.back-link {
  text-align: center;
  margin-top: 2rem;

  a {
    color: rgba(255, 255, 255, 0.8);
    text-decoration: none;
    transition: color 0.2s;

    &:hover {
      color: #fff;
      text-decoration: underline;
    }
  }
}

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

.admin-hint {
  background-color: rgba(198, 40, 40, 0.1);
  border: 2px dashed #C62828;
  padding: 1rem;
  margin-bottom: 1rem;
  text-align: center;

  .hint-text {
    font-family: $font-mono;
    font-size: 0.95rem;
    color: #C62828;
    font-weight: 600;
    letter-spacing: 0.05em;
  }
}
</style>
