package top.wboost.common.netty.protocol;

import top.wboost.common.base.enums.CharsetEnum;

/**
 * @Auther: jwsun
 * @Date: 2019/1/11 21:55
 */
public class NettyConstant {

    public static int MAX_BYTES = 32 * 1024;
    public static int HEAD_DATA = 0X66;
    public static byte[] END_DATA = "|-end-|".getBytes(CharsetEnum.UTF_8.getCharset());
}
