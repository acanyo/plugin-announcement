package cc.lik.bingeWatching;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import cc.lik.bingeWatching.entity.HandsomeMovie;
import cc.lik.bingeWatching.finders.HandsomeMovieFinder;
import cc.lik.bingeWatching.service.SettingConfigGetter;
import java.util.HashMap;
import java.util.Map;

import cc.lik.bingeWatching.vo.HandsomeMovieVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.theme.TemplateNameResolver;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MovieRouter {
    private final TemplateNameResolver templateNameResolver;
    private final HandsomeMovieFinder movieFinder;
    private final SettingConfigGetter settingConfigGetter;
    private static final int DEFAULT_PAGE_SIZE = 20;

    @Bean
    RouterFunction<ServerResponse> movieRouterFunction() {
        return route(GET("/movies"), this::renderMovieWallPage)
            .andRoute(GET("/movies/{name}"), this::renderMovieDetailPage);
    }

    private Mono<ServerResponse> renderMovieWallPage(ServerRequest request) {
        return this.settingConfigGetter.getStyleConfig()
            .flatMap(styleConfig -> {
                Map<String, Object> model = new HashMap<>();
                model.put("styleConfig", styleConfig);
                
                String keyword = request.queryParam("keyword").orElse(null);
                int page = request.queryParam("page").map(Integer::parseInt).orElse(1);
                int size = DEFAULT_PAGE_SIZE;
                
                Mono<run.halo.app.extension.ListResult<HandsomeMovieVo>> moviesMono = 
                    (keyword != null && !keyword.isBlank())
                        ? movieFinder.listFuzzySearchByName(page, size, keyword)
                        : movieFinder.list(page, size);
                
                return moviesMono.flatMap(listResult -> {
                    model.put("movies", listResult.getItems());
                    model.put("currentPage", page);
                    model.put("totalPages", (int) Math.ceil((double) listResult.getTotal() / size));
                    return templateNameResolver.resolveTemplateNameOrDefault(request.exchange(), "movie-wall")
                        .flatMap(templateName -> ServerResponse.ok()
                            .render(templateName, model));
                });
            });
    }

    private Mono<ServerResponse> renderMovieDetailPage(ServerRequest request) {
        String name = request.pathVariable("name");
        return Mono.zip(
            movieFinder.getByMetadataName(name).next(),
            this.settingConfigGetter.getStyleConfig()
        ).flatMap(tuple -> {
            HandsomeMovieVo movie = tuple.getT1();
            SettingConfigGetter.StyleConfig styleConfig = tuple.getT2();
            Map<String, Object> model = new HashMap<>();
            model.put("movie", movie);
            model.put("styleConfig", styleConfig);
            model.put("title", styleConfig.getTitle() + "|" + movie.getSpec().getVodName());
            return templateNameResolver.resolveTemplateNameOrDefault(request.exchange(), "movie-detail")
                .flatMap(templateName -> ServerResponse.ok()
                    .render(templateName, model));
        });
    }
} 