package org.db;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.ScrollResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            throw new IllegalArgumentException("URLs and dataList must be the same size");
        }

        var bulkRequest = new co.elastic.clients.elasticsearch.core.BulkRequest.Builder();

        for (int i = 0; i < urls.size(); i++) {
            int finalI = i;
            bulkRequest.operations(op -> op.index(idx -> idx
                    .id(urls.get(finalI))
                    .index(index)
                    .document(dataList.get(finalI))));
        }

        int retries = 0;
        final int MAX_RETRIES = 3;
        long RETRY_DELAY_MS = 1000;

        while (retries < MAX_RETRIES) {
            try {
                BulkResponse response = client.bulk(bulkRequest.build());
                if (response.errors()) {
                    response.items().stream()
                            .filter(item -> item.error() != null)
                            .forEach(item -> System.err.println("Error for ID " + item.id() + ": " + item.error().reason()));
                }
                return;
            } catch (Exception e) {
                System.err.println("Elasticsearch bulk request failed (attempt " + (retries + 1) + "): " + e.getMessage());
                if (++retries == MAX_RETRIES) {
                    throw new IOException("Failed to index documents after " + MAX_RETRIES + " retries", e);
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS * (1L << (retries - 1)));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during retry backoff", ie);
                }
            }
        }
    }

    public List<String> getIDsScroll(String index) throws IOException {
        List<String> ids = new ArrayList<>();

        // Initial search with scroll parameter
        SearchResponse<Map> searchResponse = client.search(s -> s
                        .index(index)
                        .size(1000)
                        .scroll(sc -> sc.time("1m"))
                        .query(q -> q.matchAll(m -> m)),
                Map.class);

        // Add initial batch of hits
        ids.addAll(searchResponse.hits().hits().stream()
                .map(Hit::id)
                .collect(Collectors.toList()));

        // Continue scrolling while we have a scroll ID and hits
        String initialScrollId = searchResponse.scrollId();
        while (initialScrollId != null && !searchResponse.hits().hits().isEmpty()) {
            // Use a final variable in lambda to avoid "Variable must be final" error
            final String currentScrollId = initialScrollId;

            // Execute scroll request
            ScrollResponse<Map> scrollResponse = client.scroll(s -> s
                            .scrollId(currentScrollId)
                            .scroll(sc -> sc.time("1m")),
                    Map.class);

            // Update scroll ID for next iteration
            initialScrollId = scrollResponse.scrollId();

            // Add this batch of hits
            ids.addAll(scrollResponse.hits().hits().stream()
                    .map(Hit::id)
                    .collect(Collectors.toList()));

            // Break if we have no more hits
            if (scrollResponse.hits().hits().isEmpty()) {
                break;
            }
        }

        // Clear the scroll to free resources
        if (initialScrollId != null) {
            String finalInitialScrollId = initialScrollId;
            client.clearScroll(c -> c.scrollId(finalInitialScrollId));
        }

        return ids;
    }

    public List<String> getIDs(String index) throws IOException {
        // Ideally use pagination or scroll API. For smaller datasets, size(10000) may suffice.
        SearchResponse<Map> response = client.search(s -> s
                        .index(index)
                        .size(10000)
                        .query(q -> q.matchAll(m -> m)),
                Map.class
        );
        return response.hits().hits().stream().map(Hit::id).collect(Collectors.toList());
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
            System.out.println(response.toString());
        } catch (Exception e) {
            System.err.println("Error deleting document with ID " + id + ": " + e.getMessage());
            throw e;
        }
    }

    public boolean documentExists(String index, String id) throws IOException {
        return client.exists(e -> e
                .index(index)
                .id(id)
        ).value();
    }

    public void printAllIds(String index) throws IOException {
        List<String> ids = getIDsScroll(index);
        System.out.println("Document IDs in index " + index + ":");
        ids.forEach(System.out::println);
        System.out.println("Total documents: " + ids.size());
    }
}