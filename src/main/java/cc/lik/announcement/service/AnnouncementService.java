package cc.lik.announcement.service;

import cc.lik.announcement.AnnouncementQuery;
import cc.lik.announcement.extension.Announcement;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;

import java.util.List;

public interface AnnouncementService {

    Mono<ListResult<Announcement>> listAnnouncement(AnnouncementQuery query);
    
    /**
     * 根据允许的权限列表查询公告
     */
    Mono<ListResult<Announcement>> listAnnouncementByPermissions(AnnouncementQuery query, List<String> allowedPermissions);
    
    /**
     * 根据名称获取公告
     * @param name 公告名称
     * @return 公告对象
     */
    Mono<Announcement> getAnnouncementByName(String name);
}
