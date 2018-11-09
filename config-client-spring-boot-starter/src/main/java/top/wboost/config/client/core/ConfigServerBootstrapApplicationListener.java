package top.wboost.config.client.core;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Collections;

/**
 * Normally you don't want the config server to be a config client itself, so this
 * listener disables the config client unless <code>spring.cloud.config.enabled</code> is
 * explicitly "true". It has to be "true" at the time this listener is fired, which means
 * <b>before</b> the <code>bootstrap.yml</code> is parsed, which in turn means to you need
 * to launch the application with an existing primed {@link Environment} (e.g. via System
 * properties or a {@link SpringApplicationBuilder}). This is the same rule of precedence
 * as for anything else affecting the bootstrap process itself, e.g. setting
 * <code>spring.cloud.bootstrap.name</code> to something other than "bootstrap".
 * <p>
 * N.B. a config server can always be an "embedded" config client (using its own config
 * repository as a property source) if you set
 * <code>spring.cloud.config.server.bootstrap=true</code> in <code>bootstrap.yml</code>.
 * This listener is only to prevent it from using HTTP to contact itself.
 *
 * @author Dave Syer
 *
 */
public class ConfigServerBootstrapApplicationListener implements
ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

	public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 5;

	private int order = DEFAULT_ORDER;

	private PropertySource<?> propertySource = new MapPropertySource(
			"configServerClient", Collections.<String, Object> singletonMap(
					"spring.cloud.config.enabled", "false"));

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		ConfigurableEnvironment environment = event.getEnvironment();
		if (!environment.resolvePlaceholders("${spring.cloud.config.enabled:false}")
				.equalsIgnoreCase("true")) {
			if (!environment.getPropertySources().contains(this.propertySource.getName())) {
				environment.getPropertySources().addLast(this.propertySource);
			}
		}
	}

}
