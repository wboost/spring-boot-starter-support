package top.wboost.base.spring.boot.starter.logo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import top.wboost.base.spring.boot.starter.GlobalForSpringBootStarter;
import top.wboost.common.annotation.Explain;
import top.wboost.common.base.entity.ResultEntity;
import top.wboost.common.system.code.SystemCode;

/**
 * 日志导出
 * @Auther: jwsun
 * @Date: 2019/3/17 10:08
 */
@Controller
@EnableConfigurationProperties(ViewProperties.class)
@RequestMapping("${" + GlobalForSpringBootStarter.PROPERTIES_PREFIX + "view.prefix:/view}")
public class ViewController {

    @Autowired
    ViewProperties viewProperties;

    @GetMapping
    @ResponseBody
    @Explain("get view")
    public ResultEntity getViewConfig() {
        return ResultEntity.success(SystemCode.DO_OK).setData(viewProperties).build();
    }

}
