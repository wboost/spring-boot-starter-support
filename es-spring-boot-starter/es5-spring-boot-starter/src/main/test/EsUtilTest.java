import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.join.aggregations.ChildrenAggregationBuilder;
import org.elasticsearch.join.query.HasChildQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filters.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCountAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.bucketselector.BucketSelectorPipelineAggregationBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SortBy;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.term.TermSuggestionBuilder;
import org.junit.Test;
import org.springframework.core.env.PropertySource;
import top.wboost.common.base.page.BasePage;
import top.wboost.common.base.page.PageBuilder;
import top.wboost.common.es.entity.EsFilter;
import top.wboost.common.es.entity.EsPut;
import top.wboost.common.es.entity.EsResultEntity;
import top.wboost.common.es.exception.EsSearchException;
import top.wboost.common.es.search.EsAggregationSearch;
import top.wboost.common.es.search.EsFieldSearch;
import top.wboost.common.es.search.EsQueryType;
import top.wboost.common.es.search.EsSearch;
import top.wboost.common.es.util.EsChangeUtil;
import top.wboost.common.es.util.EsQueryAction;
import top.wboost.common.es.util.EsQueryUtil;
import top.wboost.common.util.QuickHashMap;
import top.wboost.common.utils.web.core.ConfigProperties;
import top.wboost.common.utils.web.utils.PropertiesUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static top.wboost.common.es.util.EsQueryAction.TERMS_NAME;

/**
 * @Auther: jwsun
 * @Date: 2019/3/28 18:10
 */
public class EsUtilTest {

    private void config() {
        PropertySource<?> propertySource = PropertiesUtil.loadPropertySource("example/es.properties");
        new ConfigProperties().setEmbeddedValueResolver(str -> propertySource.getProperty(str.substring(2).substring(0, str.length() - 3)).toString());
    }

    @Test
    public void checkFieldSearch() {
        config();
        EsFieldSearch esFieldSearch = new EsFieldSearch("cap_ccrc", "ccrc");
        esFieldSearch.putField("car_type").putField("car_num");
        EsResultEntity entity = EsQueryUtil.queryFieldList(esFieldSearch, null);
        System.out.println(entity);
    }

    @Test
    public void checkSearch() {
        config();
        EsSearch esFieldSearch = new EsSearch("cap_ccrc", "ccrc");
        EsResultEntity entity = EsQueryUtil.querySimpleList(esFieldSearch, null);
        System.out.println(entity);
    }

    @Test
    public void test() throws InterruptedException {
        LinkedBlockingQueue<Object> objects = new LinkedBlockingQueue<>(2);
        objects.put(1);
        objects.put(2);
        objects.put(3);
        objects.put(4);
        objects.put(5);
        System.out.println(objects.take());
        System.out.println(objects.take());
        System.out.println(objects.take());
        System.out.println(objects.take());
        System.out.println(objects.take());
        System.out.println(objects.take());
    }

    @Test
    public void checkTopHits() {
        config();

        /*EsAggregationSearch esAggregationSearch = new EsAggregationSearch("trajx_znss_test", "adm_gj");
        esAggregationSearch.getAggs().setField("gmsfhm");
        esAggregationSearch.getAggs().setType("adm_rk");
        esAggregationSearch.special(EsQueryType.MUST).child("adm_rk", new EsSearch("trajx_znss_test", "adm_rk").must("xb", "男"));

        EsResultEntity entity = queryAggregationList(esAggregationSearch, null);
        System.out.println(entity);*/
    }

    @Test
    public void xn() {
        config();
        BoolQueryBuilder child = QueryBuilders.boolQuery().filter(QueryBuilders.termQuery("xb", "男"));
        FiltersAggregationBuilder filters = AggregationBuilders.filters("abc", new HasChildQueryBuilder("adm_rk", child, ScoreMode.None));
        SearchRequestBuilder builder = EsQueryUtil.getClient().prepareSearch("trajx_znss_test").setTypes("adm_gj")
                .setSearchType(SearchType.QUERY_THEN_FETCH).setFrom(0).setSize(0).setPostFilter(child);
        builder.addAggregation(AggregationBuilders.filters("a",QueryBuilders.termQuery("kssj_y", 1)).subAggregation(filters.subAggregation(new ChildrenAggregationBuilder(TERMS_NAME, "adm_rk").subAggregation(AggregationBuilders.terms(TERMS_NAME).field("xb").size(10)))));
        System.out.println(builder);
    }

    @Test
    public void suggest() {
        config();
        SearchRequestBuilder builder = EsQueryUtil.getClient().prepareSearch("test").setTypes("test").suggest(new SuggestBuilder().addSuggestion("suggest_user", SuggestBuilders.completionSuggestion("name").text("Name1").size(20))).addSort("id", SortOrder.DESC);
        System.out.println(builder.toString());
    }

    @Test
    public void testGateway() {
        config();
        String apiId = "example:/api310:get";
        EsAggregationSearch idAggs = new EsAggregationSearch("gateway-log-all", "log").setField("apiId");
        idAggs.must("apiId", "\"" + apiId + "\"");
        EsResultEntity entity = EsQueryUtil.queryAggregationList(idAggs, null);
        System.out.println(entity.getAggregationMap().get(apiId));
    }

    @Test
    public void create() {
        // config();

    }

}
