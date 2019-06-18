package org.htmlflow.samples.topgenius.model;

import com.google.gson.annotations.SerializedName;

public class TopTracks {
    private final Track[] track;
    @SerializedName("@attr")
    private final Attributes attr;

    public TopTracks(Track[] track, Attributes attr) {
        this.track = track;
        this.attr = attr;
    }

    public Track[] getTrack() {
        return track;
    }

    public Attributes getAttr() {
        return attr;
    }
}
