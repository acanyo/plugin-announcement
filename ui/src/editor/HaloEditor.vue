<script lang="ts" setup>
import {
  ExtensionsKit,
  RichTextEditor,
  VueEditor,
  type Extensions, Extension, Editor, ToolbarItem
} from '@halo-dev/richtext-editor'
import { VLoading, VTabs, VTabItem  } from "@halo-dev/components";
import type {PluginModule} from "@halo-dev/ui-shared";
import { useDebounceFn, useLocalStorage } from '@vueuse/core'
import {
  onBeforeUnmount,
  onMounted,
  ref,
  shallowRef,
  watch,
  nextTick, 
  markRaw
} from 'vue'
import { useAttachmentSelect } from "@/editor/composables/use-attachment";
import type {Attachment} from "@halo-dev/api-client";
import type {AxiosRequestConfig} from "axios";
import LucideHeading1 from "~icons/lucide/heading-1";
import LucideHeading2 from "~icons/lucide/heading-2";
import LucideHeading3 from "~icons/lucide/heading-3";
import LucideHeading4 from "~icons/lucide/heading-4";
import LucideHeading5 from "~icons/lucide/heading-5";
import LucideHeading6 from "~icons/lucide/heading-6";
import MingcuteLayoutRightLine from "~icons/mingcute/layout-right-line";
import MingcuteFullscreenLine from '~icons/mingcute/fullscreen-line'
import MingcuteFullscreenExitLine from '~icons/mingcute/fullscreen-exit-line'
import { OverlayScrollbarsComponent } from 'overlayscrollbars-vue'



const props = withDefaults(
  defineProps<{
    title?: string;
    content: string;
    uploadImage?: (
      file: File,
      options?: AxiosRequestConfig
    ) => Promise<Attachment>;
  }>(),
  {
    title: "",
    content: "",
    uploadImage: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:title", value: string): void;
  (event: "update:content", value: string): void;
  (event: "update", value: string): void;
}>();

interface HeadingNode {
  id: string;
  level: number;
  text: string;
}

const headingIcons: Record<number, any> = {
  1: markRaw(LucideHeading1),
  2: markRaw(LucideHeading2),
  3: markRaw(LucideHeading3),
  4: markRaw(LucideHeading4),
  5: markRaw(LucideHeading5),
  6: markRaw(LucideHeading6),
};


const headingNodes = ref<HeadingNode[]>();
const selectedHeadingNode = ref<HeadingNode>();
const extraActiveId = ref("toc");

const editor = shallowRef<VueEditor>();
const editorTitleRef = ref();

const showSidebar = useLocalStorage("shop-product:editor:show-sidebar", false);
const fullScreenEditor = useLocalStorage("shop-product:editor:full-screen-editor", false);

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

const isInitialized = ref(false);

onMounted(async () => {
  const enabledPluginNames = ((window as any).enabledPlugins || []).map((plugin: any) => plugin.name);
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

  // debounce OnUpdate
  const debounceOnUpdate = useDebounceFn(() => {
    const html = editor.value?.getHTML() + "";
    emit("update:content", html);
    emit("update", html);
  }, 250);

  editor.value = new VueEditor({
    content: props.content,
    extensions: [
      ExtensionsKit.configure({
        placeholder: {
          placeholder: "有什么想说的吗...",
        },
        customExtensions: [...extensionsFromPlugins],
      }),
      Extension.create({
        name: "custom-sidebar-toggle-extension",
        addOptions() {
          return {
            getToolbarItems({ editor }: { editor: Editor }) {
              return {
                priority: 1000,
                component: markRaw(ToolbarItem),
                props: {
                  editor,
                  isActive: showSidebar.value,
                  icon: markRaw(MingcuteLayoutRightLine),
                  title: '显示 / 隐藏侧边栏',
                  action: () => {
                    showSidebar.value = !showSidebar.value;
                  },
                },
              };
            },
          };
        },
      }),
      Extension.create({
        name: "custom-full-screen-editor-extension",
        addOptions() {
          return {
            getToolbarItems({ editor }: { editor: Editor }) {
              return {
                priority: 1001,
                component: markRaw(ToolbarItem),
                props: {
                  editor,
                  isActive: fullScreenEditor.value,
                  icon: markRaw(fullScreenEditor.value ? MingcuteFullscreenExitLine : MingcuteFullscreenLine),
                  title: '全屏编辑',
                  action: () => {
                    fullScreenEditor.value = !fullScreenEditor.value;
                  },
                },
              };
            },
          };
        },
      }),
    ],
    parseOptions: {
      preserveWhitespace: true,
    },
    onUpdate: () => {
      debounceOnUpdate();
    },
    onCreate() {
      isInitialized.value = true;
      nextTick(() => {
        if (editor.value?.isEmpty && !props.title) {
          editorTitleRef.value.focus();
        } else {
          editor.value?.commands.focus();
        }
      });
    },
  });
});

onBeforeUnmount(() => {
  editor.value?.destroy();
});

const handleSelectHeadingNode = (node: HeadingNode) => {
  selectedHeadingNode.value = node;
  document.getElementById(node.id)?.scrollIntoView({ behavior: "smooth" });
};

watch(
  () => props.content,
  () => {
    if (props.content !== editor.value?.getHTML()) {
      editor.value?.commands.setContent(props.content);
    }
  },
  {
    immediate: true,
  }
);

function onTitleInput(event: Event) {
  emit("update:title", (event.target as HTMLInputElement).value);
}

function handleFocusEditor(event:any) {
  if (event.isComposing) {
    return;
  }
  editor.value?.commands.focus("start");
}
</script>

<template>
  <VLoading v-if="!isInitialized" />
  
  <div
    v-else
    class=":uno: h-full overflow-auto rounded-lg border">
    <AttachmentSelectorModal
      v-bind="attachmentOptions"
      v-model:visible="attachmentSelectorModal"
      v-permission="['system:attachments:view', 'uc:attachments:manage']"
      @select="onAttachmentSelect"
      @close="handleCloseAttachmentSelectorModal"
    />
    <div class=":uno: size-full" :class="{':uno: fixed inset-0 z-10' : fullScreenEditor}">
      <RichTextEditor v-if="editor" :editor="editor" locale="zh-CN">
        <template #content>
          <input
            ref="editorTitleRef"
            :value="title"
            type="text"
            :placeholder="'请输入标题'"
            class="w-full border-x-0 !border-b border-t-0 !border-solid !border-gray-100 p-0 !py-2 text-4xl font-semibold leading-none placeholder:text-gray-300"
            @input="onTitleInput"
            @keydown.enter="handleFocusEditor"
          />
        </template>
        <template v-if="showSidebar" #extra>
          <OverlayScrollbarsComponent
            element="div"
            :options="{ scrollbars: { autoHide: 'scroll' } }"
            class=":uno: h-full border-l bg-white"
            defer
          >
            <VTabs v-model:active-id="extraActiveId" type="outline">
              <VTabItem
                id="toc"
                label="大纲"
              >
                <div class="p-1 pt-0">
                  <ul v-if="headingNodes?.length" class=":uno: space-y-1">
                    <li
                      v-for="(node, index) in headingNodes"
                      :key="index"
                      :class="[
                      { ':uno: bg-gray-100': node.id === selectedHeadingNode?.id },
                    ]"
                      class=":uno: bg-gray-100 group cursor-pointer truncate rounded-base px-1.5 py-1 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                      @click="handleSelectHeadingNode(node)"
                    >
                      <div
                        :style="{
                        paddingLeft: `${(node.level - 1) * 0.8}rem`,
                      }"
                        class=":uno: flex items-center gap-2"
                      >
                        <component
                          :is="headingIcons[node.level]"
                          class=":uno: h-4 w-4 rounded-sm bg-gray-100 p-0.5 group-hover:bg-white"
                          :class="[
                          {
                            ':uno: !bg-white': node.id === selectedHeadingNode?.id,
                          },
                        ]"
                        />
                        <span class=":uno: flex-1 truncate">{{ node.text }}</span>
                      </div>
                    </li>
                  </ul>
                  <div v-else class=":uno: flex flex-col items-center py-10">
                  <span class=":uno: text-sm text-gray-600">
                    暂无大纲
                  </span>
                  </div>
                </div>
              </VTabItem>
            </VTabs>
          </OverlayScrollbarsComponent>
        </template>
      </RichTextEditor>
    </div>
  </div>
</template>
