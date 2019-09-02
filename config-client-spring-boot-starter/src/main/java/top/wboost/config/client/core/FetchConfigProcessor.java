package top.wboost.config.client.core;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import java.util.List;

public interface FetchConfigProcessor {

    /**
     * 注册系统至配置中心
     */
    public void registerClient(ConfigurableEnvironment environment);

    /**
     * 获得配置中心配置
     * @return List<PropertySource<?>>
     */
    public List<PropertySource<?>> fetchConfig();
}
