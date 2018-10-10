package top.wboost.boot.config.server;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
//@EnableConfigServer
public @interface EnableConfigServerSupport {
}
