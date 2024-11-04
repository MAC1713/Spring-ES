package org.sunhr.es.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunhr.es.service.TestSearchService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author mac
 */
@Service
public class TestSearchServiceImpl implements TestSearchService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private static final String[] SEARCH_FIELDS = {
            "f_meta_description", "f_title"
    };

    @Override
    public List<Map<String, Object>> searchTest(String keyword) {
        try {
            // 构建查询
            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                            .index("cms_info_detail")  // 指定索引
                            .query(q -> q
                                    .multiMatch(m -> m
                                            .fields(Arrays.asList(SEARCH_FIELDS))  // 指定搜索字段
                                            .query(keyword)
                                            .fuzziness("AUTO")  // 启用模糊匹配
                                            .prefixLength(3)
                                            .maxExpansions(10)
                                    )
                            )
                            .size(100),  // 限制返回结果数量
                    Map.class
            );

            // 提取结果
            List<Map<String, Object>> results = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    results.add(hit.source());
                }
            }
            return results;

        } catch (Exception e) {
            throw new RuntimeException("测试查询失败: " + e.getMessage(), e);
        }
    }
}
