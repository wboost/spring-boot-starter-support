package top.wboost.base.spring.boot.starter;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;

/**
 * 配置禁用数据层
 * @className DisableDataSourceConfigurationImportFilter
 * @author jwSun
 * @date 2018年6月22日 上午11:37:30
 * @version 1.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DisableDataSourceConfigurationImportFilter implements AutoConfigurationImportFilter {

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        Set<String> excludes = new HashSet<>();
        try {
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            StackTraceElement element = stackTraceElements[stackTraceElements.length - 1];
            String bootRunName = element.getClassName();
            Class<?> clazz = Class.forName(bootRunName);
            DisableDataSource disableDataSource = AnnotationUtils.findAnnotation(clazz, DisableDataSource.class);
            if (disableDataSource != null) {
                excludes.add(DataSourceAutoConfiguration.class.getName());
                excludes.add(DataSourceTransactionManagerAutoConfiguration.class.getName());
                excludes.add(HibernateJpaAutoConfiguration.class.getName());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        boolean[] match = new boolean[autoConfigurationClasses.length];
        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            if (excludes.contains(autoConfigurationClasses[i])) {
                match[i] = false;
            } else {
                match[i] = true;
            }
        }
        return match;
    }

}