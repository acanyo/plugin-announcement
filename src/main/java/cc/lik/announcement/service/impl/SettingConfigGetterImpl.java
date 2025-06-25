package cc.lik.announcement.service.impl;


import cc.lik.announcement.service.SettingConfigGetter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;

@Component
@RequiredArgsConstructor
public class SettingConfigGetterImpl implements SettingConfigGetter {
    private final ReactiveSettingFetcher settingFetcher;

    @Override
    public Mono<BasicConfig> getBasicConfig() {
       return settingFetcher.fetch(BasicConfig.GROUP, BasicConfig.class)
           .defaultIfEmpty(new BasicConfig());
    }
    
    @Override
    public Mono<ButtonConfig> getButtonConfig() {
       return settingFetcher.fetch(ButtonConfig.GROUP, ButtonConfig.class)
           .defaultIfEmpty(new ButtonConfig());
    }
}
