package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.transaction;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.DataSourceMultipleAutoConfiguration;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.GlobalForDataSourceBootStarter;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.util.TransactionBeanNameGeneratorUtil;
import top.wboost.common.log.entity.Logger;
import top.wboost.common.log.util.LoggerUtil;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 多数据源TransactionManager自动初始化
 * @className DataSourceTransactionManagerAutoConfiguration
 * @author jwSun
 * @date 2018年4月20日 下午3:15:04
 * @version 1.0.0
 */
@Configuration
@ConditionalOnClass({ JdbcTemplate.class, PlatformTransactionManager.class })
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE - 1)
@AutoConfigureBefore(org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class)
@AutoConfigureAfter(DataSourceMultipleAutoConfiguration.class)
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceMultipleTransactionManagerAutoConfiguration implements Ordered {

    public final static String PRIMARYT_RANSACTION_MANAGER= "transactionManager";

    @Override
    public int getOrder() {
        // AnnotationAwareAspectJAutoProxyCreator类将会初始化所有Advisor,包含使用EnableTransactionInterceptors注解生成的拦截类，此拦截类
        return Ordered.LOWEST_PRECEDENCE + 1;
    }

    @Configuration
    static class DataSourceTransactionManagerConfiguration {

        private final TransactionManagerCustomizers transactionManagerCustomizers;
        private Logger log = LoggerUtil.getLogger(DataSourceTransactionManagerConfiguration.class);
        @Autowired
        private DefaultListableBeanFactory beanFactory;

        DataSourceTransactionManagerConfiguration(
                ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
            this.transactionManagerCustomizers = transactionManagerCustomizers.getIfAvailable();
        }

        @Bean
        @Primary
        @ConditionalOnMissingBean(PlatformTransactionManager.class)
        public DataSourceTransactionManager transactionManager() {
            String[] dataSources = this.beanFactory.getBeanNamesForType(DataSource.class, true, false);
            DataSourceTransactionManager primaryTransactionManager = null;
            String primaryDsName = null;
            if (dataSources.length == 1) {
                primaryDsName = dataSources[0];
                primaryTransactionManager = new DataSourceTransactionManager(
                        this.beanFactory.getBean(dataSources[0], DataSource.class));
            } else {
                Set<String> set = new HashSet<>(Arrays.asList(dataSources));
                if (set.contains(GlobalForDataSourceBootStarter.PRIMARY_DATASOURCE_NAME)) {
                    primaryDsName = GlobalForDataSourceBootStarter.PRIMARY_DATASOURCE_NAME;
                    primaryTransactionManager = new DataSourceTransactionManager(this.beanFactory
                            .getBean(GlobalForDataSourceBootStarter.PRIMARY_DATASOURCE_NAME, DataSource.class));
                    log.info("register primary transactionManager: {} for {}", PRIMARYT_RANSACTION_MANAGER,
                            GlobalForDataSourceBootStarter.PRIMARY_DATASOURCE_NAME);
                    set.remove(GlobalForDataSourceBootStarter.PRIMARY_DATASOURCE_NAME);
                }
                for (String dsName : set) {
                    if (primaryTransactionManager == null) {
                        primaryTransactionManager = new DataSourceTransactionManager(
                                this.beanFactory.getBean(dsName, DataSource.class));
                        this.beanFactory.registerSingleton("transactionManager",
                                primaryTransactionManager);
                        primaryDsName = dsName;
                        log.info("register primary transactionManager: {} for {}", PRIMARYT_RANSACTION_MANAGER, dsName);
                    } else {
                        this.beanFactory.registerSingleton(TransactionBeanNameGeneratorUtil.generatorTransactionManagerName(dsName),
                                new DataSourceTransactionManager(this.beanFactory.getBean(dsName, DataSource.class)));
                        log.info("register transactionManager: {} for {}", TransactionBeanNameGeneratorUtil.generatorTransactionManagerName(dsName), dsName);
                    }
                }
            }
            if (this.transactionManagerCustomizers != null) {
                this.transactionManagerCustomizers.customize(primaryTransactionManager);
            }
            this.beanFactory.registerAlias("transactionManager", TransactionBeanNameGeneratorUtil.generatorTransactionManagerName(primaryDsName));
            this.beanFactory.registerAlias("transactionManager", TransactionBeanNameGeneratorUtil.generatorTransactionManagerName("primary"));
            return primaryTransactionManager;
        }

    }

}