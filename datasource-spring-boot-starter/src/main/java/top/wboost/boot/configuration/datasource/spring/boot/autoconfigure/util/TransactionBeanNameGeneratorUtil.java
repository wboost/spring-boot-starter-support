package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.util;

public class TransactionBeanNameGeneratorUtil {

    public static String generatorTransactionManagerName(String dataSourceName) {
        return dataSourceName + "_TransactionManager";
    }
}
