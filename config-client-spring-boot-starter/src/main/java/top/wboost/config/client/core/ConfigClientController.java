package top.wboost.config.client.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wboost.common.annotation.Explain;
import top.wboost.common.system.code.SystemCode;

@RestController
@RequestMapping("/config/client")
public class ConfigClientController {

    @Autowired
    ConfigClientApplicationListener configClientApplicationListener;

    @GetMapping("/restart")
    @Explain(systemCode = SystemCode.DO_FAIL, value = "example")
    public  void callTimeResult() {
        configClientApplicationListener.restartContext();
    }

}
