package top.wboost.common.spring.boot.swagger.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import top.wboost.common.base.annotation.AutoWebApplicationConfig;
import top.wboost.common.spring.boot.swagger.api.ApiResponseDoc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AutoWebApplicationConfig
@Order(Ordered.HIGHEST_PRECEDENCE - 10)
public class ResponseOperationBuilderPlugin implements OperationBuilderPlugin {
    // private final ModelAttributeParameterExpander expander;
    // private final EnumTypeDeterminer enumTypeDeterminer;

    // @Autowired
    // private DocumentationPluginsManager pluginsManager;

    /*@Autowired
    public ExplainOperationBuilderPlugin(ModelAttributeParameterExpander expander,
            EnumTypeDeterminer enumTypeDeterminer) {
        this.expander = expander;
        this.enumTypeDeterminer = enumTypeDeterminer;
    }*/

    @Override
    public void apply(OperationContext context) {
        List<ApiResponseDoc> list = context.findAllAnnotations(ApiResponseDoc.class);
        if (list.size() != 0) {
            ApiResponseDoc responseDoc = list.get(0);
            ModelRef modelRef = new ModelRef("ModelShow_" + responseDoc.value().getSimpleName(),null,true);
            context.operationBuilder().responseModel(modelRef);
            Set<ResponseMessage> responseMessages = new HashSet<>();
            responseMessages.add(new ResponseMessage(200,"",modelRef,null,null));
            context.operationBuilder().responseMessages(responseMessages);
        }
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }

}
