package top.wboost.config.client.core;

import org.springframework.context.annotation.Import;
import top.wboost.config.client.core.controller.ConfigClientPropertiesController;
import top.wboost.config.client.core.controller.ConfigClientSysController;

/**
 * 注册所需各个组件
 * @Auther: jwsun
 * @Date: 2018/11/30 11:54
 */
@Import({
        ConfigClientSysController.class,
        ConfigClientPropertiesController.class,
        ClientBeanRegister.class})
public class ConfigClientRegister {

    public static final String WEB_PREFIX = "/config/client/sys";

    public static final String REQUEST_END = "end...";
}
