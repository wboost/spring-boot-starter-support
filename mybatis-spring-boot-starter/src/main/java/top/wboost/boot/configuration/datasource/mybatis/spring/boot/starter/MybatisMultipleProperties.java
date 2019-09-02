package top.wboost.boot.configuration.datasource.mybatis.spring.boot.starter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.wboost.base.spring.boot.starter.GlobalForSpringBootStarter;

import java.util.Map;

/**
 * @Auther: jwsun
 * @Date: 2019/7/9 14:01
 */
@Data
@ConfigurationProperties("common")
public class MybatisMultipleProperties {

    Map<String,MybatisMultiplePropertiesOne> mybatis;

}
