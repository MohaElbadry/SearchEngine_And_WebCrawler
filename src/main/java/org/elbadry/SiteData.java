package org.elbadry;

import java.util.List;

public class SiteData {
    String url;
    String title;
    String ScrapedData;
    List<Double> embedding; // Changed from List<List<Double>> to List<Double>

    public SiteData(String url, String title, String DATA, List<Double> embedding) {
        this.url = url;
        this.title = title;
        this.ScrapedData = DATA;
        this.embedding = embedding;
    }

    public String getUrl() {
        return url;
    }

    public String getScrapedData() {
        return ScrapedData;
    }

    public String getTitle() {
        return title;
    }

    public List<Double> getEmbedding() {
        return embedding;
    }
}