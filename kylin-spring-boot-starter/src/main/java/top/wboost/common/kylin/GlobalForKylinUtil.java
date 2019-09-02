package top.wboost.common.kylin;

import top.wboost.common.kylin.entity.Dimension;
import top.wboost.common.kylin.entity.Measures;
import top.wboost.common.utils.web.utils.PropertiesUtil;

/**
 * kylin工具所需常量值
 * @className GlobalForKylinUtil
 * @author jwSun
 * @date 2017年8月22日 下午7:26:11
 * @version 1.0.0
 */
public class GlobalForKylinUtil {

    public static final String KYLIN_SERVER_URL = "kylin.server.url";
    public static final String KYLIN_SERVER_NAME = "kylin.auth.name";
    public static final String KYLIN_SERVER_PASSWORD = "kylin.auth.password";

    public static final String kylinUrl = PropertiesUtil.getProperty(KYLIN_SERVER_URL);
    public static final String kylinUserName = PropertiesUtil.getProperty(KYLIN_SERVER_NAME);
    public static final String kylinPassword = PropertiesUtil.getProperty(KYLIN_SERVER_PASSWORD);

    /*public static final String kylinUrl = "http://192.168.51.70:7070/kylin";
    public static final String kylinUserName = "ADMIN";
    public static final String kylinPassword = "KYLIN";*/

    public interface CubeDescriptor {
        /**维度**/
        public static final String DIMENSIONS = "dimensions";
        /**维度类**/
        public static final Class<Dimension> DIMENSIONS_CLASS = Dimension.class;
        /**度量**/
        public static final String MEASURES = "measures";
        /**维度类**/
        public static final Class<Measures> MEASURES_CLASS = Measures.class;
        /**模块名**/
        public static final String MODEL_NAME = "model_name";
    }
}
