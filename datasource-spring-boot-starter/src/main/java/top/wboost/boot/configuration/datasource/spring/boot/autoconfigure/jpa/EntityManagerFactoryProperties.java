
package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.jpa;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.GlobalForDataSourceBootStarter;

/**
 * JDBC所有配置项
 * <pre>
 * common:
 *   jdbc:
 *     jpa:
 *       entityManagerFactory:
 *         entityManagerFactory:
 *           entity-packages: com.chinaoly.primary
 *           datasource: primary
 *           name: entityManagerFactory
 *         entityManagerFactorySec:
 *           entity-packages: com.chinaoly.sec
 *           datasource: sec
 *           name: entityManagerFactorySec
 * </pre>
 */
@Data
@ConfigurationProperties(GlobalForDataSourceBootStarter.PROPERTIES_JDBC + ".jpa.entityManagerFactory")
public class EntityManagerFactoryProperties {

    /**repository-package*/
    //private String repositoryPackages;
    /** 数据库连接池 **/
    private String datasource;
    /** 实体类包 **/
    private String entityPackages;
    /**entityManagerFactory名称**/
    private String name;

}
