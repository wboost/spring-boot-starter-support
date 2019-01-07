package top.wboost.config.client.core;

import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import top.wboost.common.boot.util.SpringBootUtil;
import top.wboost.common.log.util.LoggerUtil;
import top.wboost.common.util.StringUtil;
import top.wboost.common.utils.web.utils.DateUtil;
import top.wboost.common.utils.web.utils.FileUtil;
import top.wboost.common.utils.web.utils.PropertiesUtil;

import java.io.IOException;
import java.util.Date;

public class ConfigClientApplicationListener implements SpringApplicationRunListener {

    public String jarName;
    private ConfigurableApplicationContext context;
    private Class<?> launcherClass;
    private SpringApplication application;
    private String[] args;
    private String restartCmd;
    private boolean restartStatus;
    private Logger logger = LoggerUtil.getLogger(ConfigClientApplicationListener.class);

    public ConfigClientApplicationListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
        //this.restartCmd = getRestartCmd();
    }

    @Override
    public void starting() {
        //System.out.println("starting");
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        //System.out.println("environmentPrepared");
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        this.context = context;
        context.getBeanFactory().registerSingleton("ConfigClientApplicationListener", this);
        this.launcherClass = SpringBootUtil.getLauncherClass();
        String file = launcherClass.getProtectionDomain().getCodeSource().getLocation().getFile();
        this.jarName = file;
        //System.out.println(file);
        //System.out.println("contextPrepared");
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        //System.out.println("contextLoaded");
    }

    @Override
    public void finished(ConfigurableApplicationContext context, Throwable exception) {
        //System.out.println("finished");
    }

    public void restartContext() {
        String restartCmd = getRestartCmd();
        if (restartStatus) {
            new Thread(() -> {
                try {
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        logger.warn("restart...");

                        logger.warn("use cmd:" + restartCmd);
                        try {
                            Runtime.getRuntime().exec(new String[]{"sh", "-c", restartCmd});
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }));
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                context.close();
            }).start();
        }
    }

    protected String getRestartCmd() {
        try {
            String filePath = ConfigClientApplicationListener.class.getResource("/").getPath().split("!")[0].split(":")[1];
            String jarName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
            logger.info("jarName is " + jarName);
//            Process execPid = Runtime.getRuntime().exec(new String[]{"sh", "-c", "jps -l |grep " + jarName + " |awk '{print $1}'"});
//            String pid = FileUtil.importFile(execPid.getInputStream()).trim();
            String pid = PropertiesUtil.getProperty("PID");
            String jvmParam = getParam(pid, "v");
            String arg = getParam(pid, "m");
            String cmd = StringUtil.format("/usr/bin/nohup java -jar " + jvmParam + " " + filePath + " " + arg + " >> " + filePath + ".restart." + DateUtil.format(new Date(), "yyyy-MM-dd-HH-mm-ss") + ".log 2>&1 &");
            restartStatus = true;
            logger.info("restart cmd is " + cmd);
            return cmd;
        } catch (Exception e) {
            logger.info("restart status is false");
            restartStatus = false;
            return "";
            //throw new SystemCodeException(SystemCode.DO_FAIL);
        }
    }

    private String getParam(String pid, String opt) throws IOException {
        Process execJpsVNum = Runtime.getRuntime().exec(new String[]{"sh", "-c", "jps -" + opt + " |grep " + pid + " | awk '{print NF}'"});
        String cmdJpsVNum = FileUtil.importFile(execJpsVNum.getInputStream()).trim();
        String param;
        if (StringUtil.notEmpty(cmdJpsVNum) && Integer.parseInt(cmdJpsVNum) > 2) {
            int num = Integer.parseInt(cmdJpsVNum);
            StringBuffer sb = new StringBuffer("jps -" + opt + " |grep " + pid + " | awk '{print ");
            for (int i = 2; i < num; ) {
                sb.append("$" + ++i + ",");
            }
            sb.setLength(sb.length() - 1);
            sb.append("}'");
            logger.info("exec " + opt + " cmd is " + sb);
            Process exec = Runtime.getRuntime().exec(new String[]{"sh", "-c", sb.toString()});
            param = FileUtil.importFile(exec.getInputStream()).trim();
        } else {
            param = "";
        }
        return param;
    }
}
