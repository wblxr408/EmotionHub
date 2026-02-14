<template>
  <div id="app">
    <transition name="fade" mode="out-in">
      <router-view />
    </transition>
  </div>
</template>

<script setup lang="ts">
// App根组件 - 复古学院派设计
</script>

<style>
@import './assets/styles/vintage.css';
</style>
