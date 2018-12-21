package top.wboost.base.spring.boot.starter.util;

import org.springframework.boot.env.PropertySourcesLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.util.Arrays;

/**
 * 用于spring初始化前获得配置文件
 * @Auther: jwsun
 * @Date: 2018/12/9 12:46
 */
public class SpringBootPropertiesLoadUtil {

    private static final String[] resources = new String[]{"file:./bootstrap.yml", "classpath:bootstrap.yml", "file:./application.yml", "classpath:application.yml"};
    private static PropertySourcesLoader propertySourcesLoader = new PropertySourcesLoader();
    private static ResourceLoader resourceLoader = new DefaultResourceLoader();
    private static StandardEnvironment environmentInit;

    static {
        environmentInit = new StandardEnvironment();
        try {
            for (String resource : Arrays.asList(resources)) {
                Resource bootstrapResource = resourceLoader.getResource(resource);
                PropertySource<?> load = propertySourcesLoader.load(bootstrapResource, "applicationConfig: [profile=]", "wboostConfigClient: [" + resource + "]", null);
                if (load != null) {
                    environmentInit.getPropertySources().addLast(load);
                }
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    public static StandardEnvironment getInitEnvironment() {
        StandardEnvironment retEnv = new StandardEnvironment();
        retEnv.merge(environmentInit);
        return retEnv;
    }


}
