<template>
  <section class="admin-page">
    <!-- Header -->
    <div class="admin-toolbar">
      <div>
        <p class="meta">LLM CONFIGURATION</p>
        <h3>API密钥管理</h3>
        <p class="meta page-hint">{{ pageHint }}</p>
      </div>
      <button class="archive-button archive-button-outline" @click="loadConfigs">
        REFRESH
      </button>
    </div>

    <!-- Info Banner -->
    <div class="archive-card info-banner">
      <p class="meta">PRIORITY CHAIN</p>
      <p>生效优先级：用户个人配置 &gt; 平台默认配置 &gt; 服务端配置文件</p>
      <p class="mt-1">当前平台使用 <strong>通义千问（Qwen）</strong> 进行情感分析。配置你的专属API Key后，分析将使用你的配额。</p>
    </div>

    <!-- Provider Cards -->
    <div class="provider-grid">
      <div
        v-for="p in PROVIDERS"
        :key="p.code"
        class="archive-card provider-card"
        :class="{ active: activeProvider === p.code }"
        @click="selectProvider(p.code)"
      >
        <div class="provider-header">
          <span class="provider-icon">{{ p.icon }}</span>
          <div>
            <h4>{{ p.name }}</h4>
            <p class="meta">{{ p.code.toUpperCase() }}</p>
          </div>
          <span
            v-if="getConfigByProvider(p.code)"
            class="stamp stamp-logged mt-stamp"
          >
            {{ getConfigByProvider(p.code)?.isDefault ? 'DEFAULT' : 'CONFIGURED' }}
          </span>
        </div>
        <p class="provider-desc">{{ p.description }}</p>
        <div v-if="getConfigByProvider(p.code)" class="provider-status">
          <span class="masked-key">{{ getConfigByProvider(p.code)?.maskedApiKey }}</span>
        </div>
        <div v-else class="provider-empty">
          <span class="meta">NOT CONFIGURED</span>
        </div>
      </div>
    </div>

    <!-- Configuration Panel -->
    <div class="archive-card admin-panel config-panel">
      <div class="admin-toolbar">
        <div>
          <p class="meta">CONFIGURATION</p>
          <h3>{{ currentProviderInfo?.name || '选择平台' }} 配置</h3>
        </div>
      </div>

      <div v-if="activeProvider" class="config-form">
        <div class="form-group">
          <label class="meta">LLM PROVIDER</label>
          <div class="provider-badge">
            <span>{{ currentProviderInfo?.icon }} {{ currentProviderInfo?.name }}</span>
          </div>
        </div>

        <div class="form-group">
          <label class="meta">API KEY *</label>
          <div class="input-with-action">
            <input
              v-model="form.apiKey"
              :type="showKey ? 'text' : 'password'"
              class="archive-input"
              :placeholder="currentProviderInfo?.keyPlaceholder"
              autocomplete="off"
            />
            <button
              class="toggle-visibility"
              @click="showKey = !showKey"
              type="button"
            >
              {{ showKey ? 'HIDE' : 'SHOW' }}
            </button>
          </div>
          <p class="form-hint">
            <template v-if="getConfigByProvider(activeProvider)">
              当前已配置：{{ getConfigByProvider(activeProvider)?.maskedApiKey }}
            </template>
            <template v-else>
              留空将使用平台默认Key
            </template>
          </p>
        </div>

        <div class="form-group">
          <label class="meta">CUSTOM API URL <span class="optional">(OPTIONAL)</span></label>
          <input
            v-model="form.apiUrl"
            type="text"
            class="archive-input"
            :placeholder="currentProviderInfo?.urlPlaceholder"
          />
          <p class="form-hint">如使用代理或自定义端点，请填写完整URL</p>
        </div>

        <div class="form-group">
          <label class="meta">MODEL <span class="optional">(OPTIONAL)</span></label>
          <input
            v-model="form.model"
            type="text"
            class="archive-input"
            placeholder="留空使用平台默认模型"
          />
        </div>

        <div class="form-actions">
          <button
            class="archive-button"
            :disabled="saving || !form.apiKey"
            @click="handleSave"
          >
            {{ saving ? 'SAVING...' : 'SAVE CONFIGURATION' }}
          </button>
          <button
            v-if="getConfigByProvider(activeProvider)"
            class="archive-button archive-button-outline danger-btn"
            :disabled="saving"
            @click="handleDelete"
          >
            DELETE
          </button>
        </div>
      </div>

      <div v-else class="empty-state">
        <p class="meta">NO PROVIDER SELECTED</p>
        <p>请从上方选择一个LLM平台进行配置</p>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMyApiKeyConfigs,
  saveApiKeyConfig,
  deleteApiKeyConfig,
  getPlatformApiKeyConfigs,
  PROVIDERS,
  type ApiKeyConfigVO
} from '@/api/apikey'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const route = useRoute()

const pageHint = computed(() =>
  route.path.startsWith('/admin')
    ? '当前页绑定「登录中的账号」；管理员也可从顶栏 LLM KEYS 进入此页。'
    : '为当前登录用户配置 Key；发帖情感分析会优先使用你的通义千问等密钥。'
)

const loading = ref(false)
const saving = ref(false)
const showKey = ref(false)
const activeProvider = ref<string | null>(null)
const userConfigs = ref<ApiKeyConfigVO[]>([])
const platformConfigs = ref<ApiKeyConfigVO[]>([])

const form = reactive({
  apiKey: '',
  apiUrl: '',
  model: ''
})

const currentProviderInfo = computed(() =>
  PROVIDERS.find(p => p.code === activeProvider.value)
)

const allConfigs = computed(() => [...userConfigs.value, ...platformConfigs.value])

function getConfigByProvider(provider: string): ApiKeyConfigVO | undefined {
  return allConfigs.value.find(c => c.provider === provider)
}

function selectProvider(code: string) {
  activeProvider.value = code
  form.apiKey = ''
  form.apiUrl = ''
  form.model = ''
  showKey.value = false

  const existing = getConfigByProvider(code)
  if (existing) {
    form.apiUrl = existing.apiUrl || ''
    form.model = existing.model || ''
  }
}

async function loadConfigs() {
  loading.value = true
  try {
    const [userRes, platformRes] = await Promise.all([
      getMyApiKeyConfigs(),
      userStore.isAdmin ? getPlatformApiKeyConfigs() : Promise.resolve({ data: [] } as any)
    ])
    userConfigs.value = userRes.data || []
    platformConfigs.value = platformRes.data || []
  } catch (error) {
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!activeProvider.value || !form.apiKey) return

  saving.value = true
  try {
    await saveApiKeyConfig({
      provider: activeProvider.value,
      apiKey: form.apiKey,
      apiUrl: form.apiUrl || undefined,
      model: form.model || undefined,
      isDefault: true
    })
    ElMessage.success('配置保存成功')
    form.apiKey = ''
    showKey.value = false
    await loadConfigs()
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

async function handleDelete() {
  if (!activeProvider.value) return

  try {
    await ElMessageBox.confirm(
      `确定删除 ${currentProviderInfo.value?.name} 的API配置？删除后将使用平台默认Key。`,
      '确认删除',
      { type: 'warning' }
    )
    await deleteApiKeyConfig(activeProvider.value)
    ElMessage.success('已删除')
    await loadConfigs()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadConfigs()
})
</script>

<style scoped lang="scss">
@import '@/styles/admin.scss';

.page-hint {
  margin-top: 0.5rem;
  max-width: 42rem;
  line-height: 1.5;
  text-transform: none;
  letter-spacing: 0.04em;
}

.info-banner {
  border-left: 4px solid $color-bordeaux;

  &::before {
    display: none;
  }
}

.provider-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.provider-card {
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    transform: rotate(0deg) translateY(-2px);
  }

  &.active {
    border-color: $color-bordeaux;
    border-width: 2px;
    box-shadow: $shadow-offset $shadow-offset 0 rgba($color-bordeaux, 0.3);
  }

  &::before {
    display: none;
  }
}

.provider-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.5rem;

  h4 {
    margin: 0;
    font-size: 1.1rem;
  }

  .meta {
    margin: 0;
  }
}

.provider-icon {
  font-size: 1.75rem;
  line-height: 1;
}

.provider-desc {
  font-size: 0.875rem;
  color: $color-border;
  margin-bottom: 0.75rem;
}

.provider-status {
  .masked-key {
    font-family: $font-mono;
    font-size: 0.8rem;
    color: $color-charcoal;
    background: rgba($color-border, 0.2);
    padding: 0.2rem 0.5rem;
  }
}

.provider-empty {
  .meta {
    color: $color-border;
  }
}

.mt-stamp {
  margin-left: auto;
}

.config-panel {
  &::before {
    display: none;
  }
}

.config-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;

  label {
    margin: 0;
  }
}

.provider-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: rgba($color-bordeaux, 0.08);
  border: 1px solid $color-bordeaux;
  font-family: $font-mono;
  font-size: 0.875rem;
  color: $color-bordeaux;
  width: fit-content;
}

.input-with-action {
  display: flex;
  gap: 0.5rem;

  .archive-input {
    flex: 1;
  }
}

.toggle-visibility {
  padding: 0.75rem 1rem;
  border: 1px solid $color-border;
  background: rgba($color-parchment, 0.8);
  color: $color-charcoal;
  font-family: $font-mono;
  font-size: 0.75rem;
  letter-spacing: 0.1em;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: $color-border;
    color: $color-charcoal;
  }
}

.form-hint {
  font-size: 0.8rem;
  color: $color-border;
  margin: 0;
  font-family: $font-mono;
}

.optional {
  color: $color-border;
  font-size: 0.75rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 0.5rem;
}

.danger-btn {
  border-color: rgba($color-bordeaux, 0.5);
  color: rgba($color-bordeaux, 0.8);

  &:hover {
    background: $color-bordeaux;
    color: $color-parchment;
    border-color: $color-bordeaux;
  }
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: $color-border;

  p {
    margin: 0.5rem 0 0;
  }
}
</style>
