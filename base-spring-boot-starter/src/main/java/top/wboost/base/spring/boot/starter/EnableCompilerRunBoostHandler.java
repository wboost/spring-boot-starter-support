package top.wboost.base.spring.boot.starter;

import java.lang.annotation.*;

/**
 * 禁用数据层
 * @className DisableDataSource
 * @author jwSun
 * @date 2018年6月22日 上午11:34:01
 * @version 1.0.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableCompilerRunBoostHandler {

}
