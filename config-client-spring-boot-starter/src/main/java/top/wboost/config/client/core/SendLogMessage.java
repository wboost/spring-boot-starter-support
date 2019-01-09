package top.wboost.config.client.core;

import lombok.Data;

/**
 * @Auther: jwsun
 * @Date: 2019/1/8 15:50
 */
@Data
public class SendLogMessage {

    private String appName;
    private String msg;

    public String toString() {
        return msg;
    }


}
