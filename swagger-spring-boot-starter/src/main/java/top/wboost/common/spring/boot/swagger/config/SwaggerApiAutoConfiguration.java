package top.wboost.common.spring.boot.swagger.config;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.spring.web.PropertySourcedRequestMappingHandlerMapping;
import springfox.documentation.swagger.web.ApiResourceController;
import springfox.documentation.swagger2.web.Swagger2Controller;
import top.wboost.base.spring.boot.starter.GlobalForSpringBootStarter;
import top.wboost.common.base.enums.CharsetEnum;
import top.wboost.common.boost.handler.BoostHandler;
import top.wboost.common.boot.util.SpringBootUtil;
import top.wboost.common.spring.boot.swagger.Swagger2Config;
import top.wboost.common.spring.boot.swagger.util.FileCopyUtil;
import top.wboost.common.util.ReflectUtil;
import top.wboost.common.utils.web.interfaces.context.EzBootApplicationListener;
import top.wboost.common.utils.web.utils.SpringBeanUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 导出原生离线swagger-ui文档功能.
 * <pre>
 * 启用需有
 * common.swagger.enable-export-api=true
 * 可设置导出请求地址
 * common.swagger.export-url: /api/export
 * </pre>
 * @Auther: jwsun
 * @Date: 2018/11/16 22:03
 */
@Configuration
@ConditionalOnClass(
        {Swagger2Config.class,PropertySourcedRequestMappingHandlerMapping.class,ApiResourceController.class})
@ConditionalOnProperty(prefix = GlobalForSpringBootStarter.PROPERTIES_PREFIX + "swagger", name = "enable-export-api", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(SwaggerProperties.class)
public class SwaggerApiAutoConfiguration implements Ordered {

    @Bean
    @ApiIgnore
    public BoostHandler swagger2ApiExportBoostHandler(
            SwaggerProperties swaggerProperties) {
        return new SwaggerApiBoostHandler(swaggerProperties);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    public static class SwaggerApiBoostHandler implements BoostHandler, EzBootApplicationListener {

        private Swagger2Controller swagger2Controller;
        private ApiResourceController apiResourceController;
        private SwaggerProperties swaggerProperties;

        public SwaggerApiBoostHandler(SwaggerProperties swaggerProperties) {
            this.swaggerProperties = swaggerProperties;
        }

        @Override
        public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) {
            InputStream jarInputStream = null;
            ZipInputStream zipInputStream = null;
            ZipOutputStream zipOutputStream = null;
            try {
                response.setCharacterEncoding(CharsetEnum.UTF_8.getName());
                response.setContentType("multipart/form-data");
                response.setHeader("Content-Disposition",
                        "attachment;fileName=" + URLEncoder.encode(SpringBootUtil.getLauncherClass().getName() + ".zip", CharsetEnum.UTF_8.getName()));
                jarInputStream = FileCopyUtil.readJarFile("api-doc.zip");
                zipInputStream = new ZipInputStream(jarInputStream);
                zipOutputStream = new ZipOutputStream(response.getOutputStream());
                byte[] buf = new byte[1024];
                while (true) {
                    ZipEntry nextEntry = zipInputStream.getNextEntry();
                    if (nextEntry == null) {
                        break;
                    }
                    try {
                        zipOutputStream.putNextEntry(new ZipEntry(nextEntry.getName()));
                        int len;
                        while ((len = zipInputStream.read(buf)) > 0) {
                            zipOutputStream.write(buf, 0, len);
                        }
                    } catch (Exception e) {
                        throw e;
                    }
                }
                ZipEntry apiEntry = new ZipEntry("api-doc/webjars/api.js");
                zipOutputStream.putNextEntry(apiEntry);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(resolveApi(request).getBytes());
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    zipOutputStream.write(buf, 0, len);
                }
                inputStream.close();
                zipOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(jarInputStream);
                IOUtils.closeQuietly(zipInputStream);
                IOUtils.closeQuietly(zipOutputStream);
            }
            return null;
        }

        private String resolveApi(HttpServletRequest request) {
            StringBuilder sb = new StringBuilder();
            sb.append("var wboost = {};");
            sb.append("wboost['swagger'] = {};");
            sb.append("wboost['swagger']['security'] = " + JSONObject.toJSONString(apiResourceController.securityConfiguration().getBody()) + ";");
            sb.append("wboost['swagger']['swagger_resources'] =" + JSONObject.toJSONString(apiResourceController.swaggerResources().getBody()) + ";");
            sb.append("wboost['swagger']['ui'] = " + JSONObject.toJSONString(apiResourceController.uiConfiguration().getBody()) + ";");
            String apiDocs = JSONObject.toJSONString(swagger2Controller.getDocumentation(null, request).getBody());
            sb.append("wboost['swagger']['api_docs'] = " + apiDocs + ";");
            sb.append("wboost['swagger']['api_docst'] = '" + apiDocs + "'" + ";");
            return sb.toString();
        }

        @Override
        public String getUrlMapping() {
            return swaggerProperties.getExportUrl();
        }


        @Override
        public void onBootApplicationEvent(ContextRefreshedEvent event) {
            PropertySourcedRequestMappingHandlerMapping propertySourcedRequestMappingHandlerMapping = SpringBeanUtil.getBean(PropertySourcedRequestMappingHandlerMapping.class);
            this.swagger2Controller = ReflectUtil.getFieldValue(propertySourcedRequestMappingHandlerMapping, "handler", Swagger2Controller.class);
            this.apiResourceController = SpringBeanUtil.getBean(ApiResourceController.class);
        }
    }

}
