<script lang="ts" setup>
import type { NodeViewProps } from "@halo-dev/richtext-editor";
import { NodeViewWrapper } from "@halo-dev/richtext-editor";
import { computed } from "vue";
import MdiBullhorn from "~icons/mdi/bullhorn";

const props = defineProps<NodeViewProps>();

const pageSize = computed({
  get: () => {
    return (props.node?.attrs.pageSize as number) || 10;
  },
  set: (value: number) => {
    props.updateAttributes({ pageSize: value });
  },
});
</script>

<template>
  <NodeViewWrapper
    as="div"
    :class="[
      'announcement-list-container',
      { 'announcement-list-container--selected': selected },
    ]"
  >
    <div class="announcement-list-nav">
      <div class="announcement-list-nav-start">
        <MdiBullhorn class="announcement-list-icon" />
        <span>公告列表</span>
      </div>
      <div class="announcement-list-nav-end">
        <label class="announcement-list-label">
          每页显示：
          <input
            v-model.number="pageSize"
            type="number"
            min="1"
            max="50"
            class="announcement-list-input"
          />
          条
        </label>
      </div>
    </div>
    <div class="announcement-list-preview">
      <div class="announcement-list-hint">
        此组件将在前台显示公告列表
      </div>
    </div>
  </NodeViewWrapper>
</template>

<style>
.announcement-list-container {
  --tw-ring-offset-shadow: var(--tw-ring-inset) 0 0 0
    var(--tw-ring-offset-width) var(--tw-ring-offset-color);
  --tw-ring-shadow: var(--tw-ring-inset) 0 0 0
    calc(1px + var(--tw-ring-offset-width)) var(--tw-ring-color);
  box-shadow: var(--tw-ring-offset-shadow), var(--tw-ring-shadow),
    var(--tw-shadow, 0 0 #0000);
  --tw-ring-opacity: 1;
  --tw-ring-color: rgb(229 231 235 / var(--tw-ring-opacity));
  border-radius: 4px;
  overflow: hidden;
  margin-top: 0.75em;
}

.announcement-list-container--selected {
  --tw-ring-shadow: var(--tw-ring-inset) 0 0 0
    calc(2px + var(--tw-ring-offset-width)) var(--tw-ring-color);
  --tw-ring-color: #3b82f6;
}

.announcement-list-nav {
  border-bottom: 1px #e7e7e7 solid;
  display: flex;
  padding: 8px 12px;
  align-items: center;
  background: #f9fafb;
}

.announcement-list-nav-start {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.announcement-list-icon {
  width: 18px;
  height: 18px;
  color: #6b7280;
}

.announcement-list-nav-end {
  display: flex;
  align-items: center;
}

.announcement-list-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #4b5563;
}

.announcement-list-input {
  width: 60px;
  padding: 4px 8px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 13px;
}

.announcement-list-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.announcement-list-preview {
  padding: 16px;
  background: #fff;
}

.announcement-list-hint {
  font-size: 13px;
  color: #9ca3af;
  text-align: center;
}
</style>
