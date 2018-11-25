package top.wboost.base.spring.boot.starter.Condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * 有配置前缀条件
 * @Auther: jwsun
 * @Date: 2018/11/21 14:03
 */
public @Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(HasPropertyPrefixCondition.class)
@interface ConditionalHasPropertyPrefix {

    String prefix();

}