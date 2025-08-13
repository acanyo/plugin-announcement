import { definePlugin } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import Announcements from "@/views/Announcements.vue";
import IconAnnouncementMegaphone from '~icons/streamline-plump-color/announcement-megaphone?width=1.2em&height=1.2em';

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: "ToolsRoot",
      route: {
        path: "announcements",
        name: "Announcements",
        component: Announcements,
        meta: {
          title: "公告管理",
          description: "管理网站公告，支持自定义弹窗内容、按钮行为、动画效果和智能弹出控制",
          searchable: true,
          permissions: ["plugin:announcement:manage"],
          menu: {
            name: "公告管理",
            icon: markRaw(IconAnnouncementMegaphone),
            priority: 0,
          },
        },
      },
    },
    {
      parentName: "ToolsRoot",
      route: {
        path: "announcements/new",
        name: "AnnouncementCreate",
        component: () => import("@/views/AnnouncementEditor.vue"),
        meta: {
          title: "新建公告",
          description: "创建新的公告",
          searchable: false,
          permissions: ["plugin:announcement:manage"],
        },
      },
    },
  ],
  extensionPoints: {},
});
