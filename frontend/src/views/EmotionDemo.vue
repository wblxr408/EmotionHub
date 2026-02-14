<template>
  <div class="demo-page">
    <header class="demo-header">
      <h1>EMOTION VISUALIZATION</h1>
      <div class="header-line"></div>
      <p class="subtitle">Three Approaches to Sentiment Representation</p>
    </header>

    <main class="demo-main">
      <div class="content-wrapper">
        <!-- 控制面板 -->
        <section class="control-panel card">
          <h3>Experimental Controls</h3>

          <div class="control-group">
            <label class="control-label">Sentiment Type</label>
            <div class="button-group">
              <button
                class="btn"
                :class="{ 'btn-primary': emotion === 'positive' }"
                @click="emotion = 'positive'"
              >
                Positive
              </button>
              <button
                class="btn"
                :class="{ 'btn-primary': emotion === 'neutral' }"
                @click="emotion = 'neutral'"
              >
                Neutral
              </button>
              <button
                class="btn"
                :class="{ 'btn-primary': emotion === 'negative' }"
                @click="emotion = 'negative'"
              >
                Negative
              </button>
            </div>
          </div>

          <div class="control-group">
            <label class="control-label">Intensity: {{ intensity.toFixed(2) }}</label>
            <input
              type="range"
              v-model.number="intensity"
              min="0"
              max="1"
              step="0.01"
              class="slider"
            />
          </div>
        </section>

        <div class="divider"></div>

        <!-- 方案A: 动态线条 -->
        <section class="visualization-section">
          <h2>I. The Fluid Line</h2>
          <p class="section-description">
            A delicate horizontal line whose form reflects emotional valence.
            Positive sentiments manifest as gentle waves, while negative emotions
            produce sharp, tremulous movements.
          </p>
          <div class="card viz-card">
            <EmotionVisualizer
              :emotion="emotion"
              :intensity="intensity"
              mode="line"
              :width="vizWidth"
              :height="100"
            />
          </div>
        </section>

        <div class="divider"></div>

        <!-- 方案B: 墨迹扩散 -->
        <section class="visualization-section">
          <h2>II. Ink Density</h2>
          <p class="section-description">
            Inspired by the diffusion of ink upon parchment, this visualization
            employs varying degrees of opacity and blur to represent emotional
            intensity and character.
          </p>
          <div class="card viz-card">
            <EmotionVisualizer
              :emotion="emotion"
              :intensity="intensity"
              mode="ink"
            />
          </div>
        </section>

        <div class="divider"></div>

        <!-- 方案C: 古典刻度 -->
        <section class="visualization-section">
          <h2>III. Antique Scale</h2>
          <p class="section-description">
            A precision instrument reminiscent of Victorian-era scientific apparatus.
            The needle indicates the emotional position along a spectrum from
            melancholy through rationality to fervor.
          </p>
          <div class="card viz-card">
            <EmotionVisualizer
              :emotion="emotion"
              :intensity="intensity"
              mode="scale"
              :width="vizWidth"
              :height="200"
            />
          </div>
        </section>

        <!-- 返回首页 -->
        <div class="back-link">
          <router-link to="/" class="link-text">← Return to Main Archive</router-link>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import EmotionVisualizer from '@/components/EmotionVisualizer.vue'

const emotion = ref<'positive' | 'negative' | 'neutral'>('neutral')
const intensity = ref(0.5)
const windowWidth = ref(window.innerWidth)

const vizWidth = computed(() => {
  return Math.min(windowWidth.value - 100, 600)
})

const handleResize = () => {
  windowWidth.value = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.demo-page {
  min-height: 100vh;
  padding: var(--spacing-lg) var(--spacing-md);
}

/* 页眉 */
.demo-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.demo-header h1 {
  font-size: 2.5rem;
  letter-spacing: 0.15em;
}

.header-line {
  width: 200px;
  height: 2px;
  background-color: var(--color-burgundy);
  margin: var(--spacing-md) auto;
}

.subtitle {
  font-family: var(--font-serif);
  font-style: italic;
  font-size: 1.1rem;
  color: var(--color-sand);
  letter-spacing: 0.05em;
}

/* 主内容 */
.demo-main {
  max-width: 900px;
  margin: 0 auto;
}

.content-wrapper {
  width: 100%;
}

/* 控制面板 */
.control-panel {
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.control-group {
  margin-bottom: var(--spacing-md);
}

.control-group:last-child {
  margin-bottom: 0;
}

.control-label {
  display: block;
  font-family: var(--font-serif);
  font-size: 0.95rem;
  color: var(--color-burgundy);
  margin-bottom: var(--spacing-sm);
  letter-spacing: 0.05em;
}

.button-group {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.button-group .btn {
  flex: 1;
  min-width: 100px;
}

/* 滑块 */
.slider {
  width: 100%;
  height: 2px;
  background: var(--color-sand);
  outline: none;
  -webkit-appearance: none;
  appearance: none;
}

.slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 16px;
  height: 16px;
  background: var(--color-burgundy);
  cursor: pointer;
  border-radius: 50%;
}

.slider::-moz-range-thumb {
  width: 16px;
  height: 16px;
  background: var(--color-burgundy);
  cursor: pointer;
  border-radius: 50%;
  border: none;
}

/* 可视化区域 */
.visualization-section {
  margin-bottom: var(--spacing-xl);
}

.section-description {
  font-size: 1rem;
  line-height: 1.8;
  color: var(--color-charcoal);
  margin-bottom: var(--spacing-md);
  text-align: justify;
}

.viz-card {
  padding: var(--spacing-lg);
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 返回链接 */
.back-link {
  text-align: center;
  margin-top: var(--spacing-xl);
  padding-top: var(--spacing-lg);
  border-top: var(--border-thin);
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
  .demo-header h1 {
    font-size: 2rem;
  }

  .button-group {
    flex-direction: column;
  }

  .button-group .btn {
    width: 100%;
  }

  .control-panel,
  .viz-card {
    padding: var(--spacing-md);
  }
}
</style>
