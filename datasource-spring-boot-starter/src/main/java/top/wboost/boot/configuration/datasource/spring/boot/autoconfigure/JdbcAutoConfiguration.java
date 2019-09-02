package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import top.wboost.base.spring.boot.starter.Condition.ConditionalHasPropertyPrefix;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.datasource.DataSourceWrapper;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.datasource.DataSourcesRegister;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.jpa.EntityManagerFactoryProperties;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.jpa.EntityManagerRegister;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.transaction.TransactionsRegister;

import static top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.GlobalForDataSourceBootStarter.PROPERTIES_JDBC;

/**
 * 多数据源连接池/事物管理器自动配置
 * @Auther: jwsun
 * @Date: 2018/11/20 20:57
 */
@Configuration
@AutoConfigureBefore({
        DruidDataSourceAutoConfigure.class,
        DataSourceAutoConfiguration.class,
        TransactionAutoConfiguration.class})
//@EnableConfigurationProperties(JdbcProperties.class)
public class JdbcAutoConfiguration {

    @Configuration
    @ConditionalOnClass(PlatformTransactionManager.class)
    @ConditionalHasPropertyPrefix(prefix=PROPERTIES_JDBC + ".transaction")
    @Import(TransactionsRegister.class)
    static class TransactionsAutoConfiguration {

    }

    @Configuration
    @ConditionalHasPropertyPrefix(prefix=PROPERTIES_JDBC + ".datasource")
    @Import(DataSourcesRegister.class)
    @EnableConfigurationProperties(DataSourceWrapper.class)
    static class DataSourcesAutoConfiguration {
        public DataSourcesAutoConfiguration() {

        }
    }

    @Configuration
    @ConditionalHasPropertyPrefix(prefix = PROPERTIES_JDBC + ".jpa.entityManagerFactory")
    @Import(EntityManagerRegister.class)
    @EnableConfigurationProperties(EntityManagerFactoryProperties.class)
    static class EntityManagerRegisterConfiguration {
        public EntityManagerRegisterConfiguration() {

        }
    }




}
