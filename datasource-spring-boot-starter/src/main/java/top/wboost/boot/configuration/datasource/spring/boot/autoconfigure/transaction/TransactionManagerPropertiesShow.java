
package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.transaction;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.GlobalForDataSourceBootStarter;

/**
 * IDE提示,无用
 * @author jwSun
 * @date 2019/1/7
 */
@Data
@ConfigurationProperties(GlobalForDataSourceBootStarter.PROPERTIES_JDBC + ".transaction.primary")
public class TransactionManagerPropertiesShow {

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
