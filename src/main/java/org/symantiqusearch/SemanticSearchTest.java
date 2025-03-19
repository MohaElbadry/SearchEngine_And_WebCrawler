package org.symantiqusearch;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.InputStream;

public class SemanticSearchTest {

    public static void main(String[] args) {
        try {
            // Load index name from config
            Properties props = new Properties();
            try (InputStream input = SemanticSearchTest.class.getClassLoader()
                    .getResourceAsStream("config.properties")) {
                if (input == null) {
                    System.out.println("Unable to find config.properties");
                    return;
                }
                props.load(input);
            }
            String index = props.getProperty("index_db", "my_index");

            // Create semantic search service
            SemanticSearchService searchService = new SemanticSearchService();

            // Test different queries
            testQuery(searchService, "elazizi ?", index);
            testQuery(searchService, "Tell me about the blog", index);
//            testQuery(searchService, "What projects does GeeksBlabla have?", index);
//            testQuery(searchService, "How can I contribute to Geeksblabla?", index);

            // Custom query from command line args if provided
            if (args.length > 0) {
                String customQuery = String.join(" ", args);
                testQuery(searchService, customQuery, index);
            }

        } catch (IOException e) {
            System.err.println("Error during semantic search test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testQuery(SemanticSearchService service, String query, String index) throws IOException {
        System.out.println("\n====================================");
        System.out.println("QUERY: " + query);
        System.out.println("====================================");

        Map<String, Double> results = service.semanticSearch(query, index);

        if (results.isEmpty()) {
            System.out.println("No results found for this query.");
            return;
        }

        System.out.println("Top " + results.size() + " results:");
        int rank = 1;
        for (Map.Entry<String, Double> entry : results.entrySet()) {
            // Format score to 4 decimal places for readability
            String formattedScore = String.format("%.4f", entry.getValue());
            System.out.println(rank + ". " + entry.getKey() + " (score: " + formattedScore + ")");
            rank++;
        }
    }
}