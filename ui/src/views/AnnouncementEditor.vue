<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { VPageHeader, VCard, VButton, Toast, VLoading, VModal, VSpace } from "@halo-dev/components";
import HaloEditor from "@/editor/HaloEditor.vue";
import { announcementV1alpha1Api, announcementApiClient } from "@/api";
import type { Announcement } from "@/api/generated";
import IconAnnouncementMegaphone from '~icons/streamline-plump/announcement-megaphone?width=1.2em&height=1.2em';
import IconSettings from '~icons/tabler/settings';
import { useRoute } from "vue-router";
import { axiosInstance } from "@halo-dev/api-client";
import type { AxiosRequestConfig } from "axios";

const route = useRoute();
const isEditMode = computed(() => route.name === "AnnouncementEdit");
const announcementName = computed(() => route.params.name as string);

const title = ref("");
const type = ref("");
const permissions = ref("everyone");
const position = ref("center");
const autoClose = ref(0);
const closeOnClickOutside = ref(true);
const popupInterval = ref(0);
const confettiEnable = ref(false);
const enablePopup = ref(false);
const enablePinning = ref(false);

// 编辑器内容
const html = ref("");

// UI 状态
const isLoading = ref(false);
const isSubmitting = ref(false);
const settingsModalVisible = ref(false);

// 公告类型列表
interface AnnouncementType {
  displayName: string;
  color: string;
}
const announcementTypes = ref<AnnouncementType[]>([]);

async function loadAnnouncementTypes() {
  try {
    const { data } = await axiosInstance.get("/api/v1alpha1/configmaps/plugin-announcement-configMap");
    const typesConfig = data?.data?.types;
    if (typesConfig) {
      const parsed = JSON.parse(typesConfig);
      announcementTypes.value = parsed.announcementTypes || [];
    }
  } catch (e) {
    console.error("Failed to load announcement types:", e);
  }
}

const typeOptions = computed(() => {
  const options = [{ label: "无", value: "" }];
  announcementTypes.value.forEach(t => {
    options.push({ label: t.displayName, value: t.displayName });
  });
  return options;
});

async function handleUploadImage(file: File, options?: AxiosRequestConfig) {
  const form = new FormData();
  form.append("file", file);
  form.append("policyName", "default-policy");
  const { data } = await axiosInstance.post(
    "/apis/api.console.halo.run/v1alpha1/attachments/upload",
    form,
    { ...options }
  );
  return data;
}

function slugify(input: string): string {
  return input
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9\u4e00-\u9fa5\s-]/g, "")
    .replace(/\s+/g, "-")
    .replace(/-+/g, "-");
}

const goBack = () => {
  if (window.history.length > 1) window.history.back();
  else window.location.href = "/console/tools/announcements";
};

const loadAnnouncement = async () => {
  if (!isEditMode.value || !announcementName.value) return;

  try {
    isLoading.value = true;
    const { data } = await announcementApiClient.getAnnouncementByName({ name: announcementName.value });

    title.value = data.announcementSpec.title || "";
    type.value = (data.announcementSpec as any).type || "";
    permissions.value = data.announcementSpec.permissions || "everyone";
    position.value = data.announcementSpec.position || "center";
    autoClose.value = data.announcementSpec.autoClose || 0;
    closeOnClickOutside.value = data.announcementSpec.closeOnClickOutside ?? true;
    popupInterval.value = data.announcementSpec.popupInterval || 0;
    confettiEnable.value = data.announcementSpec.confettiEnable ?? false;
    enablePopup.value = (data.announcementSpec as any).enablePopup ?? false;
    enablePinning.value = (data.announcementSpec as any).enablePinning ?? false;
    html.value = data.announcementSpec.content || "";
  } catch (e) {
    console.error("Failed to load announcement:", e);
    Toast.error("加载公告失败");
  } finally {
    isLoading.value = false;
  }
};

const handleSubmit = async () => {
  if (!title.value.trim()) return Toast.warning("请输入标题");
  if (!html.value.trim()) return Toast.warning("请输入公告内容");

  // 弹窗唯一性校验
  if (enablePopup.value) {
    try {
      const { data } = await announcementApiClient.listAnnouncements({
        page: 1,
        size: 100,
        sort: undefined,
        keyword: undefined,
        announcementSpecPermissions: undefined
      });

      const otherEnabledAnnouncements = data.items.filter(
        (announcement: Announcement) =>
          announcement.metadata.name !== announcementName.value &&
          (announcement.announcementSpec as any).enablePopup === true
      );

      if (otherEnabledAnnouncements.length > 0) {
        Toast.error(`全局只能存在一个启用的弹窗！当前检测到 ${otherEnabledAnnouncements.length} 个其他已启用的弹窗，请先禁用后再保存。`);
        return;
      }
    } catch (e) {
      console.error("唯一性校验失败:", e);
      Toast.error("无法完成唯一性校验，请重试");
      return;
    }
  }

  try {
    isSubmitting.value = true;

    const announcementSpec = {
      title: title.value,
      type: type.value || undefined,
      permissions: permissions.value as any,
      content: html.value,
      position: position.value,
      autoClose: Number(autoClose.value || 0),
      closeOnClickOutside: Boolean(closeOnClickOutside.value),
      popupInterval: Number(popupInterval.value || 0),
      confettiEnable: Boolean(confettiEnable.value),
      enablePopup: Boolean(enablePopup.value),
      enablePinning: Boolean(enablePinning.value),
    };

    if (isEditMode.value) {
      const { data } = await announcementApiClient.getAnnouncementByName({ name: announcementName.value });
      await announcementV1alpha1Api.updateAnnouncement({
        name: announcementName.value,
        announcement: { ...data, announcementSpec } as any
      });
      Toast.success("更新成功");
    } else {
      await announcementV1alpha1Api.createAnnouncement({
        announcement: {
          apiVersion: "announcement.lik.cc/v1alpha1",
          kind: "Announcement",
          metadata: { name: slugify(title.value) || `announcement-${Date.now()}` },
          announcementSpec,
        } as any
      });
      Toast.success("创建成功");
    }

    goBack();
  } catch (e) {
    console.error(e);
    Toast.error(isEditMode.value ? "更新失败" : "创建失败");
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(() => {
  loadAnnouncementTypes();
  loadAnnouncement();
});

// 设置项配置
const permissionOptions = [
  { label: "登录用户", value: "loggedInUsers" },
  { label: "未登录用户", value: "nonLoggedInUsers" },
  { label: "所有人", value: "everyone" },
  { label: "不显示", value: "notShown" },
];

const positionOptions = [
  { label: "居中", value: "center" },
  { label: "左下角", value: "left-bottom" },
  { label: "右下角", value: "right-bottom" },
  { label: "左上角", value: "left-top" },
  { label: "右上角", value: "right-top" },
];
</script>

<template>
  <VPageHeader :title="isEditMode ? '编辑公告' : '新建公告'">
    <template #icon>
      <IconAnnouncementMegaphone class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton @click="goBack">返回</VButton>
        <VButton @click="settingsModalVisible = true">
          <template #icon>
            <IconSettings class="h-full w-full" />
          </template>
          设置
        </VButton>
        <VButton :loading="isSubmitting" type="secondary" @click="handleSubmit">
          {{ isEditMode ? '更新' : '发布' }}
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <VLoading v-if="isLoading" />
      <div v-else class="editor-container">
        <HaloEditor
          v-model:raw="html"
          v-model:content="html"
          v-model:title="title"
          :upload-image="handleUploadImage"
        />
      </div>
    </VCard>
  </div>

  <!-- 设置弹窗 -->
  <VModal
    v-model:visible="settingsModalVisible"
    title="公告设置"
    :width="560"
    @close="settingsModalVisible = false"
  >
    <FormKit type="form" :actions="false" class="settings-form">
      <!-- 显示设置 -->
      <div class="settings-section">
        <div class="section-title">显示设置</div>

        <FormKit
          v-model="type"
          type="select"
          label="公告类型"
          :options="typeOptions"
          help="在插件设置中配置类型列表"
        />
        
        <FormKit
          v-model="enablePopup"
          type="switch"
          label="启用弹窗"
          help="全局只能存在一个启用的弹窗"
        />

        <FormKit
          v-model="enablePinning"
          type="switch"
          label="置顶显示"
          help="在公告列表中置顶显示"
        />

        <FormKit
          v-model="permissions"
          type="select"
          label="可见范围"
          :options="permissionOptions"
        />

        <FormKit
          v-model="position"
          type="select"
          label="弹窗位置"
          :options="positionOptions"
        />
      </div>

      <!-- 行为设置 -->
      <div class="settings-section">
        <div class="section-title">行为设置</div>

        <FormKit
          v-model="autoClose"
          type="number"
          label="自动关闭（秒）"
          help="0 表示不自动关闭"
          :min="0"
        />

        <FormKit
          v-model="closeOnClickOutside"
          type="switch"
          label="点击外部关闭"
          help="允许点击弹窗外部区域关闭"
        />

        <FormKit
          v-model="popupInterval"
          type="number"
          label="弹窗间隔（小时）"
          help="用户关闭后，间隔多少小时再次弹出，0 表示不限制"
          :min="0"
        />

        <FormKit
          v-model="confettiEnable"
          type="switch"
          label="礼花效果"
          help="弹窗弹出时显示礼花动画"
        />
      </div>
    </FormKit>

    <template #footer>
      <VSpace>
        <VButton @click="settingsModalVisible = false">关闭</VButton>
      </VSpace>
    </template>
  </VModal>
</template>

<style scoped>
.editor-container {
  min-height: 70vh;
}

.settings-form {
  padding: 8px 0;
}

.settings-section {
  margin-bottom: 24px;
}

.settings-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f3f4f6;
}
</style>
