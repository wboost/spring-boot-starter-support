package top.wboost.config.client.core;

import org.springframework.http.ResponseEntity;
import top.wboost.common.base.entity.HttpRequestBuilder;
import top.wboost.common.util.HttpClientUtil;

/**
 * @Auther: jwsun
 * @Date: 2019/1/3 18:39
 */
public class RestartProvider {

    public RestartStatus restartApp(String ip,int port,String contex,boolean wait) {
        restartByInstance(ip, port, contex);
        return new RestartStatus();
    }

    public ResponseEntity<String> restartByInstance(String ip, int port, String context) {
        return HttpClientUtil.execute(HttpRequestBuilder.post(getRestartUrl(ip,port,context)));
    }

    private String getRestartUrl (String ip,int port,String context) {
        return (context != null?"http://" + ip + ":" + port + "/" + context + ConfigClientRegister.WEB_PREFIX
                :"http://" + ip + ":" + port + ConfigClientRegister.WEB_PREFIX) + "/restart";
    }

}
