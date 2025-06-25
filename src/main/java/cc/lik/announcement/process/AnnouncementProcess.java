package cc.lik.announcement.process;

import cc.lik.announcement.service.SettingConfigGetter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
            .flatMap(jsConfig -> {
                // 生成 CSS 和 JS 标签
                String cssContent = String.format("<link rel=\"stylesheet\" href=\"%s\" />", CSS_URL);
                String scriptTag = String.format("<script src=\"%s\"></script>", SCRIPT_URL);
                
                // 拼接完整的 HTML 内容
                String fullScript = cssContent + "\n" + scriptTag + "\n" + jsConfig;
                iModel.add(modelFactory.createText(fullScript));
                return Mono.empty();
            });
    }

    public Mono<String> jsConfigTemplate() {
        return Mono.zip(
                settingConfigGetter.getBasicConfig().switchIfEmpty(Mono.empty()),
                settingConfigGetter.getButtonConfig().switchIfEmpty(Mono.empty())
            )
            .flatMap(tuple -> {
                SettingConfigGetter.BasicConfig basicConfig = tuple.getT1();
                SettingConfigGetter.ButtonConfig buttonConfig = tuple.getT2();
                
                // 如果没有基本配置或未启用，返回空字符串
                if (!basicConfig.isShowOnLoad()) {
                    return Mono.just("");
                }
                
                // 处理URL模式
                this.urlPatterns = Optional.ofNullable(basicConfig.getUrlPatterns())
                    .map(url -> Arrays.stream(url.split("\n"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()))
                    .orElseGet(ArrayList::new);
                
                return Mono.just(buildScriptContent(basicConfig, buttonConfig));
            })
            .switchIfEmpty(Mono.just("")); // 如果zip操作失败，返回空字符串
    }

    private String buildScriptContent(SettingConfigGetter.BasicConfig basicConfig, 
                                    SettingConfigGetter.ButtonConfig buttonConfig) {
        final Properties properties = new Properties();

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

        // 主按钮
        properties.setProperty("mainButtonText", escapeJavaScript(
            buttonConfig != null ? buttonConfig.getMainButtonText() : "确认"));
        properties.setProperty("mainButtonColor", 
            buttonConfig != null ? buttonConfig.getMainButtonColor() : "#165DFF");
        properties.setProperty("mianPopupContent", escapeJavaScript(
            buttonConfig != null ? buttonConfig.getMianPopupContent() : ""));
        // 主按钮 url/alertMessage 注入
        properties.setProperty("mainButtonUrl", escapeJavaScript(
            buttonConfig != null ? buttonConfig.getJumpUrl() : ""));
        properties.setProperty("mainButtonAlertMessage", escapeJavaScript(
            buttonConfig != null && buttonConfig.getMianPopupContent() != null ? buttonConfig.getMianPopupContent() : ""));
        // 主按钮 callback 代码片段
        String mainButtonCallbackCode = buttonConfig != null && buttonConfig.getMainButtonCallback() != null
            ? buttonConfig.getMainButtonCallback().replaceAll("(?i)<script[^>]*>", "").replaceAll("(?i)</script>", "").trim()
            : "";
        // 副按钮
        properties.setProperty("secondaryButtonText", escapeJavaScript(
            buttonConfig != null ? buttonConfig.getSecondaryButtonText() : "关闭"));
        properties.setProperty("secondaryButtonColor", 
            buttonConfig != null ? buttonConfig.getSecondaryButtonColor() : "#86909C");
        properties.setProperty("secondaryPopupContent", escapeJavaScript(
            buttonConfig != null ? buttonConfig.getSecondaryPopupContent() : ""));
        // 副按钮 url/alertMessage 注入
        properties.setProperty("secondaryButtonUrl", escapeJavaScript(
            buttonConfig != null ? buttonConfig.getSecondaryJumpUrl() : ""));
        properties.setProperty("secondaryButtonAlertMessage", escapeJavaScript(
            buttonConfig != null && buttonConfig.getSecondaryPopupContent() != null ? buttonConfig.getSecondaryPopupContent() : ""));
        // 副按钮 callback 代码片段
        String secondaryButtonCallbackCode = buttonConfig != null && buttonConfig.getSecondaryButtonCallback() != null
            ? buttonConfig.getSecondaryButtonCallback().replaceAll("(?i)<script[^>]*>", "").replaceAll("(?i)</script>", "").trim()
            : "";

        // 主按钮 callbackFunction 动态赋值
        String mainCallbackFunction = "null";
        if (buttonConfig != null && buttonConfig.getMainButtonCallbackOption() != null) {
            mainCallbackFunction = switch (buttonConfig.getMainButtonCallbackOption()) {
                case "jump" -> "LikccNotification.openNewWindow";
                case "confirmJump" -> "LikccNotification.openNewWindowWithAlert";
                case "customContent" ->buttonConfig.getMianCallback();
                default -> null;
            };
        }
        // 副按钮 callbackFunction 动态赋值
        String secondaryCallbackFunction = "null";
        if (buttonConfig != null && buttonConfig.getSecondaryButtonCallbackOption() != null) {
            secondaryCallbackFunction = switch (buttonConfig.getSecondaryButtonCallbackOption()) {
                case "jump" -> "openNewWindow";
                case "confirmJump" -> "openNewWindowWithAlert";
                case "customContent" -> buttonConfig.getSecondaryCallback();
                default -> null;
            };
        }

        // 生成最终 JS
        String script = """
            <script>
            %s
            %s
                document.addEventListener('DOMContentLoaded', () => {
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
                        showOnLoad: ${showOnLoad}
                    });
                });
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