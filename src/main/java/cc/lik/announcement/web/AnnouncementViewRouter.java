package cc.lik.announcement.web;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import cc.lik.announcement.finders.AnnouncementFinder;
import cc.lik.announcement.vo.AnnouncementVo;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.theme.TemplateNameResolver;
import run.halo.app.extension.ListResult;

@Slf4j
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class AnnouncementViewRouter {

    private final TemplateNameResolver templateNameResolver;
    private final AnnouncementFinder announcementFinder;

    @Bean
    RouterFunction<ServerResponse> announcementViewRoutes() {
        return route(GET("/announcements"), this::renderAnnouncementsPage)
            .andRoute(GET("/announcements/{name}"), this::renderAnnouncementDetailPage);
    }

    Mono<ServerResponse> renderAnnouncementsPage(ServerRequest request) {
        try {
            // 获取查询参数
            String keyword = request.queryParam("keyword").orElse(null);
            final int page = Math.max(1, request.queryParam("page").map(Integer::parseInt).orElse(1));
            final int size = Math.max(1, Math.min(100, request.queryParam("size").map(Integer::parseInt).orElse(20)));
            
            log.debug("Rendering announcements page - page: {}, size: {}, keyword: {}", page, size, keyword);
            
            // 根据是否有关键词选择查询方法
            Mono<ListResult<AnnouncementVo>> announcementsMono = 
                (keyword != null && !keyword.isBlank())
                    ? announcementFinder.listFuzzySearchByTitle(page, size, keyword)
                    : announcementFinder.list(page, size);
            
            return announcementsMono
                .defaultIfEmpty(new ListResult<>(page, size, 0L, List.of()))
                .flatMap(listResult -> {
                    try {
                        // 确保数据完整性
                        if (listResult == null) {
                            listResult = new ListResult<>(page, size, 0L, List.of());
                        }
                        
                        List<AnnouncementVo> items = listResult.getItems();
                        if (items == null) {
                            items = List.of();
                        }
                        
                        long total = listResult.getTotal();
                        
                        Map<String, Object> model = new HashMap<>();
                        model.put("title", "公告列表");
                        model.put("announcements", items);
                        model.put("currentPage", page);
                        model.put("totalPages", (int) Math.ceil((double) total / size));
                        model.put("total", total);
                        model.put("keyword", keyword);
                        
                        log.debug("Model prepared - items: {}, total: {}, totalPages: {}", 
                                items.size(), total, (int) Math.ceil((double) total / size));
                        
                        return templateNameResolver.resolveTemplateNameOrDefault(request.exchange(), "announcements")
                            .flatMap(template -> {
                                log.debug("Template resolved: {}", template);
                                return ServerResponse.ok().render(template, model);
                            });
                    } catch (Exception e) {
                        log.error("Error preparing model data", e);
                        return ServerResponse.status(500).bodyValue("Error preparing page data");
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error rendering announcements page", e);
                    return ServerResponse.status(500).bodyValue("Error loading announcements");
                });
        } catch (Exception e) {
            log.error("Unexpected error in renderAnnouncementsPage", e);
            return ServerResponse.status(500).bodyValue("Unexpected error occurred");
        }
    }

    Mono<ServerResponse> renderAnnouncementDetailPage(ServerRequest request) {
        try {
            String name = request.pathVariable("name");
            log.debug("Rendering announcement detail page for name: {}", name);
            
            if (name.trim().isEmpty()) {
                return ServerResponse.badRequest().bodyValue("Invalid announcement name");
            }
            
            return announcementFinder.getByName(name)
                .flatMap(announcement -> {
                    try {
                        if (announcement == null) {
                            return ServerResponse.notFound().build();
                        }
                        
                        Map<String, Object> model = new HashMap<>();
                        model.put("announcement", announcement);
                        model.put("title", announcement.getSpec() != null ? announcement.getSpec().getTitle() : "公告详情");
                        
                        log.debug("Announcement detail model prepared for: {}", name);
                        
                        return templateNameResolver.resolveTemplateNameOrDefault(request.exchange(), "announcement-detail")
                            .flatMap(template -> {
                                log.debug("Detail template resolved: {}", template);
                                return ServerResponse.ok().render(template, model);
                            });
                    } catch (Exception e) {
                        log.error("Error preparing announcement detail model", e);
                        return ServerResponse.status(500).bodyValue("Error preparing announcement detail");
                    }
                })
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(e -> {
                    log.error("Error rendering announcement detail page", e);
                    return ServerResponse.status(500).bodyValue("Error loading announcement detail");
                });
        } catch (Exception e) {
            log.error("Unexpected error in renderAnnouncementDetailPage", e);
            return ServerResponse.status(500).bodyValue("Unexpected error occurred");
        }
    }
} 