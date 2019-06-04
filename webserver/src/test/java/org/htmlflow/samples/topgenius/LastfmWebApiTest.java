package org.htmlflow.samples.topgenius;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.htmlflow.samples.topgenius.model.Track;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;


public class LastfmWebApiTest {

    @Test
    public void getTopTracksFromMuse() throws IOException {
        LastfmWebApi api = new LastfmWebApi();
        String mbid = "fd857293-5ab8-40de-b29e-55a69d4e4d0f";
        Track[] tracks = api.artistTopTracks(mbid, 1);
        String expected = expectedArtistTopTrack(mbid);
        Assert.assertEquals(expected, tracks[0].getName());
    }

    @Test
    public void getGeographicTopTracksInAustralia() throws IOException {
        LastfmWebApi api = new LastfmWebApi();
        Track[] tracks = api.geographicTopTracks("Australia", 1);
        String expected = expectedCountryTopTrack("Australia");
        Assert.assertEquals(expected, tracks[0].getName());
    }

    static String expectedArtistTopTrack(String mbid) throws IOException {
        final String LASTFM_API_KEY = "038cde478fb0eff567330587e8e981a4";
        final String LASTFM_HOST = "http://ws.audioscrobbler.com/2.0/";
        final String LASTFM_ARTIST_TOP_TRACKS = LASTFM_HOST
                                            + "?method=artist.gettoptracks&format=json&mbid=%s&page=%d&api_key="
                                            + LASTFM_API_KEY;
        String path = String.format(LASTFM_ARTIST_TOP_TRACKS, mbid, 1);
        InputStream in = new URL(path).openStream();
        JsonReader reader = new Gson().newJsonReader(new BufferedReader(new InputStreamReader(in)));
        reader.beginObject(); // enter root
        reader.nextName();
        reader.beginObject(); // enter "toptracks"
        reader.nextName();
        reader.beginArray();  // enter "track"
        reader.beginObject(); // enter "track[]"
        reader.nextName();    // enter "track[]::name"
        String res = reader.nextString();
        reader.close();
        return res;
    }

    static String expectedCountryTopTrack(String country) throws IOException {
        final String LASTFM_API_KEY = "038cde478fb0eff567330587e8e981a4";
        final String LASTFM_HOST = "http://ws.audioscrobbler.com/2.0/";
        final String LASTFM_GEOGRAPHIC_TOP_TRACKS = LASTFM_HOST
                                                    + "?method=geo.gettoptracks&format=json&country=%s&page=%d&api_key="
                                                    + LASTFM_API_KEY;
        String path = String.format(LASTFM_GEOGRAPHIC_TOP_TRACKS , country, 1);
        InputStream in = new URL(path).openStream();
        JsonReader reader = new Gson().newJsonReader(new BufferedReader(new InputStreamReader(in)));
        reader.beginObject(); // enter root
        reader.nextName();
        reader.beginObject(); // enter "tracks"
        reader.nextName();
        reader.beginArray();  // enter "track"
        reader.beginObject(); // enter "track[]"
        reader.nextName();    // enter "track[]::name"
        String res = reader.nextString();
        reader.close();
        return res;
    }

}
