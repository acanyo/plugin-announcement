package cc.lik.announcement.service.impl;

import cc.lik.announcement.AnnouncementQuery;
import cc.lik.announcement.extension.Announcement;
import cc.lik.announcement.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final ReactiveExtensionClient client;
    @Override
    public Mono<ListResult<Announcement>> listAnnouncement(AnnouncementQuery query) {
        return client.listBy(Announcement.class, query.toListOptions(),
            PageRequestImpl.of(query.getPage(), query.getSize(), query.getSort()));
    }


}
