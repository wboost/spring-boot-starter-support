
package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.transaction;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.GlobalForDataSourceBootStarter;

/**
 * 注册所有配置的Druid数据库连接池
 * <pre>
 * application.yml文件配置
 * common.datasource.primary：主数据库连接池，若没有此属性则随机在所有连接池配置中随机选择一个为主连接池
 * common.datasource下配置为共用配置，如下例子,表示为主与d1连接类型为oracle,d2为mysql
 * 如下例子,则会注册3个连接池bean beanId分别为dataSource&primaryDataSource,d1,d2
 * common: 
     datasource: 
       driver-class-name: oracle.jdbc.driver.OracleDriver
       primary: 
         url: jdbc:oracle:thin:@192.168.1.1:1521:orcl
         username: usernameprimary
         password: passwordprimary
       d1: 
         url: jdbc:oracle:thin:@192.168.1.1:1521:orcl
         username: usernamed1
         password: passwordd1
       d2: 
         url: jdbc:mysql://localhost:3306/example
         username: mysqlName
         password: mysqlPwd
         driver-class-name: com.mysql.jdbc.Driver
 * </pre>
 * @className DataSourceWrapper
 * @author jwSun
 * @date 2018年4月14日 下午11:58:10
 * @version 1.0.0
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
