package top.wboost.common.spring.boot.swagger.api;

import java.lang.annotation.*;

/**
 * 返回类型
 * @className ApiResponseDoc
 * @author jwSun
 * @date 2018年5月28日 下午2:40:57
 * @version 1.0.0
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiResponseDoc {

    Class<?> value();

    //String text() default "";
}