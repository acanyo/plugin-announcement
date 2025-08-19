package cc.lik.announcement.finders;

import cc.lik.announcement.vo.AnnouncementVo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;

public interface AnnouncementFinder {

    Flux<AnnouncementVo> listAll();

    Mono<ListResult<AnnouncementVo>> list(Integer page, Integer size);

    Mono<AnnouncementVo> getByName(String announcementName);

    Flux<AnnouncementVo> getByMetadataName(String metadataName);

    Mono<ListResult<AnnouncementVo>> listByName(Integer page, Integer size, String announcementName);

    /**
     * 根据标题模糊查询公告
     */
    Flux<AnnouncementVo> fuzzySearchByTitle(String keyword);
    
    Mono<ListResult<AnnouncementVo>> listFuzzySearchByTitle(Integer page, Integer size, String keyword);
}
