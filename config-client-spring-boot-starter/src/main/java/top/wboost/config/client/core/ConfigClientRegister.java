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
        ConfigClientPropertiesController.class})
public class ConfigClientRegister {
}