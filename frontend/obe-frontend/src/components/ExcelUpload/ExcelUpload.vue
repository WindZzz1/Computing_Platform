<template>
  <el-upload drag action="#" :auto-upload="false" accept=".xlsx,.xls">
    <el-icon class="upload-icon"><UploadFilled /></el-icon>
    <div class="upload-title">拖拽 Excel 到这里，或点击选择成绩文件</div>
    <template #tip>
      <div class="muted">支持按“学生-考核点”最小成绩单元导入，字段可映射到 student_score 表。</div>
    </template>
  </el-upload>
</template>

<script setup lang="ts">
import { UploadFilled } from '@element-plus/icons-vue'
</script>

<style scoped>
.upload-icon {
  margin-top: 10px;
  color: var(--primary);
  font-size: 40px;
}

.upload-title {
  margin: 8px 0;
  color: #1e3555;
  font-weight: 700;
}
</style>
