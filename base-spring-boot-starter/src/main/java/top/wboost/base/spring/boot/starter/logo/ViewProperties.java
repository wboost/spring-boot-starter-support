package top.wboost.base.spring.boot.starter.logo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.wboost.base.spring.boot.starter.GlobalForSpringBootStarter;

import java.util.Map;

/**
 * @Auther: jwsun
 * @Date: 2019/3/18 16:56
 */
@Data
@ConfigurationProperties(GlobalForSpringBootStarter.PROPERTIES_PREFIX + "view")
public class ViewProperties {


    /**
     * 图片
     */
    String img;

    /**
     * 顶部文字
     */
    String topText;

    /**
     * 底部文字
     */
    String bottomText;

    /**
     * 展示下方栏
     */
    boolean showBottom = true;

    /**
     * 展示上方栏
     */
    boolean showTop = true;

    /**
     * 其他配置
     */
    Map<String, String> config;


}
