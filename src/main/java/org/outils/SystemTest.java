package org.outils;

import org.db.ElasticsearchConnection;
import org.db.IndexManager;
import org.db.ElasticsearchService;
import org.embeding.GenerateEmbeddings;
import org.elbadry.SiteData;
import org.elbadry.Crawler;
import org.symantiqusearch.SemanticSearchService;
import org.outils.ObjectToMapConverter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class SystemTest {
    private static final String TEST_INDEX = "test_index_" + UUID.randomUUID().toString().substring(0, 8);
    private static final int EMBEDDING_DIMENSION = 768;

    public static void main(String[] args) {
        try {
            System.out.println("Starting system test with index: " + TEST_INDEX);

            // 1. Test index management
            testIndexManagement();

            // 2. Test embedding generation
            testEmbeddingGeneration();

            // 3. Test document storage and retrieval
            testDocumentStorageAndRetrieval();

            // 4. Test crawling and data storage
            testCrawling();

            // 5. Test semantic search
            testSemanticSearch();

            // Clean up
            cleanUp();

            System.out.println("\n✅ All tests passed!");
            return;
        } catch (Exception e) {
            System.err.println("\n❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testIndexManagement() throws IOException {
        System.out.println("\n🧪 Testing index management...");
        IndexManager indexManager = new IndexManager();
        indexManager.createIndex(TEST_INDEX, EMBEDDING_DIMENSION);

        if (!indexManager.indexExists(TEST_INDEX)) {
            throw new RuntimeException("Index creation failed");
        }
        System.out.println("✅ Index management tests passed");
    }

    private static void testEmbeddingGeneration() {
        System.out.println("\n🧪 Testing embedding generation...");
        String testText = "This is a test text for embedding generation";
        List<Double> embedding = GenerateEmbeddings.getEmbeddings("nomic-embed-text", testText);

        if (embedding == null || embedding.isEmpty()) {
            throw new RuntimeException("Embedding generation failed");
        }

        if (embedding.size() != EMBEDDING_DIMENSION) {
            throw new RuntimeException("Embedding dimension mismatch: expected " + EMBEDDING_DIMENSION +
                    " but got " + embedding.size());
        }
        System.out.println("✅ Embedding generation tests passed");
    }

    private static void testDocumentStorageAndRetrieval() throws IOException {
        System.out.println("\n🧪 Testing document storage and retrieval...");
        ElasticsearchService service = new ElasticsearchService();

        // Create test document
        String docId = "test_doc_" + UUID.randomUUID().toString().substring(0, 8);
        String testText = "This is a test document for storage and retrieval";
        List<Double> embedding = GenerateEmbeddings.getEmbeddings("nomic-embed-text", testText);
        SiteData siteData = new SiteData(docId, "Test Document", testText, embedding);

        // Store document
        Map<String, Object> dataMap = ObjectToMapConverter.convertToMap(siteData);
        service.storeData(docId, TEST_INDEX, dataMap);

        // Verify document exists
        if (!service.documentExists(TEST_INDEX, docId)) {
            throw new RuntimeException("Document storage failed");
        }
        System.out.println("✅ Document storage and retrieval tests passed");
    }

    private static void testCrawling() throws IOException {
        System.out.println("\n🧪 Testing crawler functionality...");

        // Use a stable site for testing - GitHub's raw content is reliable
        final String testUrl = "https://example.com";

        // Create a crawler with very limited scope for testing
        Crawler crawler = new Crawler(testUrl) {
            public Properties loadConfig() {
                Properties testProps = new Properties();
                testProps.setProperty("max_Depth", "1"); // Only crawl the home page
                testProps.setProperty("blocked_Domains", "facebook.com,twitter.com");
                testProps.setProperty("index_db", TEST_INDEX);
                testProps.setProperty("base_url", testUrl);
                return testProps;
            }
        };

        // Execute the crawl
        crawler.crawl();

        // Check if data was stored in Elasticsearch
        ElasticsearchService service = crawler.getService();
        List<String> ids = service.getIDs(TEST_INDEX);

        if (ids.isEmpty()) {
            throw new RuntimeException("Crawler test failed: No documents were indexed");
        }

        System.out.println("Crawler indexed " + ids.size() + " URLs");
        System.out.println("✅ Crawler and data storage tests passed");
    }

    private static void testSemanticSearch() throws IOException {
        System.out.println("\n🧪 Testing semantic search...");
        ElasticsearchService service = new ElasticsearchService();

        // Add test documents with different content
        addTestDocument(service, "Java is a popular programming language", "Java", "java_doc");
        addTestDocument(service, "Python is used for data science", "Python", "python_doc");
        addTestDocument(service, "JavaScript runs in browsers", "JavaScript", "js_doc");

        // Allow time for indexing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Test search
        SemanticSearchService searchService = new SemanticSearchService();
        Map<String, Double> results = searchService.semanticSearch("programming with Java", TEST_INDEX);

        if (results.isEmpty()) {
            throw new RuntimeException("Semantic search returned no results");
        }

        System.out.println("Search results: " + results);
        System.out.println("✅ Semantic search tests passed");
    }

    private static void addTestDocument(ElasticsearchService service, String content, String title, String idPrefix)
            throws IOException {
        String docId = idPrefix + "_" + UUID.randomUUID().toString().substring(0, 8);
        List<Double> embedding = GenerateEmbeddings.getEmbeddings("nomic-embed-text", content);
        SiteData siteData = new SiteData(docId, title, content, embedding);
        Map<String, Object> dataMap = ObjectToMapConverter.convertToMap(siteData);
        service.storeData(docId, TEST_INDEX, dataMap);
    }

    private static void cleanUp() throws IOException {
        System.out.println("\n🧹 Cleaning up test resources...");
        IndexManager indexManager = new IndexManager();
        indexManager.deleteIndex(TEST_INDEX);
    }
}