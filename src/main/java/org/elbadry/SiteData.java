package org.elbadry;

public class SiteData {
    String url;
    String title;
    String ScrapedData;

    public SiteData(String url, String title, String DATA) {
        this.url = url;
        this.title = title;
        this.ScrapedData = DATA;
    }

    public String getUrl() {
        return url;
    }
}
