package top.wboost.config.client.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import top.wboost.common.boot.util.SpringBootUtil;

public class ConfigClientApplicationListener implements SpringApplicationRunListener {

    public String jarName;
    private ConfigurableApplicationContext context;
    private Class<?> launcherClass;
    private SpringApplication application;
    private String[] args;

    public ConfigClientApplicationListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("hook...");
        }));
        context.close();
        new Thread(() -> {
            for (int i = 0 ; i < 10;i ++) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("dosomething....");
            }
        }).start();

//        try {
//            application.run(args);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
