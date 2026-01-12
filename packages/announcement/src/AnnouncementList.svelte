<svelte:options
  customElement={{
    tag: "announcement-list",
    shadow: "none",
    props: {
      pageSize: { reflect: true, type: "Number", attribute: "page-size" },
      typesConfig: { reflect: true, type: "String", attribute: "types-config" },
    },
  }}
/>

<script lang="ts">
  import type { Announcement, AnnouncementType } from "./types";

  let { pageSize = 10, typesConfig = "" }: { pageSize?: number; typesConfig?: string } = $props();

  let loading = $state(true);
  let list = $state<Announcement[]>([]);
  let page = $state(1);
  let total = $state(0);
  let totalPages = $state(1);
  let show = $state(false);
  let current = $state<Announcement | null>(null);
  let types = $state<AnnouncementType[]>([]);
  let activeType = $state("");

  // 加载类型配置
  async function loadTypes() {
    // 优先使用传入的配置
    if (typesConfig) {
      try { types = JSON.parse(typesConfig); return; } catch {}
    }
    // 从公开 API 获取
    try {
      const r = await fetch("http://localhost:8090/apis/public.announcement.lik.cc/v1alpha1/types");
      if (r.ok) {
        types = await r.json();
      }
    } catch { types = []; }
  }

  function getTypeInfo(typeName?: string): AnnouncementType | undefined {
    if (!typeName) return undefined;
    return types.find(t => t.displayName === typeName);
  }

  async function load(p: number = 1) {
    loading = true;
    try {
      let url = `http://localhost:8090/apis/public.announcement.lik.cc/v1alpha1/announcements?page=${p}&size=${pageSize}`;
      if (activeType) url += `&type=${encodeURIComponent(activeType)}`;
      const r = await fetch(url);
      const d = await r.json();
      list = d.items || [];
      total = d.total || 0;
      totalPages = d.totalPages || 1;
      page = p;
    } catch { list = []; total = 0; }
    loading = false;
  }

  function switchType(t: string) {
    if (activeType !== t) { activeType = t; load(1); }
  }

  function fmt(s: string) {
    const d = new Date(s);
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
  }

  function open(a: Announcement) { current = a; show = true; document.body.style.overflow = 'hidden'; }
  function close() { show = false; document.body.style.overflow = ''; }

  $effect(() => {
    loadTypes().then(() => load());
    const fn = (e: KeyboardEvent) => e.key === 'Escape' && close();
    document.addEventListener('keydown', fn);
    return () => document.removeEventListener('keydown', fn);
  });
</script>

<div class="likcc-ann">
  <!-- Tab 筛选 -->
  <div class="likcc-ann__tabs">
    <button 
      class="likcc-ann__tab" 
      class:likcc-ann__tab--active={activeType === ""}
      onclick={() => switchType("")}
    >全部</button>
    {#each types as t}
      <button 
        class="likcc-ann__tab"
        class:likcc-ann__tab--active={activeType === t.displayName}
        onclick={() => switchType(t.displayName)}
      >{t.displayName}</button>
    {/each}
  </div>

  <!-- 统计 -->
  <div class="likcc-ann__stats">为你展示 {total} 条公告</div>

  <!-- 列表 -->
  <div class="likcc-ann__list">
    {#if loading}
      {#each [1,2,3,4] as _}
        <div class="likcc-ann__row likcc-ann__row--loading">
          <span class="likcc-ann__skeleton" style="width: 60%"></span>
          <span class="likcc-ann__skeleton" style="width: 140px"></span>
        </div>
      {/each}
    {:else if !list.length}
      <div class="likcc-ann__empty">暂无公告</div>
    {:else}
      {#each list as a}
        {@const typeInfo = getTypeInfo(a.announcementSpec?.type)}
        <button class="likcc-ann__row" onclick={() => open(a)}>
          <span class="likcc-ann__content">
            {#if typeInfo}
              <span class="likcc-ann__type" style="color: {typeInfo.color}">【{typeInfo.displayName}】</span>
            {/if}
            <span class="likcc-ann__title">{a.announcementSpec?.title}</span>
          </span>
          <span class="likcc-ann__date">{fmt(a.metadata?.creationTimestamp)}</span>
        </button>
      {/each}
    {/if}
  </div>

  <!-- 分页 -->
  {#if totalPages > 1}
    <div class="likcc-ann__pager">
      <button class="likcc-ann__pager-btn" disabled={page <= 1} onclick={() => load(page - 1)}>上一页</button>
      <span class="likcc-ann__pager-info">{page} / {totalPages}</span>
      <button class="likcc-ann__pager-btn" disabled={page >= totalPages} onclick={() => load(page + 1)}>下一页</button>
    </div>
  {/if}
</div>

<!-- 详情弹窗 -->
{#if show && current}
  <div class="likcc-ann-modal__overlay" onclick={close} role="dialog" aria-modal="true">
    <div class="likcc-ann-modal" onclick={e => e.stopPropagation()}>
      <div class="likcc-ann-modal__header">
        <h2 class="likcc-ann-modal__title">{current.announcementSpec?.title}</h2>
        <span class="likcc-ann-modal__date">{fmt(current.metadata?.creationTimestamp)}</span>
        <button class="likcc-ann-modal__close" onclick={close} aria-label="关闭">×</button>
      </div>
      <div class="likcc-ann-modal__body">{@html current.announcementSpec?.content}</div>
    </div>
  </div>
{/if}

<style>
  .likcc-ann {
    --likcc-c-text: #1d2129;
    --likcc-c-text-2: #4e5969;
    --likcc-c-text-3: #86909c;
    --likcc-c-primary: #165dff;
    --likcc-c-border: #e5e6eb;
    --likcc-c-bg: #fff;
    --likcc-c-bg-hover: #f7f8fa;
    font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
    font-size: 14px;
    color: var(--likcc-c-text);
    line-height: 1.5;
  }

  /* Tabs */
  .likcc-ann__tabs {
    display: flex;
    gap: 32px;
    border-bottom: 1px solid var(--likcc-c-border);
    margin-bottom: 16px;
  }

  .likcc-ann__tab {
    position: relative;
    padding: 12px 0;
    background: none;
    border: none;
    font-size: 14px;
    color: var(--likcc-c-text-2);
    cursor: pointer;
    transition: color 0.2s;
  }

  .likcc-ann__tab:hover { color: var(--likcc-c-text); }

  .likcc-ann__tab--active {
    color: var(--likcc-c-primary);
    font-weight: 500;
  }

  .likcc-ann__tab--active::after {
    content: "";
    position: absolute;
    left: 0;
    right: 0;
    bottom: -1px;
    height: 2px;
    background: var(--likcc-c-primary);
  }

  /* Stats */
  .likcc-ann__stats {
    padding: 12px 0;
    font-size: 13px;
    color: var(--likcc-c-text-3);
  }

  /* List */
  .likcc-ann__list {
    border-top: 1px solid var(--likcc-c-border);
  }

  .likcc-ann__row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    padding: 16px 0;
    background: none;
    border: none;
    border-bottom: 1px solid var(--likcc-c-border);
    text-align: left;
    cursor: pointer;
    transition: background 0.15s;
    font: inherit;
    color: inherit;
  }

  .likcc-ann__row:hover { background: var(--likcc-c-bg-hover); }
  .likcc-ann__row--loading { pointer-events: none; }

  .likcc-ann__content {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
  }

  .likcc-ann__type {
    flex-shrink: 0;
    font-weight: 500;
  }

  .likcc-ann__title {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .likcc-ann__row:hover .likcc-ann__title { color: var(--likcc-c-primary); }

  .likcc-ann__date {
    flex-shrink: 0;
    margin-left: 24px;
    font-size: 13px;
    color: var(--likcc-c-text-3);
    font-variant-numeric: tabular-nums;
  }

  /* Empty */
  .likcc-ann__empty {
    padding: 48px 0;
    text-align: center;
    color: var(--likcc-c-text-3);
  }

  /* Skeleton */
  .likcc-ann__skeleton {
    display: block;
    height: 16px;
    background: linear-gradient(90deg, #f2f3f5 25%, #e5e6eb 50%, #f2f3f5 75%);
    background-size: 200% 100%;
    border-radius: 4px;
    animation: likcc-shimmer 1.5s infinite;
  }

  @keyframes likcc-shimmer {
    0% { background-position: 200% 0; }
    100% { background-position: -200% 0; }
  }

  /* Pager */
  .likcc-ann__pager {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16px;
    padding: 24px 0;
  }

  .likcc-ann__pager-btn {
    padding: 6px 16px;
    background: var(--likcc-c-bg);
    border: 1px solid var(--likcc-c-border);
    border-radius: 4px;
    font-size: 13px;
    color: var(--likcc-c-text-2);
    cursor: pointer;
    transition: all 0.15s;
  }

  .likcc-ann__pager-btn:hover:not(:disabled) {
    color: var(--likcc-c-primary);
    border-color: var(--likcc-c-primary);
  }

  .likcc-ann__pager-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .likcc-ann__pager-info {
    font-size: 13px;
    color: var(--likcc-c-text-3);
  }

  /* Modal */
  .likcc-ann-modal__overlay {
    position: fixed;
    inset: 0;
    z-index: 99999;
    background: rgba(0, 0, 0, 0.45);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24px;
  }

  .likcc-ann-modal {
    width: 100%;
    max-width: 640px;
    max-height: 80vh;
    background: #fff;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    box-shadow: 0 8px 40px rgba(0, 0, 0, 0.12);
  }

  .likcc-ann-modal__header {
    position: relative;
    padding: 20px 48px 16px 24px;
    border-bottom: 1px solid var(--likcc-c-border);
  }

  .likcc-ann-modal__title {
    font-size: 18px;
    font-weight: 600;
    margin: 0 0 8px;
    line-height: 1.4;
  }

  .likcc-ann-modal__date {
    font-size: 13px;
    color: var(--likcc-c-text-3);
  }

  .likcc-ann-modal__close {
    position: absolute;
    top: 16px;
    right: 16px;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: none;
    border: none;
    font-size: 24px;
    color: var(--likcc-c-text-3);
    cursor: pointer;
    border-radius: 4px;
    transition: all 0.15s;
  }

  .likcc-ann-modal__close:hover {
    background: var(--likcc-c-bg-hover);
    color: var(--likcc-c-text);
  }

  .likcc-ann-modal__body {
    flex: 1;
    overflow-y: auto;
    padding: 24px;
    font-size: 15px;
    line-height: 1.8;
  }

  .likcc-ann-modal__body :global(p) { margin: 0 0 1em; }
  .likcc-ann-modal__body :global(img) { max-width: 100%; height: auto; border-radius: 4px; }
  .likcc-ann-modal__body :global(a) { color: var(--likcc-c-primary); }

  @media (max-width: 640px) {
    .likcc-ann__tabs { gap: 20px; overflow-x: auto; }
    .likcc-ann__date { display: none; }
    .likcc-ann-modal { max-height: 90vh; border-radius: 12px 12px 0 0; }
    .likcc-ann-modal__overlay { padding: 0; align-items: flex-end; }
  }
</style>
