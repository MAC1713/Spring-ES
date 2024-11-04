package org.sunhr.es.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunhr.es.service.CrossIndexSearchService;
import org.sunhr.sql.entity.CMS;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author mac
 */
@Service
public class CrossIndexSearchServiceImpl implements CrossIndexSearchService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private static final String[] SEARCH_FIELDS = {
            "f_title", "f_meta_description", "f_values"
    };

    private static final int MAX_RETRIES = 3;
    // 1 second
    private static final long RETRY_DELAY = 1000;

    @Override
    public List<CMS> searchAcrossIndices(String keyword) {
        try {
            // 1. 创建带有重试机制的并行搜索
            CompletableFuture<List<Map<String, Object>>> infoFuture =
                    CompletableFuture.supplyAsync(() -> retryableSearch(() -> searchIndex("cms_info", keyword)));
            CompletableFuture<List<Map<String, Object>>> detailFuture =
                    CompletableFuture.supplyAsync(() -> retryableSearch(() -> searchIndex("cms_info_detail", keyword)));
            CompletableFuture<List<Map<String, Object>>> customFuture =
                    CompletableFuture.supplyAsync(() -> retryableSearch(() -> searchIndex("cms_info_custom", keyword)));

            // 2. 设置超时和异常处理
            List<Map<String, Object>> infoResults = infoFuture.completeOnTimeout(new ArrayList<>(), 30, TimeUnit.SECONDS).get();
            List<Map<String, Object>> detailResults = detailFuture.completeOnTimeout(new ArrayList<>(), 30, TimeUnit.SECONDS).get();
            List<Map<String, Object>> customResults = customFuture.completeOnTimeout(new ArrayList<>(), 30, TimeUnit.SECONDS).get();

            // 3. 收集所有f_info_id
            Set<String> infoIds = new HashSet<>();
            collectIds(infoResults, "f_info_id", infoIds);
            collectIds(detailResults, "f_info_id", infoIds);
            collectIds(customResults, "f_info_id", infoIds);

            // 4. 批量查询相关info数据
            if (!infoIds.isEmpty()) {
                CompletableFuture<List<Map<String, Object>>> relatedInfoFuture =
                        CompletableFuture.supplyAsync(() -> retryableSearch(() -> searchByIds("cms_info", "f_info_id", infoIds)));
                CompletableFuture<List<Map<String, Object>>> relatedDetailFuture =
                        CompletableFuture.supplyAsync(() -> retryableSearch(() -> searchByIds("cms_info_detail", "f_info_id", infoIds)));
                CompletableFuture<List<Map<String, Object>>> relatedCustomFuture =
                        CompletableFuture.supplyAsync(() -> retryableSearch(() -> searchByIds("cms_info_custom", "f_info_id", infoIds)));

                // 设置超时和异常处理
                infoResults.addAll(relatedInfoFuture.completeOnTimeout(new ArrayList<>(), 30, TimeUnit.SECONDS).get());
                detailResults.addAll(relatedDetailFuture.completeOnTimeout(new ArrayList<>(), 30, TimeUnit.SECONDS).get());
                customResults.addAll(relatedCustomFuture.completeOnTimeout(new ArrayList<>(), 30, TimeUnit.SECONDS).get());
            }

            // 6. 整合数据到CMS实体
            return mergeToCMSEntities(infoResults, detailResults, customResults);

        } catch (Exception e) {
            throw new RuntimeException("跨索引搜索失败: " + e.getMessage(), e);
        }
    }

    private List<CMS> mergeToCMSEntities(List<Map<String, Object>> infoResults,
                                         List<Map<String, Object>> detailResults,
                                         List<Map<String, Object>> customResults) {
        Map<Integer, CMS> cmsMap = new HashMap<>();

        // 处理info结果
        for (Map<String, Object> info : infoResults) {
            Integer infoId = getIntegerValue(info.get("f_info_id"));
            if (infoId != null) {
                CMS cms = cmsMap.computeIfAbsent(infoId, k -> new CMS());
                cms.setFInfoId(infoId);
                cms.setFSiteId(getIntegerValue(info.get("f_site_id")));
                cms.setFPublishDate(getDateValue(info.get("f_publish_date")));
            }
        }

        // 处理detail结果
        for (Map<String, Object> detail : detailResults) {
            Integer infoId = getIntegerValue(detail.get("f_info_id"));
            if (infoId != null) {
                CMS cms = cmsMap.computeIfAbsent(infoId, k -> new CMS());
                cms.setFInfoId(infoId);
                cms.setFTitle((String) detail.get("f_title"));
                cms.setFMetaDescription((String) detail.get("f_meta_description"));
            }
        }

        // 处理custom结果
        for (Map<String, Object> custom : customResults) {
            Integer infoId = getIntegerValue(custom.get("f_info_id"));
            if (infoId != null) {
                CMS cms = cmsMap.computeIfAbsent(infoId, k -> new CMS());
                cms.setFInfoId(infoId);
                cms.setFValues((String) custom.get("f_values"));
            }
        }

        return new ArrayList<>(cmsMap.values());
    }

    private Integer getIntegerValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            return Integer.valueOf((String) value);
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    private Date getDateValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        if (value instanceof String) {
            try {
                return new Date(Long.parseLong((String) value));
            } catch (NumberFormatException e) {
                // 如果需要，这里可以添加更多的日期格式解析逻辑
                return null;
            }
        }
        return null;
    }

    private <T> T retryableSearch(Supplier<T> searchOperation) {
        Exception lastException = null;
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return searchOperation.get();
            } catch (Exception e) {
                lastException = e;
                if (i < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(RETRY_DELAY * (i + 1)); // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                }
            }
        }
        throw new RuntimeException("重试" + MAX_RETRIES + "次后仍然失败", lastException);
    }

    private void collectIds(List<Map<String, Object>> results, String field, Set<String> ids) {
        for (Map<String, Object> map : results) {
            if (map.get(field) != null) {
                ids.add(map.get(field).toString());
            }
        }
    }

    private List<Map<String, Object>> searchIndex(String indexName, String keyword) {
        try {
            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                            .index(indexName)
                            .query(q -> q
                                    .multiMatch(m -> m
                                            .fields(Arrays.asList(SEARCH_FIELDS))
                                            .query(keyword)
                                            .fuzziness("AUTO")
                                            .prefixLength(3)
                                            .maxExpansions(10)
                                            .analyzer("ik_smart")
                                    )
                            )
                            .size(100),
                    Map.class
            );

            return extractHits(response);
        } catch (Exception e) {
            throw new RuntimeException("索引查询失败: " + e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> searchNodesByModelIds(Set<String> modelIds) {
        try {
            List<FieldValue> fieldValues = modelIds.stream()
                    .map(FieldValue::of)
                    .collect(Collectors.toList());

            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                            .index("cms_node")
                            .query(q -> q
                                    .terms(t -> t
                                            .field("f_info_model_id")
                                            .terms(t2 -> t2.value(fieldValues))
                                    )
                            )
                            .size(1000),
                    Map.class
            );

            return extractHits(response);
        } catch (Exception e) {
            throw new RuntimeException("节点查询失败: " + e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> searchByIds(String indexName, String field, Set<String> ids) {
        try {
            List<FieldValue> fieldValues = ids.stream()
                    .map(FieldValue::of)
                    .collect(Collectors.toList());

            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                            .index(indexName)
                            .query(q -> q
                                    .terms(t -> t
                                            .field(field)
                                            .terms(t2 -> t2.value(fieldValues))
                                    )
                            )
                            .size(1000),
                    Map.class
            );

            return extractHits(response);
        } catch (Exception e) {
            throw new RuntimeException("关联信息查询失败: " + e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> extractHits(SearchResponse<Map> response) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (Hit<Map> hit : response.hits().hits()) {
            if (hit.source() != null) {
                results.add(hit.source());
            }
        }
        return results;
    }
}