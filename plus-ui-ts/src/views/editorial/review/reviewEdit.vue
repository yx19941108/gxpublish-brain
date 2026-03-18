<template>
  <div class="p-2">
    <el-card shadow="never">
      <el-skeleton :rows="4" animated />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { createLegacyReviewFallbackLocation } from './integration';

const route = useRoute();
const router = useRouter();

onMounted(() => {
  router.replace(createLegacyReviewFallbackLocation(route.query as Record<string, unknown>));
});
</script>
