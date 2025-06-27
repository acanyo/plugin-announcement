package cc.lik.announcement.process;

import cc.lik.announcement.service.SettingConfigGetter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.PropertyPlaceholderHelper;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.theme.dialect.TemplateHeadProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class AnnouncementProcess implements TemplateHeadProcessor {

    static final PropertyPlaceholderHelper
        PROPERTY_PLACEHOLDER_HELPER = new PropertyPlaceholderHelper("${", "}");

    private static final String SCRIPT_URL = "/plugins/announcement/assets/static/likcc-notification.js";
    private static final String CONFETTI_URL = "/plugins/announcement/assets/static/confetti.browser.min.js";
    private static final String CSS_URL = "/plugins/announcement/assets/static/likcc-notification.css";

    private final SettingConfigGetter settingConfigGetter;
    
    List<String> urlPatterns = new ArrayList<>();

    @Override
    public Mono<Void> process(ITemplateContext iTemplateContext, IModel iModel,
        IElementModelStructureHandler iElementModelStructureHandler) {
        final IModelFactory modelFactory = iTemplateContext.getModelFactory();
        return insertJsAndCss(iModel, modelFactory);
    }

    @NotNull
    private Mono<Void> insertJsAndCss(IModel iModel, IModelFactory modelFactory) {
        return jsConfigTemplate()
            .flatMap(result -> {
                // 从结果中获取CSS地址和JS配置
                String cssUrl = (String) result.get("cssUrl");
                String jsConfig = (String) result.get("jsConfig");
                
                // 生成 CSS 和 JS 标签
                String cssContent = String.format("<link rel=\"stylesheet\" href=\"%s\" />", cssUrl);
                String scriptTag = String.format("<script src=\"%s\"></script>", SCRIPT_URL);
                String confettiTag = String.format("<script src=\"%s\"></script>", CONFETTI_URL);
                // 拼接完整的 HTML 内容
                String fullScript = cssContent + "\n" + scriptTag + "\n" + jsConfig+ "\n" + confettiTag;
                iModel.add(modelFactory.createText(fullScript));
                return Mono.empty();
            });
    }

    public Mono<Map<String, String>> jsConfigTemplate() {
        return Mono.zip(
                settingConfigGetter.getBasicConfig().switchIfEmpty(Mono.empty()),
                settingConfigGetter.getButtonConfig().switchIfEmpty(Mono.empty())
            )
            .flatMap(tuple -> {
                SettingConfigGetter.BasicConfig basicConfig = tuple.getT1();
                SettingConfigGetter.ButtonConfig buttonConfig = tuple.getT2();
                
                // 如果没有基本配置或未启用，返回空结果
                if (!basicConfig.isShowOnLoad()) {
                    return Mono.just(Map.of("jsConfig", "", "cssUrl", CSS_URL));
                }
                
                // 处理URL模式
                this.urlPatterns = Optional.ofNullable(basicConfig.getUrlPatterns())
                    .map(url -> Arrays.stream(url.split("\n"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()))
                    .orElseGet(ArrayList::new);
                
                // 获取CSS地址，如果设置中没有则使用默认值
                String cssUrl = Optional.ofNullable(basicConfig.getAnnouncementStyle())
                    .filter(style -> !style.trim().isEmpty())
                    .orElse(CSS_URL);
                
                return Mono.just(Map.of(
                    "jsConfig", buildScriptContent(basicConfig, buttonConfig),
                    "cssUrl", cssUrl
                ));
            })
            .switchIfEmpty(Mono.just(Map.of("jsConfig", "", "cssUrl", CSS_URL))); // 如果zip操作失败，返回空结果
    }

    private String buildScriptContent(SettingConfigGetter.BasicConfig basicConfig, SettingConfigGetter.ButtonConfig buttonConfig) {
        var properties = new Properties();
        properties.setProperty("title", escapeJavaScript(basicConfig.getTitle()));
        properties.setProperty("content", escapeJavaScript(basicConfig.getContent()));
        properties.setProperty("position", basicConfig.getPosition());
        String urlPatternsJson;
        try {
            urlPatternsJson = urlPatterns.isEmpty() ? "[]" : new ObjectMapper().writeValueAsString(urlPatterns);
        } catch (Exception e) {
            urlPatternsJson = "[]";
        }
        properties.setProperty("urlPatterns", urlPatternsJson);
        properties.setProperty("autoClose", String.valueOf(basicConfig.getAutoClose()));
        properties.setProperty("closeOnClickOutside", String.valueOf(basicConfig.isCloseOnClickOutside()));
        properties.setProperty("showOnLoad", String.valueOf(basicConfig.isShowOnLoad()));
        properties.setProperty("popupInterval", String.valueOf(basicConfig.getPopupInterval()));
        properties.setProperty("confettiEnable", String.valueOf(basicConfig.isConfettiEnable()));

        // 主按钮
        properties.setProperty("mainButtonText", escapeJavaScript(buttonConfig == null ? "确认" : buttonConfig.getMainButtonText()));
        properties.setProperty("mainButtonColor", buttonConfig == null ? "#165DFF" : buttonConfig.getMainButtonColor());
        properties.setProperty("mianPopupContent", escapeJavaScript(buttonConfig == null ? "" : buttonConfig.getMianPopupContent()));
        properties.setProperty("mainButtonUrl", escapeJavaScript(buttonConfig == null ? "" : buttonConfig.getJumpUrl()));
        properties.setProperty("mainButtonAlertMessage", escapeJavaScript(buttonConfig != null && buttonConfig.getMianPopupContent() != null ? buttonConfig.getMianPopupContent() : ""));
        String mainButtonCallbackCode = buttonConfig != null && buttonConfig.getMainButtonCallback() != null
            ? buttonConfig.getMainButtonCallback().replaceAll("(?i)<script[^>]*>", "").replaceAll("(?i)</script>", "").trim()
            : "";

        // 副按钮
        properties.setProperty("secondaryButtonText", escapeJavaScript(buttonConfig == null ? "关闭" : buttonConfig.getSecondaryButtonText()));
        properties.setProperty("secondaryButtonColor", buttonConfig == null ? "#86909C" : buttonConfig.getSecondaryButtonColor());
        properties.setProperty("secondaryPopupContent", escapeJavaScript(buttonConfig == null ? "" : buttonConfig.getSecondaryPopupContent()));
        properties.setProperty("secondaryButtonUrl", escapeJavaScript(buttonConfig == null ? "" : buttonConfig.getSecondaryJumpUrl()));
        properties.setProperty("secondaryButtonAlertMessage", escapeJavaScript(buttonConfig != null && buttonConfig.getSecondaryPopupContent() != null ? buttonConfig.getSecondaryPopupContent() : ""));
        String secondaryButtonCallbackCode = buttonConfig != null && buttonConfig.getSecondaryButtonCallback() != null
            ? buttonConfig.getSecondaryButtonCallback().replaceAll("(?i)<script[^>]*>", "").replaceAll("(?i)</script>", "").trim()
            : "";

        // 主按钮 callbackFunction 动态赋值
        var mainCallbackFunction = "null";
        if (buttonConfig != null && buttonConfig.getMainButtonCallbackOption() != null) {
            mainCallbackFunction = switch (buttonConfig.getMainButtonCallbackOption()) {
                case "jump" -> "LikccNotification.openNewWindow";
                case "confirmJump" -> "LikccNotification.openNewWindowWithAlert";
                case "customContent" -> buttonConfig.getMianCallback();
                default -> null;
            };
        }
        // 副按钮 callbackFunction 动态赋值
        var secondaryCallbackFunction = "null";
        if (buttonConfig != null && buttonConfig.getSecondaryButtonCallbackOption() != null) {
            secondaryCallbackFunction = switch (buttonConfig.getSecondaryButtonCallbackOption()) {
                case "jump" -> "LikccNotification.openNewWindow";
                case "confirmJump" -> "LikccNotification.openNewWindowWithAlert";
                case "customContent" -> buttonConfig.getSecondaryCallback();
                default -> null;
            };
        }

        // 生成最终 JS
        var script = """
            <script>
            %s
            %s
                function showLikccNotification() {
                    LikccNotification.create({
                        title: '${title}',
                        content: `${content}`,
                        position: '${position}',
                        urlPatterns: ${urlPatterns},
                        mainButton: {
                            text: '${mainButtonText}',
                            color: '${mainButtonColor}',
                            popupContent: '${mianPopupContent}',
                            url: '${mainButtonUrl}',
                            alertMessage: '${mainButtonAlertMessage}',
                            callbackFunction: %s
                        },
                        secondaryButton: {
                            text: '${secondaryButtonText}',
                            color: '${secondaryButtonColor}',
                            popupContent: '${secondaryPopupContent}',
                            url: '${secondaryButtonUrl}',
                            alertMessage: '${secondaryButtonAlertMessage}',
                            callbackFunction: %s
                        },
                        autoClose: ${autoClose},
                        closeOnClickOutside: ${closeOnClickOutside},
                        showOnLoad: ${showOnLoad},
                        popupInterval: ${popupInterval},
                        confettiEnable: ${confettiEnable}
                    });
                }
                document.addEventListener('DOMContentLoaded', showLikccNotification, { once: true });
                document.addEventListener('pjax:success', showLikccNotification);
            </script>
            """.formatted(mainButtonCallbackCode, secondaryButtonCallbackCode, mainCallbackFunction, secondaryCallbackFunction);
        return PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders(script, properties);
    }

    private String escapeJavaScript(String text) {
        if (text == null) {
            return "";
        }

        return text
            // 处理换行和空格
            .replace("\n", " ")
            .replace("\r", " ")
            .replaceAll("\\s+", " ")
            .trim()
            // 处理JavaScript特殊字符
            .replace("\\", "\\\\")  // 必须先处理反斜杠
            .replace("\"", "\\\"")
            .replace("'", "\\'")
            .replace("\b", "\\b")
            .replace("\f", "\\f")
            .replace("\t", "\\t");
    }
}