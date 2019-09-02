package top.wboost.config.client.core;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @Auther: jwsun
 * @Date: 2019/1/9 10:04
 */
public class ClientBeanRegister implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder loggerSender = BeanDefinitionBuilder.rootBeanDefinition(LoggerSender.class);
        registry.registerBeanDefinition("configClientLoggerSender", loggerSender.getBeanDefinition());
    }
}
