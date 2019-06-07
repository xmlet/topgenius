package org.htmlflow.samples.topgenius.model;

import java.util.List;

public class MockTopTracks {
    private final List<Track> track;

    public MockTopTracks(List<Track> track) {
        this.track = track;
    }

    public List<Track> getTrack() {
        return track;
    }
}
