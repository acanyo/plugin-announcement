package cc.lik.announcement.service;

import cc.lik.announcement.AnnouncementQuery;
import cc.lik.announcement.extension.Announcement;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;

public interface AnnouncementService {

    Mono<ListResult<Announcement>> listAnnouncement(AnnouncementQuery query);
}
