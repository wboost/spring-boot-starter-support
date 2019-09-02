package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Auther: jwsun
 * @Date: 2018/11/21 11:31
 */
@Data
@ConfigurationProperties(GlobalForDataSourceBootStarter.PROPERTIES_JDBC)
public class JdbcConfigurationProperties {

}
