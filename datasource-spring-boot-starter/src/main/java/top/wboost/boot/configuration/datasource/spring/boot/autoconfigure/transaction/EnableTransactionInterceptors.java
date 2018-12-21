package top.wboost.boot.configuration.datasource.spring.boot.autoconfigure.transaction;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

import java.lang.annotation.*;

/**
 * <pre>
 *     开启全局事物拦截器,与tx:advice,aop:config功能相同
 *     注解配置：
 *     @EnableTransactionInterceptors({
 *         @EnableTransactionInterceptors.Config(
 *                 name = "transactionInterceptorsForTransactionManager",
 *                 transactionManager = "transactionManager",
 *                 attributes = @EnableTransactionInterceptors.Config.Attribute(
 *                     {
 *                             @EnableTransactionInterceptors.Config.Attribute.Method(name="query*",propagation = Propagation.REQUIRED,readOnly = true),
 *                             @EnableTransactionInterceptors.Config.Attribute.Method(name="update*",propagation = Propagation.REQUIRED)
 *                     }
 *      )
 *     )})
 *
 *     等同于以下xml配置：
 *     <!-- 方法拦截 -->
 *     <tx:advice id="methodAdvice" transaction-manager="transactionManager">
 *         <tx:attributes>
 *             <tx:method name="query*" propagation="REQUIRED" read-only="true" />
 *             <tx:method name="update*" propagation="REQUIRED" />
 *         </tx:attributes>
 *     </tx:advice>
 *
 *    	<!-- aop通知 service..表示service文件夹下的所有文件包括子文件夹-->
 * 	    <aop:config>
 * 		  <aop:advisor advice-ref="methodAdvice" pointcut="execution(* top.wboost..service..*.*(..))" order="3"/>
 * 		  <aop:advisor advice-ref="methodAdvice" pointcut="execution(* com.chinaoly.track.*.service..*.*(..))" order="4"/>
 * 	    </aop:config>
 * </pre>
 * @className EnableJpa
 * @author jwSun
 * @date 2018年4月16日 下午9:49:07
 * @version 1.0.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({TransactionInterceptorConfiguration.class})
public @interface EnableTransactionInterceptors {

    //@AliasFor("value")
    Config[] transactionInterceptors() default {};

    /**
     * 多个拦截器配置
     */
    /*@AliasFor("transactionInterceptors")
    Config[] value();*/

    Attribute[] attributes();

    int order() default Ordered.LOWEST_PRECEDENCE;

    /**
     * Indicate how transactional advice should be applied.
     * <p><b>The default is {@link AdviceMode#PROXY}.</b>
     * Please note that proxy mode allows for interception of calls through the proxy
     * only. Local calls within the same class cannot get intercepted that way; an
     * {@link org.springframework.transaction.annotation.Transactional} annotation on such a method within a local call will be
     * ignored since Spring's interceptor does not even kick in for such a runtime
     * scenario. For a more advanced mode of interception, consider switching this to
     * {@link AdviceMode#ASPECTJ}.
     */
    AdviceMode mode() default AdviceMode.PROXY;

    /**
     * 代理方式->cglib
     * @return
     */
    boolean proxyTargetClass() default true;


    /**
     * 事物配置
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    @interface Config {

        /**
         * 关联配置名
         *
         * @return
         */
        String attributesRef();

        /**
         * 创建bean名
         */
        String name() default "";

        /**
         * 事物管理器
         */
        String transactionManagerRef();

        /**
         * 选择连接池默认对应的事物管理器，若事物管理器已指定，则此配置项无效
         */
        String datasourceRef() default "";

        /**
         * 切面指定
         * @return
         */
        PointCutConfig[] pointCutConfigs();

        /**
         * 配置传播
         */
        @Retention(RetentionPolicy.RUNTIME)
        @Target({})
        @interface PointCutConfig {

            /**
             * 切面,语法为Pointcut表示式(expression) 如execution(* top.wboost..service..*.*(..))
             * @return
             */
            String expression();

            /**
             * 优先级排序，小的为先执行,排序算法见{@link org.springframework.core.OrderComparator}类
             * @return
             */
            int order() default Ordered.LOWEST_PRECEDENCE;
        }


    }

    /**
     * 配置传播
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    @interface Attribute {

        String name();

        Method[] value();

        @Retention(RetentionPolicy.RUNTIME)
        @Target({})
        @interface Method {

            /**
             * The method name(s) with which the transaction attributes are to be
             * associated. The wildcard (*) character can be used to associate the
             * same transaction attribute settings with a number of methods; for
             * example, 'get*', 'handle*', '*Order', 'on*Event', etc.
             *
             * @return
             */
            String name();

            /**
             * The transaction propagation behavior.
             *
             * @return
             */
            Propagation propagation() default Propagation.REQUIRED;

            /**
             * The transaction isolation level.
             *
             * @return
             */
            Isolation isolation() default Isolation.DEFAULT;

            /**
             * The transaction timeout value (in seconds).
             *
             * @return
             */
            int timeout() default -1;

            /**
             * Is this transaction read-only?
             *
             * @return
             */
            boolean readOnly() default false;

            /**
             * The Exception(s) that will trigger rollback; comma-delimited.
             * For example, 'com.foo.MyBusinessException,ServletException'
             *
             * @return
             */
            String rollbackFor() default "";

            /**
             * The Exception(s) that will *not* trigger rollback; comma-delimited.
             * For example, 'com.foo.MyBusinessException,ServletException'
             *
             * @return
             */
            String noRollbackFor() default "";

        }

    }

}
