package cc.lik.announcement;

import cc.lik.announcement.extension.Announcement;
import org.springframework.stereotype.Component;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexSpec;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

import static run.halo.app.extension.index.IndexAttributeFactory.simpleAttribute;

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
            indexSpecs.add(new IndexSpec()
                .setName("announcementSpec.title")
                .setIndexFunc(simpleAttribute(Announcement.class,
                    selectedComment -> selectedComment.getAnnouncementSpec().getTitle())
                ));
            indexSpecs.add(new IndexSpec()
                .setName("announcementSpec.permissions")
                .setIndexFunc(simpleAttribute(Announcement.class, announcement -> {
                    var permission = announcement.getAnnouncementSpec().getPermissions();
                    return permission == null ? null : permission.name();
                })));
            indexSpecs.add(new IndexSpec()
                .setName("announcementSpec.content")
                .setIndexFunc(simpleAttribute(Announcement.class,
                    selectedComment -> selectedComment.getAnnouncementSpec().getContent())
                ));
            indexSpecs.add(new IndexSpec()
                .setName("announcementSpec.position")
                .setIndexFunc(simpleAttribute(Announcement.class,
                    selectedComment -> selectedComment.getAnnouncementSpec().getPosition())
                ));
            indexSpecs.add(new IndexSpec()
                .setName("announcementSpec.autoClose")
                .setIndexFunc(simpleAttribute(Announcement.class,
                    selectedComment -> String.valueOf(
                        selectedComment.getAnnouncementSpec().getAutoClose()))
                ));
            indexSpecs.add(new IndexSpec()
                .setName("announcementSpec.closeOnClickOutside")
                .setIndexFunc(simpleAttribute(Announcement.class,
                    selectedComment -> String.valueOf(
                        selectedComment.getAnnouncementSpec().getCloseOnClickOutside()))
                ));
            indexSpecs.add(new IndexSpec()
                .setName("announcementSpec.confettiEnable")
                .setIndexFunc(simpleAttribute(Announcement.class,
                    selectedComment -> String.valueOf(
                        selectedComment.getAnnouncementSpec().getConfettiEnable()))
                ));
        });
    }

    @Override
    public void stop() {
        System.out.println("插件停止！");
    }
}
