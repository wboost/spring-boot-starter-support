package top.wboost.common.kylin;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Auther: jwsun
 * @Date: 2019/2/26 09:28
 */
@Data
@ConfigurationProperties("common.kylin")
public class KylinProperties {

    Server server;
    Auth auth;

    @Data
    static class Server {
        String url="http://127.0.0.1:7070/kylin";
    }

    @Data
    static class Auth {
        String name = "ADMIN";
        String password = "KYLIN";
    }
}
