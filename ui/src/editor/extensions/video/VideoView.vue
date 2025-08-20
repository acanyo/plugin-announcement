<script lang="ts" setup>
import { VButton } from "@halo-dev/components";
import type { NodeViewProps } from "@halo-dev/richtext-editor";
import { computed, ref } from "vue";
import RiVideoAddLine from "~icons/ri/video-add-line";
import { EditorLinkObtain } from "../../components";
import InlineBlockBox from "../../components/InlineBlockBox.vue";
import type { AttachmentAttr } from "../../utils/attachment";

const props = defineProps<NodeViewProps>();

const src = computed({
  get: () => {
    return props.node?.attrs.src;
  },
  set: (src: string) => {
    props.updateAttributes({ src: src });
  },
});

const controls = computed(() => {
  return props.node.attrs.controls;
});

const autoplay = computed(() => {
  return props.node.attrs.autoplay;
});

const loop = computed(() => {
  return props.node.attrs.loop;
});

const initialization = computed(() => {
  return !src.value;
});

const editorLinkObtain = ref();

const handleSetExternalLink = (attachment: AttachmentAttr) => {
  props.updateAttributes({
    src: attachment.url,
  });
};

const resetUpload = () => {
  const { file } = props.node.attrs;
  if (file) {
    props.updateAttributes({
      width: undefined,
      height: undefined,
      file: undefined,
    });
  }
};

const handleUploadRetry = () => {
  editorLinkObtain.value?.reset();
};

const handleUploadAbort = () => {
  editorLinkObtain.value?.abort();
};

const handleResetInit = () => {
  editorLinkObtain.value?.reset();
  props.updateAttributes({
    src: "",
    file: undefined,
  });
};
</script>

<template>
  <InlineBlockBox>
    <div
      class=":uno: relative inline-block h-full max-w-full overflow-hidden rounded-md text-center transition-all"
      :class="{
        ':uno: rounded ring-2': selected,
      }"
      :style="{
        width: initialization ? '100%' : node.attrs.width,
      }"
    >
      <div v-if="src" class=":uno: group relative">
        <video
          :src="src"
          :controls="controls"
          :autoplay="autoplay"
          :loop="loop"
          playsinline
          preload="metadata"
          class=":uno: m-0 rounded-md"
          :style="{
            width: node.attrs.width,
            height: node.attrs.height,
          }"
        ></video>
        <div
          v-if="src"
          class=":uno: absolute left-0 top-0 hidden h-1/4 w-full cursor-pointer justify-end bg-gradient-to-b from-gray-300 to-transparent p-2 ease-in-out group-hover:flex"
        >
          <VButton size="sm" type="secondary" @click="handleResetInit">
            替换
          </VButton>
        </div>
      </div>
      <div v-show="!src" class=":uno: relative">
        <EditorLinkObtain
          ref="editorLinkObtain"
          :accept="'video/*'"
          :editor="editor"
          :upload-to-attachment-file="extension.options.uploadVideo"
          :uploaded-file="node?.attrs.file"
          @set-external-link="handleSetExternalLink"
          @on-upload-finish="resetUpload"
          @on-upload-abort="resetUpload"
        >
          <template #icon>
            <div
              class=":uno: flex h-14 w-14 items-center justify-center rounded-full bg-primary/20"
            >
              <RiVideoAddLine class=":uno: text-xl text-primary" />
            </div>
          </template>
          <template #uploading="{ progress }">
            <div class=":uno: absolute top-0 h-full w-full bg-black bg-opacity-20">
              <div class=":uno: absolute top-[50%] w-full space-y-2 text-white">
                <div class=":uno: px-10">
                  <div
                    class=":uno: relative h-4 w-full overflow-hidden rounded-full bg-gray-200"
                  >
                    <div
                      class=":uno: h-full bg-primary"
                      :style="{
                        width: `${progress || 0}%`,
                      }"
                    ></div>
                    <div
                      class=":uno: absolute left-[50%] top-0 -translate-x-[50%] text-xs leading-4 text-white"
                    >
                      {{
                        progress
                          ? `${progress}%`
                          : `等待中...`
                      }}
                    </div>
                  </div>
                </div>

                <div
                  class=":uno: inline-block cursor-pointer text-sm hover:opacity-70"
                  @click="handleUploadAbort"
                >
                  取消
                </div>
              </div>
            </div>
          </template>
          <template #error>
            <div class=":uno: absolute top-0 h-full w-full bg-black bg-opacity-20">
              <div class=":uno: absolute top-[50%] w-full space-y-2 text-white">
                <div class=":uno: px-10">
                  <div
                    class=":uno: relative h-4 w-full overflow-hidden rounded-full bg-gray-200"
                  >
                    <div class=":uno: h-full w-full bg-red-600"></div>
                    <div
                      class=":uno: absolute left-[50%] top-0 -translate-x-[50%] text-xs leading-4 text-white"
                    >
                      上传失败
                    </div>
                  </div>
                </div>
                <div
                  class=":uno: inline-block cursor-pointer text-sm hover:opacity-70"
                  @click="handleUploadRetry"
                >
                  点击重试
                </div>
              </div>
            </div>
          </template>
        </EditorLinkObtain>
      </div>
    </div>
  </InlineBlockBox>
</template>
