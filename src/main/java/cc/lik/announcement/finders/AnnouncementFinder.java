package cc.lik.bingeWatching.finders;

import cc.lik.bingeWatching.vo.HandsomeMovieVo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;

public interface HandsomeMovieFinder {

    Flux<HandsomeMovieVo> listAll();

    Mono<ListResult<HandsomeMovieVo>> list(Integer page, Integer size);

    Mono<HandsomeMovieVo> getByName(String movieName);
    Flux<HandsomeMovieVo> getByMetadataName(String MetadataName);

    Mono<ListResult<HandsomeMovieVo>> listByName(Integer page, Integer size,String movieName);

    /**
     * 根据名称模糊查询影视
     */
    Flux<HandsomeMovieVo> fuzzySearchByName(String keyword);
    Mono<ListResult<HandsomeMovieVo>>  listFuzzySearchByName(Integer page, Integer size,String keyword);
}
