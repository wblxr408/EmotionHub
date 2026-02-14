<template>
  <div class="emotion-visualizer">
    <!-- 方案A: 动态线条 (The Fluid Line) -->
    <div v-if="mode === 'line'" class="fluid-line-container">
      <svg :width="width" :height="height" class="emotion-svg">
        <path
          :d="linePath"
          fill="none"
          :stroke="lineColor"
          stroke-width="2"
          class="emotion-path"
        />
      </svg>
      <p class="emotion-label">{{ emotionLabel }}</p>
    </div>

    <!-- 方案B: 墨迹扩散 (Ink Density) -->
    <div v-else-if="mode === 'ink'" class="ink-density-container">
      <div class="ink-blot" :style="inkStyle">
        <div class="ink-core" :style="inkCoreStyle"></div>
      </div>
      <p class="emotion-label">{{ emotionLabel }}</p>
    </div>

    <!-- 方案C: 古典刻度 (Antique Scale) -->
    <div v-else-if="mode === 'scale'" class="antique-scale-container">
      <svg :width="width" :height="height" class="scale-svg">
        <!-- 刻度盘背景 -->
        <circle
          :cx="width / 2"
          :cy="height / 2"
          :r="radius"
          fill="none"
          :stroke="scaleColor"
          stroke-width="2"
        />

        <!-- 刻度线 -->
        <g v-for="(tick, index) in ticks" :key="index">
          <line
            :x1="tick.x1"
            :y1="tick.y1"
            :x2="tick.x2"
            :y2="tick.y2"
            :stroke="scaleColor"
            stroke-width="1"
          />
        </g>

        <!-- 指针 -->
        <line
          :x1="width / 2"
          :y1="height / 2"
          :x2="pointerX"
          :y2="pointerY"
          :stroke="lineColor"
          stroke-width="3"
          class="scale-pointer"
        />

        <!-- 中心点 -->
        <circle
          :cx="width / 2"
          :cy="height / 2"
          r="5"
          :fill="lineColor"
        />
      </svg>

      <!-- 刻度标签 -->
      <div class="scale-labels">
        <span class="scale-label left">Melancholy</span>
        <span class="scale-label center">Rational</span>
        <span class="scale-label right">Fervent</span>
      </div>

      <p class="emotion-label">{{ emotionLabel }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

interface Props {
  emotion: 'positive' | 'negative' | 'neutral'
  intensity?: number // 0-1
  mode?: 'line' | 'ink' | 'scale'
  width?: number
  height?: number
}

const props = withDefaults(defineProps<Props>(), {
  intensity: 0.5,
  mode: 'line',
  width: 400,
  height: 100
})

// 颜色定义
const lineColor = computed(() => {
  const burgundy = '#5C0120'
  const sand = '#8C8471'
  const charcoal = '#2F2F2F'

  if (props.emotion === 'positive') return burgundy
  if (props.emotion === 'negative') return charcoal
  return sand
})

const scaleColor = ref('#8C8471')

// 情感标签
const emotionLabel = computed(() => {
  const labels = {
    positive: 'Positive Sentiment Detected',
    negative: 'Negative Sentiment Detected',
    neutral: 'Neutral Sentiment Detected'
  }
  return labels[props.emotion]
})

// ===== 方案A: 动态线条 =====
const linePath = computed(() => {
  const points: string[] = []
  const segments = 50
  const amplitude = props.emotion === 'positive' ? 20 : props.emotion === 'negative' ? 30 : 10
  const frequency = props.emotion === 'positive' ? 0.05 : props.emotion === 'negative' ? 0.15 : 0.02

  for (let i = 0; i <= segments; i++) {
    const x = (i / segments) * props.width
    let y = props.height / 2

    if (props.emotion === 'positive') {
      // 平滑波浪
      y += Math.sin(i * frequency) * amplitude * props.intensity
    } else if (props.emotion === 'negative') {
      // 锐利折角
      y += (Math.random() - 0.5) * amplitude * props.intensity
    }

    points.push(i === 0 ? `M ${x} ${y}` : `L ${x} ${y}`)
  }

  return points.join(' ')
})

// ===== 方案B: 墨迹扩散 =====
const inkStyle = computed(() => {
  const size = 80 + props.intensity * 120
  const opacity = 0.3 + props.intensity * 0.4

  return {
    width: `${size}px`,
    height: `${size}px`,
    backgroundColor: lineColor.value,
    opacity: opacity,
    filter: `blur(${props.intensity * 20}px)`
  }
})

const inkCoreStyle = computed(() => {
  const coreSize = 40 + props.intensity * 60
  return {
    width: `${coreSize}px`,
    height: `${coreSize}px`,
    backgroundColor: lineColor.value,
    opacity: 0.8
  }
})

// ===== 方案C: 古典刻度 =====
const radius = computed(() => Math.min(props.width, props.height) / 2 - 20)

const ticks = computed(() => {
  const tickArray = []
  const tickCount = 12
  const centerX = props.width / 2
  const centerY = props.height / 2

  for (let i = 0; i < tickCount; i++) {
    const angle = (i / tickCount) * Math.PI - Math.PI / 2
    const innerRadius = radius.value - 10
    const outerRadius = radius.value

    tickArray.push({
      x1: centerX + Math.cos(angle) * innerRadius,
      y1: centerY + Math.sin(angle) * innerRadius,
      x2: centerX + Math.cos(angle) * outerRadius,
      y2: centerY + Math.sin(angle) * outerRadius
    })
  }

  return tickArray
})

const pointerAngle = computed(() => {
  // 负面: -60度, 中性: 0度, 正面: 60度
  const baseAngle = props.emotion === 'positive' ? 60 : props.emotion === 'negative' ? -60 : 0
  return (baseAngle * Math.PI) / 180 - Math.PI / 2
})

const pointerX = computed(() => {
  return props.width / 2 + Math.cos(pointerAngle.value) * (radius.value - 15)
})

const pointerY = computed(() => {
  return props.height / 2 + Math.sin(pointerAngle.value) * (radius.value - 15)
})
</script>

<style scoped>
.emotion-visualizer {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-md);
}

/* 方案A: 动态线条 */
.fluid-line-container {
  width: 100%;
  text-align: center;
}

.emotion-svg {
  display: block;
  margin: 0 auto;
}

.emotion-path {
  transition: all 0.5s ease;
}

/* 方案B: 墨迹扩散 */
.ink-density-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-lg);
}

.ink-blot {
  position: relative;
  border-radius: 50%;
  transition: all 0.8s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ink-core {
  border-radius: 50%;
  transition: all 0.8s ease;
}

/* 方案C: 古典刻度 */
.antique-scale-container {
  width: 100%;
  text-align: center;
}

.scale-svg {
  display: block;
  margin: 0 auto;
}

.scale-pointer {
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.scale-labels {
  display: flex;
  justify-content: space-between;
  margin-top: var(--spacing-sm);
  padding: 0 var(--spacing-md);
}

.scale-label {
  font-family: var(--font-serif);
  font-size: 0.85rem;
  font-style: italic;
  color: var(--color-sand);
  letter-spacing: 0.05em;
}

.scale-label.center {
  color: var(--color-burgundy);
}

/* 情感标签 */
.emotion-label {
  font-family: var(--font-serif);
  font-size: 1rem;
  font-style: italic;
  color: var(--color-burgundy);
  margin-top: var(--spacing-md);
  letter-spacing: 0.05em;
}

/* 响应式 */
@media (max-width: 768px) {
  .scale-labels {
    flex-direction: column;
    gap: var(--spacing-xs);
  }
}
</style>
