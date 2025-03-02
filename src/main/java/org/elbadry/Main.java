package org.elbadry;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final int MAX_THREADS = 9; // Maximum allowed threads
    private static final AtomicInteger activeThreads = new AtomicInteger(3); // Track active threads
    static ConcurrentHashMap<String, SiteData> map = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        Crawler crawler = new Crawler();
        crawler.crawl(0);
        crawler.printVisitedUrls();
//        ExecutorService executor = Executors.newCachedThreadPool(); // Dynamic thread pool

//        for (int i = 1; i <= 20; i++) {
//            if (activeThreads.get() < MAX_THREADS) { // Check condition
//                activeThreads.incrementAndGet(); // Increase count
//                Callable<SiteData> task = new Crawler(); // Create an instance of MyCallable
//                Future<SiteData> sft = executor.submit(task);
//                SiteData tmp = sft.get();
//                map.put(  "-" + i, tmp);
//            } else {
//                System.out.println("Skipping task, max threads reached!");
//            }
//        }
//        for (String key : map.keySet()) {
//            System.out.println("Key: " + key + " Value: " + map.get(key).getUrl());
//        }
//        executor.shutdown();
    }
}
