# Halo 公告通知插件

一个功能强大的 Halo 博客系统公告通知插件，支持自定义弹窗内容、按钮行为、动画效果和智能弹出控制。

## ✨ 功能特性

- 🎯 **智能路径匹配**：支持精确路径和通配符匹配，只在指定页面显示通知
- 🎨 **丰富自定义**：标题、内容、位置、颜色、按钮文字完全可配置
- 🚀 **多种交互**：跳转链接、确认跳转、关闭通知、自定义回调函数
- ⏰ **智能控制**：自动关闭、点击外部关闭、弹出间隔控制
- 🎆 **动画效果**：可选的礼花爆炸动画，增强用户体验
- 📱 **响应式设计**：适配各种屏幕尺寸和设备
- 🔄 **PJAX 兼容**：完美支持 Halo 主题的 PJAX 无刷新跳转

## 🌐 演示与交流

- **演示站点**：[https://www.lik.cc/](https://www.lik.cc/)
- **QQ 交流群**：[![QQ群](https://www.lik.cc/upload/iShot_2025-03-03_16.03.00.png)](https://www.lik.cc/upload/iShot_2025-03-03_16.03.00.png)

## 🚀 快速开始

### 环境要求

- Halo 2.x
- Java 21+
- Node.js 18+ (开发环境)

### 安装

1. 下载插件 jar 文件
2. 在 Halo 后台 → 插件 → 安装插件
3. 上传并启用插件
4. 在插件设置中配置你的通知内容

## 📖 使用指南

### 基本配置

1. **开启公告**：勾选"开启公告"开关
2. **设置内容**：
   - 通知标题：弹窗的标题文字
   - 通知内容：支持 HTML 格式的弹窗内容
   - 弹窗位置：居中、左下角、右下角、左上角、右上角

### 路径匹配规则

支持多种路径匹配模式：

```
/                    # 只匹配首页
/archives/**         # 匹配所有文章页面
/about               # 匹配关于页面
/categories/*        # 匹配分类页面
```

### 按钮配置

#### 主按钮/副按钮事件类型

- **跳转**：直接跳转到指定 URL
- **确认跳转**：显示确认弹窗后跳转
- **关闭公告**：关闭当前弹窗
- **自定义内容**：执行自定义 JavaScript 代码

#### 自定义回调函数

在"自定义内容"模式下，你可以：

1. **填写函数名**：在"回调事件"字段填写全局函数名
2. **编写自定义代码**：在代码编辑器中编写完整的 JavaScript 函数

示例：
```javascript
function myCustomFunction(event, instance) {
    // 获取弹窗内容
    const popupContent = instance.config.mainButton.popupContent;
    alert('自定义弹窗内容：' + popupContent);
    
    // 执行其他逻辑
    console.log('用户点击了自定义按钮');
}
```

### 高级设置

- **自动关闭时间**：设置弹窗自动关闭的秒数，0 表示不自动关闭
- **点击外部关闭**：是否允许点击弹窗外部区域关闭
- **弹窗弹出间隔**：设置弹窗的最小弹出间隔（小时），0 表示不限制
- **礼花爆炸效果**：是否在弹窗弹出时显示动画效果

## 🎯 最佳实践

### 1. 公告内容设计

#### 标题设计
- **简洁有力**：控制在 15 字以内，突出核心信息
- **吸引注意**：使用"重要"、"新功能"、"更新"等关键词
- **避免标题党**：内容要与标题匹配

#### 内容设计
- **结构化布局**：使用 `<h4>`、`<p>`、`<ul>` 等标签组织内容
- **重点突出**：使用 `<strong>`、`<em>` 强调关键信息
- **行动导向**：明确告诉用户下一步该做什么

示例：
```html
<h4>🎉 新功能上线</h4>
<p>我们新增了 <strong>评论系统</strong>，现在可以：</p>
<ul>
    <li>实时回复评论</li>
    <li>表情包支持</li>
    <li>评论通知</li>
</ul>
<p>立即体验新功能！</p>
```

### 2. 公告时机控制

#### 重要公告
- **功能更新**：新功能发布、系统升级
- **活动通知**：限时活动、重要活动
- **维护通知**：系统维护、服务暂停

#### 避免过度使用
- **不要频繁弹窗**：设置合理的弹出间隔（建议 4-24 小时）
- **避免重复内容**：相同内容不要重复弹出
- **考虑用户感受**：重要程度不够的内容用其他方式展示

### 3. 路径匹配策略

#### 精准投放
```bash
# 首页公告
/                    # 重要公告、活动通知

# 文章页公告
/archives/**         # 功能更新、内容推荐

# 特定页面公告
/about               # 关于页面特殊公告
/contact             # 联系方式变更

# 避免配置
/*                   # 过于宽泛，影响性能
```

#### 场景化配置
- **新用户引导**：只在首页显示
- **功能推广**：在相关页面显示
- **活动宣传**：在文章页显示

### 4. 按钮交互设计

#### 主按钮（重要操作）
- **跳转**：引导用户访问相关页面
- **确认跳转**：重要链接，确保用户注意
- **自定义**：复杂交互逻辑

#### 副按钮（次要操作）
- **关闭**：简单的关闭操作
- **了解更多**：引导用户获取更多信息

#### 按钮文案建议
- 主按钮：`立即体验`、`查看详情`、`参与活动`
- 副按钮：`稍后再说`、`我知道了`、`关闭`

### 5. 用户体验优化

#### 弹窗位置选择
- **重要公告**：居中显示，强制用户注意
- **提示信息**：右下角显示，不干扰阅读
- **活动通知**：左下角显示，易于发现

#### 自动关闭设置
- **重要公告**：不自动关闭，用户主动关闭
- **提示信息**：3-5 秒自动关闭
- **活动通知**：10-15 秒自动关闭

### 6. 自定义回调函数示例

#### 示例 1：数据统计
```javascript
function trackAnnouncementClick(event, instance) {
    // 统计公告点击
    gtag('event', 'announcement_click', {
        'event_category': 'engagement',
        'event_label': instance.config.title,
        'announcement_type': 'feature_update'
    });
    
    // 记录用户行为
    console.log('用户点击了公告：', instance.config.title);
}
```

#### 示例 2：用户反馈收集
```javascript
function collectFeedback(event, instance) {
    const feedback = prompt('请告诉我们您的想法：');
    if (feedback) {
        // 发送反馈到服务器
        fetch('/api/feedback', {
            method: 'POST',
            body: JSON.stringify({
                content: feedback,
                announcement: instance.config.title
            })
        });
    }
}
```

#### 示例 3：动态内容更新
```javascript
function updateAnnouncementContent(event, instance) {
    // 根据用户行为更新公告内容
    const userType = getUserType(); // 获取用户类型
    const content = userType === 'vip' ? 
        'VIP用户专享内容' : 
        '普通用户内容';
    
    instance.updateContent(content);
}
```

#### 示例 4：条件跳转
```javascript
function conditionalRedirect(event, instance) {
    const isLoggedIn = checkLoginStatus();
    if (isLoggedIn) {
        // 已登录用户跳转到高级功能
        window.location.href = '/advanced-features';
    } else {
        // 未登录用户跳转到登录页
        window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
    }
}
```

### 7. 性能优化建议

- **合理设置弹出间隔**：避免频繁弹窗影响用户体验
- **优化路径匹配**：使用精确路径而非通配符
- **控制动画效果**：在重要场景使用礼花效果
- **避免复杂 DOM 操作**：自定义回调中避免大量 DOM 操作

## 🛠️ 开发环境

- Java 21+
- Node.js 18+
- pnpm

## 🚀 开发

```bash
# 构建插件
./gradlew build

# 开发前端
cd ui
pnpm install
pnpm dev
```

## 📦 构建

```bash
./gradlew build
```

构建完成后，可以在 `build/libs` 目录找到插件 jar 文件。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

[GPL-3.0](./LICENSE) © Handsome 