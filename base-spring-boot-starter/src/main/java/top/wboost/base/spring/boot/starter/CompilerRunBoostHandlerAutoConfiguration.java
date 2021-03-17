package top.wboost.base.spring.boot.starter;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import top.wboost.common.boost.handler.compiler.CompilerRunBoostHandler;
import top.wboost.common.log.util.LoggerUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * @Auther: jwsun
 * @Date: 2021/3/17 21:15
 */
@Configuration
@ConditionalOnClass({CompilerRunBoostHandler.class})
public class CompilerRunBoostHandlerAutoConfiguration {

    private Logger logger = LoggerUtil.getLogger(CompilerRunBoostHandlerAutoConfiguration.class);

    public CompilerRunBoostHandler compilerRunBoostHandler() {
        logger.info("init default compilerRunBoostHandler");
        return new CompilerRunBoostHandler() {
            @Override
            public boolean checkAccess(HttpServletRequest request) {
                return true;
            }

            @Override
            public String resolveCode(String code) {
                return code;
            }
        };
    }

}
