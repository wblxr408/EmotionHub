<template>
  <div class="comment-item">
    <div class="comment-header">
      <div class="comment-avatar">{{ comment.nickname.charAt(0).toUpperCase() }}</div>
      <div class="comment-info">
        <span class="meta username">{{ comment.nickname }}</span>
        <span class="timestamp">{{ formatDate(comment.createdAt) }}</span>
      </div>
    </div>

    <div class="comment-content">
      <p>{{ comment.content }}</p>
    </div>

    <div class="comment-actions">
      <button class="link-btn" @click="emit('reply', comment)">
        <span class="meta">↩ REPLY</span>
      </button>
      <button v-if="comment.likeCount > 0" class="link-btn">
        <span class="meta">♥ {{ comment.likeCount }}</span>
      </button>
    </div>

    <!-- 子评论 -->
    <div v-if="comment.children && comment.children.length > 0" class="comment-children">
      <CommentItem
        v-for="child in comment.children"
        :key="child.id"
        :comment="child"
        @reply="emit('reply', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Comment } from '@/api/interaction'

defineProps<{
  comment: Comment
}>()

const emit = defineEmits<{
  reply: [comment: Comment]
}>()

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}
</script>

<style scoped lang="scss">
@import '@/styles/theme.scss';

.comment-item {
  padding: 1rem 0;
  border-bottom: 1px solid rgba(166, 158, 133, 0.3);

  &:last-child {
    border-bottom: none;
  }
}

.comment-header {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  background: $color-border;
  color: $color-charcoal;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: $font-heading;
  font-size: 1.25rem;
  font-weight: 700;
  flex-shrink: 0;
}

.comment-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.username {
  font-weight: 700;
}

.comment-content {
  margin-left: 56px;
  margin-bottom: 0.5rem;
  line-height: 1.6;
}

.comment-actions {
  margin-left: 56px;
  display: flex;
  gap: 1.5rem;
}

.link-btn {
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  color: $color-bordeaux;

  &:hover {
    text-decoration: underline;
  }
}

.comment-children {
  margin-left: 56px;
  margin-top: 1rem;
  padding-left: 1rem;
  border-left: 2px solid $color-border;
}
</style>
