package top.wboost.config.client.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wboost.common.annotation.Explain;
import top.wboost.common.system.code.SystemCode;
import top.wboost.config.client.core.ConfigClientApplicationListener;

@RestController
@RequestMapping("/config/client/sys")
public class ConfigClientSysController {

    @Autowired
    ConfigClientApplicationListener configClientApplicationListener;

    @PostMapping("/restart")
    @Explain(systemCode = SystemCode.DO_FAIL, value = "restart")
    public  void callTimeResult() {
        configClientApplicationListener.restartContext();
    }

}
