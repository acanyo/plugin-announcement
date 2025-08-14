import Logo from "@/assets/logo.svg";
import type { EditorProvider } from "@halo-dev/console-shared";
import { markRaw, ref, type Ref } from "vue";
import HaloEditor from "./HaloEditor.vue";


interface useEditorExtensionPointsReturn {
  editorProviders: Ref<EditorProvider[]>;
}

export function useEditorExtensionPoints(): useEditorExtensionPointsReturn {

  const editorProviders = ref<EditorProvider[]>([
    {
      name: "halo",
      displayName: 'Halo 编辑器',
      component: markRaw(HaloEditor),
      rawType: "html",
      logo: Logo,
    },
  ]);

  return {
    editorProviders
  };
}
