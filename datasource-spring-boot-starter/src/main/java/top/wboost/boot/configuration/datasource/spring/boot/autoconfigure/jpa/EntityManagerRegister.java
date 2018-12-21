package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.jpa;

import lombok.Data;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.*;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import top.wboost.base.spring.boot.starter.CustomerPropertiesTreeUtil;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.GlobalForDataSourceBootStarter;
import top.wboost.common.log.util.LoggerUtil;
import top.wboost.common.util.StringUtil;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Map;

/**
 * @Auther: jwsun
 * @Date: 2018/11/20 21:16
 */
public class EntityManagerRegister implements ImportBeanDefinitionRegistrar {

    Map<String, EntityManagerFactoryProperties> entityManagerFactoryPropertiesMap;
    private BeanDefinitionRegistry registry;
    private Logger logger = LoggerUtil.getLogger(getClass());

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        this.registry = registry;
        this.entityManagerFactoryPropertiesMap = CustomerPropertiesTreeUtil.resolvePropertiesTree(
                EntityManagerFactoryProperties.class, GlobalForDataSourceBootStarter.PROPERTIES_JDBC + ".jpa.entityManagerFactory", "entityManagerFactory");

        registry.registerBeanDefinition(EntityManagerBeanDefinitionRegistrarPostProcessor.class.getName(), new RootBeanDefinition(EntityManagerBeanDefinitionRegistrarPostProcessor.class));
        initConfig();
    }

    private void initConfig() {
        entityManagerFactoryPropertiesMap.forEach((name,properties) -> {
            if (!StringUtil.notEmpty(properties.getName())) {
                properties.setName(name);
            }
            logger.info("register EntityManagerFactory: {} for packages: {} and datasource {}", name, properties.getEntityPackages(), properties.getDatasource());
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(EntityManagerFactoryFactoryBean.class);
            beanDefinitionBuilder.addPropertyReference("dataSource", properties.getDatasource());
            beanDefinitionBuilder.addPropertyValue("entityManagerFactoryProperties", properties);
            AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
            if (name.equals("entityManagerFactory")) {
                beanDefinition.setPrimary(true);
            }
            this.registry.registerBeanDefinition(name, beanDefinitionBuilder.getBeanDefinition());
        });
    }

    @Data
    public static class EntityManagerFactoryFactoryBean implements FactoryBean<EntityManagerFactory>, BeanFactoryAware {

        private DataSource dataSource;
        private EntityManagerFactoryProperties entityManagerFactoryProperties;
        private EntityManagerFactoryBuilder entityManagerFactoryBuilder;
        private BeanFactory beanFactory;
        private EntityManagerFactory entityManagerFactory;

        public EntityManagerFactoryFactoryBean() {
        }

        @Override
        public EntityManagerFactory getObject() throws Exception {
            return entityManagerFactory;
        }

        @Override
        public Class<?> getObjectType() {
            return EntityManagerFactory.class;
        }

        @Override
        public boolean isSingleton() {
            return false;
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
            this.entityManagerFactoryBuilder = beanFactory.getBean(EntityManagerFactoryBuilder.class);
            LocalContainerEntityManagerFactoryBean build = entityManagerFactoryBuilder.dataSource(dataSource).packages(entityManagerFactoryProperties.getEntityPackages()).build();
            String beanName = entityManagerFactoryProperties.getName() + "FACTORY";
            ((DefaultListableBeanFactory) beanFactory).registerSingleton(beanName, build);
            ((DefaultListableBeanFactory) beanFactory).initializeBean(build, beanName);
            this.entityManagerFactory = (EntityManagerFactory) beanFactory.getBean(entityManagerFactoryProperties.getName() + "FACTORY");
        }
    }

}
