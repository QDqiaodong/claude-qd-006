<template>
  <el-container style="min-height: 100vh">
    <el-header style="display:flex;align-items:center;gap:28px;background:#f5f7fa">
      <strong style="font-size:16px">中央厨房 · 备餐批次与出餐配送</strong>
      <el-menu mode="horizontal" :default-active="route.path" router style="flex:1;border:none;background:transparent">
        <el-menu-item index="/kitchens">操作间与设备</el-menu-item>
        <el-menu-item index="/batches">菜品与备餐批次</el-menu-item>
        <el-menu-item index="/slots">操作间排期</el-menu-item>
        <el-menu-item index="/deliveries">出餐配送</el-menu-item>
      </el-menu>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
</script>
