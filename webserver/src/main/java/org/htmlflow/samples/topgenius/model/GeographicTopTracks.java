package org.htmlflow.samples.topgenius.model;

import com.google.gson.annotations.SerializedName;

public class GeographicTopTracks {
    private final TopTracks tracks;

    public GeographicTopTracks(TopTracks tracks) {
        this.tracks = tracks;
    }

    public TopTracks getTracks() {
        return tracks;
    }
}
