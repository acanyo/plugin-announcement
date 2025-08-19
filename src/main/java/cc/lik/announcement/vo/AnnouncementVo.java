package cc.lik.announcement.vo;

import cc.lik.announcement.extension.Announcement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import run.halo.app.extension.MetadataOperator;


@Data
@SuperBuilder
@ToString
@EqualsAndHashCode
public class AnnouncementVo {

    private MetadataOperator metadata;

    private Announcement.AnnouncementSpec spec;

    public static AnnouncementVo from(Announcement footprint) {
        return AnnouncementVo.builder()
            .metadata(footprint.getMetadata())
            .spec(footprint.getAnnouncementSpec())
            .build();
    }
}
