package top.wboost.common.spring.boot.swagger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.wboost.base.spring.boot.starter.GlobalForSpringBootStarter;

@Data
@ConfigurationProperties(GlobalForSpringBootStarter.PROPERTIES_PREFIX + "swagger")
public class SwaggerProperties {

    private String title;
    private String description;
    private String termsOfServiceUrl;
    private String version;
    /**默认使用@ApiParam注解生成的参数类型 {@code path}, {@code query}, {@code body}, {@code header} or {@code form}.**/
    private String defaultApiParamType;
    /**
     * 是否允许导出api文档
     **/
    private boolean enableExportApi;
    /**
     * 导出请求路径
     */
    private String exportUrl;

}
