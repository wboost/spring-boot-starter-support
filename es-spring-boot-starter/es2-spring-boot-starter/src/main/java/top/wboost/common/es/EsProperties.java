package top.wboost.common.es;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Auther: jwsun
 * @Date: 2019/2/26 09:12
 */
@Data
@ConfigurationProperties("es.server")
public class EsProperties {

    String clustername;
    //ip列表，逗号分隔
    String ips;
    String port;
}
