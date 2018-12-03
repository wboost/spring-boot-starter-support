package top.wboost.config.client.core;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用配置中心功能
 * @className EnableConfigClient
 * @author jwSun
 * @date 2018年6月22日 上午11:34:01
 * @version 1.0.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ConfigClientRegister.class)
public @interface EnableConfigClient {

}