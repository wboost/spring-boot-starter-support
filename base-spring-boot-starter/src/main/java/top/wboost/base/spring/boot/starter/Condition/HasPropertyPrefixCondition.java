package top.wboost.base.spring.boot.starter.Condition;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import top.wboost.common.utils.web.utils.PropertiesUtil;

import java.util.Map;

/**
 * @Auther: jwsun
 * @Date: 2018/11/21 14:03
 */
public class HasPropertyPrefixCondition  extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> conditionalHasPropertyPrefix = metadata.getAnnotationAttributes("top.wboost.base.spring.boot.starter.Condition.ConditionalHasPropertyPrefix");
        String prefix = conditionalHasPropertyPrefix.get("prefix").toString();
        Map<String, Object> propertiesByPrefix = PropertiesUtil.getPropertiesByPrefix(prefix);
        if (propertiesByPrefix.size() > 0)
            return new ConditionOutcome(true, prefix);
        return new ConditionOutcome(false, prefix);
    }
}