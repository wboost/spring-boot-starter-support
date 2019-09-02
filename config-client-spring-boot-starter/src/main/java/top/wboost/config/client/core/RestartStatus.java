package top.wboost.config.client.core;

/**
 * @Auther: jwsun
 * @Date: 2019/1/3 18:42
 */
public class RestartStatus {

    String ip;
    String applicationName;
    int beforePort;
    int nextPort;
    String contextPath;

    public boolean checkAlive() {
        return true;
    }


}
