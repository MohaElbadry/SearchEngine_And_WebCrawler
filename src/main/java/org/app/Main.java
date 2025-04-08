package org.app;

import org.db.ElasticsearchConnection;
import org.db.ElasticsearchService;
import org.db.IndexManager;
import org.elbadry.Crawler;
import org.symantiqusearch.SemanticSearchService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    private static final String DEFAULT_INDEX = "my_index";
    private static String indexName;
    private static final Scanner scanner = new Scanner(System.in);
    private static ElasticsearchService service;
    private static SemanticSearchService searchService;

    // ANSI color codes for terminal coloring
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BOLD = "\u001B[1m";

    public static void main(String[] args) {
        try {
            clearScreen();
            showBanner();
            initialize();

            boolean running = true;
            while (running) {
                displayMainMenu();
                int choice = getMenuChoice();

                clearScreen();

                switch (choice) {
                    case 1 -> handleCrawling();
                    case 2 -> handleSearch();
                    case 3 -> showAllDocuments();
                    case 4 -> running = false;
                    default -> System.out.println(ANSI_YELLOW + "❌ Invalid choice. Please try again." + ANSI_RESET);
                }

                if (running) {
                    System.out.print("\n" + ANSI_BLUE + "Press Enter to continue..." + ANSI_RESET);
                    scanner.nextLine();
                    clearScreen();
                }
            }

            System.out.println(ANSI_GREEN + "👋 Thank you for using Semantic Search System. Goodbye!" + ANSI_RESET);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ElasticsearchConnection.closeClient();
            scanner.close();
        }
    }

    /**
     * Clears the terminal screen
     */
    private static void clearScreen() {
        try {
            String operatingSystem = System.getProperty("os.name").toLowerCase();

            if (operatingSystem.contains("windows")) {
                // For Windows
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // For Unix/Linux/MacOS
                System.out.print("\033[H\033[2J");
                System.out.flush();

                // Alternative approach using ANSI escape codes
                // System.out.print("\033c");
            }
        } catch (Exception e) {
            // Fallback if the above methods fail
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    private static void showBanner() {
        System.out.println(ANSI_CYAN + ANSI_BOLD);
        System.out.println("  _____                          _   _      _____                     _     ");
        System.out.println(" / ____|                        | | (_)    / ____|                   | |    ");
        System.out.println("| (___   ___ _ __ ___   __ _ ___| |_ _  ___| (___   ___  __ _ _ __ ___| |__  ");
        System.out.println(" \\___ \\ / _ \\ '_ ` _ \\ / _` / __| __| |/ __|\\___ \\ / _ \\/ _` | '__/ __| '_ \\ ");
        System.out.println(" ____) |  __/ | | | | | (_| \\__ \\ |_| | (__ ____) |  __/ (_| | | | (__| | | |");
        System.out.println("|_____/ \\___|_| |_| |_|\\__,_|___/\\__|_|\\___|_____/ \\___|\\__,_|_|  \\___|_| |_|");
        System.out.println("                                                                              ");
        System.out.println(ANSI_RESET);
    }

    private static void initialize() {
        try {
            Properties props = new Properties();
            try (InputStream input = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (input == null) {
                    System.out.println("Unable to find config.properties, using default values");
                    indexName = DEFAULT_INDEX;
                } else {
                    props.load(input);
                    indexName = props.getProperty("index_db", DEFAULT_INDEX);
                }
            }

            // Initialize services
            service = new ElasticsearchService();
            searchService = new SemanticSearchService();

            System.out.println(ANSI_GREEN + "✅ Services initialized successfully" + ANSI_RESET);
        } catch (IOException e) {
            System.err.println(ANSI_YELLOW + "❌ Error initializing application: " + e.getMessage() + ANSI_RESET);
            System.exit(1);
        }
    }

    private static void displayMainMenu() {
        System.out.println("\n" + ANSI_BLUE + ANSI_BOLD + "╔══════════════════════════════════╗");
        System.out.println("║      Semantic Search System      ║");
        System.out.println("╚══════════════════════════════════╝" + ANSI_RESET);
        System.out.println(ANSI_CYAN + " 1. " + ANSI_RESET + "🌐 Web Crawling");
        System.out.println(ANSI_CYAN + " 2. " + ANSI_RESET + "🔍 Semantic Search");
        System.out.println(ANSI_CYAN + " 3. " + ANSI_RESET + "📋 Show All Documents");
        System.out.println(ANSI_CYAN + " 4. " + ANSI_RESET + "🚪 Exit");
        System.out.print("\n" + ANSI_BLUE + "➤ " + ANSI_RESET + "Enter your choice: ");
    }

    private static int getMenuChoice() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void handleCrawling() throws IOException {
        System.out.println("\n" + ANSI_BLUE + ANSI_BOLD + "╔══════════════════════════════════╗");
        System.out.println("║           Web Crawling          ║");
        System.out.println("╚══════════════════════════════════╝" + ANSI_RESET);
        System.out.println(ANSI_CYAN + " 1. " + ANSI_RESET + "🚀 Crawl with default URL");
        System.out.println(ANSI_CYAN + " 2. " + ANSI_RESET + "🔗 Crawl with custom URL");
        System.out.println(ANSI_CYAN + " 3. " + ANSI_RESET + "⬅️  Back to main menu");
        System.out.print("\n" + ANSI_BLUE + "➤ " + ANSI_RESET + "Enter your choice: ");

        int choice = getMenuChoice();

        switch (choice) {
            case 1 -> {
                System.out.println(ANSI_GREEN + "🕸️ Starting crawler with default URL..." + ANSI_RESET);
                Crawler crawler = new Crawler();
                crawler.crawl();
                crawler.printVisitedUrls();
                System.out.println(ANSI_GREEN + "✅ Crawling completed!" + ANSI_RESET);
            }
            case 2 -> {
                System.out.print(ANSI_BLUE + "➤ " + ANSI_RESET + "Enter URL to crawl: ");
                String url = scanner.nextLine().trim();
                if (url.isEmpty() || !url.startsWith("http")) {
                    System.out.println(ANSI_YELLOW + "❌ Invalid URL. Please enter a valid URL starting with http:// or https://" + ANSI_RESET);
                    return;
                }
                System.out.println(ANSI_GREEN + "🕸️ Starting crawler for: " + url + ANSI_RESET);
                Crawler crawler = new Crawler(url);
                crawler.crawl();
                crawler.printVisitedUrls();
                System.out.println(ANSI_GREEN + "✅ Crawling completed!" + ANSI_RESET);
            }
            case 3 -> { /* Return to main menu */ }
            default -> System.out.println(ANSI_YELLOW + "❌ Invalid choice. Returning to main menu." + ANSI_RESET);
        }
    }

    private static void handleSearch() throws IOException {
        System.out.println("\n" + ANSI_BLUE + ANSI_BOLD + "╔══════════════════════════════════╗");
        System.out.println("║         Semantic Search         ║");
        System.out.println("╚══════════════════════════════════╝" + ANSI_RESET);

        System.out.print(ANSI_BLUE + "➤ " + ANSI_RESET + "Enter search query: ");
        String query = scanner.nextLine().trim();

        if (query.isEmpty()) {
            System.out.println(ANSI_YELLOW + "❌ Query cannot be empty." + ANSI_RESET);
            return;
        }

        System.out.println(ANSI_GREEN + "🔍 Searching for: \"" + query + "\"..." + ANSI_RESET);
        Map<String, Double> results = searchService.semanticSearch(query, indexName);

        if (results.isEmpty()) {
            System.out.println(ANSI_YELLOW + "⚠️  No results found for your query." + ANSI_RESET);
            return;
        }

        System.out.println("\n" + ANSI_GREEN + "🎯 Found " + results.size() + " results:" + ANSI_RESET);
        int rank = 1;
        for (Map.Entry<String, Double> entry : results.entrySet()) {
            String formattedScore = String.format("%.4f", entry.getValue());
            System.out.println(ANSI_CYAN + " " + rank + ". " + ANSI_RESET + entry.getKey());
            System.out.println("    " + ANSI_BLUE + "Score: " + formattedScore + ANSI_RESET);
            rank++;
        }
    }

    private static void showAllDocuments() throws IOException {
        System.out.println("\n" + ANSI_BLUE + ANSI_BOLD + "���══════════════════════════════════╗");
        System.out.println("║        Document Database        ║");
        System.out.println("╚══════════════════════════════════╝" + ANSI_RESET);

        System.out.println(ANSI_GREEN + "📋 Retrieving all documents from index: " + indexName + ANSI_RESET);
        service.printAllIds(indexName);
    }
}