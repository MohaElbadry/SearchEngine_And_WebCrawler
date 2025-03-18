package org.db;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ElasticsearchService {
    private final ElasticsearchClient client;

    public ElasticsearchService() {
        this.client = ElasticsearchConnection.getClient();
    }

    public void storeData(String url, String index, Map<String, Object> data) throws IOException {
        try {
            IndexResponse response = client.index(i -> i
                    .id(url)
                    .index(index)
                    .document(data)
            );
            System.out.println("Indexed document with ID: " + response.id());
        } catch (Exception e) {
            System.err.println("Error indexing document for URL " + url + ": " + e.getMessage());
            throw e;
        }
    }

    public void storeBulkData(List<String> urls, String index, List<Map<String, Object>> dataList) throws IOException {
        if (urls.size() != dataList.size()) {
            throw new IllegalArgumentException("URLs and data lists must be the same size");
        }

        var bulkRequest = new co.elastic.clients.elasticsearch.core.BulkRequest.Builder();

        // Add bulk operations
        for (int i = 0; i < urls.size(); i++) {
            int finalI = i;
            bulkRequest.operations(op -> op.index(idx -> idx
                    .id(urls.get(finalI))
                    .index(index)
                    .document(dataList.get(finalI))));
        }

        // Retry logic with exponential backoff
        int retries = 0, MAX_RETRIES = 3;
        long RETRY_DELAY_MS = 1000;

        while (retries < MAX_RETRIES) {

            try {
                BulkResponse response = client.bulk(bulkRequest.build());

                // Check for errors
                if (response.errors()) {
                    response.items().stream()
                            .filter(item -> item.error() != null)
                            .forEach(item -> System.err.println("Error for ID " + item.id() + ": " + item.error().reason()));
                }
                return; // Exit if successful
            } catch (Exception e) {
                System.err.println("Elasticsearch bulk request failed (attempt " + (retries + 1) + "): " + e.getMessage());
                if (++retries == MAX_RETRIES) {
                    throw new IOException("Failed to index documents after " + MAX_RETRIES + " retries", e);
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS * (1L << (retries - 1))); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during retry backoff", ie);
                }
            }
        }
    }

    public List<String> getIDs(String index) throws IOException {
        SearchResponse<Map> response = client.search(s -> s
                        .index(index)
                        .size(10000)  // Maximum size in one request
                        .query(q -> q.matchAll(m -> m)),
                Map.class
        );

        return response.hits().hits().stream().map(Hit::id).toList();
    }



    public void deleteListe(String index, String idPrefix) throws IOException {
        List<String> ids = getIDs(index);
        for (String id : ids) {
            if (id.contains(idPrefix)) {
                System.out.println("Deleting " + id);
                deleteData(index, id);
            }
        }
    }

    public void deleteData(String index, String id) throws IOException {
        try {
            DeleteResponse response = client.delete(d -> d
                    .index(index)
                    .id(id)
            );
        } catch (Exception e) {
            System.err.println("Error deleting document with ID " + id + ": " + e.getMessage());
            throw e;
        }
    }

    public void printAllIds(String index) throws IOException {
        SearchResponse<Map> response = client.search(s -> s
                        .index(index)
                        .size(10000)  // Maximum size in one request
                        .query(q -> q.matchAll(m -> m)),
                Map.class
        );
        System.out.println("Document IDs in index " + index + ":");
        for (Hit<Map> hit : response.hits().hits()) {
            System.out.println(hit.id());
        }
        System.out.println("Total documents: " + response.hits().total().value());
    }
}