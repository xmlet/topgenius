package org.htmlflow.samples.topgenius.model;

public class Track {
    private final String name;
    private final String url;
    private final int listeners;

    public Track(String name, String url, int listeners) {
        this.name = name;
        this.url = url;
        this.listeners = listeners;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public int getListeners() {
        return listeners;
    }

}
