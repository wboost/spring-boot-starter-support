package top.wboost.config.client.core;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertySourcesLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import top.wboost.common.base.entity.HttpRequestBuilder;
import top.wboost.common.log.util.LoggerUtil;
import top.wboost.common.util.HttpClientUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConfigClientPropertiesEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    //优先于ConfigFileApplicationListener
    public static final int DEFAULT_ORDER = ConfigFileApplicationListener.DEFAULT_ORDER - 2;
    private PropertySourcesLoader propertySourcesLoader = new PropertySourcesLoader();
    private ResourceLoader resourceLoader = new DefaultResourceLoader();
    private final String[] resources = new String[]{"classpath:/bootstrap.yml","classpath:/application.yml"};
    private Logger logger = LoggerUtil.getLogger(ConfigClientPropertiesEnvironmentPostProcessor.class);


    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        StandardEnvironment environmentInit = new StandardEnvironment();
        environmentInit.merge(environment);
        try {
            for (String resource : Arrays.asList(resources)) {
                Resource bootstrapResource = this.resourceLoader.getResource(resource);
                PropertySource<?> load = propertySourcesLoader.load(bootstrapResource, "applicationConfig: [profile=]", "wboostConfigClient: [" + resource + "]", null);
                environmentInit.getPropertySources().addLast(load);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String serverId = environmentInit.getProperty("common.config.client.server-id");
        String serverAddr = environmentInit.getProperty("common.config.client.server-addr");
        String applicationName = environmentInit.getProperty("spring.application.name");
        FetchConfigProcessor fetchConfigProcessor = new FetchConfigProcessor();
        fetchConfigProcessor.setServerId(serverId);
        fetchConfigProcessor.setServerAddr(serverAddr);
        fetchConfigProcessor.setApplicationName(applicationName);
        fetchConfigProcessor.setProfiles(environmentInit.getActiveProfiles());
        List<PropertySource> environmentFetch = fetchConfigProcessor.fetchConfig();
        environmentFetch.forEach(propertySource -> environment.getPropertySources().addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, propertySource));
        System.out.println("postProcessEnvironment");
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    @Data
    class FetchConfigProcessor {
        private String serverAddr;

        private String serverId;

        private String applicationName;

        private String[] profiles;

        private final String requestMappingPrefix = "/fetch";

        public List<PropertySource> fetchConfig() {
            List<PropertySource> environmentList = new ArrayList<>();
            environmentList.add(fetchPublic());
            environmentList.addAll(fetchOwnByProfile());
            return environmentList;
        }

        public PropertySource<?> fetchPublic() {
            String url = serverAddr + requestMappingPrefix + "/public/" + applicationName;
            logger.info("fetch public config from config-server {}",serverAddr);
            ResponseEntity<String> profiles = HttpClientUtil.execute(HttpRequestBuilder.get(url));
            try {
                if (profiles.getStatusCode() == HttpStatus.OK) {
                    String body = profiles.getBody();
                    JSONObject jsonObject = JSONObject.parseObject(body);
                    JSONObject data = jsonObject.getJSONObject("data");
                    JSONObject source = data.getJSONObject("source");
                    MapPropertySource mapPropertySource = new MapPropertySource("wboostConfigClientFetch:" + jsonObject.getString("name"), source);
                    logger.info("fetch success. {}",mapPropertySource);
                    return mapPropertySource;
                } else {
                    throw new RuntimeException(profiles.getStatusCode().toString());
                }
            }catch (Exception e) {
                return new MapPropertySource("wboostConfigClientFetch:public", new HashMap<>());
            }
        }

        public List<PropertySource<?>> fetchOwnByProfile() {
            String url = serverAddr + "/" + applicationName + "/";
            List<PropertySource<?>> sources = new ArrayList<>();
            try {
                for (String profile : Arrays.asList(profiles)) {
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
    }
}
