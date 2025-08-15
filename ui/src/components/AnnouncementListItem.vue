<script lang="ts" setup>
import { formatDatetime } from "@/utils/date";
import {
  VEntity,
  VEntityField,
  VStatusDot,
  VDropdownItem,
  Dialog,
  Toast,
} from "@halo-dev/components";
import {type Announcement} from "@/api/generated";
import {announcementApiClient, announcementV1alpha1Api} from "@/api";
import { nextTick } from "vue";

const props = withDefaults(
  defineProps<{
    announcement: Announcement;
    isSelected?: boolean;
  }>(),
  {
    isSelected: false,
  },
);

const emit = defineEmits(["refresh"]);

const handleDelete = async () => {
  Dialog.warning({
    title: "确定要删除该公告吗？",
    description: "删除之后将无法恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await announcementV1alpha1Api.deleteAnnouncement({ name: props.announcement.metadata.name as string });
        Toast.success("删除成功");
      } catch (error) {
        console.error("Failed to delete announcement", error);
      } finally {
        emit("refresh");
      }
    },
  });
};

const handleEdit = async () => {
  // Navigate to edit page
  const base = window.location.origin + window.location.pathname.replace(/\/?$/, "");
  if (base.endsWith("/announcements")) {
    window.location.href = base + "/edit/" + props.announcement.metadata.name;
  } else {
    window.location.href = "/console/tools/announcements/edit/" + props.announcement.metadata.name;
  }
};

const getPermissionText = (permission: string) => {
  switch (permission) {
    case 'loggedInUsers':
      return '登录用户';
    case 'nonLoggedInUsers':
      return '未登录用户';
    case 'everyone':
      return '所有人';
    case 'notShown':
      return '不显示';
    default:
      return '未知';
  }
};

const getPermissionState = (permission: string) => {
  switch (permission) {
    case 'loggedInUsers':
      return 'success';
    case 'nonLoggedInUsers':
      return 'warning';
    case 'everyone':
      return 'success';
    case 'notShown':
      return 'default';
    default:
      return 'default';
  }
};

const getPositionText = (position?: string) => {
  switch (position) {
    case 'center':
      return '居中';
    case 'left-bottom':
      return '左下角';
    case 'right-bottom':
      return '右下角';
    case 'left-top':
      return '左上角';
    case 'right-top':
      return '右上角';
    default:
      return '未设置';
  }
};

const getBehaviorSummary = (a: Announcement) => {
  const s = a.announcementSpec;
  const auto = s.autoClose && s.autoClose > 0 ? `${s.autoClose}s 自动关闭` : '不自动关闭';
  const outside = s.closeOnClickOutside ? '可外部关闭' : '不可外部关闭';
  const interval = s.popupInterval && s.popupInterval > 0 ? `${s.popupInterval}h 间隔` : '无间隔限制';
  const confetti = s.confettiEnable ? '礼花开启' : '礼花关闭';
  return `${auto} · ${outside} · ${interval} · ${confetti}`;
};

</script>
<template>
  <VEntity :is-selected="isSelected">
    <template #checkbox>
      <HasPermission :permissions="['plugin:announcement:manage']">
        <slot name="checkbox" />
      </HasPermission>
    </template>
    <template #start>
      <VEntityField
        :title="announcement.announcementSpec.title"
      >
        <template #description>
          <div class="text-xs text-gray-500">
            <span>弹出设置：{{ getPositionText(announcement.announcementSpec.position) }}</span>
          </div>
          <div class="text-xs text-gray-500 mt-0.5">
            <span>{{ getBehaviorSummary(announcement) }}</span>
          </div>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField>
        <template #description>
          <VStatusDot
            :state="getPermissionState(announcement.announcementSpec.permissions)"
            :text="getPermissionText(announcement.announcementSpec.permissions)"
          />
        </template>
      </VEntityField>
      <VEntityField v-if="announcement.metadata.deletionTimestamp">
        <template #description>
          <VStatusDot
            v-tooltip="'删除中'"
            state="warning"
            text="删除中"
          />
        </template>
      </VEntityField>
      <VEntityField>
        <template #description>
          <span class="truncate text-xs tabular-nums text-gray-500">
            {{ formatDatetime(announcement.metadata.creationTimestamp) }}
          </span>
        </template>
      </VEntityField>
    </template>
    <template #dropdownItems>
      <HasPermission :permissions="['plugin:announcement:manage']">
        <VDropdownItem @click="handleEdit">
          编辑
        </VDropdownItem>
        <VDropdownItem type="danger" @click="handleDelete">
          删除
        </VDropdownItem>
      </HasPermission>
    </template>
  </VEntity>
</template>
