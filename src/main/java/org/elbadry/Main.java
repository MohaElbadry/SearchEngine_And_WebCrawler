package org.elbadry;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Crawler crawler = new Crawler();
        crawler.crawl();
        crawler.printVisitedUrls();
        crawler.getService().printAllIds("my_index");

        //DELETE THE INDEX in the table
        //crawler.getService().deleteListe("my_index",".c");
    }
}
