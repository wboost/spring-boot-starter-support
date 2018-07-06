package top.wboost.common.spring.boot.swagger.config;

import java.lang.annotation.Annotation;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;
import top.wboost.common.base.annotation.AutoWebApplicationConfig;
import top.wboost.common.util.StringUtil;

/**
 * 配置使用ApiParam注解时的默认类型 (使用common.swagger.defaultApiParamType指定")
 * @className ParameterTypeResolverBuilderPlugin
 * @author jwSun
 * @date 2018年6月7日 下午3:25:56
 * @version 1.0.0
 */
@AutoWebApplicationConfig
@EnableConfigurationProperties(SwaggerProperties.class)
public class ParameterTypeResolverBuilderPlugin implements ParameterBuilderPlugin {

    @Autowired
    SwaggerProperties swaggerProperties;

    @Override
    public boolean supports(DocumentationType delimiter) {
        return delimiter == DocumentationType.SWAGGER_2;
    }

    @Override
    public void apply(ParameterContext parameterContext) {
        ResolvedMethodParameter parameter = parameterContext.resolvedMethodParameter();
        List<Annotation> annotations = parameter.getAnnotations();
        //判断是否已有RequestParam RequestBody RequestAttribute注解，若有则不变，若没有则默认改为RequestParam(form表单类型)
        boolean isChoose = false;
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (type == PathVariable.class || type == RequestParam.class || type == RequestBody.class
                    || type == RequestAttribute.class) {
                isChoose = true;
                if (type == PathVariable.class) {
                    parameterContext.parameterBuilder().required(true);
                }
                break;
            }
            if (type == ApiParam.class) {
                ApiParam api = (ApiParam) annotation;
                if (StringUtil.notEmpty(api.type())) {
                    isChoose = true;
                }
            }
        }
        if (!isChoose && swaggerProperties.getDefaultApiParamType() != null) {
            parameterContext.parameterBuilder().parameterType(swaggerProperties.getDefaultApiParamType());
        }
    }

}
