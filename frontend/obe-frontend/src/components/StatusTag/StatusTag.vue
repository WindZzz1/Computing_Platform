<template>
  <el-tag :type="type" effect="light" round>{{ status }}</el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ status: string }>()
const type = computed(() => {
  if (props.status === '已锁定') return 'success'
  if (props.status === '未提交' || props.status === '未配置') return 'danger'
  return 'warning'
})
</script>
