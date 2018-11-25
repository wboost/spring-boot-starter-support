package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.jpa;

import lombok.Data;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.transaction.jta.JtaTransactionManager;
import top.wboost.base.spring.boot.starter.Condition.ConditionalHasPropertyPrefix;
import top.wboost.base.spring.boot.starter.CustomerPropertiesTreeUtil;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.GlobalForDataSourceBootStarter;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @Auther: jwsun
 * @Date: 2018/11/20 21:16
 */
@Configuration
@ConditionalHasPropertyPrefix(prefix = GlobalForDataSourceBootStarter.PROPERTIES_JDBC + ".jpa.entityManagerFactory")
public class EntityManagerRegister implements ImportBeanDefinitionRegistrar {

    //private final JpaProperties properties;

    Map<String, EntityManagerFactoryProperties> entityManagerFactoryPropertiesMap;
    JpaVendorAdapter jpaVendorAdapter;
    PersistenceUnitManager persistenceUnitManager;
    private DefaultListableBeanFactory beanFactory;

//    protected EntityManagerRegister(JpaVendorAdapter jpaVendorAdapter,
//                                    ObjectProvider<PersistenceUnitManager> persistenceUnitManager,JpaProperties properties,
//                                    ObjectProvider<JtaTransactionManager> jtaTransactionManager,
//                                    ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
//        this.persistenceUnitManager = persistenceUnitManager.getIfAvailable();
//        this.jpaVendorAdapter = jpaVendorAdapter;
//        this.properties = properties;
//    }

//    protected final JpaProperties getProperties() {
//        return this.properties;
//    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        this.entityManagerFactoryPropertiesMap = CustomerPropertiesTreeUtil.resolvePropertiesTree(
                EntityManagerFactoryProperties.class, GlobalForDataSourceBootStarter.PROPERTIES_JDBC + ".jpa.entityManagerFactory", "entityManagerFactory");
        initConfig();
    }

//    private LocalContainerEntityManagerFactoryBean initEntityManagerFactory(EntityManagerFactoryProperties entityManagerFactoryProperties) {
//        EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(
//                jpaVendorAdapter, properties.getProperties(),
//                persistenceUnitManager);
//        builder.setCallback(null);
//        return builder.dataSource((DataSource) SpringBeanUtil.getBean(entityManagerFactoryProperties.getDatasource())).packages(entityManagerFactoryProperties.getEntityPackages()).build();
//    }

    private void initConfig() {
        entityManagerFactoryPropertiesMap.forEach((name,properties) -> {
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(EntityManagerFactoryFactoryBean.class);
            beanDefinitionBuilder.addPropertyReference("dataSource", properties.getDatasource());
            this.beanFactory.registerBeanDefinition(name,beanDefinitionBuilder.getBeanDefinition());
        });
    }

    @Data
    public static class EntityManagerFactoryFactoryBean implements FactoryBean<LocalContainerEntityManagerFactoryBean> {

        ObjectProvider<PersistenceUnitManager> persistenceUnitManager;
        private JpaProperties properties;
        private JpaVendorAdapter jpaVendorAdapter;
        private DataSource dataSource;
        private EntityManagerFactoryProperties entityManagerFactoryProperties;

        public EntityManagerFactoryFactoryBean(JpaVendorAdapter jpaVendorAdapter,
                                               ObjectProvider<PersistenceUnitManager> persistenceUnitManager,JpaProperties properties,
                                               ObjectProvider<JtaTransactionManager> jtaTransactionManager,
                                               ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
            this.persistenceUnitManager = persistenceUnitManager;
            this.jpaVendorAdapter = jpaVendorAdapter;
            this.properties = properties;
        }

        @Override
        public LocalContainerEntityManagerFactoryBean getObject() throws Exception {
            EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(
                    jpaVendorAdapter, properties.getProperties(),
                    persistenceUnitManager.getIfAvailable());
            builder.setCallback(null);
            return builder.dataSource(dataSource).packages(entityManagerFactoryProperties.getEntityPackages()).build();
        }

        @Override
        public Class<?> getObjectType() {
            return LocalContainerEntityManagerFactoryBean.class;
        }

        @Override
        public boolean isSingleton() {
            return false;
        }
    }

}
