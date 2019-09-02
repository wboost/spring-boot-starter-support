package top.wboost.common.es.repository;

import org.aopalliance.intercept.MethodInvocation;
import top.wboost.common.context.config.AutoProxy;

import java.util.Map;

public class EsRepositoryProxy implements AutoProxy {

    public AutoProxy getObject(Class<?> clazz, Map<String, Object> config) throws Exception {
        EsRepositoryProxy proxy = new EsRepositoryProxy();
        return proxy;
    }

    @Override public Object invoke(MethodInvocation invocation) throws Throwable {
        // TODO Auto-generated method stub
        return null;
    }

}