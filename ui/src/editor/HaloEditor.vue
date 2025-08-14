<script lang="ts" setup>
import {
  DecorationSet,
  Editor,
  Extension,
  ExtensionBlockquote,
  ExtensionBold,
  ExtensionBulletList,
  ExtensionClearFormat,
  ExtensionCode,
  ExtensionCodeBlock,
  ExtensionColor,
  ExtensionColumn,
  ExtensionColumns,
  ExtensionCommands,
  ExtensionDetails,
  ExtensionDocument,
  ExtensionDraggable,
  ExtensionDropcursor,
  ExtensionFontSize,
  ExtensionFormatBrush,
  ExtensionGapcursor,
  ExtensionHardBreak,
  ExtensionHeading,
  ExtensionHighlight,
  ExtensionHistory,
  ExtensionHorizontalRule,
  ExtensionIframe,
  ExtensionIndent,
  ExtensionItalic,
  ExtensionLink,
  ExtensionListKeymap,
  ExtensionNodeSelected,
  ExtensionOrderedList,
  ExtensionPlaceholder,
  ExtensionRangeSelection,
  ExtensionSearchAndReplace,
  ExtensionStrike,
  ExtensionSubscript,
  ExtensionSuperscript,
  ExtensionTable,
  ExtensionTaskList,
  ExtensionText,
  ExtensionTextAlign,
  ExtensionTrailingNode,
  ExtensionUnderline,
  Plugin,
  PluginKey,
  RichTextEditor,
  ToolbarItem,
  ToolboxItem,
  type Extensions,
} from "@halo-dev/richtext-editor";
import {
  IconFolder,
} from "@halo-dev/components";
import type {AttachmentLike, PluginModule} from "@halo-dev/console-shared";
import ExtensionCharacterCount from "@tiptap/extension-character-count";
import { useDebounceFn } from "@vueuse/core";
import {
  inject,
  markRaw,
  onBeforeUnmount,
  onMounted,
  ref,
  shallowRef,
  watch,
  type ComputedRef,
} from "vue";
import { useAttachmentSelect } from "@/composables/use-attachment";
import { getContents } from "./utils/attachment";
import {useExtension} from "@/components/editor/composables/use-extension.ts";
import {
  UiExtensionAudio,
  UiExtensionImage,
  UiExtensionUpload,
  UiExtensionVideo,
} from "./extensions";
import type {Attachment} from "@halo-dev/api-client";
import type {AxiosRequestConfig} from "axios";
import ContentHiddenExtension from "@/editor/content-hidden.ts";
import ContentSeparatorExtension from "@/editor/separator.ts";



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

declare module "@halo-dev/richtext-editor" {
  interface Commands<ReturnType> {
    global: {
      openAttachmentSelector: (
        callback: (attachments: AttachmentLike[]) => void,
        options?: {
          accepts?: string[];
          min?: number;
          max?: number;
        }
      ) => ReturnType;
    };
  }
}

interface HeadingNode {
  id: string;
  level: number;
  text: string;
}

const headingNodes = ref<HeadingNode[]>();
const selectedHeadingNode = ref<HeadingNode>();


const editor = shallowRef<Editor>();
const editorTitleRef = ref();
const supportedPluginNames = ["editor-hyperlink-card","hybrid-edit-block","text-diagram","plugin-katex","ai-assistant","PluginHighlightJS"];

const attachmentSelectorModal = ref(false);
const { onAttachmentSelect, attachmentResult } = useAttachmentSelect();

const initAttachmentOptions = {
  accepts: ["*/*"],
  min: undefined,
  max: undefined,
};
const attachmentOptions = ref<{
  accepts?: string[];
  min?: number;
  max?: number;
}>(initAttachmentOptions);

const handleCloseAttachmentSelectorModal = () => {
  attachmentOptions.value = initAttachmentOptions;
};

const presetExtensions = [
  ExtensionBlockquote,
  ExtensionBold,
  ExtensionBulletList,
  ExtensionCode,
  ExtensionDocument,
  ExtensionDropcursor.configure({
    width: 2,
    class: "dropcursor",
    color: "skyblue",
  }),
  ExtensionGapcursor,
  ExtensionHardBreak,
  ExtensionHeading,
  ExtensionHistory,
  ExtensionHorizontalRule,
  ExtensionItalic,
  ExtensionOrderedList,
  ExtensionStrike,
  ExtensionText,
  UiExtensionImage.configure({
    inline: true,
    allowBase64: false,
    HTMLAttributes: {
      loading: "lazy",
    },
    uploadImage: props.uploadImage,
  }),
  ExtensionTaskList,
  ExtensionLink.configure({
    autolink: false,
    openOnClick: false,
  }),
  ExtensionTextAlign.configure({
    types: ["heading", "paragraph"],
  }),
  ExtensionUnderline,
  ExtensionTable.configure({
    resizable: true,
  }),
  ExtensionSubscript,
  ExtensionSuperscript,
  ExtensionPlaceholder.configure({
    placeholder: '输入 / 以选择输入类型',
  }),
  ExtensionHighlight,
  ExtensionCommands,
  ExtensionCodeBlock,
  ExtensionIframe,
  UiExtensionVideo.configure({
    uploadVideo: props.uploadImage,
  }),
  UiExtensionAudio.configure({
    uploadAudio: props.uploadImage,
  }),
  ExtensionCharacterCount,
  ExtensionFontSize,
  ExtensionColor,
  ExtensionIndent,
  Extension.create({
    name: "custom-heading-extension",
    addGlobalAttributes() {
      return [
        {
          types: ["heading"],
          attributes: {
            id: {
              default: null,
            },
          },
        },
      ];
    },
  }),
  Extension.create({
    name: "custom-attachment-extension",
    addOptions() {
      return {
        getToolboxItems({ editor }: { editor: Editor }) {
          return [
            {
              priority: 0,
              component: markRaw(ToolboxItem),
              props: {
                editor,
                icon: markRaw(IconFolder),
                title: '选择附件',
                action: () => {
                  editor.commands.openAttachmentSelector((attachment) => {
                    editor
                        .chain()
                        .focus()
                        .insertContent(getContents(attachment))
                        .run();
                  });
                  return true;
                },
              },
            },
          ];
        },
      };
    },
    addCommands() {
      return {
        openAttachmentSelector: (callback, options) => () => {
          if (options) {
            attachmentOptions.value = options;
          }
          attachmentSelectorModal.value = true;
          attachmentResult.updateAttachment = (
                  attachments: AttachmentLike[]
          ) => {
            callback(attachments);
          };
          return true;
        },
      };
    },
  }),
  ExtensionDraggable,
  ExtensionColumns,
  ExtensionColumn,
  ExtensionNodeSelected,
  ExtensionTrailingNode,
  Extension.create({
    name: "get-heading-id-extension",
    addProseMirrorPlugins() {
      return [
        new Plugin({
          key: new PluginKey("get-heading-id"),
          props: {
            decorations: (state) => {
              const headings: HeadingNode[] = [];
              const { doc } = state;
              doc.descendants((node) => {
                if (node.type.name === ExtensionHeading.name) {
                  headings.push({
                    level: node.attrs.level,
                    text: node.textContent,
                    id: node.attrs.id,
                  });
                }
              });
              headingNodes.value = headings;
              if (!selectedHeadingNode.value) {
                selectedHeadingNode.value = headings[0];
              }
              return DecorationSet.empty;
            },
          },
        }),
      ];
    },
  }),
  ExtensionListKeymap,
  UiExtensionUpload,
  ExtensionSearchAndReplace,
  ExtensionClearFormat,
  ExtensionFormatBrush,
  ExtensionRangeSelection,
  ExtensionDetails.configure({
    persist: true,
  }),
  ContentHiddenExtension,
  ContentSeparatorExtension
];

const { filterDuplicateExtensions } = useExtension();

onMounted(async () => {
  const enabledPlugins = window.enabledPlugins.filter((plugin) =>
          supportedPluginNames.includes(plugin.name)
  );
  const enabledPluginNames = enabledPlugins.map((plugin) => plugin.name);
  const enabledPluginModules: PluginModule[] = enabledPluginNames
          .map((name) => {
            if (window[name as keyof Window]) {
              return window[name as keyof Window];
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

  const extensions = filterDuplicateExtensions([
    ...presetExtensions,
    ...extensionsFromPlugins,
  ]);

  // debounce OnUpdate
  const debounceOnUpdate = useDebounceFn(() => {
    const html = editor.value?.getHTML() + "";
    emit("update:raw", html);
    emit("update:content", html);
    emit("update", html);
  }, 250);

  editor.value = new Editor({
    content: props.raw,
    extensions,
    parseOptions: {
      preserveWhitespace: true,
    },
    onUpdate: () => {
      debounceOnUpdate();
    },
    onCreate() {
      if (editor.value?.isEmpty && !props.title) {
        editorTitleRef.value.focus();
      } else {
        editor.value?.commands.focus();
      }
    },
  });
});

onBeforeUnmount(() => {
  editor.value?.destroy();
});

watch(
  () => props.raw,
  () => {
    if (props.raw !== editor.value?.getHTML()) {
      editor.value?.commands.setContent(props.raw);
    }
  },
  {
    immediate: true,
  }
);

function onTitleInput(event: Event) {
  emit("update:title", (event.target as HTMLInputElement).value);
}

function handleFocusEditor(event) {
  if (event.isComposing) {
    return;
  }
  editor.value?.commands.focus("start");
}
</script>

<template>
  <div>
    <AttachmentSelectorModal
      v-bind="attachmentOptions"
      v-model:visible="attachmentSelectorModal"
      v-permission="['system:attachments:view', 'uc:attachments:manage']"
      @select="onAttachmentSelect"
      @close="handleCloseAttachmentSelectorModal"
    />
    <RichTextEditor v-if="editor" :editor="editor" locale="zh-CN">
      <template #content>
        <input
          ref="editorTitleRef"
          :value="title"
          type="text"
          :placeholder="'请输入标题'"
          class="w-full border-x-0 !border-b border-t-0 !border-solid !border-gray-100 p-0 !py-2 text-4xl font-semibold placeholder:text-gray-300"
          @input="onTitleInput"
          @keydown.enter="handleFocusEditor"
        />
      </template>
    </RichTextEditor>
  </div>
</template>
