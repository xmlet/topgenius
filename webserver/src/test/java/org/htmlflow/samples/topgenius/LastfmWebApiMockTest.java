package org.htmlflow.samples.topgenius;

import org.htmlflow.samples.topgenius.model.Track;
import org.junit.Test;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;


public class LastfmWebApiMockTest {

    @Test(expected=UnsupportedOperationException.class)
    public void getTopTracksFromMuse() throws IOException {
        LastfmWebApiMock api = new LastfmWebApiMock();
        String mbid = "fd857293-5ab8-40de-b29e-55a69d4e4d0f";
        Track[] tracks = api.artistTopTracks(mbid, 1);
        assertEquals("fljdflj", tracks[0].getName());
    }

    @Test
    public void getPage1FromGeographicTopTracksInAustralia() throws IOException {
        LastfmWebApiMock api = new LastfmWebApiMock();
        Track[] tracks = api.geographicTopTracks("Australia", 1);
        assertEquals("The Less I Know the Better", tracks[0].getName());
    }

    @Test
    public void getFromGeographicTopTracksInAustralia() throws IOException {
        LastfmWebApiMock api = new LastfmWebApiMock();
        Stream<Track> tracks = api.geographicTopTracks("Australia").limit(10000);
        assertEquals(10000, tracks.count());
    }
}
