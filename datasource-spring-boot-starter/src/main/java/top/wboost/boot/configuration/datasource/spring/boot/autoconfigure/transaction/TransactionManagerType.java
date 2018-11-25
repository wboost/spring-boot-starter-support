package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.transaction;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

/**
 * @Auther: jwsun
 * @Date: 2018/11/20 18:26
 */
public enum TransactionManagerType {

    /**jdbcTemplate**/
    BASE("org.springframework.jdbc.datasource.DataSourceTransactionManager",(beanFactory,transactionManagerProperties) ->{
        try {
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(Class.forName("org.springframework.jdbc.datasource.DataSourceTransactionManager"));
            beanDefinitionBuilder.addConstructorArgReference(transactionManagerProperties.getDatasource());
            return beanDefinitionBuilder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }),
    /**jpa**/
    JPA("org.springframework.orm.jpa.JpaTransactionManager", (beanFactory,transactionManagerProperties) ->{
        try {
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(Class.forName("org.springframework.orm.jpa.JpaTransactionManager"));
            beanDefinitionBuilder.addConstructorArgReference(transactionManagerProperties.getEntityManagerFactory());
            return beanDefinitionBuilder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }),
    /**hibernate**/
    HIBERNATE("org.springframework.orm.hibernate5.HibernateTransactionManager",(beanFactory,transactionManagerProperties) -> {
        throw new RuntimeException("未实现");
    });

    String dsType;
    InitVoid initVoid;

    TransactionManagerType(String dsType,InitVoid initVoid) {
        this.dsType = dsType;
        this.initVoid  = initVoid;
    }

    public BeanDefinitionBuilder getBuilder(BeanFactory beanFactory,TransactionManagerProperties transactionManagerProperties) {
        return this.initVoid.setProperties(beanFactory,transactionManagerProperties);
    }

    interface InitVoid {
        BeanDefinitionBuilder setProperties(BeanFactory beanFactory,TransactionManagerProperties transactionManagerProperties);
    }
}
