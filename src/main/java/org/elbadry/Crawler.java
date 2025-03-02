package org.elbadry;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


public class Crawler {
    private HashSet<String> visitedUrl;
    private final int MAX_DEPTH;
    private final Set<String> blockedDomains;
    private final String baseUrl;

    Crawler() {
        Properties props = loadConfig();
        this.visitedUrl = new HashSet<>();
        this.MAX_DEPTH = Integer.parseInt(props.getProperty("max_Depth"));
        this.blockedDomains = new HashSet<>(Arrays.asList(props.getProperty("blocked_Domains").split(",")));
        baseUrl = props.getProperty("base_url");
    }
    Crawler(String baseUrl) {
        Properties props = loadConfig();
        this.visitedUrl = new HashSet<>();
        this.MAX_DEPTH = Integer.parseInt(props.getProperty("max_Depth"));
        this.blockedDomains = new HashSet<>(Arrays.asList(props.getProperty("blocked_Domains").split(",")));
        this.baseUrl = baseUrl;
    }
    private Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
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



    public void crawl(int depth) throws IOException {
        crawl(baseUrl, depth);
    }

    public void crawl(String url, int depth) throws IOException {
        if (visitedUrl.contains(url) || isBlockedDomain(url)) return;
        visitedUrl.add(url);
        if (depth > MAX_DEPTH) return;

        try {
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.select("a[href]");
            System.out.println("Depth: " + depth + " [" + url + "]");
            depth++;
            for (Element elem : elements) {
                String nextUrl = elem.attr("abs:href");
                if (!nextUrl.isEmpty()) {
                    crawl(nextUrl, depth);
                }
            }
        } catch (IOException e) {
            System.err.println("Error crawling " + url + ": " + e.getMessage());
        }

        System.out.println("Visited URL: ");

        visitedUrl.forEach(System.out::println);

    }

    private boolean isBlockedDomain(String url) {
        return blockedDomains.stream().anyMatch(url.toLowerCase()::contains);
    }

    public void printVisitedUrls() {
        System.out.println("\nVisited URLs:");
        visitedUrl.forEach(System.out::println);
    }

    public static void main(String[] args) throws IOException {

    }
}


//    @Override
//    public SiteData call() throws Exception {
/// /        System.out.println("Running in: " + Thread.currentThread().getName());
/// /        SiteData st = new SiteData("https://www.google.com", "Google", "Google is a search engine");
/// /        return st;
//        String url = "https://www.imdb.com/search/title/?groups=top_250&sort=user_rating,asc";
//        /* THE name Classes
//        ipc-metadata-list ipc-metadata-list--dividers-between
//        sc-e22973a9-0 khSCXM detailed-list-view ipc-metadata-list--base
//         */
//        Connection session = Jsoup.newSession() // .newSession creates a session to maintain settings and cookies across multiple requests
//                .timeout(timeout);
//        Document doc = session.newRequest(url).get(); // .get executes a GET request, and parses the result
//        String selector = "ipc-title-link-wrapper";
//        Elements elements = doc.getElementsByClass(selector); // get each element that matches the CSS selector
//
//        for (Element element : elements) {
//            String str = String.valueOf(element.append("https://www.imdb.com/" + element.attr("href")));
//            System.out.println(str);
//        }
//        return null;
//    }

