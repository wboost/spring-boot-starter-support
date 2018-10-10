package top.wboost.base.spring.boot.starter.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;

import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import top.wboost.common.system.code.SystemCode;
import top.wboost.common.system.exception.SystemCodeException;

public class SpringBootUtil {

    private static List<BeanDefinition> beanDefinitions = new ArrayList<>();

    private static Class<?> runApp = null;

//    public static void setRunApp(Class<?> runApp) {
//        SpringBootApplication app = AnnotationUtils.getAnnotation(runApp, SpringBootApplication.class);
//        if (app != null) {
//            SpringBootUtil.runApp = runApp;
//        } else {
//            throw new RuntimeException(runApp + " not a springboot runner");
//        }
//    }

    static{
        String resource = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        if (resource.indexOf("test-classes") != -1) {
            String classFile = resource.substring(0, resource.indexOf("test-classes")) + "classes";
            String[] fileList = new File(classFile).list();
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
                    false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(SpringBootApplication.class));
            List<String> packages = new ArrayList<>();
            for (int i = 0;i < fileList.length;i++) {
                if (fileList[i].indexOf(".") == -1) {
                    packages.add(fileList[i]);
                }
            }
            for(String pack :packages) {
                beanDefinitions.addAll(new ArrayList<>(
                        scanner.findCandidateComponents(pack)));
            }
            if (runApp == null && beanDefinitions.size() > 0) {
                try {
                    runApp = Class.forName(beanDefinitions.get(0).getBeanClassName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 获得spring-boot启动类
     * @return spring-boot app
     */
    public static Class<?> getLauncherClass() {
        if (runApp == null) {
            List<StackTraceElement> stackTraceElements = Arrays.asList(Thread.currentThread().getStackTrace());
            for (StackTraceElement stackTraceElement : stackTraceElements) {
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
        } else {
            return runApp;
        }
        throw new RuntimeException("not a springBoot app or cant find SpringBootApplication class!!! if run springboot test ,please use 'SpringBootUtil.setRunApp(ApplicationRun.class)' first");
    }

}
