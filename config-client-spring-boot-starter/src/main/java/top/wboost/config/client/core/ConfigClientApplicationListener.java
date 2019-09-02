package top.wboost.config.client.core;

import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import top.wboost.common.boot.util.SpringBootUtil;
import top.wboost.common.log.util.LoggerUtil;
import top.wboost.common.system.code.SystemCode;
import top.wboost.common.system.exception.SystemCodeException;
import top.wboost.common.util.StringUtil;
import top.wboost.common.utils.web.utils.DateUtil;
import top.wboost.common.utils.web.utils.FileUtil;
import top.wboost.common.utils.web.utils.PropertiesUtil;

import java.io.IOException;
import java.util.Date;

public class ConfigClientApplicationListener implements SpringApplicationRunListener {

    public String jarName;
    private EmbeddedWebApplicationContext context;
    private Class<?> launcherClass;
    private String filePath;
    private long timestamp = System.currentTimeMillis();
    private String logPath;
    private boolean restartStatus;
    private Logger logger = LoggerUtil.getLogger(ConfigClientApplicationListener.class);

    public ConfigClientApplicationListener(SpringApplication application, String[] args) {
//        this.application = application;
//        this.args = args;
        try {
            filePath = ConfigClientApplicationListener.class.getResource("/").getPath().split("!")[0].split(":")[1];
            logPath = getLogPath();
        } catch (Exception e) {
            restartStatus = false;
        }

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
        context.getBeanFactory().registerSingleton("ConfigClientApplicationListener", this);
        this.launcherClass = SpringBootUtil.getLauncherClass();
        String file = launcherClass.getProtectionDomain().getCodeSource().getLocation().getFile();
        this.jarName = file;
        if (context instanceof EmbeddedWebApplicationContext) {
            this.context = (EmbeddedWebApplicationContext) context;
        } else {
            this.restartStatus = false;
        }
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
        restartContext(null, 0);
    }

    public void restartContext(String socket_ip, int socket_port) {
        String restartCmd = getRestartCmd(socket_ip, socket_port);
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
                stopAll();
            }).start();
            this.restartStatus = false;
        } else {
            throw new SystemCodeException(SystemCode.DO_FAIL).setPromptMessage("getRestartCommand error.");
        }
    }

    protected void stopAll() {
        logger.info("---------STOP SPRING CONTEXT---------");
        this.context.close();
    }

    protected String getRestartCmd(String sk_ip, int sk_port) {
        try {
            String jarName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
            logger.info("jarName is " + jarName);
            String pid = PropertiesUtil.getProperty("PID");
            String jvmParam = getParam(pid, "v").trim();
            StringBuffer sb = new StringBuffer();
            if (StringUtil.notEmpty(jvmParam)) {
                String[] jvms = jvmParam.split(" ");
                for (String jvmP : jvms) {
                    if (jvmP.indexOf("sk_ip") == -1 && jvmP.indexOf("sk_port") == -1 && jvmP.indexOf("sk_ts") == -1) {
                        sb.append(jvmP + " ");
                    }
                }
            }
            sb.append("-Dsk_ip=" + sk_ip + " -Dsk_port=" + sk_port + " -Dsk_ts=" + this.timestamp + " -Dsk_pid=" + pid);
            jvmParam = sb.toString();
            String arg = getParam(pid, "m");
            String cmd = StringUtil.format("/usr/bin/nohup java -jar " + jvmParam + " " + filePath + " " + arg + " >> " + logPath + " 2>&1 &");
            restartStatus = true;
            logger.info("restart cmd is " + cmd);
            return cmd;
        } catch (Exception e) {
            logger.info("restart status is false");
            restartStatus = false;
            return "";
        }
    }

    protected String getLogPath() {
        return filePath + ".restart." + DateUtil.format(new Date(timestamp), "yyyy-MM-dd-HH-mm-ss") + ".log";
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
