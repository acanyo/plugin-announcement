package cc.lik.announcement.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;

import cc.lik.announcement.AnnouncementQuery;
import cc.lik.announcement.extension.Announcement;
import cc.lik.announcement.service.AnnouncementService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnnouncementPublicEndpoint implements CustomEndpoint {

    private final ReactiveExtensionClient client;
    private final AnnouncementService announcementSvc;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CONFIG_MAP_NAME = "plugin-announcement-configMap";

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "public.announcement.lik.cc/v1alpha1/Public";
        return SpringdocRouteBuilder.route()
            .GET("types", this::getAnnouncementTypes, builder -> {
                builder.operationId("GetAnnouncementTypes")
                    .tag(tag)
                    .description("获取公告类型配置（公开接口）")
                    .response(responseBuilder().implementation(List.class));
            })
            .GET("announcements", this::listAnnouncements, builder -> {
                builder.operationId("ListPublicAnnouncements")
                    .tag(tag)
                    .description("获取公告列表（公开接口）")
                    .response(responseBuilder()
                        .implementation(ListResult.generateGenericClass(Announcement.class)));
                AnnouncementQuery.buildParameters(builder);
            })
            .build();
    }

    Mono<ServerResponse> getAnnouncementTypes(ServerRequest request) {
        return client.fetch(ConfigMap.class, CONFIG_MAP_NAME)
            .map(configMap -> {
                Map<String, String> data = configMap.getData();
                if (data == null || !data.containsKey("types")) {
                    return Collections.emptyList();
                }
                try {
                    JsonNode node = objectMapper.readTree(data.get("types"));
                    JsonNode typesNode = node.get("announcementTypes");
                    if (typesNode != null && typesNode.isArray()) {
                        return objectMapper.convertValue(typesNode, List.class);
                    }
                } catch (Exception e) {
                    log.error("Failed to parse announcement types", e);
                }
                return Collections.emptyList();
            })
            .defaultIfEmpty(Collections.emptyList())
            .flatMap(types -> ServerResponse.ok().bodyValue(types));
    }

    Mono<ServerResponse> listAnnouncements(ServerRequest request) {
        AnnouncementQuery query = new AnnouncementQuery(request);
        return announcementSvc.listAnnouncement(query)
            .flatMap(announcements -> ServerResponse.ok().bodyValue(announcements));
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("public.announcement.lik.cc/v1alpha1");
    }
}
