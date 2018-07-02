package top.wboost.base.spring.boot.starter.util;

import java.util.Arrays;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.annotation.AnnotationUtils;

import top.wboost.common.system.code.SystemCode;
import top.wboost.common.system.exception.SystemCodeException;

public class SpringBootUtil {

    /**
     * 获得spring-boot启动类
     * @return spring-boot app
     */
    public static Class<?> getLauncherClass() {
        for (StackTraceElement stackTraceElement : Arrays.asList(Thread.currentThread().getStackTrace())) {
            String stackClassName = stackTraceElement.getClassName();
            try {
                Class<?> clazz = Class.forName(stackClassName);
                SpringBootApplication springBootApp = AnnotationUtils.getAnnotation(clazz, SpringBootApplication.class);
                if (springBootApp != null) {
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        throw new SystemCodeException(SystemCode.PROMPT).setPromptMessage("not a springBoot app");
    }

}
