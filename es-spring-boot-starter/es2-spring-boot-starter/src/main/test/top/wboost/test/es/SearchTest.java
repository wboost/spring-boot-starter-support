package top.wboost.test.es;

import org.junit.Test;
import org.springframework.core.env.PropertySource;
import top.wboost.common.base.page.BasePage;
import top.wboost.common.es.entity.EsIndex;
import top.wboost.common.es.entity.EsPut;
import top.wboost.common.es.search.EsSearch;
import top.wboost.common.es.util.EsChangeUtil;
import top.wboost.common.es.util.EsQueryUtil;
import top.wboost.common.utils.web.core.ConfigProperties;
import top.wboost.common.utils.web.utils.PropertiesUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: jwsun
 * @Date: 2019/1/16 15:27
 */
public class SearchTest {

    public static void main(String[] args) {
        EsSearch esScrollSearch = new EsSearch("test_abc", "abc");
        System.out.println(EsQueryUtil.querySimpleList(esScrollSearch, new BasePage(1, 12345)).getResultList().size());
        /*10*/
        /*StringBuffer sb = new StringBuffer();
        for (int k = 0; k < 100; k++) {
            EsPut esPut = new EsPut("test_abc", "abc");
            for (int i = 0; i < 100 && k<100; i++,k++) {
                sb.setLength(0);
                sb.append(k);
                esPut.setPutMap(new QuickHashMap<String, Object>().quickPut("CAR_NUM", sb.toString())
                        .quickPut("CREATE_TIME", System.currentTimeMillis()));
            }
            System.out.println(k);
            EsChangeUtil.putToIndex(esPut);
        }*/

    }

    private void config() {
        PropertySource<?> propertySource = PropertiesUtil.loadPropertySource("example/es.properties");
        new ConfigProperties().setEmbeddedValueResolver(str -> propertySource.getProperty(str.substring(2).substring(0, str.length() - 3)).toString());
    }

    @Test
    public void addIndexTest() {
        config();
        EsChangeUtil.createIndex(new EsIndex("gateway_log_test", "log_test", 5, 2).setBuilderSupport(createIndexRequestBuilder -> {
            createIndexRequestBuilder.addAlias(new org.elasticsearch.action.admin.indices.alias.Alias("gateway_log_all"));
        }).putField("test_f", "index", "not_analyzed").putField("test_f", "type", "string").putField("kssj", "type", "long"));
//        EsChangeUtil.createIndex(new EsIndex("gateway_log_test", "log_test", 5, 2).putField("test_f", "index", "not_analyzed").putField("test_f", "type", "string").putField("kssj", "type", "long"));

        Map<String, Object> map = new HashMap<>();
        map.put("test_f", "ceshishishishsi");
        map.put("kssj", System.currentTimeMillis());
        EsPut esPut = new EsPut("gateway_log_test", "log_test").setPutMap(map);
        EsChangeUtil.putToIndex(esPut);
    }

}
