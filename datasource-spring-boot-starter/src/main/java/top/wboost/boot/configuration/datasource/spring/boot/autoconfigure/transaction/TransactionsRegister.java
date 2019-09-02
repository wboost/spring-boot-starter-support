package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.transaction;

import lombok.Data;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;
import top.wboost.base.spring.boot.starter.CustomerPropertiesTreeUtil;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.GlobalForDataSourceBootStarter;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.util.TransactionBeanNameGeneratorUtil;
import top.wboost.common.log.util.LoggerUtil;
import top.wboost.common.system.code.SystemCode;
import top.wboost.common.system.exception.SystemCodeException;
import top.wboost.common.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.GlobalForDataSourceBootStarter.PRIMARYT_TRANSACTION_MANAGER;

/**
 * @Auther: jwsun
 * @Date: 2018/11/20 21:16
 */
public class TransactionsRegister implements ImportBeanDefinitionRegistrar, BeanFactoryAware, Ordered {


    /**解析配置 key:dsName,val:props**/
    Map<String, List<TransactionManagerProperties>> transactionManagerPropertiesUse = new HashMap<>();
    private TransactionManagerCustomizers transactionManagerCustomizers;
    //private BeanDefinitionRegistry registry;
    private DefaultListableBeanFactory beanFactory;
    //private Map<String, DataSourceWrapper> dataSourceWrapperMap;
    private Logger log = LoggerUtil.getLogger(TransactionsRegister.class);

    public TransactionsRegister() {
        //this.transactionManagerCustomizers = transactionManagerCustomizers.getIfAvailable();
    }

    public TransactionsRegister(ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
        this.transactionManagerCustomizers = transactionManagerCustomizers.getIfAvailable();
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, TransactionManagerProperties> transactionManagerPropertiesMap = CustomerPropertiesTreeUtil.resolvePropertiesTree(
                TransactionManagerProperties.class, "common.jdbc.transaction", "primary", "datasource", "transactionManager");
        transactionManagerPropertiesMap.forEach((name,val) -> {
            String datasource = val.getDatasource();
            if (StringUtil.notEmpty(datasource)) {
                if (!transactionManagerPropertiesUse.containsKey(datasource)) {
                    transactionManagerPropertiesUse.put(datasource,new ArrayList<>() );
                }
                transactionManagerPropertiesUse.get(datasource).add(val);
            } else {
                throw new PropertiesNotFindException("datasource",name);
            }
        });
        initConfig();
    }

    private void initConfig() {
        List<TransactionManagerDto> transactionManagerDtos = new ArrayList<>();
        transactionManagerPropertiesUse.forEach((datasource,props) -> {
            transactionManagerDtos.addAll(initPlatformTransactionManager(props));
        });
        TransactionManagerDto primaryTransactionManager = null;
        for(int i = 0;i<transactionManagerDtos.size();i++) {
            TransactionManagerDto transactionManagerDto = transactionManagerDtos.get(i);
            if (this.transactionManagerCustomizers != null) {
                //this.transactionManagerCustomizers.customize(transactionManagerDto.platformTransactionManager);
            }
            if (primaryTransactionManager == null && transactionManagerDto.dsName.equals(GlobalForDataSourceBootStarter.PRIMARY_DATASOURCE_NAME) || transactionManagerDto.dsName.equals("primary")) {
                primaryTransactionManager = transactionManagerDto;
            }
        }
        if (primaryTransactionManager == null) {
            primaryTransactionManager = transactionManagerDtos.get(0);
        }
        primaryTransactionManager.getBeanDefinition().setPrimary(true);
        for(TransactionManagerDto transactionManagerDto : transactionManagerDtos) {
            this.beanFactory.registerBeanDefinition(transactionManagerDto.beanName, transactionManagerDto.beanDefinition);
            log.info("register transactionManager: {}, for datasource: {}", transactionManagerDto.beanName,
                    transactionManagerDto.dsName);
        }
        this.beanFactory.registerAlias( primaryTransactionManager.beanName, PRIMARYT_TRANSACTION_MANAGER);
        log.info("alias primary transactionManager: {} for {}. datasource is {}", PRIMARYT_TRANSACTION_MANAGER,
                primaryTransactionManager.beanName,primaryTransactionManager.dsName);
    }

    private List<TransactionManagerDto> initPlatformTransactionManager(List<TransactionManagerProperties> transactionManagerProperties) {
        List<TransactionManagerDto> transactionManagerDtos = new ArrayList<>();
        transactionManagerProperties.forEach(prop -> {
            BeanDefinitionBuilder builder = prop.getTransactionManagerType().getBuilder(beanFactory, prop);
            String transactionManagerName = prop.getTransactionManagerName();
            String beanName;
            if (StringUtil.notEmpty(transactionManagerName)) {
                beanName = transactionManagerName;
            } else {
                beanName = TransactionBeanNameGeneratorUtil.generatorTransactionManagerName(prop.getDatasource());
            }
            transactionManagerDtos.add(new TransactionManagerDto(beanName,prop.getDatasource(), this.beanFactory,builder.getBeanDefinition()));
        });
        return transactionManagerDtos;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Data
    static class TransactionManagerDto {
        private String beanName;
        private String dsName;
        private String[] dsAliasNames;
        private BeanDefinition beanDefinition;
        //private PlatformTransactionManager platformTransactionManager;
        TransactionManagerDto(String beanName,String dsName,BeanFactory beanFactory,BeanDefinition beanDefinition) {
            this.beanName = beanName;
            this.dsAliasNames = beanFactory.getAliases(dsName);
            this.dsName = dsName;
            this.beanDefinition = beanDefinition;
            //this.platformTransactionManager = (PlatformTransactionManager) beanFactory.getBean(beanName);
        }
    }

    class TransactionPropertiesNotFindException extends SystemCodeException {

        TransactionPropertiesNotFindException(String dsName) {
            super(SystemCode.ERROR, " datasource: " + dsName + ", cant find!");
        }

    }

    class PropertiesNotFindException extends SystemCodeException {

        PropertiesNotFindException(String name,String configName) {
            super(SystemCode.ERROR, "transaction config: " + configName + " check error! Properties:" + name + " should be not empty!");
        }

    }
}
