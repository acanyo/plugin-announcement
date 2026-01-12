<script lang="ts" setup>
import type { Attachment } from "@halo-dev/api-client";
import { consoleApiClient, ucApiClient } from "@halo-dev/api-client";
import { VLoading } from "@halo-dev/components";
import {
  ExtensionsKit,
  RichTextEditor,
  VueEditor,
  type Extensions,
} from "@halo-dev/richtext-editor";
import type { AttachmentLike, PluginModule } from "@halo-dev/ui-shared";
import { utils } from "@halo-dev/ui-shared";
import type { AxiosRequestConfig } from "axios";
import { onMounted, ref, shallowRef, watch } from "vue";

const props = withDefaults(
  defineProps<{
    title?: string;
    raw?: string;
    content: string;
    uploadImage?: (
      file: File,
      options?: AxiosRequestConfig
    ) => Promise<Attachment>;
  }>(),
  {
    title: "",
    raw: "",
    content: "",
    uploadImage: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:title", value: string): void;
  (event: "update:raw", value: string): void;
  (event: "update:content", value: string): void;
  (event: "update", value: string): void;
}>();

const editor = shallowRef<VueEditor>();
const editorTitleRef = ref();
const isInitialized = ref(false);

const supportedPluginNames = [
  "editor-hyperlink-card",
  "hybrid-edit-block",
  "text-diagram",
  "plugin-katex",
  "ai-assistant",
  "PluginHighlightJS",
  "shiki",
];

async function handleUpload(file: File, options?: AxiosRequestConfig) {
  if (props.uploadImage) {
    return props.uploadImage(file, options);
  }

  if (utils.permission.has(["system:attachments:manage"])) {
    const { data } = await consoleApiClient.storage.attachment.uploadAttachmentForConsole(
      { file },
      options
    );
    return data;
  } else if (utils.permission.has(["uc:attachments:manage"])) {
    const { data } = await ucApiClient.storage.attachment.uploadAttachmentForUc(
      { file },
      options
    );
    return data;
  } else {
    throw new Error("Permission denied");
  }
}

onMounted(async () => {
  const enabledPlugins = (window as any).enabledPlugins?.filter((plugin: any) =>
    supportedPluginNames.includes(plugin.name)
  ) || [];
  const enabledPluginNames = enabledPlugins.map((plugin: any) => plugin.name);
  const enabledPluginModules: PluginModule[] = enabledPluginNames
    .map((name: string) => {
      if ((window as any)[name]) {
        return (window as any)[name];
      }
    })
    .filter(Boolean);

  const extensionsFromPlugins: Extensions = [];

  for (const pluginModule of enabledPluginModules) {
    const callbackFunction =
      pluginModule?.extensionPoints?.["default:editor:extension:create"];

    if (typeof callbackFunction !== "function") {
      continue;
    }

    const extensions = await callbackFunction();
    extensionsFromPlugins.push(...extensions);
  }

  editor.value = new VueEditor({
    content: props.raw,
    extensions: [
      ExtensionsKit.configure({
        placeholder: {
          placeholder: "输入 / 以选择输入类型",
        },
        image: {
          uploadImage: handleUpload,
        },
        video: {
          uploadVideo: handleUpload,
        },
        audio: {
          uploadAudio: handleUpload,
        },
        gallery: {
          uploadImage: handleUpload,
        },
        customExtensions: extensionsFromPlugins,
      }),
    ],
    autofocus: "end",
    onUpdate: () => {
      const html = editor.value?.getHTML() + "";
      emit("update:raw", html);
      emit("update:content", html);
      emit("update", html);
    },
    onCreate: () => {
      isInitialized.value = true;
      if (editor.value?.isEmpty && !props.title) {
        editorTitleRef.value?.focus();
      }
    },
  });
});

watch(
  () => props.raw,
  () => {
    if (props.raw !== editor.value?.getHTML()) {
      editor.value?.commands.setContent(props.raw);
    }
  }
);

function onTitleInput(event: Event) {
  emit("update:title", (event.target as HTMLInputElement).value);
}

function handleFocusEditor(event: KeyboardEvent) {
  if (event.isComposing) {
    return;
  }
  editor.value?.commands.focus("start");
}
</script>

<template>
  <div class="halo-announcement-editor relative">
    <VLoading v-if="!isInitialized" />
    <RichTextEditor v-else-if="editor" :editor="editor" locale="zh-CN">
      <template #content>
        <input
          ref="editorTitleRef"
          :value="title"
          type="text"
          placeholder="请输入标题"
          class="w-full border-x-0 !border-b border-t-0 !border-solid !border-gray-100 p-0 !py-2 text-4xl font-semibold placeholder:text-gray-300"
          @input="onTitleInput"
          @keydown.enter="handleFocusEditor"
        />
      </template>
    </RichTextEditor>
  </div>
</template>

<style lang="scss">
.halo-announcement-editor {
  .ProseMirror {
    padding-bottom: 2rem !important;
    padding-top: 2rem !important;
  }
}
</style>
