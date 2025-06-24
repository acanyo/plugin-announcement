(function() {
    // 定义插件对象
    window.LikccNotification = {
        instances: [],
        
        // 创建弹窗
        create: function(options) {
            // 合并默认配置
            const config = {
                title: '通知',
                content: '这是一条通知内容',
                position: 'center', // center, left-bottom, right-bottom, left-top, right-top
                mainButton: {
                    text: '确认',
                    color: '#165DFF',
                    callback: function() {}
                },
                secondaryButton: {
                    text: '关闭',
                    color: '#86909C',
                    callback: function() {}
                },
                urlPatterns: [],      // 支持多个路径模式的数组
                showOnLoad: true,     // 是否在页面加载时自动显示
                autoClose: 0,         // 自动关闭时间(秒)，0表示不自动关闭
                closeOnClickOutside: true
            };
            
            // 合并用户配置
            if (options) {
                Object.assign(config, options);
                
                if (options.mainButton) {
                    Object.assign(config.mainButton, options.mainButton);
                }
                
                if (options.secondaryButton) {
                    Object.assign(config.secondaryButton, options.secondaryButton);
                }
            }
            
            // 创建通知实例
            const instance = {
                config: config,
                element: null,
                isOpen: false,
                
                // 初始化方法
                init: function() {
                    // 检查URL匹配
                    if (!this.checkUrlPattern()) {
                        return;
                    }
                    
                    // 创建DOM元素
                    this.createElement();
                    
                    // 添加到文档
                    document.body.appendChild(this.element);
                    
                    // 如果配置为页面加载时显示
                    if (config.showOnLoad) {
                        setTimeout(() => {
                            this.open();
                        }, 100);
                    }
                    
                    // 添加到实例列表
                    LikccNotification.instances.push(this);
                },
                
                // 检查URL匹配
                checkUrlPattern: function() {
                    const currentPath = window.location.pathname;
                    
                    // 如果没有设置任何路径模式，默认显示
                    if (config.urlPatterns.length === 0) {
                        return true;
                    }
                    
                    // 检查是否匹配任何路径模式
                    return config.urlPatterns.some(pattern => {
                        const regex = new RegExp(`^${pattern.replace(/\*\*/g, '.*').replace(/\*/g, '[^/]*')}$`);
                        return regex.test(currentPath);
                    });
                },
                
                // 创建DOM元素
                createElement: function() {
                    // 创建容器元素
                    const container = document.createElement('div');
                    container.className = `likcc-notification-container likcc-notification-container-${config.position}`;
                    container.style.display = 'none';
                    
                    // 创建包裹元素
                    const wrapper = document.createElement('div');
                    wrapper.className = 'likcc-notification-wrapper';
                    
                    // 创建头部
                    const header = document.createElement('div');
                    header.className = 'likcc-notification-header';
                    
                    // 创建标题
                    const title = document.createElement('h3');
                    title.className = 'likcc-notification-title';
                    title.textContent = config.title;
                    
                    // 创建关闭按钮
                    const closeBtn = document.createElement('button');
                    closeBtn.className = 'likcc-notification-close';
                    closeBtn.innerHTML = '&times;';
                    closeBtn.addEventListener('click', () => this.close());
                    
                    // 添加头部元素
                    header.appendChild(title);
                    header.appendChild(closeBtn);
                    
                    // 创建内容
                    const content = document.createElement('div');
                    content.className = 'likcc-notification-content';
                    
                    // 如果内容是HTML字符串
                    if (typeof config.content === 'string' && 
                        (config.content.startsWith('<') || config.content.includes('</'))) {
                        content.innerHTML = config.content;
                    } else {
                        content.textContent = config.content;
                    }
                    
                    // 创建按钮区域
                    const buttons = document.createElement('div');
                    buttons.className = 'likcc-notification-buttons';
                    
                    // 创建主按钮
                    const mainButton = document.createElement('button');
                    mainButton.className = 'likcc-notification-main-button';
                    mainButton.textContent = config.mainButton.text;
                    mainButton.style.backgroundColor = config.mainButton.color;
                    mainButton.addEventListener('click', (e) => {
                        config.mainButton.callback(e);
                        this.close();
                    });
                    
                    // 创建次要按钮
                    const secondaryButton = document.createElement('button');
                    secondaryButton.className = 'likcc-notification-secondary-button';
                    secondaryButton.textContent = config.secondaryButton.text;
                    secondaryButton.style.backgroundColor = config.secondaryButton.color;
                    secondaryButton.addEventListener('click', (e) => {
                        config.secondaryButton.callback(e);
                        this.close();
                    });
                    
                    // 添加按钮
                    buttons.appendChild(mainButton);
                    buttons.appendChild(secondaryButton);
                    
                    // 添加所有元素到包装器
                    wrapper.appendChild(header);
                    wrapper.appendChild(content);
                    wrapper.appendChild(buttons);
                    
                    // 添加包装器到容器
                    container.appendChild(wrapper);
                    
                    // 添加点击外部关闭事件
                    if (config.closeOnClickOutside) {
                        container.addEventListener('click', (e) => {
                            if (e.target === container) {
                                this.close();
                            }
                        });
                    }
                    
                    this.element = container;
                },
                
                // 打开弹窗
                open: function() {
                    if (this.isOpen) return;
                    
                    this.element.style.display = 'flex';
                    // 添加动画类
                    setTimeout(() => {
                        this.element.classList.add('likcc-notification-container--active');
                    }, 10);
                    
                    this.isOpen = true;
                    
                    // 设置自动关闭
                    if (config.autoClose > 0) {
                        setTimeout(() => {
                            this.close();
                        }, config.autoClose * 1000);
                    }
                },
                
                // 关闭弹窗
                close: function() {
                    if (!this.isOpen) return;
                    
                    this.element.classList.remove('likcc-notification-container--active');
                    
                    // 动画结束后隐藏
                    setTimeout(() => {
                        this.element.style.display = 'none';
                    }, 300);
                    
                    this.isOpen = false;
                },
                
                // 更新内容
                updateContent: function(newContent) {
                    if (!this.element) return;
                    
                    const contentElement = this.element.querySelector('.likcc-notification-content');
                    if (contentElement) {
                        // 如果内容是HTML字符串
                        if (typeof newContent === 'string' && 
                            (newContent.startsWith('<') || newContent.includes('</'))) {
                            contentElement.innerHTML = newContent;
                        } else {
                            contentElement.textContent = newContent;
                        }
                    }
                },
                
                // 销毁实例
                destroy: function() {
                    if (this.element && this.element.parentNode) {
                        this.element.parentNode.removeChild(this.element);
                    }
                    
                    // 从实例列表中移除
                    const index = LikccNotification.instances.indexOf(this);
                    if (index !== -1) {
                        LikccNotification.instances.splice(index, 1);
                    }
                }
            };
            
            // 初始化实例
            instance.init();
            
            return instance;
        },
        
        // 获取所有实例
        getAll: function() {
            return this.instances;
        },
        
        // 关闭所有弹窗
        closeAll: function() {
            this.instances.forEach(instance => {
                instance.close();
            });
        },
        
        // 销毁所有实例
        destroyAll: function() {
            this.instances.forEach(instance => {
                instance.destroy();
            });
            this.instances = [];
        }
    };
})();
    