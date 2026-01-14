<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { VPageHeader, VButton, Toast, VLoading, VSpace, VCard, VTabbar } from "@halo-dev/components";
import HaloEditor from "@/editor/HaloEditor.vue";
import { announcementV1alpha1Api, announcementApiClient } from "@/api";
import type { Announcement } from "@/api/generated";
import IconAnnouncementMegaphone from '~icons/streamline-plump/announcement-megaphone?width=1.2em&height=1.2em';
import { useRoute } from "vue-router";
import { axiosInstance } from "@halo-dev/api-client";
import type { AxiosRequestConfig } from "axios";

const route = useRoute();
const isEditMode = computed(() => route.name === "AnnouncementEdit");
const announcementName = computed(() => route.params.name as string);

const activeTab = ref("basic");
const title = ref("");
const type = ref("");
const permissions = ref("everyone");
const enablePopup = ref(false);
const enablePinning = ref(false);
const position = ref("center");
const autoClose = ref(0);
const closeOnClickOutside = ref(true);
const popupInterval = ref(0);
const confettiEnable = ref(false);
const popupIcon = ref("");
const popupIconBgColor = ref("#60a5fa");
const primaryButtonText = ref("确认");
const primaryButtonColor = ref("#3b82f6");
const primaryButtonAction = ref("closeNotice");
const primaryButtonUrl = ref("");
const primaryButtonCallback = ref("");
const secondaryButtonText = ref("关闭");
const secondaryButtonColor = ref("#6b7280");
const secondaryButtonAction = ref("closeNotice");
const secondaryButtonUrl = ref("");
const secondaryButtonCallback = ref("");
const urlPatterns = ref("");
const html = ref("");
const isLoading = ref(false);
const isSubmitting = ref(false);

interface AnnouncementType { displayName: string; color: string; }
const announcementTypes = ref<AnnouncementType[]>([]);

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

const buttonActionOptions = [
  { label: "关闭公告", value: "closeNotice" },
  { label: "跳转链接", value: "jump" },
  { label: "确认后跳转", value: "confirmJump" },
  { label: "JS回调", value: "callback" },
];

async function loadAnnouncementTypes() {
  try {
    const { data } = await axiosInstance.get("/api/v1alpha1/configmaps/plugin-announcement-configMap");
    const typesConfig = data?.data?.types;
    if (typesConfig) {
      announcementTypes.value = JSON.parse(typesConfig).announcementTypes || [];
    }
  } catch (e) { console.error(e); }
}

const typeOptions = computed(() => {
  const opts = [{ label: "无", value: "" }];
  announcementTypes.value.forEach(t => opts.push({ label: t.displayName, value: t.displayName }));
  return opts;
});

async function handleUploadImage(file: File, options?: AxiosRequestConfig) {
  const form = new FormData();
  form.append("file", file);
  form.append("policyName", "default-policy");
  const { data } = await axiosInstance.post("/apis/api.console.halo.run/v1alpha1/attachments/upload", form, { ...options });
  return data;
}

function slugify(input: string): string {
  return input.toLowerCase().trim().replace(/[^a-z0-9\u4e00-\u9fa5\s-]/g, "").replace(/\s+/g, "-").replace(/-+/g, "-");
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
    const spec = data.announcementSpec as any;
    title.value = spec.title || "";
    type.value = spec.type || "";
    permissions.value = spec.permissions || "everyone";
    position.value = spec.position || "center";
    autoClose.value = spec.autoClose || 0;
    closeOnClickOutside.value = spec.closeOnClickOutside ?? true;
    popupInterval.value = spec.popupInterval || 0;
    confettiEnable.value = spec.confettiEnable ?? false;
    popupIcon.value = spec.popupIcon || "";
    popupIconBgColor.value = spec.popupIconBgColor || "#60a5fa";
    enablePopup.value = spec.enablePopup ?? false;
    enablePinning.value = spec.enablePinning ?? false;
    primaryButtonText.value = spec.primaryButtonText || "确认";
    primaryButtonColor.value = spec.primaryButtonColor || "#3b82f6";
    primaryButtonAction.value = spec.primaryButtonAction || "closeNotice";
    primaryButtonUrl.value = spec.primaryButtonUrl || "";
    primaryButtonCallback.value = spec.primaryButtonCallback || "";
    secondaryButtonText.value = spec.secondaryButtonText || "关闭";
    secondaryButtonColor.value = spec.secondaryButtonColor || "#6b7280";
    secondaryButtonAction.value = spec.secondaryButtonAction || "closeNotice";
    secondaryButtonUrl.value = spec.secondaryButtonUrl || "";
    secondaryButtonCallback.value = spec.secondaryButtonCallback || "";
    urlPatterns.value = spec.urlPatterns || "";
    html.value = spec.content || "";
  } catch (e) {
    Toast.error("加载公告失败");
  } finally {
    isLoading.value = false;
  }
};

const handleSubmit = async () => {
  if (!title.value.trim()) return Toast.warning("请输入标题");
  if (!html.value.trim()) return Toast.warning("请输入公告内容");

  if (enablePopup.value) {
    try {
      const { data } = await announcementApiClient.listAnnouncements({ page: 1, size: 100, sort: undefined, keyword: undefined, announcementSpecPermissions: undefined });
      const others = data.items.filter((a: Announcement) => a.metadata.name !== announcementName.value && (a.announcementSpec as any).enablePopup);
      
      const currentPerm = permissions.value;
      for (const other of others) {
        const otherPerm = (other.announcementSpec as any).permissions;
        
        // 如果其他弹窗是"所有人"，则当前不能启用任何弹窗
        if (otherPerm === "everyone") {
          Toast.error("已存在「所有人」可见的弹窗，无法创建新弹窗");
          return;
        }
        
        // 如果当前选了"所有人"，则不能有任何其他弹窗
        if (currentPerm === "everyone" && others.length > 0) {
          Toast.error("已存在其他弹窗，无法设置为「所有人」可见");
          return;
        }
        
        // 检查登录用户冲突
        if (currentPerm === "loggedInUsers" && otherPerm === "loggedInUsers") {
          Toast.error("已存在针对「登录用户」的弹窗");
          return;
        }
        
        // 检查未登录用户冲突
        if (currentPerm === "nonLoggedInUsers" && otherPerm === "nonLoggedInUsers") {
          Toast.error("已存在针对「未登录用户」的弹窗");
          return;
        }
      }
    } catch (e) {
      Toast.error("无法完成唯一性校验");
      return;
    }
  }

  try {
    isSubmitting.value = true;
    const announcementSpec = {
      title: title.value, type: type.value || undefined, permissions: permissions.value as any, content: html.value,
      position: position.value, autoClose: Number(autoClose.value || 0), closeOnClickOutside: Boolean(closeOnClickOutside.value),
      popupInterval: Number(popupInterval.value || 0), confettiEnable: Boolean(confettiEnable.value),
      popupIcon: popupIcon.value || undefined, popupIconBgColor: popupIconBgColor.value || undefined,
      enablePopup: Boolean(enablePopup.value), enablePinning: Boolean(enablePinning.value),
      primaryButtonText: primaryButtonText.value || undefined, primaryButtonColor: primaryButtonColor.value || undefined,
      primaryButtonAction: primaryButtonAction.value || undefined, primaryButtonUrl: primaryButtonUrl.value || undefined,
      primaryButtonCallback: primaryButtonCallback.value || undefined,
      secondaryButtonText: secondaryButtonText.value || undefined, secondaryButtonColor: secondaryButtonColor.value || undefined,
      secondaryButtonAction: secondaryButtonAction.value || undefined, secondaryButtonUrl: secondaryButtonUrl.value || undefined,
      secondaryButtonCallback: secondaryButtonCallback.value || undefined,
      urlPatterns: urlPatterns.value || undefined,
    };

    if (isEditMode.value) {
      const { data } = await announcementApiClient.getAnnouncementByName({ name: announcementName.value });
      await announcementV1alpha1Api.updateAnnouncement({ name: announcementName.value, announcement: { ...data, announcementSpec } as any });
      Toast.success("更新成功");
    } else {
      await announcementV1alpha1Api.createAnnouncement({
        announcement: { apiVersion: "announcement.lik.cc/v1alpha1", kind: "Announcement", metadata: { name: slugify(title.value) || "announcement-" + Date.now() }, announcementSpec } as any
      });
      Toast.success("创建成功");
    }
    goBack();
  } catch (e) {
    Toast.error(isEditMode.value ? "更新失败" : "创建失败");
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(() => { loadAnnouncementTypes(); loadAnnouncement(); });
</script>

<template>
  <VPageHeader :title="isEditMode ? '编辑公告' : '新建公告'">
    <template #icon>
      <IconAnnouncementMegaphone class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton @click="goBack">返回</VButton>
        <VButton :loading="isSubmitting" type="secondary" @click="handleSubmit">
          {{ isEditMode ? '更新' : '发布' }}
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <VLoading v-if="isLoading" />
  <div v-else class="p-4 flex gap-4">
    <VCard class="flex-1 min-w-0" :body-class="['!p-0']">
      <HaloEditor v-model:raw="html" v-model:content="html" v-model:title="title" :upload-image="handleUploadImage" />
    </VCard>

    <div class="w-72 flex-shrink-0">
      <VCard :body-class="['!p-0']">
        <VTabbar
          v-model:active-id="activeTab"
          :items="[
            { id: 'basic', label: '基本' },
            { id: 'popup', label: '弹窗' },
            { id: 'button', label: '按钮' }
          ]"
          type="outline"
          class="w-full"
        />
        
        <div class="p-4">
          <!-- 基本设置 -->
          <div v-if="activeTab === 'basic'">
            <FormKit type="form" :actions="false">
              <FormKit v-model="type" type="select" label="类型" :options="typeOptions" />
              <FormKit v-model="permissions" type="select" label="可见范围" :options="permissionOptions" />
              <FormKit v-model="enablePinning" type="switch" label="置顶显示" />
              <FormKit v-model="enablePopup" type="switch" label="启用弹窗" />
            </FormKit>
          </div>

          <!-- 显示设置 -->
          <div v-if="activeTab === 'popup'">
            <div v-if="!enablePopup" class="text-sm text-gray-500 text-center py-8">
              请先启用弹窗
            </div>
            <FormKit v-else type="form" :actions="false">
              <FormKit v-model="popupIcon" type="iconify" label="弹窗图标" :value-only="true" format="svg" />
              <FormKit v-model="popupIconBgColor" type="color" label="图标背景色" />
              <FormKit v-model="position" type="select" label="弹窗位置" :options="positionOptions" />
              <FormKit v-model="autoClose" type="number" label="自动关闭(秒)" :min="0" help="0 表示不自动关闭" />
              <FormKit v-model="popupInterval" type="number" label="弹窗间隔(时)" :min="0" help="0 表示不限制" />
              <FormKit v-model="closeOnClickOutside" type="switch" label="点击外部关闭" />
              <FormKit v-model="confettiEnable" type="switch" label="礼花效果" />
              <FormKit v-model="urlPatterns" type="textarea" label="URL匹配规则" rows="3" 
                placeholder="每行一个路径，如：&#10;/&#10;/archives/*&#10;/categories/tech/*" 
                help="为空或/表示仅首页，支持*通配符" />
            </FormKit>
          </div>

          <!-- 按钮设置 -->
          <div v-if="activeTab === 'button'">
            <div v-if="!enablePopup" class="text-sm text-gray-500 text-center py-8">
              请先启用弹窗
            </div>
            <FormKit v-else type="form" :actions="false">
              <div class="space-y-4">
                <div class="text-sm font-medium text-gray-900">主按钮</div>
                <FormKit v-model="primaryButtonText" type="text" label="按钮文字" placeholder="确认" />
                <FormKit v-model="primaryButtonColor" type="color" label="按钮颜色" />
                <FormKit v-model="primaryButtonAction" type="select" label="按钮事件" :options="buttonActionOptions" />
                <FormKit v-if="primaryButtonAction === 'jump' || primaryButtonAction === 'confirmJump'" v-model="primaryButtonUrl" type="text" label="跳转链接" placeholder="https://" />
                <FormKit v-if="primaryButtonAction === 'callback'" v-model="primaryButtonCallback" type="code" label="JS代码" language="javascript" height="120px" help="点击按钮时执行的JS代码" />
                
                <div class="border-t border-gray-100 pt-4 mt-4">
                  <div class="text-sm font-medium text-gray-900 mb-4">副按钮</div>
                </div>
                <FormKit v-model="secondaryButtonText" type="text" label="按钮文字" placeholder="关闭（留空不显示）" />
                <FormKit v-model="secondaryButtonColor" type="color" label="按钮颜色" />
                <FormKit v-model="secondaryButtonAction" type="select" label="按钮事件" :options="buttonActionOptions" />
                <FormKit v-if="secondaryButtonAction === 'jump' || secondaryButtonAction === 'confirmJump'" v-model="secondaryButtonUrl" type="text" label="跳转链接" placeholder="https://" />
                <FormKit v-if="secondaryButtonAction === 'callback'" v-model="secondaryButtonCallback" type="code" label="JS代码" language="javascript" height="120px" help="点击按钮时执行的JS代码" />
              </div>
            </FormKit>
          </div>
        </div>
      </VCard>
    </div>
  </div>
</template>

