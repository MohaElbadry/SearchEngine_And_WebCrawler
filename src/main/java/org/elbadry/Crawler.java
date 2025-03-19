package org.elbadry;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.db.ElasticsearchService;
import org.embeding.GenerateEmbeddings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.outils.ObjectToMapConverter;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Crawler {
    private final Set<String> visitedUrl;
    private final Queue<CrawlTask> queue;
    private final int MAX_DEPTH;
    private final Set<String> blockedDomains;
    private final String baseUrl;
    private ElasticsearchService service;
    private final String INDEX_DB;
    private final List<Map<String, Object>> bulkData;
    private final List<String> bulkUrls;
    private final int BULK_SIZE = 3;
    private final int TIMEOUT_MS = 10000; // 10 seconds

    private static class CrawlTask {
        final String url;
        final int depth;

        CrawlTask(String url, int depth) {
            this.url = url;
            this.depth = depth;
        }
    }


    public Crawler() {
        Properties props = loadConfig();
        this.visitedUrl = new HashSet<>();
        this.queue = new LinkedList<>();
        this.MAX_DEPTH = Integer.parseInt(props.getProperty("max_Depth"));
        this.blockedDomains = new HashSet<>(Arrays.asList(props.getProperty("blocked_Domains").split(",")));
        this.baseUrl = props.getProperty("base_url");
        this.INDEX_DB = props.getProperty("index_db");
        this.service = new ElasticsearchService();
        this.bulkData = new ArrayList<>();
        this.bulkUrls = new ArrayList<>();
    }

    public Crawler(String baseUrl) {
        Properties props = loadConfig();
        this.visitedUrl = new HashSet<>();
        this.queue = new LinkedList<>();
        this.MAX_DEPTH = Integer.parseInt(props.getProperty("max_Depth"));
        this.blockedDomains = new HashSet<>(Arrays.asList(props.getProperty("blocked_Domains").split(",")));
        this.baseUrl = baseUrl;
        this.INDEX_DB = props.getProperty("index_db");
        this.service = new ElasticsearchService();
        this.bulkData = new ArrayList<>();
        this.bulkUrls = new ArrayList<>();
    }


    private Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Unable to find config.properties");
                return props;
            }
            props.load(input);
        } catch (IOException ex) {
            System.err.println("Error loading configuration: " + ex.getMessage());
        }
        return props;
    }


    public void crawl() throws IOException {
        startCrawling(baseUrl);
    }

    public void crawl(String startUrl) throws IOException {
        startCrawling(startUrl);
    }

    /**
     * Start crawling process using a queue-based approach instead of recursion
     */
    private void startCrawling(String startUrl) throws IOException {
        queue.add(new CrawlTask(startUrl, 0));

        while (!queue.isEmpty()) {
            CrawlTask task = queue.poll();
            processUrl(task.url, task.depth);

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("\nCrawling completed!");
        System.out.println("Total URLs visited: " + visitedUrl.size());
    }

    /**
     * Process a single URL: fetch, parse, extract links and queue them
     */
    private void processUrl(String url, int depth) {

        if (visitedUrl.contains(url) || isBlockedDomain(url) || depth > MAX_DEPTH || url.contains("#")) {
            return;
        }

        visitedUrl.add(url);

        boolean skipIndexing = false;
        // Check if URL already exists in Elasticsearch
        try {
            skipIndexing = service.documentExists(INDEX_DB, url);
            if (skipIndexing) {
                System.out.println("URL already indexed: " + url + " (skipping content indexing)");
            }
        } catch (IOException e) {
            System.err.println("Error checking URL in database: " + e.getMessage());
            // Continue processing if we can't check the database
        }

        try {
            // Fetch and parse the page
            Document doc = Jsoup.connect(url).timeout(TIMEOUT_MS).get();

            System.out.println("Depth: " + depth + " [" + url + "]");

            // Only store the data if it's not already in the database
            if (!skipIndexing) {
                SiteData siteData = new SiteData(url, doc.title(), doc.text(), GenerateEmbeddings.getEmbeddings("nomic-embed-text", doc.text()));
                try {
                    // Convert to map and store data
                    Map<String, Object> dataMap = ObjectToMapConverter.convertToMap(siteData);
                    service.storeData(siteData.getUrl(), INDEX_DB, dataMap);
                } catch (JsonProcessingException e) {
                    System.err.println("Failed to convert data for URL " + url + ": " + e.getMessage());
                }
            }

            // Extract all links from the page - do this regardless of whether we indexed the content
            Elements links = doc.select("a[href]");

            // Queue links for processing
            for (Element link : links) {
                String nextUrl = link.attr("abs:href");
                if (!nextUrl.isEmpty() && !visitedUrl.contains(nextUrl) && !isBlockedDomain(nextUrl) && !nextUrl.contains("#")) {
                    queue.add(new CrawlTask(nextUrl, depth + 1));
                }
            }

        } catch (IOException e) {
            System.err.println("Error crawling " + url + ": " + e.getMessage());
        }
    }

    /**
     * Check if URL is from a blocked domain
     */
    private boolean isBlockedDomain(String url) {
        return blockedDomains.stream().anyMatch(url.toLowerCase()::contains);
    }

    /**
     * Print all visited URLs
     */
    public void printVisitedUrls() {
        System.out.println("\nVisited URLs:");
        visitedUrl.forEach(System.out::println);
    }

    public ElasticsearchService getService() {
        return service;
    }

    public void setService(ElasticsearchService service) {
        this.service = service;
    }

}