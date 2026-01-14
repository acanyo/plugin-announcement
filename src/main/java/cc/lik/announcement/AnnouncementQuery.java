package cc.lik.announcement;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static run.halo.app.extension.index.query.Queries.contains;
import static run.halo.app.extension.index.query.Queries.equal;
import static run.halo.app.extension.index.query.Queries.or;
import static run.halo.app.extension.router.QueryParamBuildUtil.sortParameter;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.server.ServerRequest;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.SortableRequest;

public class AnnouncementQuery extends SortableRequest {


    public AnnouncementQuery(ServerRequest request) {
        super(request.exchange());
    }

    @Nullable
    public String getKeyword() {
        return queryParams.getFirst("keyword");
    }


    @Nullable
    public String getPermissions() {
        return queryParams.getFirst("announcementSpec.permissions");
    }

    /**
     * 获取允许的权限列表（用于公开 API 根据登录状态过滤）
     */
    @Nullable
    public List<String> getAllowedPermissions() {
        List<String> perms = queryParams.get("allowedPermissions");
        return perms != null && !perms.isEmpty() ? perms : null;
    }

    @Nullable
    public Boolean getPopup() {
        String popup = queryParams.getFirst("popup");
        return popup != null ? Boolean.parseBoolean(popup) : null;
    }

    @Nullable
    public String getType() {
        return queryParams.getFirst("type");
    }

    @Nullable
    public String getEnablePinning() {
        return queryParams.getFirst("announcementSpec.enablePinning");
    }

    @Nullable
    public String getEnablePopup() {
        return queryParams.getFirst("announcementSpec.enablePopup");
    }

    @Override
    public ListOptions toListOptions() {
        var builder = ListOptions.builder(super.toListOptions());

        Optional.ofNullable(getKeyword())
            .filter(StringUtils::isNotBlank)
            .ifPresent(keyword -> builder.andQuery(or(
                contains("announcementSpec.title", keyword),
                contains("announcementSpec.enablePopup", keyword),
                contains("metadata.name", keyword)
            )));

        Optional.ofNullable(getPermissions())
            .filter(StringUtils::isNotBlank)
            .ifPresent(per -> builder.andQuery(equal("announcementSpec.permissions", per)));

        Optional.ofNullable(getPopup())
            .filter(popup -> popup)
            .ifPresent(popup -> builder.andQuery(equal("announcementSpec.enablePopup", "true")));

        Optional.ofNullable(getType())
            .filter(StringUtils::isNotBlank)
            .ifPresent(type -> builder.andQuery(equal("announcementSpec.type", type)));

        Optional.ofNullable(getEnablePinning())
            .filter(StringUtils::isNotBlank)
            .ifPresent(pinning -> builder.andQuery(equal("announcementSpec.enablePinning", pinning)));

        // 仅当不是 popup=true 请求时才使用 enablePopup 筛选参数
        if (getPopup() == null || !getPopup()) {
            Optional.ofNullable(getEnablePopup())
                .filter(StringUtils::isNotBlank)
                .ifPresent(popup -> builder.andQuery(equal("announcementSpec.enablePopup", popup)));
        }

        return builder.build();
    }

    public static void buildParameters(Builder builder) {
        IListRequest.buildParameters(builder);
        builder.parameter(sortParameter())
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("keyword")
                .description("Announcement filtered by keyword.")
                .implementation(String.class)
                .required(false))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("announcementSpec.permissions")
                .description("Announcement permissions.")
                .implementation(String.class)
                .required(false))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("popup")
                .description("Filter announcements with popup enabled.")
                .implementation(Boolean.class)
                .required(false))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("type")
                .description("Filter announcements by type.")
                .implementation(String.class)
                .required(false))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("announcementSpec.enablePinning")
                .description("Filter announcements by pinning status.")
                .implementation(String.class)
                .required(false))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("announcementSpec.enablePopup")
                .description("Filter announcements by popup status.")
                .implementation(String.class)
                .required(false));
    }
}
