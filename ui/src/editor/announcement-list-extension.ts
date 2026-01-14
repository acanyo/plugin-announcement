import {
  type Editor,
  mergeAttributes,
  Node,
  type Range,
  VueNodeViewRenderer,
} from "@halo-dev/richtext-editor";
import { markRaw } from "vue";
import AnnouncementListView from "./AnnouncementListView.vue";
import AnnouncementListToolboxItem from "./AnnouncementListToolboxItem.vue";
import MdiFormatListBulleted from "~icons/mdi/bullhorn";

declare module "@halo-dev/richtext-editor" {
  interface Commands<ReturnType> {
    "announcement-list": {
      setAnnouncementList: (options?: { pageSize?: number }) => ReturnType;
    };
  }
}

const AnnouncementListExtension = Node.create({
  name: "announcement-list",
  group: "block",
  atom: true,
  draggable: true,

  addAttributes() {
    return {
      pageSize: {
        default: 10,
        parseHTML: (element: HTMLElement) => {
          return element.getAttribute("page-size") || 10;
        },
        renderHTML: (attributes: { pageSize?: number }) => {
          return {
            "page-size": attributes.pageSize,
          };
        },
      },
    };
  },

  parseHTML() {
    return [
      {
        tag: "announcement-list",
      },
    ];
  },

  renderHTML({ HTMLAttributes }: { HTMLAttributes: Record<string, unknown> }) {
    return ["announcement-list", mergeAttributes(HTMLAttributes)];
  },

  addCommands() {
    return {
      setAnnouncementList:
        (options?: { pageSize?: number }) =>
        ({ commands }: { commands: any }) => {
          return commands.insertContent({
            type: this.name,
            attrs: options || { pageSize: 10 },
          });
        },
    };
  },

  addNodeView() {
    return VueNodeViewRenderer(AnnouncementListView);
  },

  addOptions() {
    return {
      getCommandMenuItems() {
        return {
          priority: 200,
          icon: markRaw(MdiFormatListBulleted),
          title: "公告列表",
          keywords: ["announcement", "gonggao", "list", "liebiao", "公告"],
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .setAnnouncementList({})
              .run();
          },
        };
      },
      getToolboxItems({ editor }: { editor: Editor }) {
        return {
          priority: 60,
          component: markRaw(AnnouncementListToolboxItem),
          props: {
            editor,
          },
        };
      },
    };
  },
});

export default AnnouncementListExtension;
