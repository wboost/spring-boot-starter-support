
package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.jpa;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.GlobalForDataSourceBootStarter;

/**
 * @className EntityManagerFactoryProperties
 * @author jwSun
 * @date 2018年4月14日 下午11:58:10
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(GlobalForDataSourceBootStarter.PROPERTIES_JDBC + ".jpa.entityManagerFactory.entityManagerFactory")
public class EntityManagerFactoryPropertiesShow {

    /**repository-package*/
    //private String repositoryPackages;
    /** 数据库连接池 **/
    private String datasource;
    /** 实体类包 **/
    private String entityPackages;
    /**entityManagerFactory名称**/
    private String name;

}
