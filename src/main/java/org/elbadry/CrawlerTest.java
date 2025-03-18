package org.elbadry;

import org.db.ElasticsearchService;

import java.io.IOException;
import java.util.Properties;
import java.io.InputStream;

public class CrawlerTest {

    public static void main(String[] args) {
        testCrawlSinglePage();
    }

    private static void testCrawlSinglePage() {
        try {
            // Load configuration from properties file
            Properties props = new Properties();
            try (InputStream input = CrawlerTest.class.getClassLoader()
                    .getResourceAsStream("config.properties")) {
                props.load(input);
            }

            String testIndex = "my_index";
            String testUrl = "https://geeksblabla.community"; // Simple test site

            System.out.println("Starting test crawler with URL: " + testUrl);

            // Create crawler with specific test configuration
            Crawler crawler = new Crawler(testUrl) {
                public Properties loadConfig() {
                    Properties testProps = new Properties();
                    testProps.setProperty("max_Depth", "1"); // Limit depth for testing
                    testProps.setProperty("blocked_Domains", "facebook.com,twitter.com");
                    testProps.setProperty("index_db", testIndex);
                    return testProps;
                }
            };

            // Set up the test Elasticsearch index
            System.out.println("Using test index: " + testIndex);

            // Execute crawl with reduced timeout
            long startTime = System.currentTimeMillis();
            crawler.crawl();
            long elapsedTime = System.currentTimeMillis() - startTime;

            // Print results
            System.out.println("\nCrawl completed in " + elapsedTime + "ms");
            crawler.printVisitedUrls();

            // Verify data in Elasticsearch
            ElasticsearchService service = crawler.getService();
            System.out.println("\nVerifying data in Elasticsearch index: " + testIndex);
            service.printAllIds(testIndex);

            // Optional: Clean up test data
            // System.out.println("\nCleaning up test data...");
            // service.deleteIndex(testIndex);

        } catch (IOException e) {
            System.err.println("Test failed with error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}