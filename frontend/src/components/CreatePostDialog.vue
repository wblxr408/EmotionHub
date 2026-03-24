<template>
  <el-dialog
    v-model="visible"
    title="NEW ARCHIVE ENTRY"
    width="600px"
    :close-on-click-modal="false"
    custom-class="archive-dialog"
  >
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label class="meta">ENTRY CONTENT</label>
        <textarea
          v-model="form.content"
          class="archive-input"
          rows="6"
          placeholder="Document your emotional experience..."
          required
        ></textarea>
      </div>

      <div class="form-group">
        <label class="meta">ATTACHMENTS (OPTIONAL)</label>
        <div class="image-upload-area">
          <div v-for="(img, idx) in form.images" :key="idx" class="image-preview">
            <img :src="img" alt="Preview" />
            <button type="button" class="remove-btn" @click="removeImage(idx)">×</button>
          </div>
          <div v-if="form.images.length < 9" class="upload-btn" @click="triggerUpload">
            <span class="meta">+ ADD IMAGE</span>
          </div>
        </div>
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          multiple
          style="display: none"
          @change="handleFileChange"
        />
        <p class="meta" style="margin-top: 0.5rem; opacity: 0.7">Maximum 9 images</p>
      </div>

      <div class="dialog-footer">
        <button type="button" class="archive-button-outline" @click="visible = false">
          CANCEL
        </button>
        <button type="submit" class="archive-button" :disabled="loading">
          {{ loading ? 'SUBMITTING...' : 'SUBMIT ENTRY' }}
        </button>
      </div>
    </form>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { createPost, listPosts } from '@/api/post'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = ref({
  content: '',
  images: [] as string[]
})

const loading = ref(false)
const fileInput = ref<HTMLInputElement>()

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (!files) return

  // 模拟图片上传（实际项目中需要上传到服务器）
  Array.from(files).forEach((file) => {
    if (form.value.images.length >= 9) {
      ElMessage.warning('Maximum 9 images allowed')
      return
    }

    const reader = new FileReader()
    reader.onload = (e) => {
      form.value.images.push(e.target?.result as string)
    }
    reader.readAsDataURL(file)
  })

  // 重置input
  target.value = ''
}

const removeImage = (index: number) => {
  form.value.images.splice(index, 1)
}

const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

const waitForPostPublished = async (postId: number, maxAttempts = 12, intervalMs = 1000) => {
  for (let attempt = 0; attempt < maxAttempts; attempt++) {
    try {
      const res = await listPosts({
        page: 1,
        size: 20,
        orderBy: 'LATEST'
      })
      const exists = res.data.records.some((post: { id: number }) => post.id === postId)
      if (exists) {
        return true
      }
    } catch {
      // ignore transient errors during polling
    }
    await sleep(intervalMs)
  }
  return false
}

const handleSubmit = async () => {
  loading.value = true
  try {
    const createRes = await createPost({
      content: form.value.content,
      images: form.value.images.length > 0 ? form.value.images : undefined
    })

    const postId = createRes?.data?.id
    if (postId) {
      await waitForPostPublished(postId)
    }

    ElMessage.success('Entry submitted successfully')
    visible.value = false
    emit('success')

    // 重置表单
    form.value = {
      content: '',
      images: []
    }
  } catch (error: any) {
    ElMessage.error(error.message || 'Submission failed')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss">
@import '@/styles/theme.scss';

.archive-dialog {
  .el-dialog__header {
    background: $color-bordeaux;
    color: $color-parchment;
    padding: 1.5rem;
    border: none;

    .el-dialog__title {
      font-family: $font-mono;
      font-size: 1rem;
      letter-spacing: 0.15em;
      color: $color-parchment;
    }
  }

  .el-dialog__body {
    background: $color-parchment;
    padding: 2rem;
  }

  .el-dialog__close {
    color: $color-parchment;

    &:hover {
      color: $color-parchment;
    }
  }
}
</style>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.form-group {
  margin-bottom: 1.5rem;

  label {
    display: block;
    margin-bottom: 0.5rem;
  }
}

.image-upload-area {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

.image-preview {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  border: 1px solid $color-border;

  img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .remove-btn {
    position: absolute;
    top: 0.5rem;
    right: 0.5rem;
    width: 30px;
    height: 30px;
    background: $color-bordeaux;
    color: $color-parchment;
    border: none;
    cursor: pointer;
    font-size: 1.5rem;
    line-height: 1;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      background: $color-charcoal;
    }
  }
}

.upload-btn {
  width: 100%;
  padding-bottom: 100%;
  border: 2px dashed $color-border;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;

  span {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }

  &:hover {
    border-color: $color-bordeaux;
    background: rgba(92, 1, 32, 0.05);
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
}
</style>
