package org.db;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

public class ElasticsearchConnection {
    private static final String HOST = "localhost";
    private static final int PORT = 9200;
    private static final String SCHEME = "http";

    private static RestClient restClient;
    private static ElasticsearchClient client;

    public static ElasticsearchClient getClient() {
        if (client == null) {
            org.outils.ElasticsearchHealthCheck.elasticsearchHealthCheck();
            restClient = RestClient.builder(new HttpHost(HOST, PORT, SCHEME)).build();
            client = new ElasticsearchClient(new RestClientTransport(
                    restClient, new JacksonJsonpMapper()
            ));
        }
        return client;
    }

    public static void closeClient() {
        try {
            if (restClient != null) {
                restClient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}