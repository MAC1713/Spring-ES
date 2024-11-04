package org.sunhr.es.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author mac
 */
@Configuration
public class ElasticSearchConfig {

    @Value("${spring.elasticsearch.schema:http}")
    private String schema;

    @Value("${spring.elasticsearch.uris}")
    private String address;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Value("${spring.elasticsearch.connectTimeout:5000}")
    private int connectTimeout;

    @Value("${spring.elasticsearch.socketTimeout:60000}")
    private int socketTimeout;

    @Value("${spring.elasticsearch.connectionRequestTimeout:5000}")
    private int connectionRequestTimeout;

    @Value("${spring.elasticsearch.maxConnectNum:100}")
    private int maxConnectNum;

    @Value("${spring.elasticsearch.maxConnectPerRoute:100}")
    private int maxConnectPerRoute;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // 创建凭证提供器
        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
        credsProv.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password)
        );

        // 解析主机地址
        String[] hostList = address.split(",");
        HttpHost[] httpHosts = new HttpHost[hostList.length];
        for (int i = 0; i < hostList.length; i++) {
            String[] hostPort = hostList[i].split(":");
            String host = hostPort[0].trim();
            int port = Integer.parseInt(hostPort[1].trim());
            httpHosts[i] = new HttpHost(host, port, schema);
        }

        // 创建低级客户端
        RestClient restClient = RestClient.builder(httpHosts)
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        .setDefaultCredentialsProvider(credsProv)
                        .setMaxConnTotal(maxConnectNum)
                        .setMaxConnPerRoute(maxConnectPerRoute))
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout(connectTimeout)
                        .setSocketTimeout(socketTimeout)
                        .setConnectionRequestTimeout(connectionRequestTimeout))
                .build();

        // 创建传输层
        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );

        // 创建API客户端
        return new ElasticsearchClient(transport);
    }
}