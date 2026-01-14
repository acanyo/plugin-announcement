package cc.lik.announcement.service.impl;

import cc.lik.announcement.AnnouncementQuery;
import cc.lik.announcement.extension.Announcement;
import cc.lik.announcement.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.Queries;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final ReactiveExtensionClient client;
    
    // 默认排序：置顶优先，然后按创建时间倒序
    private Sort defaultSort() {
        return Sort.by(
            Sort.Order.desc("announcementSpec.enablePinning"),
            Sort.Order.desc("metadata.creationTimestamp")
        );
    }
    
    @Override
    public Mono<ListResult<Announcement>> listAnnouncement(AnnouncementQuery query) {
        return client.listBy(Announcement.class, query.toListOptions(),
            PageRequestImpl.of(query.getPage(), query.getSize(), defaultSort()));
    }
    
    @Override
    public Mono<ListResult<Announcement>> listAnnouncementByPermissions(AnnouncementQuery query, List<String> allowedPermissions) {
        // 使用 or 组合多个 equal 查询
        var permQueries = allowedPermissions.stream()
            .map(perm -> Queries.equal("announcementSpec.permissions", perm))
            .toList();
        
        // 将多个查询用 or 组合
        var combinedQuery = permQueries.stream()
            .reduce(Queries::or)
            .orElse(null);
        
        var builder = ListOptions.builder(query.toListOptions());
        if (combinedQuery != null) {
            builder.andQuery(combinedQuery);
        }
        
        return client.listBy(Announcement.class, builder.build(),
            PageRequestImpl.of(query.getPage(), query.getSize(), defaultSort()));
    }
    
    @Override
    public Mono<Announcement> getAnnouncementByName(String name) {
        return client.get(Announcement.class, name);
    }
}
