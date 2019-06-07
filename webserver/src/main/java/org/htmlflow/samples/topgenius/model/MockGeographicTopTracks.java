package org.htmlflow.samples.topgenius.model;

import java.util.List;

public class MockGeographicTopTracks {
    private final MockTopTracks tracks;

    public MockGeographicTopTracks(MockTopTracks tracks) {
        this.tracks = tracks;
    }

    public MockGeographicTopTracks(List<Track> track) {
        this.tracks = new MockTopTracks(track);
    }

    public MockTopTracks getTracks() {
        return tracks;
    }
}
