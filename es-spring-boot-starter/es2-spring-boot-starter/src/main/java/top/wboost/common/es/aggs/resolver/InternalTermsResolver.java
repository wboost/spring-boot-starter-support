package top.wboost.common.es.aggs.resolver;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.children.InternalChildren;
import org.elasticsearch.search.aggregations.bucket.terms.InternalTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import top.wboost.common.es.entity.EsCountFilter;
import top.wboost.common.es.entity.EsResultEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: jwsun
 * @Date: 2019/4/4 10:37
 */
public class InternalTermsResolver implements AggsResolver{


    @Override
    public EsResultEntity resolve(SearchResponse response, String aggsName,EsCountFilter... filters) {
        // 有序，从大->小
        Map<String, Long> aggregationMap = new LinkedHashMap<>();
        Aggregation aggregation = response.getAggregations().get(aggsName);
        if (InternalChildren.class.isAssignableFrom(aggregation.getClass())) {
            InternalChildren internalChildren = (InternalChildren) aggregation;
            aggregation = internalChildren.getAggregations().get(aggsName);
        }
        InternalTerms internalTerms = (InternalTerms) aggregation;
        List<Terms.Bucket> list = internalTerms.getBuckets();
        list.forEach((Terms.Bucket bucket) -> {
            if (filters != null) {
                for (EsCountFilter filter : filters) {
                    if (filter != null) {
                        if (filter.getGte() != null) {
                            if (bucket.getDocCount() < filter.getGte())
                                return;
                        } else if (filter.getGt() != null) {
                            if (bucket.getDocCount() <= filter.getGt())
                                return;
                        }
                        if (filter.getLte() != null) {
                            if (bucket.getDocCount() > filter.getLte())
                                return;
                        } else if (filter.getLt() != null) {
                            if (bucket.getDocCount() >= filter.getLt())
                                return;
                        }
                    }
                }
            }
            aggregationMap.put(bucket.getKey().toString(), bucket.getDocCount());
        });

        SearchHits hits = response.getHits();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        hits.forEach((SearchHit searchHit) -> {
            result.add(searchHit.getSource());
        });
        EsResultEntity esEntity = new EsResultEntity(result, hits.getTotalHits());
        esEntity.setAggregationMap(aggregationMap);
        return esEntity;
    }
}
