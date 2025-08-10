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
import {announcementApiClient} from "@/api";
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
        // 由于API中没有deleteAnnouncement方法，这里暂时注释掉
        // await announcementApiClient.deleteAnnouncement(props.announcement.metadata.name as string);
        Toast.success("此功能需要实现删除API");
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
  // 编辑公告的逻辑，可以通过路由导航到编辑页面
  // router.push(`/announcements/edit/${props.announcement.metadata.name}`);
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
      return 'warning'; // 修改为 warning，因为 info 不是有效的状态类型
    case 'notShown':
      return 'default';
    default:
      return 'default';
  }
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
        :description="announcement.metadata.name"
      >
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
