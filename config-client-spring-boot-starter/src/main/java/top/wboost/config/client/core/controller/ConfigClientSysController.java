package top.wboost.config.client.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import top.wboost.common.annotation.Explain;
import top.wboost.common.base.entity.ResultEntity;
import top.wboost.common.system.code.SystemCode;
import top.wboost.config.client.core.ConfigClientApplicationListener;
import top.wboost.config.client.core.ConfigClientRegister;

@RestController
@RequestMapping(ConfigClientRegister.WEB_PREFIX)
@ApiIgnore
public class ConfigClientSysController {

    @Autowired
    ConfigClientApplicationListener configClientApplicationListener;

    @PostMapping("/restart")
    @Explain(systemCode = SystemCode.DO_FAIL, value = "restart")
    public ResultEntity restart() {
        configClientApplicationListener.restartContext();
        return ResultEntity.success(SystemCode.DO_OK).setData("restarting.....").build();
    }

}
