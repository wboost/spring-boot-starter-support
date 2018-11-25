package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.datasource;

/*@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties(DataSourceWrapper.class)
@AutoConfigureBefore(DruidDataSourceAutoConfigure.class)*/
public class DataSourceAutoMultipleConfigurationBean {
/*

    public static final String DATASOURCE_WAPPERS = "DATASOURCE_WAPPERS_BEAN";
    @Autowired
    DataSourceWrapper DataSourceWrapper;
    @Autowired
    DefaultListableBeanFactory beanFactory;
    Logger log = LoggerUtil.getLogger(DataSourceAutoMultipleConfigurationBean.class);

    public DataSource initConfig() {
        if (!(beanFactory instanceof BeanDefinitionRegistry)) {
            log.warn("auto get dataSource error ! ckeck beanFactory is not instanceof BeanDefinitionRegistry");
        }
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        Map<String, DataSourceWrapper> dataSources = CustomerPropertiesTreeUtil.resolvePropertiesTree(
                DataSourceWrapper.class, DataSourceWrapper, "common.datasource", "primary", "url", "username",
                "password");

        beanFactory.registerSingleton(DATASOURCE_WAPPERS, dataSources);

        DataSourceWrapper primaryDataSourceWrapper;
        List<Entry<String, DataSourceWrapper>> list = new ArrayList<Entry<String, DataSourceWrapper>>(
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
        beanFactory.registerAlias(GlobalForDataSourceBootStarter.PRIMARY_DATASOURCE_NAME, primarydsName);
        beanFactory.registerAlias(GlobalForDataSourceBootStarter.PRIMARY_DATASOURCE_NAME, "dataSource");
        if (log.isInfoEnabled()) {
            log.info("registerSingleton primary dataSource bean: {}", primarydsName);
        }
        for (int i = 0; i < list.size(); i++) {
            Entry<String, DataSourceWrapper> entry = list.get(i);
            String dsBeanName = entry.getKey();
            if (primarydsName.equals(dsBeanName)) {
                continue;
            }
            DataSourceWrapper dsConfig = entry.getValue();
            SpringBeanRegisterUtil.registerBeanDefinition(dsBeanName, DruidDataSource.class,
                    ConvertUtil.beanConvertToMap(dsConfig), beanFactory);
            if (log.isInfoEnabled()) {
                log.info("registerSingleton dataSource bean: {}", dsBeanName);
            }
        }
        return beanFactory.getBean(GlobalForDataSourceBootStarter.PRIMARY_DATASOURCE_NAME, DataSource.class);
    }
*/

}