package top.wboost.boot.config.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wboost.common.annotation.Explain;
import top.wboost.common.base.entity.ResultEntity;
import top.wboost.common.system.code.SystemCode;
import top.wboost.common.utils.web.utils.PropertiesUtil;

@RestController
@RequestMapping("support/config")
public class ConfigFetchController {

    @GetMapping
    @Explain(value = "fetchConfig",systemCode = SystemCode.DO_FAIL)
    public ResultEntity fetchConfig() {
        return ResultEntity.success(SystemCode.DO_OK).setData(PropertiesUtil.getAllProperties()).build();
    }
}
