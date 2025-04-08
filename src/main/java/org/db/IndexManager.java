package org.db;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.transport.endpoints.BooleanResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IndexManager {
    private final ElasticsearchClient client;

    public IndexManager() {
        this.client = ElasticsearchConnection.getClient();
    }

    public boolean indexExists(String index) throws IOException {
        BooleanResponse response = client.indices().exists(e -> e.index(index));
        return response.value();
    }

    public void deleteIndex(String index) throws IOException {
        if (indexExists(index)) {
            DeleteIndexResponse response = client.indices().delete(b -> b.index(index));
            System.out.println("Index " + index + " deleted: " + response.acknowledged());
        }
    }

    public void createIndex(String index) throws IOException {
        if (indexExists(index)) {
            System.out.println("Index " + index + " already exists.");
            return;
        }

        // Default to 768 dimensions for embeddings
        createIndex(index, 768);
    }

    public void createIndex(String index, int embeddingDim) throws IOException {
        if (indexExists(index)) {
            System.out.println("Index " + index + " already exists.");
            return;
        }

        Map<String, Property> properties = new HashMap<>();
        properties.put("embedding", Property.of(p -> p.denseVector(dv -> dv.dims(embeddingDim))));
        properties.put("url", Property.of(p -> p.keyword(k -> k)));

        CreateIndexResponse response = client.indices().create(c -> c
                .index(index)
                .mappings(m -> m.properties(properties))
        );

        System.out.println("Index created: " + response.index());
    }
}
