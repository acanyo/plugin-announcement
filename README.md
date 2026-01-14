# Halo 公告管理插件

为 Halo 2.x 提供公告管理功能，支持公告列表展示、弹窗通知、置顶、权限控制、礼花效果等。

## 功能特性

- 公告列表 - 前台展示公告列表，支持分页、类型筛选
- 弹窗通知 - 自定义弹窗样式、位置、按钮行为
- 置顶功能 - 重要公告置顶显示
- 权限控制 - 按登录状态控制公告可见性
- 礼花效果 - 弹窗时可选礼花动画
- 编辑器扩展 - 在文章中插入公告列表组件

## 安装

1. 下载最新版本的 JAR 文件
2. 在 Halo 后台「插件」页面上传安装
3. 启用插件

## 使用说明

详细使用教程请查看 [使用文档](https://docs.lik.cc/)

## 开发

```bash
# 克隆项目
git clone https://github.com/acanyo/plugin-announcement.git

# 构建
./gradlew build

# 开发模式
./gradlew haloServer
```

## 许可证

[GPL-3.0](./LICENSE)
