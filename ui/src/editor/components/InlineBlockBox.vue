<script lang="ts" setup>
import { NodeViewWrapper } from "@halo-dev/richtext-editor";
</script>
<template>
  <node-view-wrapper as="div" class=":uno: inline-block-box inline-block">
    <slot></slot>
  </node-view-wrapper>
</template>
