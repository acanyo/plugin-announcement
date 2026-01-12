package cc.lik.announcement;

import cc.lik.announcement.extension.Announcement;
import java.util.Optional;
import org.springframework.stereotype.Component;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexSpecs;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

/**
 * <p>Plugin main class to manage the lifecycle of the plugin.</p>
 * <p>This class must be public and have a public constructor.</p>
 * <p>Only one main class extending {@link BasePlugin} is allowed per plugin.</p>
 *
 * @author Handsome
 * @since 1.0.0
 */
@Component
public class AnnouncementPlugin extends BasePlugin {
    private final SchemeManager schemeManager;
    public AnnouncementPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        schemeManager.register(Announcement.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<Announcement, String>single("announcementSpec.title", String.class)
                .indexFunc(item -> Optional.ofNullable(item.getAnnouncementSpec())
                    .map(Announcement.AnnouncementSpec::getTitle)
                    .orElse(null)));
            indexSpecs.add(IndexSpecs.<Announcement, String>single("announcementSpec.permissions", String.class)
                .indexFunc(item -> Optional.ofNullable(item.getAnnouncementSpec())
                    .map(Announcement.AnnouncementSpec::getPermissions)
                    .map(Enum::name)
                    .orElse(null)));
            indexSpecs.add(IndexSpecs.<Announcement, String>single("announcementSpec.content", String.class)
                .indexFunc(item -> Optional.ofNullable(item.getAnnouncementSpec())
                    .map(Announcement.AnnouncementSpec::getContent)
                    .orElse(null)));
            indexSpecs.add(IndexSpecs.<Announcement, String>single("announcementSpec.position", String.class)
                .indexFunc(item -> Optional.ofNullable(item.getAnnouncementSpec())
                    .map(Announcement.AnnouncementSpec::getPosition)
                    .orElse(null)));
            indexSpecs.add(IndexSpecs.<Announcement, String>single("announcementSpec.autoClose", String.class)
                .indexFunc(item -> Optional.ofNullable(item.getAnnouncementSpec())
                    .map(spec -> String.valueOf(spec.getAutoClose()))
                    .orElse(null)));
            indexSpecs.add(IndexSpecs.<Announcement, String>single("announcementSpec.closeOnClickOutside", String.class)
                .indexFunc(item -> Optional.ofNullable(item.getAnnouncementSpec())
                    .map(spec -> String.valueOf(spec.getCloseOnClickOutside()))
                    .orElse(null)));
            indexSpecs.add(IndexSpecs.<Announcement, String>single("announcementSpec.confettiEnable", String.class)
                .indexFunc(item -> Optional.ofNullable(item.getAnnouncementSpec())
                    .map(spec -> String.valueOf(spec.getConfettiEnable()))
                    .orElse(null)));
            indexSpecs.add(IndexSpecs.<Announcement, String>single("announcementSpec.enablePopup", String.class)
                .indexFunc(item -> Optional.ofNullable(item.getAnnouncementSpec())
                    .map(spec -> String.valueOf(spec.getEnablePopup()))
                    .orElse(null)));
            indexSpecs.add(IndexSpecs.<Announcement, String>single("announcementSpec.type", String.class)
                .indexFunc(item -> Optional.ofNullable(item.getAnnouncementSpec())
                    .map(Announcement.AnnouncementSpec::getType)
                    .orElse(null)));
        });
    }

    @Override
    public void stop() {
    }
}
