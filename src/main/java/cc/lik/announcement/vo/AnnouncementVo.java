package cc.lik.bingeWatching.vo;

import cc.lik.bingeWatching.entity.HandsomeMovie;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import run.halo.app.extension.MetadataOperator;


@Data
@SuperBuilder
@ToString
@EqualsAndHashCode
public class HandsomeMovieVo {

    private MetadataOperator metadata;

    private HandsomeMovie.HandsomeMovieSpec spec;

    public static HandsomeMovieVo from(HandsomeMovie footprint) {
        return HandsomeMovieVo.builder()
            .metadata(footprint.getMetadata())
            .spec(footprint.getSpec())
            .build();
    }
}
