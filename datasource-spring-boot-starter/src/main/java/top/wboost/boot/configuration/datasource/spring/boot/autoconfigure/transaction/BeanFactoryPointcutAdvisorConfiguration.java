package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.transaction;

import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;
import top.wboost.common.system.code.SystemCode;
import top.wboost.common.system.exception.SystemCodeException;

import java.util.List;

@Configuration
public class BeanFactoryPointcutAdvisorConfiguration {

    /**
     * Create a {@link RootBeanDefinition} for the advisor described in the supplied. Does <strong>not</strong>
     * parse any associated '{@code pointcut}' or '{@code pointcut-ref}' attributes.
     */
    public AbstractBeanDefinition createAdvisorBeanDefinition(String ref, EnableTransactionInterceptors.Config.PointCutConfig config) {
        RootBeanDefinition advisorDefinition = new RootBeanDefinition(InterceptorDefaultBeanFactoryPointcutAdvisor.class);

        String adviceRef = ref;
        if (!StringUtils.hasText(adviceRef)) {
            throw new BeanFactoryPointcutAdvisorConfigurationError("EnableTransactionInterceptors.Config.name() contains empty value.");
        }
        else {
            advisorDefinition.getPropertyValues().add(
                    "adviceBeanName", new RuntimeBeanNameReference(adviceRef));
        }
        advisorDefinition.getPropertyValues().add(
                    "order", config.order());

        return advisorDefinition;
    }

    public AbstractBeanDefinition createPointcutDefinition(String expression) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(AspectJExpressionPointcut.class);
        beanDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        beanDefinition.setSynthetic(true);
        beanDefinition.getPropertyValues().add("expression", expression);
        return beanDefinition;
    }

    public static class InterceptorDefaultBeanFactoryPointcutAdvisor extends DefaultBeanFactoryPointcutAdvisor {
        private List<PlatformTransactionManager> platformTransactionManagers;
        public InterceptorDefaultBeanFactoryPointcutAdvisor(
                List<PlatformTransactionManager> platformTransactionManagers) {
            this.platformTransactionManagers = platformTransactionManagers;
        }

        public List<PlatformTransactionManager> getPlatformTransactionManagers() {
            return platformTransactionManagers;
        }
    }

    class BeanFactoryPointcutAdvisorConfigurationError extends SystemCodeException {

        BeanFactoryPointcutAdvisorConfigurationError(String message) {
            super(SystemCode.ERROR, message);
        }
    }

}
