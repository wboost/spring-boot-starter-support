package top.wboost.common.spring.boot.swagger.config;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.spi.service.OperationModelsProviderPlugin;
import springfox.documentation.spi.service.contexts.RequestMappingContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;
import top.wboost.common.asm.ClassGenerator;
import top.wboost.common.asm.ClassGeneratorEntity;
import top.wboost.common.base.annotation.AutoWebApplicationConfig;
import top.wboost.common.log.entity.Logger;
import top.wboost.common.log.util.LoggerUtil;
import top.wboost.common.spring.boot.swagger.api.ApiResponseDoc;
import top.wboost.common.spring.boot.swagger.template.ResultEntityTemplate;
import top.wboost.common.system.code.SystemCode;
import top.wboost.common.system.exception.SystemCodeException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static springfox.documentation.schema.ResolvedTypes.resolvedTypeSignature;

/**
 * @Auther: jwsun
 * @Date: 2018/12/29 15:06
 */
@AutoWebApplicationConfig
@Order(Ordered.HIGHEST_PRECEDENCE - 10)
public class ResponseDocModelBuilderPlugin implements OperationModelsProviderPlugin {

    private final TypeResolver typeResolver;

    private Logger logger = LoggerUtil.getLogger(getClass());
    private Map<String,Class<?>> genMap = new HashMap<>();

    @Autowired
    public ResponseDocModelBuilderPlugin(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    private Class<?> forClass(ModelContext context) {
        return typeResolver.resolve(context.getType()).getErasedType();
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return SwaggerPluginSupport.pluginDoesApply(delimiter);
    }

    @Override
    public void apply(RequestMappingContext context) {
        List<ApiResponseDoc> annotations = context.findAnnotations(ApiResponseDoc.class);
        if (annotations != null && annotations.size() > 0) {
            ClassGeneratorEntity classGenerator = new ClassGeneratorEntity();
            try {
                if (!genMap.containsKey(annotations.get(0).value().getSimpleName())) {
                    classGenerator.setName("ModelShow_" + annotations.get(0).value().getSimpleName());
                    //classGenerator.setVisitClass(ResultEntityNoDataTemplate.class);
                    classGenerator.addFieldGenerator(new ClassGeneratorEntity.FieldGenerator("data", annotations.get(0).value()));
                    classGenerator.addFieldGenerator(new ClassGeneratorEntity.FieldGenerator("info", ResultEntityTemplate.ReturnInfoTemplate.class));
                    classGenerator.addFieldGenerator(new ClassGeneratorEntity.FieldGenerator("status", Integer.class));
                    classGenerator.addFieldGenerator(new ClassGeneratorEntity.FieldGenerator("validate", Boolean.class));
                    Class<?> generatorClass = ClassGenerator.generatorClass(classGenerator);
                    genMap.put(annotations.get(0).value().getSimpleName(), generatorClass);
                }
                ResolvedType modelType = context.alternateFor(typeResolver.resolve(genMap.get(annotations.get(0).value().getSimpleName())));
                logger.debug("Adding return parameter of type {}", resolvedTypeSignature(modelType).or("<null>"));
                context.operationModelsBuilder().addReturn(modelType);
            } catch (IOException e) {
                e.printStackTrace();
                throw new SystemCodeException(SystemCode.DO_FAIL,"ASM CREATE ERROR! " + JSONObject.toJSONString(classGenerator));
            }
        }
    }
}
