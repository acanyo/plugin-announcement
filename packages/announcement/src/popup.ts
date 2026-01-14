// 公告弹窗 - 引入即自动执行
import type { Announcement } from "./types/index";
import confetti from "canvas-confetti";

interface PopupConfig {
  position: string;
  autoClose: number;
  closeOnClickOutside: boolean;
  popupInterval: number;
  confetti: boolean;
}

const defaultConfig: PopupConfig = {
  position: "center",
  autoClose: 0,
  closeOnClickOutside: true,
  popupInterval: 0,
  confetti: false,
};

function getStorageKey(name: string) {
  return `likcc-popup-${name}-lastTime`;
}

function shouldShow(ann: Announcement, config: PopupConfig): boolean {
  if (!ann.announcementSpec?.enablePopup) return false;

  // URL 路径匹配
  const urlPatterns = ann.announcementSpec.urlPatterns;
  if (!matchUrlPattern(urlPatterns)) return false;

  const interval = ann.announcementSpec.popupInterval || config.popupInterval;
  if (interval > 0) {
    const key = getStorageKey(ann.metadata.name);
    const lastTime = localStorage.getItem(key);
    if (lastTime && Date.now() - Number(lastTime) < interval * 3600 * 1000) {
      return false;
    }
  }
  return true;
}

function matchUrlPattern(patterns?: string): boolean {
  const path = window.location.pathname;
  
  // 为空或只有/，表示仅首页
  if (!patterns || !patterns.trim()) {
    return path === "/" || path === "";
  }
  
  const lines = patterns.split("\n").map(l => l.trim()).filter(l => l);
  
  // 如果只配置了/，也是仅首页
  if (lines.length === 1 && lines[0] === "/") {
    return path === "/" || path === "";
  }
  
  // 匹配任意一个规则即可
  return lines.some(pattern => {
    // 转换通配符为正则
    // * 匹配任意字符（不包括/）
    // ** 匹配任意字符（包括/）
    let regex = pattern
      .replace(/[.+?^${}()|[\]\\]/g, "\\$&") // 转义特殊字符
      .replace(/\*\*/g, "{{DOUBLE}}") // 临时替换**
      .replace(/\*/g, "[^/]*") // * 匹配非/字符
      .replace(/{{DOUBLE}}/g, ".*"); // ** 匹配任意字符
    
    // 确保完整匹配路径
    regex = "^" + regex + "$";
    
    try {
      return new RegExp(regex).test(path);
    } catch {
      return false;
    }
  });
}

function recordShow(ann: Announcement, config: PopupConfig) {
  const interval = ann.announcementSpec?.popupInterval || config.popupInterval;
  if (interval > 0) {
    localStorage.setItem(getStorageKey(ann.metadata.name), Date.now().toString());
  }
}

function createPopupElement(ann: Announcement): HTMLElement {
  const spec = ann.announcementSpec;
  const primaryText = spec?.primaryButtonText || "确认";
  const primaryColor = spec?.primaryButtonColor || "#3b82f6";
  const primaryAction = spec?.primaryButtonAction || "closeNotice";
  const primaryUrl = spec?.primaryButtonUrl || "";
  const primaryCallback = spec?.primaryButtonCallback || "";
  const secondaryText = spec?.secondaryButtonText || "";
  const secondaryColor = spec?.secondaryButtonColor || "#6b7280";
  const secondaryAction = spec?.secondaryButtonAction || "closeNotice";
  const secondaryUrl = spec?.secondaryButtonUrl || "";
  const secondaryCallback = spec?.secondaryButtonCallback || "";
  const popupIconSvg = spec?.popupIcon || "";
  const popupIconBgColor = spec?.popupIconBgColor || "#60a5fa";

  const overlay = document.createElement("div");
  overlay.className = "likcc-popup__overlay";
  
  // 对 callback 进行 HTML 转义，避免 XSS
  const escapeHtml = (str: string) => str.replace(/&/g, "&amp;").replace(/"/g, "&quot;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
  
  let buttonsHtml = `<button class="likcc-popup__btn likcc-popup__btn--primary" style="background-color: ${primaryColor}" data-action="${primaryAction}" data-url="${primaryUrl}" data-callback="${escapeHtml(primaryCallback)}">${primaryText}</button>`;
  
  if (secondaryText) {
    buttonsHtml += `<button class="likcc-popup__btn likcc-popup__btn--secondary" style="color: ${secondaryColor}" data-action="${secondaryAction}" data-url="${secondaryUrl}" data-callback="${escapeHtml(secondaryCallback)}">${secondaryText}</button>`;
  }

  // 默认图标 SVG
  const defaultIconSvg = `<svg fill="currentColor" viewBox="0 0 20 20"><path clip-rule="evenodd" d="M18 3a1 1 0 00-1.447-.894L8.763 6H5a3 3 0 000 6h.28l1.771 5.316A1 1 0 008 18h1a1 1 0 001-1v-4.382l6.553 3.276A1 1 0 0018 15V3z" fill-rule="evenodd"></path></svg>`;
  const iconHtml = popupIconSvg || defaultIconSvg;

  overlay.innerHTML = `
    <div class="likcc-popup__wrapper">
      <button class="likcc-popup__close" aria-label="关闭">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 6L6 18M6 6l12 12"/>
        </svg>
      </button>
      <div class="likcc-popup__header">
        <span class="likcc-popup__icon" style="background-color: ${popupIconBgColor}">
          ${iconHtml}
        </span>
        <h2 class="likcc-popup__title">${spec?.title || ""}</h2>
      </div>
      <div class="likcc-popup__content">${spec?.content || ""}</div>
      <div class="likcc-popup__footer">
        ${buttonsHtml}
      </div>
    </div>
  `;
  return overlay;
}

function injectStyles() {
  if (document.getElementById("likcc-popup-styles")) return;

  const style = document.createElement("style");
  style.id = "likcc-popup-styles";
  style.textContent = `
    .likcc-popup__overlay {
      position: fixed;
      inset: 0;
      z-index: 99999;
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 20px;
      background: rgba(0, 0, 0, 0.4);
      animation: likcc-fade 0.2s ease;
    }
    @keyframes likcc-fade { from { opacity: 0; } }
    
    .likcc-popup__wrapper {
      position: relative;
      max-width: 360px;
      width: 100%;
      border: 1px solid rgba(219, 234, 254, 1);
      border-radius: 1rem;
      background-color: #fff;
      padding: 1.25rem;
      box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
      animation: likcc-slide 0.3s ease;
    }
    @keyframes likcc-slide {
      from { opacity: 0; transform: scale(0.95) translateY(-10px); }
    }
    
    .likcc-popup__header {
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }
    
    .likcc-popup__icon {
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 9999px;
      background-color: rgba(96, 165, 250, 1);
      padding: 0.5rem;
      color: #fff;
    }
    .likcc-popup__icon svg {
      height: 1rem;
      width: 1rem;
    }
    
    .likcc-popup__title {
      margin: 0;
      font-size: 1rem;
      font-weight: 600;
      color: rgba(55, 65, 81, 1);
    }
    
    .likcc-popup__close {
      position: absolute;
      top: 12px;
      right: 12px;
      width: 28px;
      height: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: transparent;
      border: none;
      color: #9ca3af;
      cursor: pointer;
      border-radius: 6px;
      transition: all 0.2s;
    }
    .likcc-popup__close:hover { background: #f3f4f6; color: #6b7280; }
    
    .likcc-popup__content {
      margin-top: 1rem;
      font-size: 0.9rem;
      color: rgba(107, 114, 128, 1);
      line-height: 1.6;
    }
    .likcc-popup__content p { margin: 0 0 0.75rem; }
    .likcc-popup__content p:last-child { margin: 0; }
    .likcc-popup__content img { 
      display: block;
      max-width: 100%; 
      height: auto; 
      border-radius: 8px; 
      margin: 12px 0;
    }
    .likcc-popup__content a { color: rgba(59, 130, 246, 1); }
    
    .likcc-popup__footer {
      margin-top: 1.25rem;
    }
    
    .likcc-popup__btn {
      display: block;
      width: 100%;
      border: none;
      border-radius: 0.5rem;
      padding: 0.75rem 1.25rem;
      text-align: center;
      font-size: 0.875rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.15s ease;
      text-decoration: none;
    }
    .likcc-popup__btn--primary {
      background-color: rgba(59, 130, 246, 1);
      color: #fff;
    }
    .likcc-popup__btn--primary:hover { 
      background-color: rgba(37, 99, 235, 1);
    }
    .likcc-popup__btn--secondary {
      margin-top: 0.5rem;
      background-color: rgba(249, 250, 251, 1);
      color: rgba(107, 114, 128, 1);
      border: none;
    }
    .likcc-popup__btn--secondary:hover {
      background-color: rgb(230, 231, 233);
    }
    
    @media (prefers-color-scheme: dark) {
      .likcc-popup__wrapper { 
        background: #1f2937; 
        border-color: #374151;
      }
      .likcc-popup__title { color: #f3f4f6; }
      .likcc-popup__content { color: #9ca3af; }
      .likcc-popup__close { color: #6b7280; }
      .likcc-popup__close:hover { background: #374151; color: #9ca3af; }
      .likcc-popup__btn--secondary { 
        background: #374151; 
        color: #9ca3af; 
      }
      .likcc-popup__btn--secondary:hover { background: #4b5563; }
    }
    
    @media (max-width: 480px) {
      .likcc-popup__overlay { padding: 16px; }
      .likcc-popup__wrapper { padding: 1rem; }
    }
  `;
  document.head.appendChild(style);
}

function closePopup(overlay: HTMLElement) {
  overlay.style.opacity = "0";
  overlay.style.transition = "opacity 0.3s ease";
  setTimeout(() => overlay.remove(), 300);
  document.body.style.overflow = "";
}

async function initPopup() {
  try {
    // 带上 credentials 以便后端判断登录状态
    const r = await fetch("/apis/public.announcement.lik.cc/v1alpha1/announcements?popup=true&size=1", {
      credentials: "same-origin"
    });
    if (!r.ok) return;

    const d = await r.json();
    const ann: Announcement = d.items?.[0];
    if (!ann) return;

    // 合并配置
    const config: PopupConfig = {
      ...defaultConfig,
      position: ann.announcementSpec?.position || defaultConfig.position,
      autoClose: ann.announcementSpec?.autoClose || defaultConfig.autoClose,
      closeOnClickOutside: ann.announcementSpec?.closeOnClickOutside ?? defaultConfig.closeOnClickOutside,
      popupInterval: ann.announcementSpec?.popupInterval || defaultConfig.popupInterval,
      confetti: ann.announcementSpec?.confettiEnable ?? defaultConfig.confetti,
    };

    if (!shouldShow(ann, config)) return;

    // 注入样式
    injectStyles();

    // 创建弹窗
    const overlay = createPopupElement(ann);
    document.body.appendChild(overlay);
    document.body.style.overflow = "hidden";

    // 记录显示
    recordShow(ann, config);

    // 关闭按钮
    overlay.querySelector(".likcc-popup__close")?.addEventListener("click", () => closePopup(overlay));
    
    // 主按钮和副按钮
    overlay.querySelectorAll(".likcc-popup__btn").forEach(btn => {
      btn.addEventListener("click", () => {
        const action = (btn as HTMLElement).dataset.action;
        const url = (btn as HTMLElement).dataset.url;
        const callback = (btn as HTMLElement).dataset.callback;
        
        if (action === "jump" && url) {
          window.open(url, "_blank");
          closePopup(overlay);
        } else if (action === "confirmJump" && url) {
          if (confirm("确定要跳转吗？")) {
            window.open(url, "_blank");
          }
          closePopup(overlay);
        } else if (action === "callback" && callback) {
          try {
            new Function(callback)();
          } catch (e) {
            console.error("执行回调失败:", e);
          }
          closePopup(overlay);
        } else {
          closePopup(overlay);
        }
      });
    });

    // 点击外部关闭
    if (config.closeOnClickOutside) {
      overlay.addEventListener("click", (e) => {
        if (e.target === overlay) closePopup(overlay);
      });
    }

    // ESC 关闭
    const escHandler = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        closePopup(overlay);
        document.removeEventListener("keydown", escHandler);
      }
    };
    document.addEventListener("keydown", escHandler);

    // 自动关闭
    if (config.autoClose > 0) {
      setTimeout(() => closePopup(overlay), config.autoClose * 1000);
    }

    // Confetti 效果
    if (config.confetti) {
      confetti({ particleCount: 100, spread: 70, origin: { y: 0.3 }, zIndex: 100000 });
    }
  } catch (e) {
    console.error("Failed to load popup announcement:", e);
  }
}

// DOM 加载完成后执行
if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initPopup);
} else {
  initPopup();
}
