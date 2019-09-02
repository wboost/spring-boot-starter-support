package top.wboost.config.client.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import top.wboost.base.spring.boot.starter.GlobalForSpringBootStarter;

@Primary
@Data
@ConfigurationProperties(GlobalForSpringBootStarter.PROPERTIES_PREFIX + "config.client")
public class ConfigClientProperties {

    /**
     * 配置服务实际地址
     */
    private String serverAddr;

    /**
     * 注册至服务注册中心的配置中心id
     */
    private String serverId;

    /**
     * 应用名
     */
    private String applicationName;

    /**
     * 配置中心前缀
     */
    private String prefix = "";

}
