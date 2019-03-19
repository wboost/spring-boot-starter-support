package top.wboost.config.client.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.OrderComparator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import top.wboost.common.annotation.Explain;
import top.wboost.common.base.entity.ResultEntity;
import top.wboost.common.system.code.SystemCode;
import top.wboost.common.utils.web.core.ConfigProperties;
import top.wboost.common.utils.web.utils.PropertiesUtil;
import top.wboost.config.client.core.ConfigClientApplicationListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/config/client/properties")
@ApiIgnore
public class ConfigClientPropertiesController {

    @Autowired
    ConfigClientApplicationListener configClientApplicationListener;

    public static List<Prop> getPropertyUses() {
        Map<String, Object> retMap = PropertiesUtil.getAllProperties();
        List<Prop> props = new ArrayList<>();
        retMap.forEach((key, val) -> props.add(new Prop(key, val)));
        props.sort(OrderComparator.INSTANCE);
        return props;
    }

    @GetMapping
    @Explain(systemCode = SystemCode.DO_FAIL, value = "获取所有使用中的配置项")
    public ResultEntity properties() {
        return ResultEntity.success(SystemCode.DO_OK).setData(getPropertyUses()).build();
    }

    @GetMapping("env")
    @Explain(systemCode = SystemCode.DO_FAIL, value = "获取环境配置")
    public ResultEntity env() {
        return ResultEntity.success(SystemCode.DO_OK).setData(ConfigProperties.environment).build();
    }

    @lombok.Data
    public static class Prop {
        private String key;
        private Object val;

        public Prop(String key, Object val) {
            this.key = key;
            this.val = val;
        }

        @Override
        public boolean equals(Object obj) {
            return ((Prop) obj).key.equals(this.key);
        }

    }

}
