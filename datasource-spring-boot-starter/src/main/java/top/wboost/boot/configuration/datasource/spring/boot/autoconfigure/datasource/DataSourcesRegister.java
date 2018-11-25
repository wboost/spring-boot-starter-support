package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import org.slf4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;
import top.wboost.base.spring.boot.starter.CustomerPropertiesTreeUtil;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.GlobalForDataSourceBootStarter;
import top.wboost.common.log.util.LoggerUtil;
import top.wboost.common.utils.web.utils.ConvertUtil;
import top.wboost.common.utils.web.utils.SpringBeanRegisterUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.GlobalForDataSourceBootStarter.DATASOURCE_WAPPERS;


/**
 * 多数据源自动注册
 * @Auther: jwsun
 * @Date: 2018/11/20 21:16
 */
public class DataSourcesRegister implements ImportBeanDefinitionRegistrar, Ordered {

    Logger log = LoggerUtil.getLogger(DataSourcesRegister.class);
    private BeanDefinitionRegistry registry;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        this.registry = registry;
        initConfig();
    }

    public void initConfig() {
        Map<String, DataSourceWrapper> dataSources = CustomerPropertiesTreeUtil.resolvePropertiesTree(
                DataSourceWrapper.class, "common.jdbc.datasource", "primary", "url", "username",
                "password");
        BeanDefinitionBuilder dataSourcesMap = BeanDefinitionBuilder.genericBeanDefinition(DataSourcesPropertiesFactpryBean.class);
        dataSourcesMap.addPropertyValue("wrapperMap", dataSources);
        registry.registerBeanDefinition(DATASOURCE_WAPPERS, dataSourcesMap.getBeanDefinition());

        DataSourceWrapper primaryDataSourceWrapper;
        List<Map.Entry<String, DataSourceWrapper>> list = new ArrayList<Map.Entry<String, DataSourceWrapper>>(
                dataSources.entrySet());
        String primarydsName;
        if (dataSources.containsKey("primary")) {
            primaryDataSourceWrapper = dataSources.get("primary");
            primarydsName = "primary";
        } else {
            primaryDataSourceWrapper = list.get(0).getValue();
            primarydsName = list.get(0).getKey();
        }
        AbstractBeanDefinition beanDefinition = SpringBeanRegisterUtil.initBeanDefinition(DruidDataSource.class,
                ConvertUtil.beanConvertToMap(primaryDataSourceWrapper));
        beanDefinition.setPrimary(true);
        registry.registerBeanDefinition(GlobalForDataSourceBootStarter.PRIMARY_DATASOURCE_NAME, beanDefinition);
        registry.registerAlias(GlobalForDataSourceBootStarter.PRIMARY_DATASOURCE_NAME, primarydsName);
        registry.registerAlias(GlobalForDataSourceBootStarter.PRIMARY_DATASOURCE_NAME, "dataSource");
        if (log.isInfoEnabled()) {
            log.info("registerSingleton primary dataSource bean: {}", primarydsName);
        }
        for (int i = 0; i < list.size(); i++) {
            Map.Entry<String, DataSourceWrapper> entry = list.get(i);
            String dsBeanName = entry.getKey();
            if (primarydsName.equals(dsBeanName)) {
                continue;
            }
            DataSourceWrapper dsConfig = entry.getValue();
            SpringBeanRegisterUtil.registerBeanDefinition(dsBeanName, DruidDataSource.class,
                    ConvertUtil.beanConvertToMap(dsConfig), (ConfigurableListableBeanFactory) registry);
            if (log.isInfoEnabled()) {
                log.info("registerSingleton dataSource bean: {}", dsBeanName);
            }
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

    public static class DataSourcesPropertiesFactpryBean implements FactoryBean<Map<String, DataSourceWrapper>> {

        private Map<String, DataSourceWrapper> wrapperMap;

        public void setWrapperMap(Map<String, DataSourceWrapper> wrapperMap) {
            this.wrapperMap = wrapperMap;
        }

        @Override
        public Map<String, DataSourceWrapper> getObject() throws Exception {
            return this.wrapperMap;
        }

        @Override
        public Class<?> getObjectType() {
            return Map.class;
        }

        @Override
        public boolean isSingleton() {
            return false;
        }
    }
}
