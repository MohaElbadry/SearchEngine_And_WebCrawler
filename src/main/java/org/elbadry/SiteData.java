package org.elbadry;

import java.util.List;

public class SiteData {
    String url;
    String title;
    String ScrapedData;
    List<List<Double>> embedding; // 🔹 Add embeddings list

    public SiteData(String url, String title, String DATA, List<List<Double>> embedding) {
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

    public List<List<Double>> getEmbedding() {
        return embedding;
    }

}
