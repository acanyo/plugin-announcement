<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { VPageHeader, VCard, VButton, Toast, VLoading } from "@halo-dev/components";
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

const title = ref("");
const permissions = ref("everyone");
const position = ref("center");
const autoClose = ref(0);
const closeOnClickOutside = ref(true);
const popupInterval = ref(0);
const confettiEnable = ref(false);
const enablePopup = ref(false);
const enablePinning = ref(false);

// 编辑器内容（默认空）
const html = ref("");

// Loading state
const isLoading = ref(false);

async function handleUploadImage(file: File, options?: AxiosRequestConfig) {
  const form = new FormData();
  form.append("file", file);
  form.append("policyName", "default-policy");
  const { data } = await axiosInstance.post(
    "/apis/api.console.halo.run/v1alpha1/attachments/upload",
    form,
    {
      ...options,
    }
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

const isSubmitting = ref(false);

// Load announcement data for editing
const loadAnnouncement = async () => {
  if (!isEditMode.value || !announcementName.value) return;

  try {
    isLoading.value = true;
    const { data } = await announcementApiClient.getAnnouncementByName({ name: announcementName.value });

    // Populate form with existing data
    title.value = data.announcementSpec.title || "";
    permissions.value = data.announcementSpec.permissions || "everyone";
    position.value = data.announcementSpec.position || "center";
    autoClose.value = data.announcementSpec.autoClose || 0;
    closeOnClickOutside.value = data.announcementSpec.closeOnClickOutside ?? true;
    popupInterval.value = data.announcementSpec.popupInterval || 0;
    confettiEnable.value = data.announcementSpec.confettiEnable ?? false;
    enablePopup.value = (data.announcementSpec as any).enablePopup ?? true;
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

    // 增强的弹窗启用唯一性校验
  if (enablePopup.value) {
    try {
      // 获取所有公告以检查唯一性
      const { data } = await announcementApiClient.listAnnouncements({
        page: 1,
        size: 20,
        sort: undefined,
        keyword: "true",
        announcementSpecPermissions: undefined
      });

      const otherEnabledAnnouncements = data.items.filter(
        (announcement: Announcement) =>
          announcement.metadata.name !== announcementName.value &&
          (announcement.announcementSpec as any).enablePopup === true
      );

      if (otherEnabledAnnouncements.length > 0) {
        Toast.error(`⚠️ 重要提示：全局只能存在一个启用的弹窗！当前检测到 ${otherEnabledAnnouncements.length} 个其他已启用的弹窗，请先禁用后再保存。`);
        return;
      }
    } catch (e) {
      console.error("禁用其他公告弹窗失败:", e);
      Toast.error("错误：无法完成唯一性校验，请检查网络连接或重试");
      return; // 如果唯一性校验失败，停止操作
    }
  }

  try {
    isSubmitting.value = true;

    if (isEditMode.value) {
      // Update existing announcement
      const { data } = await announcementApiClient.getAnnouncementByName({ name: announcementName.value });
      const updatedAnnouncement: Announcement = {
        ...data,
        announcementSpec: {
          ...data.announcementSpec,
          title: title.value,
          permissions: permissions.value as any,
          content: html.value,
          position: position.value,
          autoClose: Number(autoClose.value || 0),
          closeOnClickOutside: Boolean(closeOnClickOutside.value),
          popupInterval: Number(popupInterval.value || 0),
          confettiEnable: Boolean(confettiEnable.value),
          enablePopup: Boolean(enablePopup.value),
          enablePinning: Boolean(enablePinning.value),
        } as any,
      };
      await announcementV1alpha1Api.updateAnnouncement({
        name: announcementName.value,
        announcement: updatedAnnouncement
      });
      Toast.success("更新成功");
    } else {
      // Create new announcement
      const body: Announcement = {
        apiVersion: "announcement.lik.cc/v1alpha1",
        kind: "Announcement",
        metadata: { name: slugify(title.value) as any },
        announcementSpec: {
          title: title.value,
          permissions: permissions.value as any,
          content: html.value,
          position: position.value,
          autoClose: Number(autoClose.value || 0),
          closeOnClickOutside: Boolean(closeOnClickOutside.value),
          popupInterval: Number(popupInterval.value || 0),
          confettiEnable: Boolean(confettiEnable.value),
          enablePopup: Boolean(enablePopup.value),
          enablePinning: Boolean(enablePinning.value),
        } as any,
      };
      await announcementV1alpha1Api.createAnnouncement({ announcement: body });
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

// Load data on mount
onMounted(() => {
  loadAnnouncement();
});
</script>

<template>
  <VPageHeader :title="isEditMode ? '编辑公告' : '新建公告'">
    <template #icon>
      <IconAnnouncementMegaphone class="mr-2 self-center" />
    </template>
    <template #actions>
      <div class="header-actions">
        <VButton class="mr-2" @click="goBack">返回</VButton>
        <VButton :loading="isSubmitting" type="secondary" @click="handleSubmit">
          {{ isEditMode ? '更新' : '保存' }}
        </VButton>
      </div>
    </template>
  </VPageHeader>
  <div class="m-0 md:m-4">
    <VCard>
      <VLoading v-if="isLoading" />
      <div v-else class="editor-layout">
        <!-- 左侧：仅显示编辑器（标题使用 Halo 编辑器内置输入） -->
        <div class="main">
          <HaloEditor
            v-model:raw="html"
            v-model:content="html"
            v-model:title="title"
            :upload-image="handleUploadImage"
            class="editor-full h-full flex flex-1 flex-shrink flex-col"
          />
        </div>

        <!-- 右侧：设置栏（包含可见范围与显示位置等） -->
        <div class="aside">
          <div class="side-card">
            <div class="section-title">显示设置</div>
            <div class="form-row">
              <label class="field-label">启用弹窗</label>
              <select v-model="enablePopup" class="default-input">
                <option :value="true">是</option>
                <option :value="false">否</option>
              </select>
              <p class="field-help">⚠️ 重要提示：全局只能存在一个启用的弹窗！启用此弹窗将自动禁用其他所有弹窗。</p>
            </div>
            <div class="form-row">
              <label class="field-label">置顶</label>
              <select v-model="enablePinning" class="default-input">
                <option :value="true">是</option>
                <option :value="false">否</option>
              </select>
            </div>
            <div class="form-row">
              <label class="field-label">可见范围</label>
              <select v-model="permissions" class="default-input">
                <option value="loggedInUsers">登录用户</option>
                <option value="nonLoggedInUsers">未登录用户</option>
                <option value="everyone">所有人</option>
                <option value="notShown">不显示</option>
              </select>
            </div>
            <div class="form-row">
              <label class="field-label">弹窗位置</label>
              <select v-model="position" class="default-input">
                <option value="center">居中</option>
                <option value="left-bottom">左下角</option>
                <option value="right-bottom">右下角</option>
                <option value="left-top">左上角</option>
                <option value="right-top">右上角</option>
              </select>
            </div>
          </div>

          <div class="side-card">
            <div class="section-title">行为设置</div>
            <div class="form-row">
              <label class="field-label">自动关闭（秒）</label>
              <input v-model="autoClose" type="number" class="default-input" placeholder="0 表示不自动关闭" />
            </div>
            <div class="form-row">
              <label class="field-label">点击外部关闭</label>
              <select v-model="closeOnClickOutside" class="default-input">
                <option :value="true">是</option>
                <option :value="false">否</option>
              </select>
            </div>
            <div class="form-row">
              <label class="field-label">弹窗间隔（小时）</label>
              <input v-model="popupInterval" type="number" class="default-input" placeholder="0 表示不限制" />
            </div>
            <div class="form-row">
              <label class="field-label">礼花爆炸效果</label>
              <select v-model="confettiEnable" class="default-input">
                <option :value="true">开启</option>
                <option :value="false">关闭</option>
              </select>
            </div>
          </div>
        </div>
      </div>
    </VCard>
  </div>
</template>

<style scoped>
/***** Layout *****/
.editor-layout { display: grid; grid-template-columns: minmax(0,1fr) 260px; gap: 12px; }
@media (max-width: 1024px) { .editor-layout { grid-template-columns: 1fr; } .aside { position: static; top: auto; } }
.main { min-width: 0; }
.aside { position: sticky; top: 16px; display: flex; flex-direction: column; gap: 12px; }

/***** Cards & Sections *****/
.section { border: 1px solid #f2f3f5; border-radius: 8px; padding: 16px; background: #fff; margin-bottom: 12px; }
.section-title { font-size: 14px; font-weight: 600; color: #1d2129; }
.section-desc { font-size: 12px; color: #86909c; margin: 6px 0 12px; }
.side-card { border: 1px solid #f2f3f5; border-radius: 8px; padding: 16px; background: #fff; }
.form-row { margin-top: 12px; }
.field-label { display:block; font-size: 12px; color: #4e5969; margin-bottom: 6px; }
.field-help { font-size: 11px; color: #86909c; margin-top: 4px; font-style: italic; }

/***** Inputs *****/
.default-input { width: 100%; height: 36px; border: 1px solid #e5e7eb; border-radius: 6px; padding: 6px 10px; background: #fff; }
.default-input:focus { outline: none; border-color: #165dff; box-shadow: 0 0 0 3px rgba(22,93,255,0.12); }

/***** Editor *****/
.editor-full { width: 100%; }

/***** Header actions *****/
.header-actions { display:flex; align-items:center; }
</style>
