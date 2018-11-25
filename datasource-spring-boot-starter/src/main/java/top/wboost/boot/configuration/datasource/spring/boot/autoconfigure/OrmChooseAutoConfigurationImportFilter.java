package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import top.wboost.common.boot.util.SpringBootUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * 配置使用JPA或MYBATIS
 * @className OrmChooseAutoConfigurationImportFilter
 * @author jwSun
 * @date 2018年4月16日 下午9:47:22
 * @version 1.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OrmChooseAutoConfigurationImportFilter implements AutoConfigurationImportFilter {

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        Set<String> excludes = new HashSet<>();
        try {
            Class<?> clazz = SpringBootUtil.getLauncherClass();
            EnableJpa enableJpa = AnnotationUtils.findAnnotation(clazz, EnableJpa.class);
            if (enableJpa == null) {
                excludes.add(HibernateJpaAutoConfiguration.class.getName());
                excludes.add(JpaRepositoriesAutoConfiguration.class.getName());
                //excludes.add(JdbcAutoConfiguration.class.getName());
            }
            excludes.add(DruidDataSourceAutoConfigure.class.getName());
        } catch (Exception e) {
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