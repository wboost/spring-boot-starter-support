package top.wboost.common.register;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import top.wboost.common.base.annotation.AutoRootApplicationConfig;

@AutoRootApplicationConfig
public class BeanDefinitionRegister {

    @Autowired
    DefaultListableBeanFactory beanFactory;

    public String registerWithGeneratedName(BeanDefinition beanDefintion) {
        beanFactory.registerBeanDefinition(beanDefintion.getBeanClassName(), beanDefintion);
        return beanDefintion.getBeanClassName();
    }
}
