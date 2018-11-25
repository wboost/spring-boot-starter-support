package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure;

import top.wboost.base.spring.boot.starter.GlobalForSpringBootStarter;

public class GlobalForDataSourceBootStarter {

    public static final String PRIMARY_DATASOURCE_NAME = "primaryDataSource";

    public static final String DATASOURCE_WAPPERS = "DATASOURCE_WAPPERS_BEAN";

    public static final String PROPERTIES_JDBC = GlobalForSpringBootStarter.PROPERTIES_PREFIX + "jdbc";

    public final static String PRIMARYT_TRANSACTION_MANAGER = "transactionManager";

}
