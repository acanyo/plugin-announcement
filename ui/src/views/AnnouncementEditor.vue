<script setup lang="ts">
import { ref } from "vue";
import { VPageHeader, VCard, VButton, Toast } from "@halo-dev/components";
import { MdEditor } from "md-editor-v3";
import "md-editor-v3/lib/style.css";
import MarkdownIt from "markdown-it";
import { announcementV1alpha1Api } from "@/api";
import type { Announcement } from "@/api/generated";

const md = new MarkdownIt({ html: false, linkify: true, breaks: true });

const title = ref("");
const permissions = ref("everyone");
const markdown = ref(`# 公告标题\n\n这里是你的公告内容，支持 **粗体**、_斜体_、列表、代码块等。\n\n- 支持 Markdown 语法\n- 支持链接与图片\n- 提交后将转换为 HTML 用于弹窗显示`);
const position = ref("center");
const autoClose = ref(0);
const closeOnClickOutside = ref(true);
const popupInterval = ref(0);
const confettiEnable = ref(false);

// Editor themes
const theme = ref<'light' | 'dark'>("light");
const previewTheme = ref<'default' | 'github' | 'vuepress' | 'smart-blue' | 'mk-cute' | 'cyanosis'>("github");
const codeTheme = ref<'atom' | 'github' | 'kimbie' | 'monokai' | 'xcode' | 'dracula'>("github");

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

const handleSubmit = async () => {
  if (!title.value.trim()) return Toast.warning("请输入标题");
  if (!markdown.value.trim()) return Toast.warning("请输入内容（Markdown）");

  try {
    isSubmitting.value = true;
    const html = md.render(markdown.value);
    const body: Announcement = {
      apiVersion: "announcement.lik.cc/v1alpha1",
      kind: "Announcement",
      metadata: { name: slugify(title.value) as any },
      announcementSpec: {
        title: title.value,
        permissions: permissions.value as any,
        content: html,
        position: position.value,
        autoClose: Number(autoClose.value || 0),
        closeOnClickOutside: Boolean(closeOnClickOutside.value),
        popupInterval: Number(popupInterval.value || 0),
        confettiEnable: Boolean(confettiEnable.value),
      } as any,
    };
    await announcementV1alpha1Api.createAnnouncement({ announcement: body });
    Toast.success("创建成功");
    goBack();
  } catch (e) {
    console.error(e);
    Toast.error("创建失败");
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<template>
  <VPageHeader title="新建公告">
    <template #actions>
      <div class="header-actions">
        <VButton class="mr-2" @click="goBack">返回</VButton>
        <VButton :loading="isSubmitting" type="primary" @click="handleSubmit">保存</VButton>
      </div>
    </template>
  </VPageHeader>
  <div class="m-0 md:m-4">
    <VCard>
      <div class="editor-layout">
        <!-- 左侧：主内容 -->
        <div class="main">
          <div class="section">
            <div class="section-title">基础信息</div>
            <div class="form-row">
              <label class="field-label">标题</label>
              <input v-model="title" class="default-input" placeholder="请输入标题" />
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
          </div>

          <div class="section">
            <div class="section-title">公告内容</div>
            <p class="section-desc">支持 Markdown 语法，保存时会自动渲染为 HTML 用于弹窗显示。</p>
            <MdEditor
              v-model="markdown"
              class="md-box"
              language="zh-CN"
              :theme="theme"
              :preview-theme="previewTheme"
              :code-theme="codeTheme"
            />
          </div>
        </div>

        <!-- 右侧：设置栏 -->
        <div class="aside">
          <div class="side-card">
            <div class="section-title">编辑器主题</div>
            <div class="form-row">
              <label class="field-label">主题（编辑器）</label>
              <select v-model="theme" class="default-input">
                <option value="light">Light</option>
                <option value="dark">Dark</option>
              </select>
            </div>
            <div class="form-row">
              <label class="field-label">预览主题</label>
              <select v-model="previewTheme" class="default-input">
                <option value="default">Default</option>
                <option value="github">GitHub</option>
                <option value="vuepress">VuePress</option>
                <option value="smart-blue">Smart Blue</option>
                <option value="mk-cute">Mk Cute</option>
                <option value="cyanosis">Cyanosis</option>
              </select>
            </div>
            <div class="form-row">
              <label class="field-label">代码主题</label>
              <select v-model="codeTheme" class="default-input">
                <option value="github">GitHub</option>
                <option value="atom">Atom</option>
                <option value="kimbie">Kimbie</option>
                <option value="monokai">Monokai</option>
                <option value="xcode">Xcode</option>
                <option value="dracula">Dracula</option>
              </select>
            </div>
          </div>

          <div class="side-card">
            <div class="section-title">显示设置</div>
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
.editor-layout { display: grid; grid-template-columns: 1fr 320px; gap: 16px; }
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

/***** Inputs *****/
.default-input { width: 100%; height: 36px; border: 1px solid #e5e7eb; border-radius: 6px; padding: 6px 10px; background: #fff; }
.default-input:focus { outline: none; border-color: #165dff; box-shadow: 0 0 0 3px rgba(22,93,255,0.12); }

/***** Editor *****/
.md-box { height: 560px; margin-top: 8px; }
:deep(.md-editor) { border: 1px solid #e5e7eb; border-radius: 8px; }

/***** Header actions *****/
.header-actions { display:flex; align-items:center; }
</style> 