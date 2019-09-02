package top.wboost.common.es.aggs.resolver;

import org.elasticsearch.action.search.SearchResponse;
import top.wboost.common.es.entity.EsCountFilter;
import top.wboost.common.es.entity.EsResultEntity;

/**
 * @Auther: jwsun
 * @Date: 2019/4/4 10:35
 */
public interface AggsResolver {

    public EsResultEntity resolve(SearchResponse response, String aggsName, EsCountFilter... filters);

}
