package org.symantiqusearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.ScoreMode;
import co.elastic.clients.json.JsonData;
import org.db.ElasticsearchConnection;
import org.db.ElasticsearchService;
import org.embeding.GenerateEmbeddings;

import java.io.IOException;
import java.util.*;

public class SemanticSearchService {
    private final ElasticsearchService service;
    private final ElasticsearchClient client;
    private static final double MIN_SCORE_THRESHOLD = 0.7; // Only return results above this threshold

    public SemanticSearchService() {
        this.service = new ElasticsearchService();
        this.client = ElasticsearchConnection.getClient();
    }

    public Map<String, Double> semanticSearch(String query, String index) throws IOException {
        // Generate query embedding
        List<List<Double>> queryEmbedding = GenerateEmbeddings.getEmbeddings("nomic-embed-text", query);
        if (queryEmbedding.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Double> qEmbed = queryEmbedding.get(0);

        try {
            // First attempt: Use script_score query for more efficient vector search
            return performScriptScoreSearch(qEmbed, index);
        } catch (Exception e) {
            System.out.println("Script score search failed, falling back to client-side calculation: " + e.getMessage());
            // Fallback to client-side calculation
            return performClientSideSearch(qEmbed, index);
        }
    }

    private Map<String, Double> performScriptScoreSearch(List<Double> qEmbed, String index) throws IOException {
        SearchResponse<Map> response = client.search(s -> s
                        .index(index)
                        .size(20)
                        .query(q -> q
                                .scriptScore(ss -> ss
                                        .query(sq -> sq.matchAll(m -> m))
                                        .script(sc -> sc
                                                .source("cosineSimilarity(params.query_vector, doc['embedding.0']) + 1.0")
                                                .params("query_vector", JsonData.of(qEmbed)))
                                )
                        ),
                Map.class);

        Map<String, Double> results = new LinkedHashMap<>();
        for (Hit<Map> hit : response.hits().hits()) {
            // Convert score from ES (1.0-2.0 range) back to 0.0-1.0 range
            double normalizedScore = hit.score() - 1.0;
            if (normalizedScore >= MIN_SCORE_THRESHOLD) {
                results.put(hit.id(), normalizedScore);
            }
        }
        return results;
    }

    private Map<String, Double> performClientSideSearch(List<Double> qEmbed, String index) throws IOException {
        // Get documents from Elasticsearch with a higher limit but not ridiculous
        SearchResponse<Map> response = client.search(s -> s
                        .index(index)
                        .size(500) // More reasonable size
                        .query(q -> q.matchAll(m -> m)),
                Map.class);

        // Compute similarities
        Map<String, Double> scoredResults = new HashMap<>();
        for (Hit<Map> hit : response.hits().hits()) {
            Map<String, Object> source = hit.source();
            if (source != null && source.containsKey("embedding")) {
                try {
                    @SuppressWarnings("unchecked")
                    List<List<Double>> docEmbedding = (List<List<Double>>) source.get("embedding");
                    if (!docEmbedding.isEmpty()) {
                        double score = cosineSimilarity(qEmbed, docEmbedding.get(0));
                        if (score >= MIN_SCORE_THRESHOLD) {
                            scoredResults.put(hit.id(), score);
                        }
                    }
                } catch (ClassCastException e) {
                    System.err.println("Error processing embedding for document " + hit.id() + ": " + e.getMessage());
                }
            }
        }

        // Sort and convert to LinkedHashMap to maintain order
        return scoredResults.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        LinkedHashMap::putAll);
    }

    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        if (v1.size() != v2.size()) {
            return 0;
        }
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < v1.size(); i++) {
            dot += v1.get(i) * v2.get(i);
            normA += Math.pow(v1.get(i), 2);
            normB += Math.pow(v2.get(i), 2);
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}