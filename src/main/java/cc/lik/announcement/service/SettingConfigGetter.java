package cc.lik.announcement.service;

import lombok.Data;
import reactor.core.publisher.Mono;

public interface SettingConfigGetter {
    Mono<BasicConfig> getBasicConfig();
    Mono<ButtonConfig> getButtonConfig();

    @Data
    class BasicConfig {
        public static final String GROUP = "basic";
        
        // 基本开关
        private boolean showOnLoad;
        
        // 内容配置
        private String title;
        private String content;
        
        // 位置和显示配置
        private String position; // center, left-bottom, right-bottom, left-top, right-top
        private String urlPatterns; // 路径匹配规则，多行文本
        private int autoClose; // 自动关闭时间（秒），0表示不自动关闭
        private boolean closeOnClickOutside; // 点击外部关闭
        private int popupInterval; // 弹窗弹出间隔（小时），0 表示不限制
    }

    @Data
    class ButtonConfig {
        public static final String GROUP = "button";
        
        // 主按钮配置
        private String mainButtonText;
        private String mainButtonColor;
        private String mainButtonCallbackOption; // jump, confirmJump, closeNotice, customContent
        private String jumpUrl; // 主按钮跳转URL
        private String mianPopupContent; // 主按钮确认弹窗内容
        private String mianCallback; // 主按钮回调函数名
        private String mainButtonCallback; // 主按钮自定义回调代码
        
        // 副按钮配置
        private String secondaryButtonText;
        private String secondaryButtonColor;
        private String secondaryButtonCallbackOption; // jump, confirmJump, closeNotice, customContent
        private String secondaryJumpUrl; // 副按钮跳转URL
        private String secondaryPopupContent; // 副按钮确认弹窗内容
        private String secondaryCallback; // 副按钮回调函数名
        private String secondaryButtonCallback; // 副按钮自定义回调代码
    }
}
