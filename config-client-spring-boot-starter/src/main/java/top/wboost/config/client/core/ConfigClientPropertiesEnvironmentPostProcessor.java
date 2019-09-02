package top.wboost.config.client.core;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import top.wboost.base.spring.boot.starter.util.SpringBootPropertiesLoadUtil;
import top.wboost.common.base.entity.HttpRequestBuilder;
import top.wboost.common.exception.BusinessCodeException;
import top.wboost.common.log.util.LoggerUtil;
import top.wboost.common.util.HttpClientUtil;
import top.wboost.common.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static top.wboost.common.base.ConfigForBase.PropertiesConfig.IS_DEBUG;

public class ConfigClientPropertiesEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    //优先于ConfigFileApplicationListener
    private static final int DEFAULT_ORDER = ConfigFileApplicationListener.DEFAULT_ORDER - 2;
    private Logger logger = LoggerUtil.getLogger(ConfigClientPropertiesEnvironmentPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        EnableConfigClient enableConfigClient = null;
        for (Object source : application.getSources()) {
            enableConfigClient = AnnotationUtils.getAnnotation(source instanceof java.lang.Class ? (java.lang.Class) source : source.getClass(), EnableConfigClient.class);
            if (enableConfigClient != null)
                break;
        }

        if (enableConfigClient == null) {
            return;
        }
        StandardEnvironment environmentInit = new StandardEnvironment();
        environmentInit.merge(SpringBootPropertiesLoadUtil.getInitEnvironment());
        String serverId = environmentInit.getProperty("common.config.client.server-id");
        String serverAddr = environmentInit.getProperty("common.config.client.server-addr");
        String prefix = environmentInit.getProperty("common.config.client.prefix");
        String applicationName = environmentInit.getProperty("spring.application.name");
        SimpleFetchConfigProcessor fetchConfigProcessor = new SimpleFetchConfigProcessor();
        fetchConfigProcessor.setServerId(serverId);
        fetchConfigProcessor.setServerAddr(serverAddr);
        fetchConfigProcessor.setApplicationName(applicationName);
        fetchConfigProcessor.setProfiles(environmentInit.getActiveProfiles());
        fetchConfigProcessor.setPrefix(prefix == null ? "" : prefix);
        List<PropertySource<?>> environmentFetch = fetchConfigProcessor.fetchConfig();
        if (environmentInit.getProperty(IS_DEBUG) != null && Boolean.valueOf(environmentInit.getProperty(IS_DEBUG))) {
            System.out.println(JSONObject.toJSONString(environmentFetch));
        }
        //environmentFetch.forEach(propertySource -> environment.getPropertySources().addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, propertySource));
        environmentFetch.forEach(propertySource -> environment.getPropertySources().addFirst(propertySource));
        fetchConfigProcessor.registerClient(environment);
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    @Data
    class SimpleFetchConfigProcessor implements FetchConfigProcessor {
        private final String requestMappingPrefix = "/fetch";
        private String serverAddr;
        private String serverId;
        private String applicationName;
        private String[] profiles;
        private String prefix;

        @Override
        public void registerClient(ConfigurableEnvironment environment) {
            String url = serverAddr + "/register/hello/" + applicationName;
            try {
                ResponseEntity<String> profiles = HttpClientUtil.execute(HttpRequestBuilder.post(url).addParameter("ip", environment.getProperty("spring.cloud.client.ipAddress")).addParameter("port", environment.getProperty("server.port")));
                if (profiles.getStatusCode() != HttpStatus.OK) {
                    throw new FetchConfigProcessorException("register client error." + profiles.getStatusCode().toString());
                }
            } catch (Exception e) {
                throw new FetchConfigProcessorException("register client error.", e);
            }
        }

        public List<PropertySource<?>> fetchConfig() {
            List<PropertySource<?>> environmentList = new ArrayList<>();
            PropertySource<?> propertySource = fetchPublic();
            if ((!StringUtil.notEmpty(prefix)) && propertySource.containsProperty("common.config.client.prefix")) {
                prefix = propertySource.getProperty("common.config.client.prefix").toString();
            }
            environmentList.add(propertySource);
            environmentList.addAll(fetchOwnByProfile());
            return environmentList;
        }

        public PropertySource<?> fetchPublic() {
            String url = serverAddr + requestMappingPrefix + "/public/" + applicationName;
            logger.info("fetch public config from config-server {}",serverAddr);
            try {
                ResponseEntity<String> profiles = HttpClientUtil.execute(HttpRequestBuilder.get(url));
                if (profiles.getStatusCode() == HttpStatus.OK) {
                    String body = profiles.getBody();
                    JSONObject jsonObject = JSONObject.parseObject(body);
                    JSONObject data = jsonObject.getJSONObject("data");
                    JSONObject source = data.getJSONObject("source");
                    MapPropertySource mapPropertySource = new MapPropertySource("wboostConfigClientFetch:public", source);
                    logger.info("fetch success. {}",mapPropertySource);
                    return mapPropertySource;
                } else {
                    throw new RuntimeException(profiles.getStatusCode().toString());
                }
            }catch (Exception e) {
                e.printStackTrace();
                return new MapPropertySource("wboostConfigClientFetch:public", new HashMap<>());
            }
        }

        public List<PropertySource<?>> fetchOwnByProfile() {
            String url = serverAddr + "/" + prefix + "/" + applicationName + "/";
            List<PropertySource<?>> sources = new ArrayList<>();
            try {
                for (String profile : profiles) {
                    ResponseEntity<String> prop = HttpClientUtil.execute(HttpRequestBuilder.get(url + profile));
                    if (prop.getStatusCode() == HttpStatus.OK) {
                        String body = prop.getBody();
                        JSONObject jsonObject = JSONObject.parseObject(body);
                        JSONArray propertySources = jsonObject.getJSONArray("propertySources");
                        propertySources.forEach(propertySource -> sources.add( new MapPropertySource("wboostConfigClientFetch:" + ((JSONObject)propertySource).getString("name"), ((JSONObject)propertySource).getJSONObject("source"))));
                    } else {
                        continue;
                    }
                }
            }catch (Exception e) {
                logger.error("fetchOwnByProfile error.",e);
            }
            return sources;
        }

        class FetchConfigProcessorException extends BusinessCodeException {

            public FetchConfigProcessorException(String message) {
                super(0, message);
            }

            public FetchConfigProcessorException(String message, Exception e) {
                super(0, message, e);
            }
        }
    }
}
