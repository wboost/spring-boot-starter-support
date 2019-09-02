package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.jpa;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.data.jpa.util.BeanDefinitionUtils.EntityManagerFactoryBeanDefinition;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.springframework.beans.factory.BeanFactoryUtils.transformedBeanName;

public class EntityManagerBeanDefinitionRegistrarPostProcessor implements BeanFactoryPostProcessor {

	private static final List<Class<?>> EMF_TYPES;
	private static final String JNDI_OBJECT_FACTORY_BEAN = "org.springframework.jndi.JndiObjectFactoryBean";

	static {

		List<Class<?>> types = new ArrayList<Class<?>>();
		types.add(EntityManagerRegister.EntityManagerFactoryFactoryBean.class);
		EMF_TYPES = Collections.unmodifiableList(types);
	}

	public static Collection<EntityManagerFactoryBeanDefinition> getEntityManagerFactoryBeanDefinitions(
			ConfigurableListableBeanFactory beanFactory) {

		List<EntityManagerFactoryBeanDefinition> definitions = new ArrayList<EntityManagerFactoryBeanDefinition>();

		for (Class<?> type : EMF_TYPES) {

			for (String name : beanFactory.getBeanNamesForType(type, true, false)) {
				registerEntityManagerFactoryBeanDefinition(transformedBeanName(name), beanFactory, definitions);
			}
		}

		BeanFactory parentBeanFactory = beanFactory.getParentBeanFactory();

		if (parentBeanFactory instanceof ConfigurableListableBeanFactory) {
			definitions.addAll(getEntityManagerFactoryBeanDefinitions((ConfigurableListableBeanFactory) parentBeanFactory));
		}

		return definitions;
	}

	private static void registerEntityManagerFactoryBeanDefinition(String name,
																   ConfigurableListableBeanFactory beanFactory, List<EntityManagerFactoryBeanDefinition> definitions) {

		BeanDefinition definition = beanFactory.getBeanDefinition(name);

		if (JNDI_OBJECT_FACTORY_BEAN.equals(definition.getBeanClassName())) {
			if (!EntityManagerFactory.class.getName().equals(definition.getPropertyValues().get("expectedType"))) {
				return;
			}
		} else if (beanFactory.getType(name) == null
				|| !EntityManagerFactory.class.isAssignableFrom(beanFactory.getType(name))) {
			return;
		}

		definitions.add(new EntityManagerFactoryBeanDefinition(name, beanFactory));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

		for (EntityManagerFactoryBeanDefinition definition : getEntityManagerFactoryBeanDefinitions(beanFactory)) {

			if (!(definition.getBeanFactory() instanceof BeanDefinitionRegistry)) {
				continue;
			}

			BeanDefinitionBuilder builder = BeanDefinitionBuilder
					.rootBeanDefinition("org.springframework.orm.jpa.SharedEntityManagerCreator");
			builder.setFactoryMethod("createSharedEntityManager");
			builder.addConstructorArgReference(definition.getBeanName());

			AbstractBeanDefinition emBeanDefinition = builder.getRawBeanDefinition();

			emBeanDefinition.addQualifier(new AutowireCandidateQualifier(Qualifier.class, definition.getBeanName()));
			emBeanDefinition.setScope(definition.getBeanDefinition().getScope());
			emBeanDefinition.setSource(definition.getBeanDefinition().getSource());

			BeanDefinitionReaderUtils.registerWithGeneratedName(emBeanDefinition,
					(BeanDefinitionRegistry) definition.getBeanFactory());
		}
	}
}