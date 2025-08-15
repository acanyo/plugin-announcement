package cc.lik.announcement.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import cc.lik.announcement.AnnouncementQuery;
import cc.lik.announcement.extension.Announcement;
import cc.lik.announcement.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnnouncementEndpoint implements CustomEndpoint {

    private final AnnouncementService announcementSvc;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.announcement.lik.cc/v1alpha1/Announcement";
        return SpringdocRouteBuilder.route()
            .GET("announcements", this::listAnnouncement, builder -> {
                    builder.operationId("ListAnnouncements")
                        .tag(tag)
                        .description("分页查询公告列表")
                        .response(
                            responseBuilder()
                                .implementation(ListResult.generateGenericClass(Announcement.class))
                        );
                AnnouncementQuery.buildParameters(builder);
                }
            )
            .GET("announcements/{name}", this::getAnnouncementByName, builder -> {
                    builder.operationId("GetAnnouncementByName")
                        .tag(tag)
                        .description("根据名称获取公告")
                        .parameter(parameterBuilder()
                            .in(ParameterIn.PATH)
                            .name("name")
                            .description("公告名称")
                            .required(true)
                        )
                        .response(
                            responseBuilder()
                                .implementation(Announcement.class)
                        );
                }
            )
            .build();
    }

    Mono<ServerResponse> listAnnouncement(ServerRequest serverRequest) {
        AnnouncementQuery query = new AnnouncementQuery(serverRequest);
        return announcementSvc.listAnnouncement(query)
            .flatMap(announcements -> ServerResponse.ok().bodyValue(announcements));
    }
    
    Mono<ServerResponse> getAnnouncementByName(ServerRequest serverRequest) {
        String name = serverRequest.pathVariable("name");
        return announcementSvc.getAnnouncementByName(name)
            .flatMap(announcement -> ServerResponse.ok().bodyValue(announcement))
            .switchIfEmpty(ServerResponse.notFound().build());
    }
    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("api.announcement.lik.cc/v1alpha1");
    }

}
