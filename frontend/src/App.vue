<template>
  <div id="app">
    <AppHeader v-if="!isLoginPage" />
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './components/AppHeader.vue'

const route = useRoute()
const isLoginPage = computed(() => route.path === '/login')
</script>

<style lang="scss">
@import './styles/theme.scss';

.app-main {
  min-height: calc(100vh - 100px);
}
</style>
