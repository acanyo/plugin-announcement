<script lang="ts" setup>
import type { Editor } from "@halo-dev/richtext-editor";
import MdiBullhorn from "~icons/mdi/bullhorn";

const props = defineProps<{
  editor: Editor;
}>();

function handleClick() {
  props.editor.chain().focus().setAnnouncementList({}).run();
}
</script>

<template>
  <button
    class="announcement-toolbox-item"
    @click="handleClick"
  >
    <MdiBullhorn class="announcement-toolbox-icon" />
    <span>公告列表</span>
  </button>
</template>

<style scoped>
.announcement-toolbox-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 12px;
  background: none;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
  text-align: left;
  transition: background 0.15s;
}

.announcement-toolbox-item:hover {
  background: #f3f4f6;
}

.announcement-toolbox-icon {
  width: 18px;
  height: 18px;
  color: #6b7280;
}
</style>
