<template>
  <div class="vintage-login">
    <div class="login-container">
      <!-- 标题区域 -->
      <header class="login-header">
        <h1 class="login-title">EMOTIONHUB</h1>
        <div class="title-line"></div>
        <p class="login-subtitle">Authentication Required</p>
      </header>

      <!-- 登录表单 -->
      <div class="login-card card">
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label for="username" class="form-label">Username</label>
            <input
              id="username"
              v-model="loginForm.username"
              type="text"
              class="form-input"
              placeholder="Enter your username"
              required
            />
          </div>

          <div class="form-group">
            <label for="password" class="form-label">Password</label>
            <input
              id="password"
              v-model="loginForm.password"
              type="password"
              class="form-input"
              placeholder="Enter your password"
              required
            />
          </div>

          <button type="submit" class="btn btn-primary login-btn" :disabled="loading">
            <span v-if="!loading">Enter</span>
            <span v-else class="loading-text">
              Authenticating<span class="loading-cursor"></span>
            </span>
          </button>
        </form>

        <!-- 错误提示 -->
        <div v-if="error" class="error-message">
          <div class="error-line"></div>
          <p class="error-text">{{ error }}</p>
        </div>

        <!-- 提示信息 -->
        <div class="login-footer">
          <p class="footer-note">
            <em>This feature is currently under development.</em>
          </p>
        </div>
      </div>

      <!-- 返回首页 -->
      <div class="back-link">
        <router-link to="/" class="link-text">← Return to Main Archive</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const loginForm = reactive({
  username: '',
  password: ''
})

const loading = ref(false)
const error = ref('')

const handleLogin = async () => {
  loading.value = true
  error.value = ''

  // 模拟登录延迟
  setTimeout(() => {
    error.value = 'Authentication system is not yet operational. Please check back later.'
    loading.value = false
  }, 1500)
}
</script>

<style scoped>
.vintage-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-md);
}

.login-container {
  width: 100%;
  max-width: 500px;
}

/* 标题区域 */
.login-header {
  text-align: center;
  margin-bottom: var(--spacing-lg);
}

.login-title {
  font-size: 2.5rem;
  letter-spacing: 0.15em;
  margin-bottom: var(--spacing-sm);
}

.title-line {
  width: 150px;
  height: 2px;
  background-color: var(--color-burgundy);
  margin: var(--spacing-sm) auto;
}

.login-subtitle {
  font-family: var(--font-serif);
  font-style: italic;
  font-size: 1rem;
  color: var(--color-sand);
  letter-spacing: 0.05em;
}

/* 登录卡片 */
.login-card {
  padding: var(--spacing-lg);
}

.login-form {
  margin-bottom: var(--spacing-md);
}

.form-group {
  margin-bottom: var(--spacing-md);
  text-align: left;
}

.form-label {
  display: block;
  font-family: var(--font-serif);
  font-size: 0.9rem;
  letter-spacing: 0.05em;
  color: var(--color-burgundy);
  margin-bottom: var(--spacing-xs);
  text-transform: uppercase;
}

.form-input {
  width: 100%;
  font-family: var(--font-body);
  font-size: 1rem;
  padding: 0.75rem 1rem;
  border: var(--border-thin);
  background-color: rgba(255, 255, 255, 0.3);
  color: var(--color-charcoal);
  transition: var(--transition-smooth);
}

.form-input:focus {
  outline: none;
  border-color: var(--color-burgundy);
  background-color: rgba(255, 255, 255, 0.5);
}

.form-input::placeholder {
  color: var(--color-sand);
  font-style: italic;
}

.login-btn {
  width: 100%;
  margin-top: var(--spacing-sm);
}

.loading-text {
  display: inline-flex;
  align-items: center;
}

/* 错误提示 */
.error-message {
  margin-top: var(--spacing-md);
  padding-top: var(--spacing-md);
}

.error-line {
  width: 100%;
  height: 2px;
  background-color: var(--color-burgundy);
  margin-bottom: var(--spacing-sm);
}

.error-text {
  color: var(--color-burgundy);
  font-style: italic;
  font-size: 0.95rem;
  text-align: center;
}

/* 页脚提示 */
.login-footer {
  margin-top: var(--spacing-md);
  padding-top: var(--spacing-md);
  border-top: var(--border-thin);
  text-align: center;
}

.footer-note {
  font-size: 0.85rem;
  color: var(--color-sand);
}

/* 返回链接 */
.back-link {
  text-align: center;
  margin-top: var(--spacing-md);
}

.link-text {
  font-family: var(--font-serif);
  font-size: 0.9rem;
  color: var(--color-burgundy);
  text-decoration: none;
  letter-spacing: 0.05em;
  transition: var(--transition-smooth);
}

.link-text:hover {
  color: var(--color-charcoal);
  text-decoration: underline;
}

/* 响应式 */
@media (max-width: 768px) {
  .login-title {
    font-size: 2rem;
  }

  .login-card {
    padding: var(--spacing-md);
  }
}
</style>
