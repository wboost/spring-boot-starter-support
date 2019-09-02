package top.wboost.common.es.util;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.slf4j.Logger;
import org.springframework.util.Assert;
import top.wboost.common.base.page.BasePage;
import top.wboost.common.base.page.PageBuilder;
import top.wboost.common.es.entity.EsFilter;
import top.wboost.common.es.entity.EsResultEntity;
import top.wboost.common.es.search.*;
import top.wboost.common.log.util.LoggerUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * ES查询工具类
 * @ClassName EsQueryUtil
 * @author jwSun
 * @Date 2017年6月24日 下午7:17:43
 * @version 1.0.0
 */
public class EsQueryUtil extends AbstractEsUtil {

    static int MAX_TOTAL = 10000;
    private static final Logger LOGGER = LoggerUtil.getLogger(EsQueryUtil.class);

    /**
     * 查询数据,获取所有字段值
     * @author jwSun
     * @date 2017年4月21日 下午5:58:26
     * @param esSearch Es查询实体类
     * @param queryPage 查询分页实体类
     * @return EsResultEntity
     */
    public static EsResultEntity querySimpleList(EsSearch esSearch, BasePage queryPage, EsFilter... filters) {
        EsResultEntity entity = null;
        // 判断查询范围是否大于规定from-to最大值，若是，则自动转换为使用scroll查询
        if (queryPage.getBeginNumber() + queryPage.getEndNumber() > MAX_TOTAL) {
            LOGGER.debug("use scroll");
            Integer beginNumber = queryPage.getBeginNumber();
            EsScrollSearch esScrollSearch = new EsScrollSearch(esSearch.getFirstIndex(), esSearch.getFirstType());
            esScrollSearch.merge(esSearch);
            esScrollSearch.setScroll(MAX_TOTAL);
            //设置为1W条每次查询
            BasePage findPage = new BasePage(1, MAX_TOTAL);
            EsResultEntity scrollList = queryScrollList(esScrollSearch, findPage, filters);
            int index = 0;
            List<Map<String, Object>> resultList = new ArrayList<>();
            boolean next = setData(index, index + MAX_TOTAL, scrollList, queryPage, resultList);
            // 直到值 大于 numEnd时停止
            while (next) {
                LOGGER.debug("next page");
                scrollList = queryByScroll(scrollList);
                index += MAX_TOTAL;
                next = setData(index, index + scrollList.getResultList().size(), scrollList, queryPage, resultList);
            }
            EsResultEntity ret = new EsResultEntity(resultList, scrollList.getTotal(), queryPage.getBeginNumber(), queryPage.getPageSize());
            ret.setTimeValue(esScrollSearch.getTimeValue());
            return ret;
        } else {
            QueryBuilder boolQueryBuilder = EsQueryAction.getBoolQueryBuilder(esSearch, filters);
            SearchRequestBuilder builder = EsQueryAction.getSearchRequestBuilder(esSearch, queryPage, boolQueryBuilder);
            entity = EsQueryAction.getSimpleEsResultEntity(builder, queryPage);
        }
        return entity;
    }

    // 如果整体大于开始值，则将需要的存入结果集
    private static boolean setData(int index, int nextIndex, EsResultEntity scrollList, BasePage queryPage, List<Map<String, Object>> resultList) {
        Integer beginAddIndex = null, endAddIndex = null;
        List<Map<String, Object>> resultListFind = scrollList.getResultList();
        boolean getEnd = false;
        if (queryPage.getBeginNumber() >= index && queryPage.getBeginNumber() < nextIndex) {
            beginAddIndex = queryPage.getBeginNumber() - index;
            getEnd = true;
        } else if (index > queryPage.getBeginNumber() && index < queryPage.getEndNumber()) {
            beginAddIndex = 0;
            getEnd = true;
        }
        if (getEnd) {
            if (queryPage.getEndNumber() > nextIndex) {
                endAddIndex = nextIndex - index;
            } else {
                endAddIndex = queryPage.getEndNumber() - index;
            }
        }
        if (beginAddIndex != null && endAddIndex != null) {
            System.out.println((beginAddIndex + index + 1) + "~" + (endAddIndex + index));
            for (int i = beginAddIndex; i < endAddIndex; i++) {
                resultList.add(resultListFind.get(i));
            }
        }
        return resultList.size() != queryPage.getPageSize() && (scrollList.getPageSize() + scrollList.getBeginNumber()) < queryPage.getEndNumber();
    }

    /**
     * 使用滚动(Scroll)方式搜索
     * @param esScrollSearch esScroll实体类
     * @param queryPage 查询分页实体类
     * @param filters ES过滤器
     * @return EsResultEntity
     */
    public static EsResultEntity queryScrollList(EsScrollSearch esScrollSearch, BasePage queryPage,
            EsFilter... filters) {
        QueryBuilder boolQueryBuilder = EsQueryAction.getBoolQueryBuilder(esScrollSearch, filters);
        if (queryPage == null)
            queryPage = countPage;
        SearchRequestBuilder builder = EsQueryAction.getSearchRequestBuilder(esScrollSearch, queryPage,
                boolQueryBuilder);
        EsQueryAction.addScroll(builder, esScrollSearch.getTimeValue());
        EsResultEntity entity = EsQueryAction.getSimpleEsResultEntity(builder, queryPage);
        entity.setTimeValue(esScrollSearch.getTimeValue());

        return entity;
    }

    /**
     * 使用滚动(Scroll)方式搜索
     * @param esScrollFieldSearch esScroll实体类
     * @param queryPage 查询分页实体类
     * @param filters ES过滤器
     * @return EsResultEntity
     */
    public static EsResultEntity queryScrollFieldList(EsScrollFieldSearch esScrollFieldSearch, BasePage queryPage,
                                                      EsFilter... filters) {
        QueryBuilder boolQueryBuilder = EsQueryAction.getBoolQueryBuilder(esScrollFieldSearch, filters);
        if (queryPage == null)
            queryPage = countPage;
        SearchRequestBuilder builder = EsQueryAction.getSearchRequestBuilder(esScrollFieldSearch, queryPage,
                boolQueryBuilder);
        EsQueryAction.addScroll(builder, esScrollFieldSearch.getTimeValue());
        Arrays.asList(esScrollFieldSearch.getFields()).forEach(builder::addDocValueField);
        EsResultEntity entity = EsQueryAction.getFieldEsResultEntity(builder, queryPage);
        entity.setTimeValue(esScrollFieldSearch.getTimeValue());
        return entity;
    }

    /**
     * 聚合查询
     * @date 2017年4月21日 下午5:58:26
     * @param esAggregationSearch Es聚合查询实体类
     * @param queryPage 查询分页实体类
     * @return EsResultEntity
     */
    public static EsResultEntity queryAggregationList(EsAggregationSearch esAggregationSearch, BasePage queryPage,
            EsFilter... filters) {
        if (queryPage == null)
            queryPage = countPage;
        BoolQueryBuilder boolQueryBuilder = EsQueryAction.getBoolQueryBuilder(esAggregationSearch, filters);
        SearchRequestBuilder builder = EsQueryAction.getSearchRequestBuilder(esAggregationSearch, queryPage,
                boolQueryBuilder);
        EsQueryAction.addAggregation(esAggregationSearch, builder);
        EsResultEntity entity = EsQueryAction.getAggregationEsResultEntity(builder, queryPage,
                esAggregationSearch.getEsCountFilter());
        return entity;
    }

    /**
     * 查询数据,获取指定部分字段值
     * @param esFieldSearch es属性值查询实体类
     * @param queryPage 查询分页实体类
     * @param filters ES过滤器
     * @return EsResultEntity
     */
    public static EsResultEntity queryFieldList(EsFieldSearch esFieldSearch, BasePage queryPage, EsFilter... filters) {
        if (queryPage == null)
            queryPage = countPage;
        SearchRequestBuilder builder = EsQueryAction.getSearchRequestBuilder(esFieldSearch, queryPage,
                EsQueryAction.getBoolQueryBuilder(esFieldSearch, filters));
        Arrays.asList(esFieldSearch.getFields()).forEach(builder::addDocValueField);
        EsResultEntity entity = EsQueryAction.getFieldEsResultEntity(builder, queryPage);
        return entity;
    }

    /**
     * 查询数量
     * @date 2017年4月24日 下午1:41:53
     * @param esSearch es基本查询实体类
     * @return EsResultEntity
     */
    public static EsResultEntity queryCount(EsSearch esSearch, EsFilter... filters) {
        SearchRequestBuilder builder = EsQueryAction.getSearchRequestBuilder(esSearch, countPage,
                EsQueryAction.getBoolQueryBuilder(esSearch, filters));
        EsResultEntity entity = EsQueryAction.getSimpleEsResultEntity(builder, countPage);
        return entity;
    }

    /**
     * 二次或以上滚动(scroll)查询
     * @param esResultEntity 上一次查询返回的实体类
     * @return EsResultEntity
     */
    public static EsResultEntity queryByScroll(EsResultEntity esResultEntity) {
        Assert.isTrue(esResultEntity.getTimeValue() != 0L);
        Assert.notNull(esResultEntity.getScrollId());
        SearchScrollRequestBuilder builder = EsQueryAction.getSearchScrollRequestBuilder(esResultEntity.getScrollId(),
                esResultEntity.getTimeValue());
        EsResultEntity entity = EsQueryAction.getSimpleEsResultEntity(builder,
                PageBuilder.begin().setBeginNumber(esResultEntity.getBeginNumber() + esResultEntity.getPageSize())
                        .setPageSize(esResultEntity.getPageSize()).build());
        entity.setTimeValue(esResultEntity.getTimeValue());
        return entity;
    }
}
