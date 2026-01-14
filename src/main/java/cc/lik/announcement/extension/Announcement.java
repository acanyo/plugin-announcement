package cc.lik.announcement.extension;


import static cc.lik.announcement.extension.Announcement.KIND;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.core.extension.attachment.Constant;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@ToString(callSuper = true)
@GVK(kind = KIND, group = "announcement.lik.cc",
    version = Constant.VERSION, singular = "announcement", plural = "announcement")
@EqualsAndHashCode(callSuper = true)
public class Announcement extends AbstractExtension {

    public static final String KIND = "Announcement";
    @Schema(requiredMode = REQUIRED)
    private AnnouncementSpec announcementSpec;
    @Data
    public static class AnnouncementSpec {
        @Schema(requiredMode = REQUIRED)
        private String title;

        @Schema(description = "编辑器类型: richtext, code")
        private String editorType;

        @Schema(description = "公告类型")
        private String type;

        @Schema(requiredMode = REQUIRED, defaultValue = "everyone")
        private ViewPermissions permissions;

        @Schema(requiredMode = REQUIRED)
        private String content;
        // 位置和显示配置
        @Schema(requiredMode = REQUIRED)
        private String position; // center, left-bottom, right-bottom, left-top, right-top
        @Schema(requiredMode = REQUIRED)
        private int autoClose; // 自动关闭时间（秒），0表示不自动关闭
        @Schema(requiredMode = REQUIRED)
        private Boolean closeOnClickOutside; // 点击外部关闭
        @Schema(requiredMode = REQUIRED)
        private int popupInterval; // 弹窗弹出间隔（小时），0 表示不限制
        @Schema(requiredMode = REQUIRED)
        private Boolean confettiEnable; // 是否启用礼花爆炸效果
        @Schema(requiredMode = REQUIRED)
        private Boolean enablePopup; // 是否启用弹窗
        @Schema(requiredMode = REQUIRED)
        private Boolean enablePinning; // 是否开启置顶
        
        @Schema(description = "URL路径匹配规则，支持通配符*，每行一个，为空或/表示仅首页")
        private String urlPatterns;

        // 弹窗图标配置
        @Schema(description = "弹窗图标，Iconify 图标名称，如 mdi:bell")
        private String popupIcon;
        @Schema(description = "弹窗图标背景色")
        private String popupIconBgColor;

        // 按钮配置
        @Schema(description = "主按钮文字")
        private String primaryButtonText;
        @Schema(description = "主按钮颜色")
        private String primaryButtonColor;
        @Schema(description = "主按钮事件类型: closeNotice, jump, confirmJump, callback")
        private String primaryButtonAction;
        @Schema(description = "主按钮链接")
        private String primaryButtonUrl;
        @Schema(description = "主按钮JS回调代码")
        private String primaryButtonCallback;
        @Schema(description = "副按钮文字")
        private String secondaryButtonText;
        @Schema(description = "副按钮颜色")
        private String secondaryButtonColor;
        @Schema(description = "副按钮事件类型: closeNotice, jump, confirmJump, callback")
        private String secondaryButtonAction;
        @Schema(description = "副按钮链接")
        private String secondaryButtonUrl;
        @Schema(description = "副按钮JS回调代码")
        private String secondaryButtonCallback;

        public enum ViewPermissions {
            loggedInUsers,
            nonLoggedInUsers,
            everyone,
            notShown
        }
    }
}
