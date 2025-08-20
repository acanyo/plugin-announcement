/**
 * 公告列表页面 JavaScript 功能文件
 * 使用 likccAnnouncementList 前缀避免全局变量冲突
 * 提供交互功能、分享功能、搜索增强等功能
 */

(function() {
    'use strict';

    // 全局变量 - 使用前缀避免冲突
    let likccAnnouncementListCurrentId = null;
    let likccAnnouncementListSearchTimeout = null;
    let likccAnnouncementListIsModalOpen = false;

    // DOM 加载完成后初始化
    document.addEventListener('DOMContentLoaded', function() {
        likccAnnouncementListInitialize();
    });

    /**
     * 初始化公告列表功能
     */
    function likccAnnouncementListInitialize() {
        likccAnnouncementListInitializeList();
        likccAnnouncementListInitializeSearch();
        likccAnnouncementListInitializeScrollEffects();
        likccAnnouncementListInitializeKeyboardNavigation();
    }

    /**
     * 初始化公告列表功能
     */
    function likccAnnouncementListInitializeList() {
        // 为所有公告项添加点击事件
        const announcementItems = document.querySelectorAll('.likcc-announcement-list-item');
        
        announcementItems.forEach(item => {
            // 添加点击波纹效果
            item.addEventListener('click', function(e) {
                // 如果点击的是按钮或链接，不触发波纹效果
                if (e.target.closest('.likcc-announcement-list-action-button') || e.target.closest('a')) {
                    return;
                }
                
                likccAnnouncementListCreateRippleEffect(e, this);
            });

            // 添加悬停动画
            item.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-8px)';
            });

            item.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0)';
            });
        });

        // 初始化置顶公告的特殊样式
        likccAnnouncementListInitializePinned();
    }

    /**
     * 初始化搜索功能增强
     */
    function likccAnnouncementListInitializeSearch() {
        const searchInput = document.querySelector('.likcc-announcement-list-search-input');
        if (!searchInput) return;

        // 实时搜索建议
        searchInput.addEventListener('input', function() {
            clearTimeout(likccAnnouncementListSearchTimeout);
            const query = this.value.trim();
            
            if (query.length > 0) {
                likccAnnouncementListSearchTimeout = setTimeout(() => {
                    likccAnnouncementListPerformLiveSearch(query);
                }, 300);
            } else {
                likccAnnouncementListHideSearchSuggestions();
            }
        });

        // 搜索框焦点事件
        searchInput.addEventListener('focus', function() {
            this.parentElement.classList.add('likcc-announcement-list-focused');
        });

        searchInput.addEventListener('blur', function() {
            this.parentElement.classList.remove('likcc-announcement-list-focused');
        });

        // 回车键搜索
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                this.closest('form').submit();
            }
        });
    }

    /**
     * 初始化滚动效果
     */
    function likccAnnouncementListInitializeScrollEffects() {
        // 滚动时添加视差效果
        window.addEventListener('scroll', function() {
            const scrolled = window.pageYOffset;
            const parallaxElements = document.querySelectorAll('.likcc-announcement-list-header, .likcc-announcement-list-search-section');
            
            parallaxElements.forEach(element => {
                const speed = element.classList.contains('likcc-announcement-list-header') ? 0.5 : 0.3;
                element.style.transform = `translateY(${scrolled * speed}px)`;
            });

            // 滚动到顶部按钮显示/隐藏
            const scrollTopButton = document.querySelector('.likcc-announcement-list-scroll-top-button');
            if (scrolled > 300) {
                if (!scrollTopButton) {
                    likccAnnouncementListCreateScrollTopButton();
                }
            } else if (scrollTopButton) {
                scrollTopButton.remove();
            }
        });

        // 平滑滚动到顶部
        document.addEventListener('click', function(e) {
            if (e.target.closest('.likcc-announcement-list-scroll-top-button')) {
                e.preventDefault();
                likccAnnouncementListSmoothScrollToTop();
            }
        });
    }

    /**
     * 初始化键盘导航
     */
    function likccAnnouncementListInitializeKeyboardNavigation() {
        document.addEventListener('keydown', function(e) {
            // ESC 键关闭弹窗
            if (e.key === 'Escape' && likccAnnouncementListIsModalOpen) {
                likccAnnouncementListCloseShareModal();
            }

            // 搜索框快捷键 (Ctrl/Cmd + K)
            if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
                e.preventDefault();
                const searchInput = document.querySelector('.likcc-announcement-list-search-input');
                if (searchInput) {
                    searchInput.focus();
                    searchInput.select();
                }
            }
        });
    }

    /**
     * 初始化置顶公告
     */
    function likccAnnouncementListInitializePinned() {
        const pinnedItems = document.querySelectorAll('.likcc-announcement-list-item.likcc-announcement-list-pinned');
        
        pinnedItems.forEach(item => {
            // 添加置顶动画
            item.style.animation = 'likcc-announcement-list-pinned-glow 2s ease-in-out infinite alternate';
            
            // 添加置顶提示
            const pinnedBadge = item.querySelector('.likcc-announcement-list-pinned-badge');
            if (pinnedBadge) {
                pinnedBadge.addEventListener('click', function() {
                    likccAnnouncementListShowToast('这是置顶公告，会优先显示', 'info');
                });
            }
        });
    }

    /**
     * 创建波纹效果
     */
    function likccAnnouncementListCreateRippleEffect(event, element) {
        const ripple = document.createElement('span');
        const rect = element.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        const x = event.clientX - rect.left - size / 2;
        const y = event.clientY - rect.top - size / 2;

        ripple.style.cssText = `
            position: absolute;
            width: ${size}px;
            height: ${size}px;
            left: ${x}px;
            top: ${y}px;
            background: rgba(102, 126, 234, 0.3);
            border-radius: 50%;
            transform: scale(0);
            animation: likcc-announcement-list-ripple 0.6s linear;
            pointer-events: none;
        `;

        element.style.position = 'relative';
        element.appendChild(ripple);

        setTimeout(() => {
            ripple.remove();
        }, 600);
    }

    /**
     * 执行实时搜索
     */
    function likccAnnouncementListPerformLiveSearch(query) {
        // 这里可以实现 AJAX 搜索建议
        // 目前只是示例实现
        console.log('执行实时搜索:', query);
        
        // 高亮搜索结果
        likccAnnouncementListHighlightSearchResults(query);
    }

    /**
     * 高亮搜索结果
     */
    function likccAnnouncementListHighlightSearchResults(query) {
        const announcementItems = document.querySelectorAll('.likcc-announcement-list-item');
        
        announcementItems.forEach(item => {
            const title = item.querySelector('.likcc-announcement-list-item-title');
            const content = item.querySelector('.likcc-announcement-list-item-content');
            
            if (title && content) {
                const titleText = title.textContent;
                const contentText = content.textContent;
                
                if (titleText.toLowerCase().includes(query.toLowerCase()) ||
                    contentText.toLowerCase().includes(query.toLowerCase())) {
                    item.style.borderColor = '#667eea';
                    item.style.boxShadow = '0 8px 25px rgba(102, 126, 234, 0.2)';
                    
                    // 3秒后恢复原样式
                    setTimeout(() => {
                        item.style.borderColor = '';
                        item.style.boxShadow = '';
                    }, 3000);
                }
            }
        });
    }

    /**
     * 隐藏搜索建议
     */
    function likccAnnouncementListHideSearchSuggestions() {
        // 实现隐藏搜索建议的逻辑
    }

    /**
     * 创建回到顶部按钮
     */
    function likccAnnouncementListCreateScrollTopButton() {
        const button = document.createElement('button');
        button.className = 'likcc-announcement-list-scroll-top-button';
        button.innerHTML = '<i class="ri-arrow-up-line"></i>';
        button.title = '回到顶部';
        
        button.style.cssText = `
            position: fixed;
            bottom: 30px;
            right: 30px;
            width: 50px;
            height: 50px;
            border-radius: 50%;
            background: #667eea;
            color: white;
            border: none;
            cursor: pointer;
            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
            transition: all 0.3s ease;
            z-index: 1000;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.2rem;
        `;

        button.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-3px)';
            this.style.boxShadow = '0 6px 20px rgba(102, 126, 234, 0.6)';
        });

        button.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 4px 15px rgba(102, 126, 234, 0.4)';
        });

        document.body.appendChild(button);
    }

    /**
     * 平滑滚动到顶部
     */
    function likccAnnouncementListSmoothScrollToTop() {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    }

    /**
     * 分享公告功能 - 列表页
     */
    window.likccAnnouncementListShare = function(announcementId) {
        likccAnnouncementListCurrentId = announcementId;
        likccAnnouncementListOpenShareModal();
    };

    /**
     * 打开分享弹窗
     */
    function likccAnnouncementListOpenShareModal() {
        const modal = document.getElementById('likccAnnouncementListShareModal');
        if (modal) {
            modal.style.display = 'block';
            likccAnnouncementListIsModalOpen = true;
            
            // 添加弹窗打开动画
            modal.style.opacity = '0';
            setTimeout(() => {
                modal.style.opacity = '1';
            }, 10);
            
            // 阻止背景滚动
            document.body.style.overflow = 'hidden';
        }
    }

    /**
     * 关闭分享弹窗
     */
    window.likccAnnouncementListCloseShareModal = function() {
        const modal = document.getElementById('likccAnnouncementListShareModal');
        if (modal) {
            modal.style.opacity = '0';
            setTimeout(() => {
                modal.style.display = 'none';
                likccAnnouncementListIsModalOpen = false;
                document.body.style.overflow = '';
            }, 300);
        }
    };

    /**
     * 分享到微信
     */
    window.likccAnnouncementListShareToWeChat = function() {
        likccAnnouncementListShowToast('请使用微信扫描二维码分享', 'info');
        // 这里可以实现微信分享逻辑
        likccAnnouncementListCloseShareModal();
    };

    /**
     * 分享到微博
     */
    window.likccAnnouncementListShareToWeibo = function() {
        const url = encodeURIComponent(window.location.href);
        const title = encodeURIComponent(document.title);
        const weiboUrl = `https://service.weibo.com/share/share.php?url=${url}&title=${title}`;
        window.open(weiboUrl, '_blank');
        likccAnnouncementListCloseShareModal();
    };

    /**
     * 复制链接
     */
    window.likccAnnouncementListCopyLink = function() {
        const url = window.location.href;
        
        if (navigator.clipboard) {
            navigator.clipboard.writeText(url).then(() => {
                likccAnnouncementListShowToast('链接已复制到剪贴板', 'success');
            }).catch(() => {
                likccAnnouncementListFallbackCopyTextToClipboard(url);
            });
        } else {
            likccAnnouncementListFallbackCopyTextToClipboard(url);
        }
        
        likccAnnouncementListCloseShareModal();
    };

    /**
     * 复制文本到剪贴板的备用方案
     */
    function likccAnnouncementListFallbackCopyTextToClipboard(text) {
        const textArea = document.createElement('textarea');
        textArea.value = text;
        textArea.style.position = 'fixed';
        textArea.style.left = '-999999px';
        textArea.style.top = '-999999px';
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();
        
        try {
            document.execCommand('copy');
            likccAnnouncementListShowToast('链接已复制到剪贴板', 'success');
        } catch (err) {
            likccAnnouncementListShowToast('复制失败，请手动复制', 'error');
        }
        
        document.body.removeChild(textArea);
    }

    /**
     * 显示提示消息
     */
    function likccAnnouncementListShowToast(message, type = 'info') {
        // 移除现有的提示
        const existingToast = document.querySelector('.likcc-announcement-list-toast-message');
        if (existingToast) {
            existingToast.remove();
        }

        const toast = document.createElement('div');
        toast.className = `likcc-announcement-list-toast-message likcc-announcement-list-toast-${type}`;
        toast.textContent = message;
        
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 20px;
            border-radius: 8px;
            color: white;
            font-weight: 500;
            z-index: 10001;
            animation: likcc-announcement-list-toast-slide-in 0.3s ease;
            max-width: 300px;
            word-wrap: break-word;
        `;

        // 根据类型设置背景色
        const colors = {
            success: '#4caf50',
            error: '#f44336',
            warning: '#ff9800',
            info: '#2196f3'
        };
        
        toast.style.background = colors[type] || colors.info;

        document.body.appendChild(toast);

        // 3秒后自动移除
        setTimeout(() => {
            toast.style.animation = 'likcc-announcement-list-toast-slide-out 0.3s ease';
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.remove();
                }
            }, 300);
        }, 3000);
    }

    /**
     * 点击弹窗外部关闭弹窗
     */
    document.addEventListener('click', function(e) {
        const modal = document.getElementById('likccAnnouncementListShareModal');
        if (modal && e.target === modal) {
            likccAnnouncementListCloseShareModal();
        }
    });

    // 添加必要的 CSS 动画
    likccAnnouncementListAddAnimationStyles();

    /**
     * 添加动画样式
     */
    function likccAnnouncementListAddAnimationStyles() {
        const style = document.createElement('style');
        style.textContent = `
            @keyframes likcc-announcement-list-ripple {
                to {
                    transform: scale(4);
                    opacity: 0;
                }
            }
            
            @keyframes likcc-announcement-list-pinned-glow {
                from {
                    box-shadow: 0 4px 20px rgba(255, 215, 0, 0.3);
                }
                to {
                    box-shadow: 0 8px 30px rgba(255, 215, 0, 0.6);
                }
            }
            
            @keyframes likcc-announcement-list-toast-slide-in {
                from {
                    transform: translateX(100%);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }
            
            @keyframes likcc-announcement-list-toast-slide-out {
                from {
                    transform: translateX(0);
                    opacity: 1;
                }
                to {
                    transform: translateX(100%);
                    opacity: 0;
                }
            }
            
            .likcc-announcement-list-search-input-group.likcc-announcement-list-focused {
                box-shadow: 0 4px 25px rgba(102, 126, 234, 0.4);
                transform: translateY(-3px);
            }
        `;
        document.head.appendChild(style);
    }

    // 导出全局函数
    window.LikccAnnouncementList = {
        share: likccAnnouncementListShare,
        closeShareModal: likccAnnouncementListCloseShareModal,
        shareToWeChat: likccAnnouncementListShareToWeChat,
        shareToWeibo: likccAnnouncementListShareToWeibo,
        copyLink: likccAnnouncementListCopyLink
    };

})(); 

// UI 交互增强
window.LikccAnnouncementListUI = (function(prev){
  const api = prev || {};

  function expand(card){
    const excerpt = card.querySelector('.likcc-announcement-card__excerpt');
    const fade = card.querySelector('.likcc-announcement-card__fade');
    if (excerpt){ excerpt.style.maxHeight = 'unset'; }
    if (fade){ fade.style.display = 'none'; }
    card.classList.add('likcc-announcement-card--expanded');
  }

  function collapse(card){
    const excerpt = card.querySelector('.likcc-announcement-card__excerpt');
    const fade = card.querySelector('.likcc-announcement-card__fade');
    if (excerpt){ excerpt.style.maxHeight = '120px'; }
    if (fade){ fade.style.display = ''; }
    card.classList.remove('likcc-announcement-card--expanded');
  }

  function toggle(btn){
    const card = btn.closest('.likcc-announcement-card');
    if (!card) return;
    const expanded = card.classList.contains('likcc-announcement-card--expanded');
    if (expanded){
      collapse(card);
      btn.textContent = btn.getAttribute('data-collapsed-text') || '展开';
    } else {
      expand(card);
      btn.textContent = btn.getAttribute('data-expanded-text') || '收起';
    }
  }

  // 初始动画与内容裁剪
  document.addEventListener('DOMContentLoaded', function(){
    const cards = document.querySelectorAll('.likcc-announcement-card');
    cards.forEach((c, idx)=>{
      c.style.animationDelay = (idx * 40)+'ms';
      // 内容首段裁剪：若HTML过长，保持前几段，剩余靠CSS渐隐
      const excerpt = c.querySelector('.likcc-announcement-card__excerpt');
      if (excerpt){
        // 可按需添加更智能的裁剪，这里保留为CSS max-height 方案
      }
    });
  });

  // 文章式列表：从富文本提取第一张图片与摘要
  document.addEventListener('DOMContentLoaded', function(){
    const items = document.querySelectorAll('.likcc-announcement-article');
    items.forEach(item => {
      try {
        const source = item.querySelector('.likcc-announcement-article__source');
        const thumbImg = item.querySelector('.likcc-announcement-article__thumb img');
        const excerptEl = item.querySelector('.likcc-announcement-article__excerpt');
        if (!source || (!thumbImg && !excerptEl)) return;

        const tmp = document.createElement('div');
        tmp.innerHTML = source.innerHTML;
        // 抓第一张图片
        const firstImg = tmp.querySelector('img');
        if (firstImg && thumbImg){
          const src = firstImg.getAttribute('src') || firstImg.getAttribute('data-src');
          if (src) thumbImg.src = src;
        } else if (thumbImg){
          // 没图用占位
          thumbImg.style.display = 'none';
        }
        // 提取纯文本摘要
        const text = tmp.textContent || '';
        const clean = text.replace(/\s+/g, ' ').trim();
        if (excerptEl){
          excerptEl.textContent = clean.length > 120 ? clean.slice(0, 118) + '…' : clean;
        }
      } catch(e){ /* 忽略单条渲染错误 */ }
    });
  });

  api.toggle = toggle;
  return api;
})(window.LikccAnnouncementListUI); 