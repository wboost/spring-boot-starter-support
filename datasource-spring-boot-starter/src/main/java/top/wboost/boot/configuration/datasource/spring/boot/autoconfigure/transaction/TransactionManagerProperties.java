
package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.transaction;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.GlobalForDataSourceBootStarter;

/**
 * JDBC所有配置项
 * <pre>
 * common:
 *   jdbc:
 *     transaction:
 *       primary:
 *         entity-manager-factory: entityManagerFactory
 *         datasource: primary
 *       sec:
 *         entity-manager-factory: entityManagerFactorySec
 *         datasource: sec
 * </pre>
 */
@Data
@ConfigurationProperties(GlobalForDataSourceBootStarter.PROPERTIES_JDBC + ".transaction")
public class TransactionManagerProperties {

    /**设置连接池名*/
    private String datasource;
    /**生成事物管理器名**/
    private String transactionManagerName;
    /**事物管理器类型 BASE/JPA/HIBERNATE **/
    private TransactionManagerType transactionManagerType = TransactionManagerType.JPA;
    /**事物管理器类型 优先级大于TransactionManagerType **/
    private String transactionManagerClass;
    /**entityManagerFactory**/
    private String entityManagerFactory;

}
