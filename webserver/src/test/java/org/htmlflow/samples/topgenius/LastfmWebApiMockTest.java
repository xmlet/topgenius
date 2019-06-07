package org.htmlflow.samples.topgenius;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.htmlflow.samples.topgenius.controllers.ControllerLastfmMock;
import org.htmlflow.samples.topgenius.model.Track;
import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;


public class LastfmWebApiMockTest {

    private static final String EXPECTED_JSON = "{" +
        "\"tracks\":{" +
            "\"track\":[" +
                "{\"name\":\"The Less I Know the Better\",\"url\":\"https://www.last.fm/music/Tame+Impala/_/The+Less+I+Know+the+Better\",\"duration\":0,\"playcount\":0,\"listeners\":434911,\"artist\":{\"name\":\"Tame Impala\",\"url\":\"https://www.last.fm/music/Tame+Impala\",\"mbid\":\"63aa26c3-d59b-4da4-84ac-716b54f1ef4d\"}}," +
                "{\"name\":\"Can\\u0027t Feel My Face\",\"url\":\"https://www.last.fm/music/The+Weeknd/_/Can%27t+Feel+My+Face\",\"duration\":0,\"playcount\":0,\"listeners\":465220,\"artist\":{\"name\":\"The Weeknd\",\"url\":\"https://www.last.fm/music/The+Weeknd\",\"mbid\":\"c8b03190-306c-4120-bb0b-6f2ebfc06ea9\"}}]}}";

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

    @Test
    public void jsonFromGeographicTopTracksInAustralia() throws IOException {
        String json = ControllerLastfmMock.jsonAustraliaTopTracks(2);
        System.out.println(json);
        assertEquals(EXPECTED_JSON, json);
    }
}
