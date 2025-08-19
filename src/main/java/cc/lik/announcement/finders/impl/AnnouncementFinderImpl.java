package cc.lik.announcement.finders.impl;

import static run.halo.app.extension.index.query.QueryFactory.all;
import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.contains;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.or;

import cc.lik.announcement.extension.Announcement;
import cc.lik.announcement.finders.AnnouncementFinder;
import cc.lik.announcement.vo.AnnouncementVo;
import jakarta.annotation.Nonnull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.Query;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.theme.finders.Finder;

@Finder("announcementFinder")
@RequiredArgsConstructor
public class AnnouncementFinderImpl implements AnnouncementFinder {

    private final ReactiveExtensionClient client;

    @Override
    public Flux<AnnouncementVo> listAll() {
        ListOptions listOptions = new ListOptions();
        Query query = all();
        listOptions.setFieldSelector(FieldSelector.of(query));
        return client.listAll(Announcement.class, listOptions, defaultSort())
            .flatMap(this::getAnnouncementVo);
    }

    @Override
    public Mono<ListResult<AnnouncementVo>> list(Integer page, Integer size) {
        PageRequest pageRequest = PageRequestImpl.of(pageNullSafe(page), sizeNullSafe(size), defaultSort());
        return pageAnnouncement(null, pageRequest);
    }

    @Override
    public Mono<AnnouncementVo> getByName(String announcementName) {
        return client.fetch(Announcement.class, announcementName)
            .flatMap(this::getAnnouncementVo);
    }

    @Override
    public Flux<AnnouncementVo> getByMetadataName(String metadataName) {
        ListOptions listOptions = new ListOptions();
        Query query = equal("metadata.name", metadataName);
        listOptions.setFieldSelector(FieldSelector.of(query));
        return client.listAll(Announcement.class, listOptions, defaultSort())
            .flatMap(this::getAnnouncementVo);
    }

    @Override
    public Mono<ListResult<AnnouncementVo>> listByName(Integer page, Integer size, String announcementName) {
        Query query = equal("metadata.name", announcementName);
        PageRequest pageRequest = PageRequestImpl.of(pageNullSafe(page), sizeNullSafe(size), defaultSort());
        return pageAnnouncement(FieldSelector.of(query), pageRequest);
    }

    @Override
    public Flux<AnnouncementVo> fuzzySearchByTitle(String keyword) {
        ListOptions listOptions = new ListOptions();
        Query query = or(
            contains("announcementSpec.title", keyword),
            contains("announcementSpec.content", keyword)
        );
        listOptions.setFieldSelector(FieldSelector.of(query));
        return client.listAll(Announcement.class, listOptions, defaultSort())
            .flatMap(this::getAnnouncementVo);
    }

    @Override
    public Mono<ListResult<AnnouncementVo>> listFuzzySearchByTitle(Integer page, Integer size, String keyword) {
        ListOptions listOptions = new ListOptions();
        Query query = or(
            contains("announcementSpec.title", keyword),
            contains("announcementSpec.content", keyword)
        );
        listOptions.setFieldSelector(FieldSelector.of(query));
        PageRequest pageRequest = PageRequestImpl.of(pageNullSafe(page), sizeNullSafe(size), defaultSort());
        return getListResultMono(listOptions, pageRequest, pageRequest.getPageNumber(),
            pageRequest.getPageSize());
    }

    private Mono<ListResult<AnnouncementVo>> pageAnnouncement(FieldSelector fieldSelector, PageRequest page) {
        ListOptions listOptions = new ListOptions();
        Query query = all();
        if (fieldSelector != null) {
            query = and(query, fieldSelector.query());
        }
        listOptions.setFieldSelector(FieldSelector.of(query));
        return getListResultMono(listOptions, page, page.getPageNumber(), page.getPageSize());
    }

    private Mono<ListResult<AnnouncementVo>> getListResultMono(ListOptions listOptions,
        PageRequest page, int page1, int page2) {
        return client.listBy(Announcement.class, listOptions, page)
            .flatMap(list -> Flux.fromStream(list.get())
                .concatMap(this::getAnnouncementVo)
                .collectList()
                .map(announcementVos -> new ListResult<>(list.getPage(), list.getSize(),
                    list.getTotal(), announcementVos)
                )
            )
            .defaultIfEmpty(
                new ListResult<>(page1, page2, 0L, List.of()));
    }

    static Sort defaultSort() {
        return Sort.by("metadata.creationTimestamp").descending();
    }

    private Mono<AnnouncementVo> getAnnouncementVo(@Nonnull Announcement announcement) {
        AnnouncementVo announcementVo = AnnouncementVo.from(announcement);
        return Mono.just(announcementVo);
    }

    int pageNullSafe(Integer page) {
        return ObjectUtils.defaultIfNull(page, 1);
    }

    int sizeNullSafe(Integer size) {
        return ObjectUtils.defaultIfNull(size, 10);
    }
}
