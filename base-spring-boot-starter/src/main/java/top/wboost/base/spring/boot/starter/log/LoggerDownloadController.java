package top.wboost.base.spring.boot.starter.log;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import top.wboost.base.spring.boot.starter.GlobalForSpringBootStarter;
import top.wboost.common.annotation.Explain;
import top.wboost.common.base.entity.ResultEntity;
import top.wboost.common.log.util.LoggerUtil;
import top.wboost.common.system.code.SystemCode;
import top.wboost.common.utils.web.utils.DownloadUtil;
import top.wboost.common.utils.web.utils.FileUtil;
import top.wboost.common.utils.web.utils.HtmlUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * 日志导出
 * @Auther: jwsun
 * @Date: 2019/3/17 10:08
 */
@Controller
@RequestMapping("${" + GlobalForSpringBootStarter.PROPERTIES_PREFIX + "log.export:/logger}")
public class LoggerDownloadController {

    @GetMapping("/down")
    public void exportLogger(HttpServletResponse response, String file) {
        if (LoggerUtil.getLoggerFile().contains(file)) {
            File exportFile = new File(file);
            DownloadUtil.download(exportFile.getName(), exportFile, response);
        } else {
            HtmlUtil.writerJson(response, "no file.");
        }
    }

    @GetMapping("/show")
    public void showLogger(HttpServletResponse response, String file) {
        if (LoggerUtil.getLoggerFile().contains(file)) {
            File exportFile = new File(file);
            HtmlUtil.writerJson(response, FileUtil.importFile(exportFile));
        } else {
            HtmlUtil.writerJson(response, "no file.");
        }
    }

    @GetMapping
    @ResponseBody
    @Explain("get all logfile path")
    public ResultEntity getLoggerFiles() {
        return ResultEntity.success(SystemCode.DO_OK).setData(LoggerUtil.getLoggerFile()).build();
    }

}
