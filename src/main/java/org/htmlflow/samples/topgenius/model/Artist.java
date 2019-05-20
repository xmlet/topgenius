package org.htmlflow.samples.topgenius.model;

public class Artist {
    private final String name;
    private final String url;
    private final String mbid;

    public Artist(String name, String url, String mbid) {
        this.name = name;
        this.url = url;
        this.mbid = mbid;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getMbid() {
        return mbid;
    }
}
