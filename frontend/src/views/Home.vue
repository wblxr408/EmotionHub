<template>
  <div class="vintage-home">
    <!-- 页眉 -->
    <header class="vintage-header">
      <div class="header-content">
        <h1 class="site-title">EMOTIONHUB</h1>
        <p class="site-subtitle">A Sentiment Analysis Platform</p>
      </div>
      <div class="header-line"></div>
    </header>

    <!-- 主内容区 -->
    <main class="vintage-main">
      <div class="content-wrapper">
        <!-- 欢迎区域 -->
        <section class="welcome-section">
          <h2>Welcome to the Archive</h2>
          <p class="intro-text">
            This platform employs advanced linguistic models to analyze the emotional undertones
            of social media discourse. Each post is examined through the lens of computational
            sentiment analysis, revealing patterns invisible to the casual observer.
          </p>
          <div class="divider"></div>
        </section>

        <!-- 测试连接卡片 -->
        <section class="test-section">
          <div class="card vintage-card">
            <h3>System Diagnostics</h3>
            <p class="card-description">
              Verify the connection between the frontend interface and the backend analysis engine.
            </p>

            <button class="btn btn-primary" @click="testApi" :disabled="loading">
              <span v-if="!loading">Test Connection</span>
              <span v-else class="loading-text">
                Connecting<span class="loading-cursor"></span>
              </span>
            </button>

            <!-- 结果显示 -->
            <div v-if="apiResult" class="result-display">
              <div class="result-line"></div>
              <blockquote class="result-quote">
                {{ apiResult }}
              </blockquote>
              <p class="result-timestamp">
                <em>Recorded at {{ formatTime(timestamp) }}</em>
              </p>
            </div>

            <!-- 错误显示 -->
            <div v-if="error" class="error-display">
              <div class="error-line"></div>
              <p class="error-text">{{ error }}</p>
            </div>
          </div>
        </section>

        <!-- 功能介绍 -->
        <section class="features-section">
          <h2>Capabilities</h2>
          <div class="features-grid">
            <div class="feature-card card">
              <h4>I. Textual Analysis</h4>
              <p>Deep examination of linguistic patterns and emotional markers within user-generated content.</p>
            </div>
            <div class="feature-card card">
              <h4>II. Statistical Visualization</h4>
              <p>Graphical representation of sentiment trends through minimalist data visualization.</p>
            </div>
            <div class="feature-card card">
              <h4>III. Real-time Monitoring</h4>
              <p>Continuous observation of emotional shifts across the social media landscape.</p>
            </div>
          </div>

          <!-- 导航按钮 -->
          <div class="navigation-buttons">
            <router-link to="/emotion-demo" class="btn btn-primary">
              View Emotion Visualizations
            </router-link>
            <router-link to="/login" class="btn">
              Access System
            </router-link>
          </div>
        </section>
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="vintage-footer">
      <div class="footer-line"></div>
      <p class="footer-text">
        © 2026 EmotionHub Research Institute · Established for Academic Purposes
      </p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { testConnection } from '@/api/test'

const apiResult = ref('')
const error = ref('')
const loading = ref(false)
const timestamp = ref(0)

const testApi = async () => {
  loading.value = true
  error.value = ''
  apiResult.value = ''

  try {
    const res = await testConnection()
    apiResult.value = res.data
    timestamp.value = res.timestamp
  } catch (err: any) {
    error.value = 'Connection failed. Please ensure the backend service is operational.'
  } finally {
    loading.value = false
  }
}

const formatTime = (ts: number) => {
  const date = new Date(ts)
  return date.toLocaleString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.vintage-home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 页眉样式 */
.vintage-header {
  padding: var(--spacing-xl) var(--spacing-md) var(--spacing-md);
  text-align: center;
}

.site-title {
  font-size: 3rem;
  letter-spacing: 0.15em;
  margin-bottom: var(--spacing-xs);
  font-weight: 700;
}

.site-subtitle {
  font-family: var(--font-serif);
  font-style: italic;
  font-size: 1.1rem;
  color: var(--color-sand);
  letter-spacing: 0.05em;
}

.header-line {
  width: 200px;
  height: 2px;
  background-color: var(--color-burgundy);
  margin: var(--spacing-md) auto 0;
}

/* 主内容区 */
.vintage-main {
  flex: 1;
  padding: var(--spacing-lg) var(--spacing-md);
}

.content-wrapper {
  max-width: 900px;
  margin: 0 auto;
}

/* 欢迎区域 */
.welcome-section {
  margin-bottom: var(--spacing-xl);
}

.intro-text {
  font-size: 1.1rem;
  line-height: 1.9;
  text-align: justify;
  color: var(--color-charcoal);
}

/* 测试区域 */
.test-section {
  margin-bottom: var(--spacing-xl);
}

.vintage-card {
  text-align: center;
  padding: var(--spacing-lg);
}

.card-description {
  margin-bottom: var(--spacing-md);
  font-style: italic;
  color: var(--color-sand);
}

.btn {
  margin-top: var(--spacing-sm);
  min-width: 200px;
}

.loading-text {
  display: inline-flex;
  align-items: center;
}

/* 结果显示 */
.result-display {
  margin-top: var(--spacing-md);
  padding-top: var(--spacing-md);
}

.result-line {
  width: 100%;
  height: 1px;
  background-color: var(--color-sand);
  margin-bottom: var(--spacing-md);
}

.result-quote {
  font-size: 1.2rem;
  margin: var(--spacing-md) 0;
  border-left: none;
  padding-left: 0;
}

.result-timestamp {
  font-size: 0.9rem;
  color: var(--color-sand);
  margin-top: var(--spacing-sm);
}

/* 错误显示 */
.error-display {
  margin-top: var(--spacing-md);
  padding-top: var(--spacing-md);
}

.error-line {
  width: 100%;
  height: 2px;
  background-color: var(--color-burgundy);
  margin-bottom: var(--spacing-md);
}

.error-text {
  color: var(--color-burgundy);
  font-style: italic;
}

/* 功能介绍 */
.features-section {
  margin-bottom: var(--spacing-xl);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--spacing-md);
  margin-top: var(--spacing-md);
}

.feature-card h4 {
  font-family: var(--font-serif);
  color: var(--color-burgundy);
  margin-bottom: var(--spacing-sm);
  font-size: 1.1rem;
  letter-spacing: 0.05em;
}

.feature-card p {
  font-size: 0.95rem;
  line-height: 1.7;
  margin-bottom: 0;
}

/* 导航按钮 */
.navigation-buttons {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
  margin-top: var(--spacing-lg);
  flex-wrap: wrap;
}

.navigation-buttons .btn {
  text-decoration: none;
  display: inline-block;
}

/* 页脚 */
.vintage-footer {
  padding: var(--spacing-lg) var(--spacing-md);
  text-align: center;
}

.footer-line {
  width: 100px;
  height: 1px;
  background-color: var(--color-sand);
  margin: 0 auto var(--spacing-md);
}

.footer-text {
  font-size: 0.85rem;
  color: var(--color-sand);
  letter-spacing: 0.05em;
}

/* 响应式 */
@media (max-width: 768px) {
  .site-title {
    font-size: 2rem;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .vintage-header,
  .vintage-main,
  .vintage-footer {
    padding-left: var(--spacing-sm);
    padding-right: var(--spacing-sm);
  }
}
</style>
