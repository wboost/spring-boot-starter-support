
package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.datasource;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.GlobalForDataSourceBootStarter;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.jpa.EntityManagerFactoryProperties;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.transaction.TransactionManagerProperties;

import java.util.Map;

/**
 * JDBC所有配置项
 * <pre>
 * common:
 *   jdbc:
 *     datasource:
 *       primary:
 *         driver-class-name: oracle.jdbc.driver.OracleDriver
 *         url: jdbc:oracle:thin:@192.168.1.225:1521:orcl
 *         username:
 *         password:
 *         max-active: 100
 *       sec:
 *         driver-class-name: com.cloudera.impala.jdbc41.Driver
 *         url: jdbc:impala://192.168.52.32:21050/trajx
 *         username:
 *         password:
 *     transaction:
 *       primary:
 *         entity-manager-factory: entityManagerFactory
 *         datasource: primary
 *       sec:
 *         entity-manager-factory: entityManagerFactorySec
 *         datasource: sec
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
@ConfigurationProperties(GlobalForDataSourceBootStarter.PROPERTIES_JDBC)
public class JdbcProperties {

    private Map<String,DataSourceWrapper> datasource;
    private Map<String, TransactionManagerProperties> transaction;
    private Map<String, EntityManagerFactoryProperties> jpa;

}
