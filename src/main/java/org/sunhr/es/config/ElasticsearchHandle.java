package org.sunhr.es.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sunhr.es.entity.DocumentPartES;

import java.io.IOException;
import java.util.Map;

/**
 * @author mac
 */
@Slf4j
@Component
public class ElasticsearchHandle {

    @Autowired
    private ElasticsearchClient client;

    /**
     * 判断索引是否存在
     *
     * @param indexName
     * @return
     * @throws IOException
     */
    public boolean hasIndex(String indexName) throws IOException {
        BooleanResponse exists = client.indices().exists(d -> d.index(indexName));
        return exists.value();
    }

    /**
     * 删除索引
     *
     * @param indexName
     * @throws IOException
     */
    public boolean deleteIndex(String indexName) throws IOException {
        DeleteIndexResponse response = client.indices().delete(d -> d.index(indexName));
        return true;
    }

    /**
     * 创建索引
     *
     * @param indexName
     * @return
     * @throws IOException
     */
    public boolean createIndex(String indexName) {
        try {
            CreateIndexResponse indexResponse = client.indices().create(c -> c.index(indexName));
        } catch (IOException e) {
            log.error("索引创建失败：{}", e.getMessage());
        }
        return true;
    }

    /**
     * 创建索引，不允许外部直接调用
     *
     * @param indexName
     * @param mapping
     * @throws IOException
     */
    private boolean createIndex(String indexName, Map<String, Property> mapping) throws IOException {
        CreateIndexResponse createIndexResponse = client.indices().create(c -> {
            c.index(indexName).mappings(mappings -> mappings.properties(mapping));
            return c;
        });
        return createIndexResponse.acknowledged();
    }

//    public Map<String, Property> buildMapping( Map<String, String> propertyKeys) {
//        Map<String, Property> documentMap = new HashMap<>();
//        for (Map.Entry<String, String> propertyKey : propertyKeys.entrySet()) {
//            String type = getIndxPropType(propertyKey.getValue());
//            String key = propertyKey.getKey();
//            log.info("属性：{}类型：{}", key, type);
//            if (type.equals("text")) {
//                documentMap.put(key, Property.of(property ->
//                                property.keyword(KeywordProperty.of(p ->
//                                                p.index(true)
//                                        )
//                                )
//                        )
//                );
//            } else if (type.equals("date")) {
//                documentMap.put(key, Property.of(property ->
//                                property.date(DateProperty.of(p ->
//                                                p.index(true).format("yyyy-MM-dd HH:mm:ss.SSS")
//                                        )
//                                )
//                        )
//                );
//            } else if (type.equals("long")) {
//                documentMap.put(key, Property.of(property ->
//                                property.long_(LongNumberProperty.of(p ->
//                                                p.index(true)
//                                        )
//                                )
//                        )
//                );
//            } else if (type.equals("integer")) {
//                documentMap.put(key, Property.of(property ->
//                                property.integer(
//                                        IntegerNumberProperty.of(p ->
//                                                p.index(false)
//                                        )
//                                )
//                        )
//                );
//            } else {
//                documentMap.put(key, Property.of(property ->
//                                property.object(
//                                        ObjectProperty.of(p ->
//                                                p.enabled(true)
//                                        )
//                                )
//                        )
//                );
//            }
//        }
//        return documentMap;
//    }

    /**
     * 重新创建索引,如果已存在先删除
     *
     * @param indexName
     * @param mapping
     */
    public void reCreateIndex(String indexName, Map<String, Property> mapping) {
        try {
            if (this.hasIndex(indexName)) {
                this.deleteIndex(indexName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.createIndex(indexName, mapping);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 新增数据
     *
     * @param indexName
     * @throws IOException
     */
    public boolean insertDocument(String indexName, Object obj, String id) {
        try {
            IndexResponse indexResponse = client.index(i -> i
                    .index(indexName)
                    .id(id)
                    .document(obj));
            return true;
        } catch (IOException e) {
            log.error("数据插入ES异常：{}", e.getMessage());
        }
        return false;
    }

    /**
     * 查询数据
     *
     * @param indexName
     * @param id
     * @return
     */
    public GetResponse<DocumentPartES> searchDocument(String indexName, String id) {

        try {
            GetResponse<DocumentPartES> getResponse = client.get(g -> g
                            .index(indexName)
                            .id(id)
                    , DocumentPartES.class
            );
            return getResponse;
        } catch (IOException e) {
            log.error("查询ES异常：{}", e.getMessage());
        }
        return null;
    }

    /**
     * 删除数据
     *
     * @param indexName
     * @param id
     * @return
     */
    public boolean deleteDocument(String indexName, String id) {
        try {
            DeleteResponse deleteResponse = client.delete(d -> d
                    .index(indexName)
                    .id(id)
            );
        } catch (IOException e) {
            log.error("删除Es数据异常：{}", e.getMessage());
        }
        return true;
    }

}

