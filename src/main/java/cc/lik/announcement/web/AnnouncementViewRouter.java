package cc.lik.announcement.web;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.TemplateNameResolver;

import cc.lik.announcement.extension.Announcement;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class AnnouncementViewRouter {

    private final TemplateNameResolver templateNameResolver;
    private final ReactiveExtensionClient client;

    @Bean
    RouterFunction<ServerResponse> announcementViewRoutes() {
        return route(GET("/announcements"), this::renderAnnouncementsPage);
    }
    static Sort defaultSort() {
        return Sort.by("metadata.creationTimestamp").descending();
    }
    Mono<ServerResponse> renderAnnouncementsPage(ServerRequest request) {
        ListOptions listOptions = ListOptions.builder().build();
        PageRequestImpl pageRequest = PageRequestImpl.of(0, 1000, defaultSort());

        return client
            .listBy(Announcement.class, listOptions, pageRequest)
            .map(result -> {
                List<Announcement> items = result.getItems();
                return items.stream()
                    .filter(a -> a.getAnnouncementSpec() != null)
                    .filter(a -> a.getAnnouncementSpec().getPermissions()
                        != Announcement.AnnouncementSpec.ViewPermissions.notShown).sorted(Comparator
                        .comparing((Announcement a) -> Boolean.TRUE.equals(
                            a.getAnnouncementSpec().getEnablePinning())).reversed()
                        .thenComparing(a -> a.getMetadata().getCreationTimestamp(),
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
            })
            .flatMap(list -> {
                Map<String, Object> model = new HashMap<>();
                model.put("title", "公告");
                model.put("announcements", list);
                return templateNameResolver.resolveTemplateNameOrDefault(request.exchange(),
                        "announcements")
                    .flatMap(template -> ServerResponse.ok().render(template, model));
            });
    }
} 