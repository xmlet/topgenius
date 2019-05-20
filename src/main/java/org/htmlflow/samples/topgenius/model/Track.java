package org.htmlflow.samples.topgenius.model;

public class Track {
    private final String name;
    private final String url;
    private final int duration;
    private final int playcount;
    private final int listeners;
    private final Artist artist;

    public Track(String name, String url, int duration, int playcount, int listeners, Artist artist) {
        this.name = name;
        this.url = url;
        this.duration = duration;
        this.playcount = playcount;
        this.listeners = listeners;
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public int getDuration() {
        return duration;
    }

    public int getPlaycount() {
        return playcount;
    }

    public int getListeners() {
        return listeners;
    }

    public Artist getArtist() {
        return artist;
    }
}
