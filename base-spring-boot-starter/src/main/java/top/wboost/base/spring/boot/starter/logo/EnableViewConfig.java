package top.wboost.base.spring.boot.starter.logo;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用日志导出功能
 * @className EnableLoggerDownload
 * @author jwSun
 * @date 2018年6月22日 上午11:34:01
 * @version 1.0.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ViewController.class)
public @interface EnableViewConfig {

}
