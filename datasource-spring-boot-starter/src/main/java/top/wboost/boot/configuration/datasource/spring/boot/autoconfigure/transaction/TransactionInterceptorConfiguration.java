package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.transaction;

import lombok.Data;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.*;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.interceptor.*;
import org.springframework.util.StringUtils;
import top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.util.TransactionBeanNameGeneratorUtil;
import top.wboost.common.boot.util.SpringBootUtil;
import top.wboost.common.log.util.LoggerUtil;
import top.wboost.common.system.code.SystemCode;
import top.wboost.common.system.exception.SystemCodeException;
import top.wboost.common.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 注册事物管理拦截器，创建全局拦截,与tx:method标签相同功能
 */
@Configuration
@ConditionalOnClass(DataSourceTransactionManager.class)
@AutoConfigureAfter(DataSourceMultipleTransactionManagerAutoConfiguration.class)
public class TransactionInterceptorConfiguration implements ImportBeanDefinitionRegistrar {

    BeanFactoryPointcutAdvisorConfiguration beanFactoryPointcutAdvisorConfiguration = new BeanFactoryPointcutAdvisorConfiguration();
    private TransactionConfig transactionConfig;
    private Logger logger = LoggerUtil.getLogger(getClass());
    private BeanDefinitionRegistry registry;
    private BeanNameGenerator beanNameGenerator;

//    public void setImportMetadata(AnnotationMetadata importMetadata) {
//        this.enableTx = AnnotationAttributes.fromMap(
//                importMetadata.getAnnotationAttributes(EnableTransactionInterceptors.class.getName(), false));
//        if (this.enableTx == null) {
//            throw new IllegalArgumentException(
//                    "@EnableTransactionInterceptors is not present on importing class " + importMetadata.getClassName());
//        }
//    }

//    @Bean
//    public Advisor transactionInterceptorMarkerAdvisor (){
//        return (Advisor)SpringBeanUtil.getBean(getTransactionConfig().getAdvisorBeanName());
//    }

    private synchronized TransactionConfig getTransactionConfig(BeanDefinitionRegistry registry) {
        this.registry = registry;
        if (transactionConfig == null) {
            logger.info("transactionInterceptorInit please wait ^-^");
            transactionInterceptorInit();
        }
        return this.transactionConfig;
    }

    public void transactionInterceptorInit() {
        Class<?> launcherClass = SpringBootUtil.getLauncherClass();
        EnableTransactionInterceptors enableTransactionInterceptor = AnnotationUtils.getAnnotation(launcherClass, EnableTransactionInterceptors.class);
        this.transactionConfig = new TransactionConfig();
        this.transactionConfig.setEnableTransactionInterceptors(enableTransactionInterceptor);
        EnableTransactionInterceptors.Config[] transactionInterceptorConfigs = enableTransactionInterceptor.value();
        for (EnableTransactionInterceptors.Config transactionInterceptorConfig : transactionInterceptorConfigs) {
            // 创建TransactionInterceptor对象并存入事物管理器映射及指定方法切面
            BeanDefinition beanDefinition = parseTransactionInterceptorConfig(transactionInterceptorConfig);
            this.transactionConfig.getTransactionInterceptors().add(beanDefinition );
            // 注册至容器
            String transactionInterceptorBeanName = generatorTransactionInterceptorBeanName(transactionInterceptorConfig);
            registry.registerBeanDefinition(transactionInterceptorBeanName, beanDefinition);
            logger.info("register TransactionInterceptor: {} for TransactionManager: {}" , transactionInterceptorBeanName,transactionInterceptorConfig.transactionManagerRef());

            // 解析切面方法，解析express表达式
            for(EnableTransactionInterceptors.Config.PointCutConfig pointcutConfig : transactionInterceptorConfig.pointCutConfigs()) {
                AbstractBeanDefinition advisorBeanDefinition = beanFactoryPointcutAdvisorConfiguration.createAdvisorBeanDefinition(transactionInterceptorBeanName,pointcutConfig);
                AbstractBeanDefinition pointcutDefinition = beanFactoryPointcutAdvisorConfiguration.createPointcutDefinition(pointcutConfig.expression());
                advisorBeanDefinition.getPropertyValues().add("pointcut", pointcutDefinition);
                String name = beanNameGenerator.generateBeanName(advisorBeanDefinition, registry);
                registry.registerBeanDefinition(name, advisorBeanDefinition);
                logger.info("register PointcutAdvisor: {} for Config: {}" ,name,transactionInterceptorBeanName);
            }
        }
    }

    private String generatorTransactionInterceptorBeanName(EnableTransactionInterceptors.Config transactionInterceptorConfig) {
        String transactionInterceptorBeanName = transactionInterceptorConfig.name();
        if (!StringUtil.notEmpty(transactionInterceptorBeanName)) {
            transactionInterceptorBeanName = "transactionInterceptor#" + DataSourceMultipleTransactionManagerAutoConfiguration.PRIMARYT_RANSACTION_MANAGER;
        }
        return transactionInterceptorBeanName;
    }

    /*@Bean(name = TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisor() {
        BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
        advisor.setTransactionAttributeSource(transactionAttributeSource());
        advisor.setAdvice(transactionInterceptor());
        advisor.setOrder(this.enableTx.<Integer>getNumber("order"));
        return advisor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public TransactionAttributeSource transactionAttributeSource() {
        TransactionAttributeSource primaryTransactionAttributeSource = getTransactionConfig().getPrimaryTransactionAttributeSource();
        if (primaryTransactionAttributeSource instanceof NameMatchTransactionAttributeSource &&  getTransactionConfig().getEnableTransactionInterceptors().autoAnnotationTransaction()) {
            List<TransactionAttributeSource> transactionAttributeSources = Arrays.asList(getTransactionConfig().getPrimaryTransactionAttributeSource(), new AnnotationTransactionAttributeSource());
            return new CompositeTransactionAttributeSource(transactionAttributeSources.toArray(new TransactionAttributeSource[0]));
        }
        return getTransactionConfig().getPrimaryTransactionAttributeSource();
    }*/

//    @Bean
//    public TransactionInterceptor transactionInterceptor() {
//        return getTransactionConfig().getPrimaryTransactionInterceptor();
//    }

    private BeanDefinition parseTransactionInterceptorConfig(EnableTransactionInterceptors.Config transactionInterceptorConfig) {
        // 创建事物拦截器
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
        builder.getRawBeanDefinition().setBeanClass(TransactionInterceptor.class);
        // 获得关联的事物管理器
//        if (!registry.containsBeanDefinition(transactionInterceptorConfig.transactionManagerRef())) {
//            throw new TransactionInterceptorConfigurationError("PlatformTransactionManager " + transactionInterceptorConfig.transactionManagerRef() + " cant find!,please check.");
//        }
        String transactionManagerRef = transactionInterceptorConfig.transactionManagerRef();
        if (!StringUtil.notEmpty(transactionManagerRef)) {
            String datasourceRef = transactionInterceptorConfig.datasourceRef();
            if (StringUtil.notEmpty(datasourceRef)) {
                transactionManagerRef = TransactionBeanNameGeneratorUtil.generatorTransactionManagerName(datasourceRef);
            } else {
                transactionManagerRef = DataSourceMultipleTransactionManagerAutoConfiguration.PRIMARYT_RANSACTION_MANAGER;
            }
        }
        logger.info("transactionInterceptorConfig {} use transactionManagerRef {}",generatorTransactionInterceptorBeanName(transactionInterceptorConfig), transactionManagerRef);
        builder.addPropertyReference("transactionManager", transactionManagerRef);
        //创建事物属性对象
        RootBeanDefinition attributeSource = parseAttributeSource(transactionInterceptorConfig.attributes());
        if (attributeSource == null) {
            builder.addPropertyValue("transactionAttributeSource",
                    new RootBeanDefinition("org.springframework.transaction.annotation.AnnotationTransactionAttributeSource"));
        } else {
            builder.addPropertyValue("transactionAttributeSource", parseAttributeSource(transactionInterceptorConfig.attributes()));
        }
        return builder.getBeanDefinition();
    }

    private RootBeanDefinition parseAttributeSource(EnableTransactionInterceptors.Config.Attribute[] attributes) {
        if (attributes.length > 1) {
            throw new TransactionInterceptorConfigurationError("Element <attributes> is allowed at most once inside element <advice>");
        }else if (attributes.length == 1) {
            // Using attributes source.
            EnableTransactionInterceptors.Config.Attribute attributeEle = attributes[0];
            List<EnableTransactionInterceptors.Config.Attribute.Method> methods = Arrays.asList(attributeEle.value());
            ManagedMap<TypedStringValue, RuleBasedTransactionAttribute> transactionAttributeMap =
                new ManagedMap<>(methods.size());
            for (EnableTransactionInterceptors.Config.Attribute.Method methodEle : methods) {
                String name = methodEle.name();
                TypedStringValue nameHolder = new TypedStringValue(name);
                RuleBasedTransactionAttribute attribute = new RuleBasedTransactionAttribute();
                String propagation = methodEle.propagation().toString();
                String isolation = methodEle.isolation().toString();
                String timeout = String.valueOf(methodEle.timeout());
                String readOnly = String.valueOf(methodEle.readOnly());
                if (StringUtils.hasText(propagation)) {
                    attribute.setPropagationBehaviorName(RuleBasedTransactionAttribute.PREFIX_PROPAGATION + propagation);
                }
                if (StringUtils.hasText(isolation)) {
                    attribute.setIsolationLevelName(RuleBasedTransactionAttribute.PREFIX_ISOLATION + isolation);
                }
                if (StringUtils.hasText(timeout)) {
                    try {
                        attribute.setTimeout(Integer.parseInt(timeout));
                    }
                    catch (NumberFormatException ex) {
                        throw new TransactionInterceptorConfigurationError("Timeout must be an integer value: [" + timeout + "]", ex);
                    }
                }
                if (StringUtils.hasText(readOnly)) {
                    attribute.setReadOnly(methodEle.readOnly());
                }

                List<RollbackRuleAttribute> rollbackRules = new LinkedList<>();
                if (StringUtil.notEmpty(methodEle.rollbackFor())) {
                    String rollbackForValue = methodEle.rollbackFor();
                    addRollbackRuleAttributesTo(rollbackRules,rollbackForValue);
                }
                if (StringUtil.notEmpty(methodEle.noRollbackFor())) {
                    String noRollbackForValue = methodEle.noRollbackFor();
                    addNoRollbackRuleAttributesTo(rollbackRules,noRollbackForValue);
                }
                attribute.setRollbackRules(rollbackRules);
                transactionAttributeMap.put(nameHolder, attribute);
            }

            RootBeanDefinition attributeSourceDefinition = new RootBeanDefinition(NameMatchTransactionAttributeSource.class);
            attributeSourceDefinition.getPropertyValues().add("nameMap", transactionAttributeMap);
            return attributeSourceDefinition;
        } else {
            // Assume annotations source.
            return null;
        }

    }

    private void addRollbackRuleAttributesTo(List<RollbackRuleAttribute> rollbackRules, String rollbackForValue) {
        String[] exceptionTypeNames = StringUtils.commaDelimitedListToStringArray(rollbackForValue);
        for (String typeName : exceptionTypeNames) {
            rollbackRules.add(new RollbackRuleAttribute(StringUtils.trimWhitespace(typeName)));
        }
    }

    private void addNoRollbackRuleAttributesTo(List<RollbackRuleAttribute> rollbackRules, String noRollbackForValue) {
        String[] exceptionTypeNames = StringUtils.commaDelimitedListToStringArray(noRollbackForValue);
        for (String typeName : exceptionTypeNames) {
            rollbackRules.add(new NoRollbackRuleAttribute(StringUtils.trimWhitespace(typeName)));
        }
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        this.beanNameGenerator = (BeanNameGenerator) ((DefaultListableBeanFactory) registry).getBean("configAnnotationBeanNameGenerator");
        getTransactionConfig(registry);
    }


    @Data
    class TransactionConfig {
        //private TransactionAttributeSource primaryTransactionAttributeSource;
        //private TransactionInterceptor primaryTransactionInterceptor;
        private List<BeanDefinition> transactionInterceptors = new ArrayList<>();
        private EnableTransactionInterceptors enableTransactionInterceptors;
        //private String advisorBeanName;
    }

    class TransactionInterceptorConfigurationError extends SystemCodeException {

        TransactionInterceptorConfigurationError(String message, Throwable cause) {
            super(SystemCode.ERROR, message, cause);
        }

        TransactionInterceptorConfigurationError(String message) {
            super(SystemCode.ERROR, message);
        }
    }
}