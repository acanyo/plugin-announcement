package cc.lik.announcement.extension;


import static cc.lik.announcement.extension.Announcement.KIND;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
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

        @Schema(requiredMode = REQUIRED, defaultValue = "pending")
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

        public enum ViewPermissions {
            loggedInUsers,
            nonLoggedInUsers,
            everyone,
            notShown
        }
    }
}
